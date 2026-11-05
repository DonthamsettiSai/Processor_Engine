package com.knot.provisioning.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.service.ProvisioningRequestService;
import com.knot.provisioning.utill.Constants;

/**
 * Keeps request claiming separate from downstream OMS calls. The DAO's claim
 * transaction is short and commits before this method returns to the poller.
 */
@Primary
@Service
public class MultiPodProvisioningRequestServiceImpl extends ProvisioningRequestServiceImpl {

    private static final Logger log = LogManager.getLogger(MultiPodProvisioningRequestServiceImpl.class);

    @Override
    public List<ProvisioningRequest> getProvisioningRequests(String tenantId, String maxRecords) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(maxRecords)) {
            throw new IllegalArgumentException("tenantId and maxRecords are required");
        }
        try {
            List<ProvisioningRequest> requests = provisioningDao.getProvisioningRequests(tenantId, maxRecords);
            log.info("Request claim completed: tenantId={}, count={}", tenantId,
                    requests == null ? 0 : requests.size());
            return requests == null ? new ArrayList<>() : requests;
        } catch (Exception e) {
            log.error("Unable to claim provisioning requests for tenantId={}", tenantId, e);
            throw e;
        }
    }
}
