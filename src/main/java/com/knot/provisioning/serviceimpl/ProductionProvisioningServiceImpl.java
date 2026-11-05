package com.knot.provisioning.serviceimpl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.network.RefMasterNetwork;
import com.knot.provisioning.network.UmsNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisionService;
import com.knot.provisioning.service.ProvisioningRequestService;
import com.knot.provisioning.soapclient.SoapClient;
import com.knot.provisioning.soapclient.SoapConnector;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.MdcAwareRunnable;
import com.knot.redis.dto.TenantsDTO;

/**
 * Conservative per-pod processor coordinator. It deliberately processes one
 * provisioning request at a time in each pod because ProvisionServiceImpl has
 * request-scoped mutable state. MySQL SKIP LOCKED allows other pods to work in
 * parallel without sharing that state.
 */
@Primary
@Service
public class ProductionProvisioningServiceImpl extends ProvisioningServiceImpl {

    private static final Logger log = LogManager.getLogger(ProductionProvisioningServiceImpl.class);
    private static final long POLL_INTERVAL_SECONDS = 20L;

    private final ProvisioningDao provisioningDao;
    private final ProvisioningRequestService requestService;
    private final ProvisionService provisionService;
    private final CacheService cache;
    private final RefMasterNetwork referenceNetwork;
    private final OMSNetwork omsNetwork;
    private final RestTemplate restTemplate;
    private final SoapConnector soapConnector;
    private final SoapClient soapClient;
    private final UmsNetwork umsNetwork;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private final ThreadPoolExecutor requestWorker = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1), runnable -> {
                Thread thread = new Thread(runnable, "provisioning-request-worker");
                thread.setDaemon(false);
                return thread;
            }, new ThreadPoolExecutor.AbortPolicy());

    private volatile java.util.concurrent.ScheduledExecutorService poller;

    public ProductionProvisioningServiceImpl(ProvisioningDao provisioningDao,
            ProvisioningRequestService requestService, ProvisionService provisionService,
            CacheService cache, RefMasterNetwork referenceNetwork, OMSNetwork omsNetwork,
            RestTemplate restTemplate, SoapConnector soapConnector, SoapClient soapClient,
            UmsNetwork umsNetwork) {
        this.provisioningDao = provisioningDao;
        this.requestService = requestService;
        this.provisionService = provisionService;
        this.cache = cache;
        this.referenceNetwork = referenceNetwork;
        this.omsNetwork = omsNetwork;
        this.restTemplate = restTemplate;
        this.soapConnector = soapConnector;
        this.soapClient = soapClient;
        this.umsNetwork = umsNetwork;
    }

    @Override
    public void processProvisioningRequest() {
        if (!started.compareAndSet(false, true)) {
            log.info("Provisioning poller is already running on this pod");
            return;
        }
        String stop = cache.configValByCodeForGraceFullStop(Constants.PRO_PROCESS_STOP, Constants.GLOBAL_TENANT);
        if (StringUtils.hasText(stop) && "Y".equalsIgnoreCase(stop)) {
            log.warn("Provisioning poller disabled by graceful-stop configuration");
            started.set(false);
            return;
        }
        poller = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "provisioning-request-poller");
            thread.setDaemon(false);
            return thread;
        });
        poller.scheduleWithFixedDelay(this::pollTenantsSafely, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("Provisioning poller started; interval={}s, per-pod request concurrency=1", POLL_INTERVAL_SECONDS);
    }

    private void pollTenantsSafely() {
        if (!polling.compareAndSet(false, true)) {
            log.warn("Previous tenant poll has not completed; skipping this tick");
            return;
        }
        Map<String, String> originalContext = ThreadContext.getImmutableContext();
        try {
            if (requestWorker.getActiveCount() > 0 || !requestWorker.getQueue().isEmpty()) {
                log.debug("Request worker is busy; skipping claim cycle");
                return;
            }
            List<TenantsDTO> tenants = referenceNetwork.getAllTenants();
            if (tenants == null || tenants.isEmpty()) {
                log.warn("Tenant lookup returned no tenants; no request claim attempted");
                return;
            }
            for (TenantsDTO tenant : tenants) {
                if (requestWorker.getActiveCount() > 0 || !requestWorker.getQueue().isEmpty()) {
                    break;
                }
                String tenantId = tenant == null ? null : tenant.getTenantID();
                if (!StringUtils.hasText(tenantId)) {
                    log.warn("Skipping tenant entry with an empty tenant ID");
                    continue;
                }
                claimAndSubmitOne(tenantId);
            }
        } catch (Exception e) {
            log.error("Provisioning poll cycle failed", e);
        } finally {
            ThreadContext.clearAll();
            ThreadContext.putAll(originalContext);
            polling.set(false);
        }
    }

    private void claimAndSubmitOne(String tenantId) {
        String maxRecords = "1";
        String configured = cache.strConfigVal(Constants.MAX_TENANTS, tenantId);
        if (StringUtils.hasText(configured) && !"NA".equalsIgnoreCase(configured.trim())
                && !"-1".equals(configured.trim())) {
            // Deliberately cap to one because the existing worker service has mutable
            // request state. Increase only after that state is made request-local.
            maxRecords = "1";
        }

        ThreadContext.put("TENANT_ID", tenantId);
        try {
            List<ProvisioningRequest> requests = requestService.getProvisioningRequests(tenantId, maxRecords);
            if (requests == null || requests.isEmpty()) {
                log.debug("No request claimed for tenantId={}", tenantId);
                return;
            }
            ProvisioningRequest request = requests.get(0);
            ThreadContext.put("REQUEST_ID", String.valueOf(request.getRequestID()));
            log.info("Submitting claimed provisioning request: tenantId={}, requestId={}, workOrderId={}",
                    tenantId, request.getRequestID(), request.getWorkOrderID());
            requestWorker.execute(new MdcAwareRunnable(() -> {
                try {
                    provisionService.processProvisioningRequest(request, provisioningDao, requestWorker,
                            cache, omsNetwork, restTemplate, soapConnector, soapClient, umsNetwork, referenceNetwork);
                } catch (Exception e) {
                    log.error("Failed to submit provisioning request: tenantId={}, requestId={}",
                            tenantId, request.getRequestID(), e);
                }
            }));
        } catch (java.util.concurrent.RejectedExecutionException e) {
            // This should be prevented by the busy check. Leave the DB record in
            // progress for the configured stale-request recovery path and report it.
            log.error("Request worker rejected claimed request for tenantId={}; stale recovery will retry", tenantId, e);
        } catch (Exception e) {
            log.error("Failed to claim/submit provisioning request for tenantId={}", tenantId, e);
        } finally {
            ThreadContext.remove("TENANT_ID");
            ThreadContext.remove("REQUEST_ID");
        }
    }

    @PreDestroy
    public void stopProcessor() {
        log.info("Stopping provisioning poller and request worker");
        java.util.concurrent.ScheduledExecutorService currentPoller = poller;
        if (currentPoller != null) {
            currentPoller.shutdown();
        }
        requestWorker.shutdown();
        try {
            if (currentPoller != null && !currentPoller.awaitTermination(30, TimeUnit.SECONDS)) {
                currentPoller.shutdownNow();
            }
            if (!requestWorker.awaitTermination(2, TimeUnit.MINUTES)) {
                requestWorker.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (currentPoller != null) {
                currentPoller.shutdownNow();
            }
            requestWorker.shutdownNow();
        }
        started.set(false);
    }
}
