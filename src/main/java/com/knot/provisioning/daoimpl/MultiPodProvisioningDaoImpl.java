package com.knot.provisioning.daoimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.dao.CacheDao;
import com.knot.provisioning.utill.Constants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * Tenant-scoped MySQL 8 request claimant. Row selection and transition to
 * in-progress are performed in the same short transaction, so concurrent pods
 * skip rows already claimed by another transaction.
 *
 * Recovery continues to use the existing REQUEST_PICKED_DATE column. This is a
 * timeout-based recovery, not a renewable lease; configure STALE_AFTER_MINUTES
 * above the maximum expected legitimate processing duration.
 */
@Primary
@Repository
public class MultiPodProvisioningDaoImpl extends ProvisioningDaoImpl {

    private static final Logger log = LogManager.getLogger(MultiPodProvisioningDaoImpl.class);
    private static final int MAX_BATCH_SIZE = 1000;
    private static final int STALE_AFTER_MINUTES = 300;

    @PersistenceContext
    private EntityManager entityManager;

    private final CacheDao cache;

    public MultiPodProvisioningDaoImpl(CacheDao cache) {
        this.cache = cache;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<ProvisioningRequest> getProvisioningRequests(String tenantId, String maxRecords) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(maxRecords)) {
            throw new IllegalArgumentException("tenantId and maxRecords are required to claim requests");
        }

        final BigInteger tenant;
        final int batchSize;
        try {
            tenant = new BigInteger(tenantId.trim());
            batchSize = Math.max(1, Math.min(MAX_BATCH_SIZE, Integer.parseInt(maxRecords.trim())));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid tenant ID or request batch size", e);
        }

        int generatedStatus = cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT);
        int inProgressStatus = cache.intConfigVal(Constants.PROV_IN_PROGRESS, Constants.GLOBAL_TENANT);

        // The batch size is strictly parsed and capped above; it is embedded because
        // native-query LIMIT binding is not portable across JPA providers.
        String sql = "SELECT pr.* FROM provisioning_requests pr "
                + "WHERE pr.TENANT_ID = :tenantId AND (pr.STATUS = :generatedStatus "
                + "OR (pr.STATUS = :inProgressStatus AND (pr.REQUEST_PICKED_DATE IS NULL "
                + "OR pr.REQUEST_PICKED_DATE < DATE_SUB(NOW(), INTERVAL " + STALE_AFTER_MINUTES + " MINUTE)))) "
                + "ORDER BY pr.STATUS ASC, (pr.PRIORITY IS NULL) ASC, pr.PRIORITY ASC, pr.WORK_ORDER_ID ASC "
                + "LIMIT " + batchSize + " FOR UPDATE SKIP LOCKED";

        Query select = entityManager.createNativeQuery(sql, ProvisioningRequest.class)
                .setParameter("tenantId", tenant)
                .setParameter("generatedStatus", generatedStatus)
                .setParameter("inProgressStatus", inProgressStatus);

        List<ProvisioningRequest> claimed = select.getResultList();
        if (claimed.isEmpty()) {
            log.debug("No eligible requests found for tenantId={}", tenantId);
            return claimed;
        }

        Query markClaimed = entityManager.createNativeQuery(
                "UPDATE provisioning_requests SET STATUS = :status, REQUEST_PICKED_DATE = NOW() "
                        + "WHERE REQUEST_ID = :requestId AND TENANT_ID = :tenantId");
        Date pickedAt = new Date();
        for (ProvisioningRequest request : claimed) {
            int updated = markClaimed.setParameter("status", inProgressStatus)
                    .setParameter("requestId", request.getRequestID())
                    .setParameter("tenantId", tenant)
                    .executeUpdate();
            if (updated != 1) {
                throw new IllegalStateException("Claim transition failed for requestId=" + request.getRequestID());
            }
            request.setStatus(inProgressStatus);
            request.setRequestPickedDate(pickedAt);
        }

        log.info("Claimed {} requests for tenantId={} across MySQL pods", claimed.size(), tenantId);
        return new ArrayList<>(claimed);
    }
}
