package com.knot.provisioning.daoimpl;

import java.math.BigInteger;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knot.provioning.exception.ProvisioningException;
import com.knot.provioning.model.ActivityConfig;
import com.knot.provioning.model.ProvRespDetails;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provioning.model.ProvisioningRequestProcessingError;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.bo.ProvReqResponseDetails;
import com.knot.provisioning.bo.ProvisioningRequestDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsNsoBO;
import com.knot.provisioning.dao.CacheDao;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dto.ActivityConfigDto;
import com.knot.provisioning.dto.PaginationDTO;
import com.knot.provisioning.utill.Constants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Sameena
 *
 */

@Repository
public class ProvisioningDaoImpl implements ProvisioningDao {

	private static final String NEW_VERSION_AND_NOT_VALIDATED = "NEW_VERSION_AND_NOT_VALIDATED";

	private static final String REQUEST_ID = "requestID";

	private static final String REQUEST_DETAIL_ID = "requestDetailID";

	private static final String UPDATE_PROVISIONING_REQUEST_DETAILS_STATUS = "UPDATE provisioning_request_details SET STATUS = :status ";

	private static final String STATUS2 = "status";

	private static final String STATUS_NEW_VERSION_AND_NOT_VALIDATED = " AND STATUS <> :NEW_VERSION_AND_NOT_VALIDATED ";

	private static final Logger log = LogManager.getLogger(ProvisioningDaoImpl.class);

	private CacheDao cache;

	private EntityManager em;

	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	private EntityManager getEntityManager() {
		return em;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Autowired
	public void setCache(CacheDao cache) {
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequest> getProvisioningRequests(String tenantID, String maxNoOfrecords) {
		List<ProvisioningRequest> provRequestList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Start ProvisioningDaoImpl :: getProvisioningRequests() ");
			log.debug(
					" ProvisioningDaoImpl :: getProvisioningRequests() :: Passing AS Argumnets as tenantID {}, maxNoOfrecords{} ",
					tenantID, maxNoOfrecords);
			builder.append(
					"SELECT REQUEST_ID,TENANT_ID,PARTNER_ID,CUSTOMER_ID,NODE_ID,CUSTOMER_ACCOUNT_ID,WORK_ORDER_ID,ACTIVITY_ID,PRIORITY,ACTION_ID,REQUEST_CREATION_DATE,");
			builder.append(
					"REQUEST_PICKED_DATE,REQUEST_COMPLETION_DATE,STATUS,CREATED_BY,CREATED_ON,CREATED_IPADDR,MODIFIED_BY,MODIFIED_ON,MODIFIED_IPADDR FROM provisioning_requests ");
			builder.append("WHERE ((STATUS IN (:PROV_REQ_GENERATED) AND TENANT_ID = :tenantID) ");
			builder.append("OR (REQUEST_PICKED_DATE < DATE_ADD(NOW(), INTERVAL -300 MINUTE) ");
			builder.append("AND STATUS = :PROV_IN_PROGRESS)) ");
			builder.append("ORDER BY STATUS ASC,(PRIORITY IS NULL) ASC, PRIORITY ASC, WORK_ORDER_ID ASC ");
			builder.append("LIMIT :maxNoOfrecords FOR UPDATE SKIP LOCKED");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequest.class);
			query.setParameter("PROV_REQ_GENERATED",
					cache.strConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT))
					.setParameter("PROV_IN_PROGRESS",
							cache.strConfigVal(Constants.PROV_IN_PROGRESS, Constants.GLOBAL_TENANT))
					.setParameter("tenantID", tenantID)
					.setParameter("maxNoOfrecords", Integer.parseInt(maxNoOfrecords));
			provRequestList = query.getResultList();
			log.info(
			        "PROCESSOR_PRIORITY_TEST: Retrieved {} requests for tenantID={}",
			        provRequestList.size(), tenantID);

			for (ProvisioningRequest request : provRequestList) {
			    log.info(
			            "PROCESSOR_PRIORITY_TEST: requestID={}, workOrderID={}, priority={}, status={}",
			            request.getRequestID(),
			            request.getWorkOrderID(),
			            request.getPriority(),
			            request.getStatus());
			}
			log.info("Ended ProvisioningDaoImpl :: getProvisioningRequests() ");
		} catch (NullPointerException e) {
			return provRequestList;
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: getProvisioningRequests() ", e);
		}
		log.info("End ProvisioningDaoImpl :: getProvisioningRequests() ");
		return provRequestList;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequest> getProvisioningErrorRequests() {
		List<ProvisioningRequest> provRequestList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Start ProvisioningDaoImpl :: getProvisioningErrorRequests() ");
			builder.append(
					"SELECT REQUEST_ID,TENANT_ID,PARTNER_ID,CUSTOMER_ID,NODE_ID,CUSTOMER_ACCOUNT_ID,WORK_ORDER_ID,ACTIVITY_ID,ACTION_ID,REQUEST_CREATION_DATE,");
			builder.append(
					"REQUEST_PICKED_DATE,REQUEST_COMPLETION_DATE,STATUS,CREATED_BY,CREATED_ON,CREATED_IPADDR,MODIFIED_BY,MODIFIED_ON,MODIFIED_IPADDR FROM provisioning_requests");
			builder.append(" WHERE STATUS IN (:ERROR_IN_EXEC ");
			builder.append(",:PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV) ORDER BY REQUEST_CREATION_DATE LIMIT 10 ");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequest.class);
			query.setParameter("ERROR_IN_EXEC", cache.strConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT))
					.setParameter("PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV", cache
							.strConfigVal(Constants.PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV, Constants.GLOBAL_TENANT));
			provRequestList = query.getResultList();
			log.debug("Response From :: getProvisioningErrorRequests() :: ProvisioningRequest list Size {}",
					provRequestList.size());
			log.info("Ended ProvisioningDaoImpl :: getProvisioningErrorRequests() ");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("ProvisioningDaoImpl :: getProvisioningErrorRequests() ", e);
		}
		log.info("End ProvisioningDaoImpl :: getProvisioningErrorRequests() ");
		return provRequestList;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequestDetails> getRequestDetailsGroups(BigInteger requestID) {
		List<ProvisioningRequestDetails> provRequestDetailList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: getRequestDetails() :: Fetching request details for requestID={}", requestID);
			log.debug(" ProvisioningDaoImpl :: getRequestDetailsGroups() :: Passing arguments as requestID {}",
					requestID);
			builder.append(
					"SELECT REQUEST_DETAIL_ID,VERSION_NO,REQUEST_ID,LOB_ID,SERVICE_ID,TOKEN_ID,SERVICE_PACKAGE_ID,RATE_PLAN_ID,LOB_PRIORITY,RATE_PLAN_PRIORITY,");
			builder.append(
					"SEQUENCE_ID,SUB_SEQUENCE_ID,DEPENDENT_SEQUENCE_ID,DEPENDENT_SUB_SEQUENCE_ID,GROUP_NO,SUB_GROUP_NO,SERVICE_PRIORITY,INTERFACE_TYPE,SERVER_URL,");
			builder.append(
					"REQUEST_OBJECT,SERVER_NAME,REQUEST_METHOD,STATUS,REQUEST_CREATION_DATE,REQUEST_COMPLETION_DATE,REQUEST_PICKED_DATE,ACTUAL_PROCESS_TIME,");
			builder.append(
					"MAX_RETRIES,TIME_GAP_BETWEEN_RETRY,VERSION_DATE,CURRENT_RETRY_COUNT,RESPONSE_UPDATE_CATEGORY,PROV_OBJECT_ID,IS_ASYNC,PROCESS_TYPE,REQ_SER_RESP_ID,ADDL_COL1 FROM provisioning_request_details");
			builder.append(" WHERE REQUEST_ID = :requestID ");
			builder.append(STATUS_NEW_VERSION_AND_NOT_VALIDATED);
			builder.append(
					"ORDER BY LOB_PRIORITY,SERVICE_PRIORITY,RATE_PLAN_PRIORITY,GROUP_NO,SUB_SEQUENCE_ID,DEPENDENT_SEQUENCE_ID,SUB_GROUP_NO ");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetails.class);
			query.setParameter(REQUEST_ID, requestID).setParameter(NEW_VERSION_AND_NOT_VALIDATED,
					cache.intConfigVal(Constants.NEW_VERSION_AND_NOT_VALIDATED, Constants.GLOBAL_TENANT));
			provRequestDetailList = query.getResultList();
			log.info("Response From :: getRequestDetailsGroups() :: ProvisioningRequestDetails list Size {}",
					provRequestDetailList.size());
			log.info("Ended ProvisioningDaoImpl :: getRequestDetails() :: Retrieved {} request details successfully for requestID={}",
			        provRequestDetailList.size(), requestID);
			return provRequestDetailList;
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: getRequestDetailsGroups() ", e);
			return new ArrayList<>();
		}

	}

	public String getDependentResponse(int requestDetailID, int requestID) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Start ProvisioningDaoImpl :: getDependentResponse() ");
			log.debug(
					"ProvisioningDaoImpl :: getDependentResponse() :: by passing as arguments :: requestDetailID {}, requestID {}",
					requestDetailID, requestID);
			builder.append(
					"SELECT  RESPONSE_OBJECT FROM provisioning_request_response_details WHERE REQUEST_DETAIL_ID = :requestDetailID ");
			builder.append("AND REQUEST_ID = :requestID ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter(REQUEST_DETAIL_ID, requestDetailID).setParameter(REQUEST_ID, requestID);
			log.debug(" Response Result From getDependentResponse() ", query.getSingleResult().toString());
			log.info("End ProvisioningDaoImpl :: getDependentResponse() ");
			return query.getSingleResult().toString();
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: getDependentResponse() ", e);
			return null;
		}
	}

	public boolean updateProvisioningRequestProgress(BigInteger requestID) {
		//
		StringBuilder builder = new StringBuilder();
		Query query = null;
		boolean status = false;
		try {
			log.info("Started ProvisioningDaoImpl :: updateProvisioningRequestProgress() :: Passing Arguments as requestID {}",requestID);
			builder.append("UPDATE provisioning_requests SET REQUEST_PICKED_DATE = NOW(),REMARKS = :PROC_IN_PROGRESS ");
			builder.append(", STATUS = :PROV_IN_PROGRESS ");
			builder.append(" WHERE REQUEST_ID = :requestID ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("PROC_IN_PROGRESS",
					cache.strConfigVal(Constants.PROC_IN_PROGRESS, Constants.GLOBAL_TENANT))
					.setParameter("PROV_IN_PROGRESS",
							cache.intConfigVal(Constants.PROV_IN_PROGRESS, Constants.GLOBAL_TENANT))
					.setParameter(REQUEST_ID, requestID);
			query.executeUpdate();
			log.debug(" Response Result From updateProvisioningRequestProgress() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequestProgress() :: Provisioning request progress updated successfully. requestID={}, status={}",
				    requestID, cache.intConfigVal(Constants.PROV_IN_PROGRESS, Constants.GLOBAL_TENANT));
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequestProgress() ", e);
		}
		log.info("End ProvisioningDaoImpl :: updateProvisioningRequestProgress() ");
		return status;
	}

	@Transactional
	public boolean updateProvReqProgress(BigInteger requestID, int stat) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		boolean status = false;
		try {
			log.info("Start ProvisioningDaoImpl :: updateProvReqProgress() ");
			log.debug("ProvisioningDaoImpl :: updateProvReqProgress() :: Passing Arg as :: requestID {}, stat {}",
					requestID, stat);

			builder.append("UPDATE provisioning_requests SET REQUEST_PICKED_DATE = NOW(), STATUS = :stat ");
			builder.append(" WHERE REQUEST_ID = :requestID  ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("stat", stat).setParameter(REQUEST_ID, requestID);
			query.executeUpdate();
			log.debug(" Response Result From updateProvReqProgress() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvReqProgress() ");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvReqProgress() ", e);
		}
		log.info("End ProvisioningDaoImpl :: updateProvReqProgress() ");
		return status;
	}

	@Transactional
	public boolean saveProcessError(ProvisioningRequestProcessingError obj) {
		log.info("Start ProvisioningDaoImpl :: saveProcessError() ");
		try {
			String id = this.getSeqNumber("PROV_RESPONSE_ERROR_RECORD_ID");
			obj.setErrorRecordID(new BigInteger(id));
			getEntityManager().merge(obj);
			log.info("End ProvisioningDaoImpl :: saveProcessError() ");
			return true;
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: saveProcessError() ", e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequestDetails> getByServiceParallel(int requestID) {
		List<ProvisioningRequestDetails> provRequestDetailList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Start ProvisioningDaoImpl :: getByServiceParallel() ");
			log.debug("ProvisioningDaoImpl :: getByServiceParallel() :: Passing As arguments  requestID {}", requestID);
			builder.append(
					"SELECT REQUEST_DETAIL_ID,VERSION_NO,REQUEST_ID,LOB_ID,SERVICE_ID,TOKEN_ID,SERVICE_PACKAGE_ID,RATE_PLAN_ID,LOB_PRIORITY,RATE_PLAN_PRIORITY,");
			builder.append(
					"SEQUENCE_ID,SUB_SEQUENCE_ID,DEPENDENT_SEQUENCE_ID,DEPENDENT_SUB_SEQUENCE_ID,GROUP_NO,SUB_GROUP_NO,SERVICE_PRIORITY,INTERFACE_TYPE,SERVER_URL,");
			builder.append(
					"REQUEST_OBJECT,SERVER_NAME,REQUEST_METHOD,STATUS,REQUEST_CREATION_DATE,REQUEST_COMPLETION_DATE,REQUEST_PICKED_DATE,ACTUAL_PROCESS_TIME");
			builder.append(
					"MAX_RETRIES,TIME_GAP_BETWEEN_RETRY,VERSION_DATE,CURRENT_RETRY_COUNT,RESPONSE_UPDATE_CATEGORY,PROV_OBJECT_ID,IS_ASYNC,PROCESS_TYPE FROM provisioning_request_details");
			builder.append(" WHERE REQUEST_ID =:requestID ");
			builder.append("AND LOB_PRIORITY = :INDEPENDANT ");
			builder.append("AND SERVICE_PRIORITY = :INDEPENDANT ");
			builder.append("ORDER BY LOB_PRIORITY,SERVICE_PRIORITY,RATE_PLAN_PRIORITY");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetails.class);
			query.setParameter(REQUEST_ID, requestID)
					.setParameter("INDEPENDANT", cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT))
					.setParameter("INDEPENDANT", cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT));
			provRequestDetailList = query.getResultList();
			log.debug("Response From getByServiceParallel() ProvisioningRequestDetails list Size {}",
					provRequestDetailList.size());
			log.info("Ended ProvisioningDaoImpl :: getByServiceParallel()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: getByServiceParallel() ", e);
		}
		log.info("End ProvisioningDaoImpl :: getByServiceParallel() ");
		return provRequestDetailList;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequestDetails> getByServiceSequence(int requestID) {
		List<ProvisioningRequestDetails> provRequestDetailList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Sart ProvisioningDaoImpl :: getByServiceParallel() ");
			log.debug(" ProvisioningDaoImpl :: getByServiceParallel() :: passing argumebts as  requestID {}",
					requestID);
			builder.append(
					"SELECT REQUEST_DETAIL_ID,VERSION_NO,REQUEST_ID,LOB_ID,SERVICE_ID,TOKEN_ID,SERVICE_PACKAGE_ID,RATE_PLAN_ID,LOB_PRIORITY,RATE_PLAN_PRIORITY,");
			builder.append(
					"SEQUENCE_ID,SUB_SEQUENCE_ID,DEPENDENT_SEQUENCE_ID,DEPENDENT_SUB_SEQUENCE_ID,GROUP_NO,SUB_GROUP_NO,SERVICE_PRIORITY,INTERFACE_TYPE,SERVER_URL,");
			builder.append(
					"REQUEST_OBJECT,SERVER_NAME,REQUEST_METHOD,STATUS,REQUEST_CREATION_DATE,REQUEST_COMPLETION_DATE,REQUEST_PICKED_DATE,ACTUAL_PROCESS_TIME");
			builder.append(
					"MAX_RETRIES,TIME_GAP_BETWEEN_RETRY,VERSION_DATE,CURRENT_RETRY_COUNT,RESPONSE_UPDATE_CATEGORY,PROV_OBJECT_ID,IS_ASYNC,PROCESS_TYPE FROM provisioning_request_details");
			builder.append(" WHERE REQUEST_ID = :requestID ");
			builder.append(" AND LOB_PRIORITY = :LOB_PRIO ");
			builder.append(" AND SERVICE_PRIORITY <> :SERVICE_PRIO ");
			builder.append(" ORDER BY LOB_PRIORITY,SERVICE_PRIORITY,RATE_PLAN_PRIORITY ");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetails.class);
			query.setParameter(REQUEST_ID, requestID)
					.setParameter("LOB_PRIO", cache.intConfigVal(Constants.LOB_PRIO, Constants.GLOBAL_TENANT))
					.setParameter("SERVICE_PRIO", cache.strConfigVal(Constants.SERVICE_PRIO, Constants.GLOBAL_TENANT));

			provRequestDetailList = query.getResultList();
			log.debug("Response From getByServiceSequence() ProvisioningRequestDetails list Size {}",
					provRequestDetailList.size());
			log.info("Ended ProvisioningDaoImpl :: getByServiceSequence()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: getByServiceSequence() ", e);
		}
		log.info("End ProvisioningDaoImpl :: getByServiceParallel() ");
		return provRequestDetailList;
	}

	@Transactional
	public boolean saveProvisioningRequestResponseDetail(ProvisioningRequestResponseDetail obj) {
		boolean flag = true;
		try {
			log.info("Started ProvisioningDaoImpl :: saveProvisioningRequestResponseDetail () with requestDTLID ", obj.getRequestDetailID());
			String id = this.getSeqNumber("PROV_RESPONSE_ID");
			obj.setResponseID(new BigInteger(id));
			getEntityManager().merge(obj);
			log.info("Ended ProvisioningDaoImpl :: saveProvisioningRequestResponseDetail () responseDetails saved successFully with responseID ", obj.getResponseID());
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: saveProvisioningRequestResponseDetail() ", e);
			obj.setErrorDescription(e.getMessage());
			flag = false;
		}
		return flag;
	}

	@Transactional
	public boolean updateProvisioningRequest(BigInteger requestID, int stats, String remarks,
			Date requestCompletionDate) {
		StringBuilder builder = new StringBuilder();
		StringBuilder requestComplDate = new StringBuilder();
		Query query = null;
		boolean status = false;
		log.info(
				" ProvisioningDaoImpl :: updateProvisioningRequest() passing argumnets as requestID {}, stats {}, remarks {}",
				requestID, stats, remarks);
		try {
			if (requestCompletionDate == null) {
				requestComplDate.append(",REQUEST_COMPLETION_DATE = NOW()");
			}
			builder.append("UPDATE provisioning_requests set STATUS = :stats ");
			builder.append(",REMARKS= :remarks ").append(requestComplDate);
			builder.append(" WHERE REQUEST_ID = :requestID ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("stats", stats).setParameter("remarks", remarks).setParameter(REQUEST_ID, requestID);

			query.executeUpdate();
			log.debug("Response From updateProvisioningRequest() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequest()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequest() ", e);
		}
		return status;
	}

	@Transactional
	public boolean updateProvisioningRequestDetailCompletion(BigInteger requestDetailID, int status, int retryCount,
			String errorCode, String errorDescription) {
		StringBuilder builder = new StringBuilder();
		Map<String, Object> setParamMap = new HashMap<>();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion()");
			log.debug(
					" ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion() passing as aruments  requestDetailID {}, status {}, retryCount{}",
					requestDetailID, status, retryCount);
			builder.append(UPDATE_PROVISIONING_REQUEST_DETAILS_STATUS);
			builder.append(", REQUEST_COMPLETION_DATE = NOW() ");
			builder.append(", CURRENT_RETRY_COUNT = :retryCount ");
			if (StringUtils.hasText(errorCode) && StringUtils.hasText(errorDescription)) {
				builder.append(", ERROR_CODE = :errorCode ");
				builder.append(", ERROR_DESCRIPTION = :errorDescription ");
				setParamMap.put("errorCode", errorCode);
				setParamMap.put("errorDescription", errorDescription);
			}
			builder.append(" WHERE REQUEST_DETAIL_ID = :requestDetailID ");
			builder.append(STATUS_NEW_VERSION_AND_NOT_VALIDATED);
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter(STATUS2, status).setParameter("retryCount", retryCount)
					.setParameter(REQUEST_DETAIL_ID, requestDetailID).setParameter(NEW_VERSION_AND_NOT_VALIDATED,
							cache.intConfigVal(Constants.NEW_VERSION_AND_NOT_VALIDATED, Constants.GLOBAL_TENANT));
			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				query.setParameter(key, val);
			}
			query.executeUpdate();
			log.debug("Response From updateProvisioningRequestDetailCompletion() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion()", e);
			return false;
		}
		return true;
	}

	@Transactional
	public boolean updateProvisioningRequestDetailCompletion(BigInteger requestDetailID, int status, int retryCount) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion() :: requestDetailID={}, status={}, retryCount={}",
			        requestDetailID, status, retryCount);
			builder.append(UPDATE_PROVISIONING_REQUEST_DETAILS_STATUS);
			builder.append(", ACTUAL_PROCESS_TIME = ");
			builder.append(" (SELECT TIMESTAMPDIFF(MINUTE, REQUEST_PICKED_DATE, NOW()))");
			builder.append(", REQUEST_COMPLETION_DATE = NOW() ");
			builder.append(", CURRENT_RETRY_COUNT = :retryCount ");
			builder.append(" WHERE REQUEST_DETAIL_ID = :requestDetailID  ");
			builder.append(STATUS_NEW_VERSION_AND_NOT_VALIDATED);
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter(STATUS2, status).setParameter("retryCount", retryCount)
					.setParameter(REQUEST_DETAIL_ID, requestDetailID).setParameter(NEW_VERSION_AND_NOT_VALIDATED,
							cache.intConfigVal(Constants.NEW_VERSION_AND_NOT_VALIDATED, Constants.GLOBAL_TENANT));
			query.executeUpdate();
			log.debug("Response From updateProvisioningRequestDetailCompletion() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion() :: requestDetailID={}, status={}, retryCount={}",
			        requestDetailID, status, retryCount);
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion() ", e);
			return false;
		}
		return true;
	}

	@Transactional
	public boolean updateProvisioningRequestDetails(BigInteger requestDetailId, BigInteger requestId) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: updateProvisioningRequestDetails()");
			log.debug(
					" ProvisioningDaoImpl :: updateProvisioningRequestDetails() take as argumnets By passing requestDetailId {}, requestId {}",
					requestDetailId, requestId);
			builder.append("UPDATE provisioning_request_details SET ");
			builder.append("REQUEST_PICKED_DATE =NOW() WHERE REQUEST_DETAIL_ID = :requestDetailId ");
			builder.append(" AND REQUEST_ID = :requestId ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("requestDetailId", requestDetailId).setParameter("requestId", requestId);
			query.executeUpdate();
			log.debug("Response From updateProvisioningRequestDetails() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequestDetails()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequestDetailCompletion() ", e);
			return false;
		}
		return true;
	}

	@Transactional
	public boolean updateProvReqDet4PostProv(int requestDetailID, int status) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: updateProvReqDet4PostProv()");
			log.debug(
					" ProvisioningDaoImpl :: updateProvReqDet4PostProv() take as argumnets By passing requestDetailID {}, status {}",
					requestDetailID, status);
			builder.append(UPDATE_PROVISIONING_REQUEST_DETAILS_STATUS);
			builder.append(" WHERE REQUEST_DETAIL_ID = :requestDetailID  ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter(STATUS2, status).setParameter(REQUEST_DETAIL_ID, requestDetailID);
			query.executeUpdate();
			log.info("Ended ProvisioningDaoImpl :: updateProvReqDet4PostProv()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvReqDet4PostProv() ", e);
			return false;
		}
		return true;
	}

	@Transactional
	public boolean updateRequestObject(BigInteger requestDetailID, String requestObject) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			log.info("Started ProvisioningDaoImpl :: updateRequestObject()");
			log.debug(
					" ProvisioningDaoImpl :: updateRequestDetailServerUrl() take as argumnets By passing requestDetailID {}, requestObject {}",
					requestDetailID, requestObject);
			builder.append("UPDATE provisioning_request_details SET REQUEST_OBJECT = :requestObject ");
			builder.append("WHERE REQUEST_DETAIL_ID = :requestDetailID ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("requestObject", requestObject).setParameter(REQUEST_DETAIL_ID, requestDetailID);
			query.executeUpdate();
			log.debug(" Response Result From updateRequestObject() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateRequestObject()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateRequestObject() ", e);
			return false;
		}
		return true;
	}

	@Transactional
	public boolean updateRequestDetailServerUrl(String url, BigInteger requestDetailID, String responseUniqueID) {
		log.info("Started ProvisioningDaoImpl :: updateRequestDetailServerUrl() ");
		log.debug(
				" ProvisioningDaoImpl :: updateRequestDetailServerUrl() take as argumnets By passing url {}, requestDetailID {}",
				url, requestDetailID);

		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			builder.append(
					"UPDATE provisioning_request_details SET SERVER_URL = :url, REQ_SER_RESP_ID = :responseUniqueID  ");
			builder.append("WHERE REQUEST_DETAIL_ID = :requestDetailID ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("url", url).setParameter("responseUniqueID", responseUniqueID)
					.setParameter(REQUEST_DETAIL_ID, requestDetailID);
			query.executeUpdate();
			log.debug(" Response Result From updateRequestDetailServerUrl() Boolean {}", query.executeUpdate());
			log.info("Ended ProvisioningDaoImpl :: updateRequestDetailServerUrl() ");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateRequestObject() ", e);
			return false;
		}
		return true;
	}

	public String getSeqNumber(String appName) {
		String sequenceNo = null;
		SimpleJdbcCall generateSequenceSP = null;
		Map<String, Object> in = null;
		Map<String, Object> out = null;
		final String constant = "P_IDNAME";
		final String nextID = "P_NEXTID";
		try {
			generateSequenceSP = new SimpleJdbcCall(jdbcTemplate).withProcedureName("GET_SEQUENCE_ID")
					.withoutProcedureColumnMetaDataAccess().useInParameterNames(constant)
					.declareParameters(new SqlParameter(constant, Types.VARCHAR),
							new SqlOutParameter(nextID, Types.INTEGER), new SqlOutParameter("P_RESULT", Types.VARCHAR));
			in = new HashMap<>();
			in.put(constant, appName);
			out = generateSequenceSP.execute(in);
			if (out.get(nextID) != null) {
				sequenceNo = out.get(nextID).toString();
			}
		} catch (Exception ex) {
			log.error("ProvisioningDaoImpl::getSeqNumber() ", ex);
		}
		return sequenceNo;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProvRespDetails> getProvisioningResponse(ProvisioningRequestDetails detail,
			ProvisioningRequest provisioningRequest) {
		log.info("Started ProvisioningDaoImpl :: getProvisioningResponse() ");
		;
		StringBuilder builder = new StringBuilder();
		try {
			builder.append(
					"SELECT PROVISIONING_RESP_ID,PROVISIONING_REQUEST_ID,SERVER_URL,RESPONSE_CODES,DESCRIPTION,API_OBJECT FROM provisioning_response_details WHERE PROVISIONING_REQUEST_ID IN ")
					.append(" (SELECT provisioning_request_id FROM provisioning_request_objects WHERE ")
					.append(" service_id = :serviceId  AND activity_id = :activityId AND action_id = :actionId and status = 1) ")
					.append("and status = :status");
			Query query = getEntityManager().createNativeQuery(builder.toString(), ProvRespDetails.class);
			query.setParameter("serviceId", detail.getServiceID())
					.setParameter("activityId", provisioningRequest.getActivityID())
					.setParameter("actionId", provisioningRequest.getActionID())
					.setParameter(STATUS2, cache.strConfigVal(Constants.ACT_STATUS, Constants.GLOBAL_TENANT));
			return query.getResultList();
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl::getProvisioningResponse() ", e);
			return new ArrayList<>();
		}

	}

	@Override
	@Transactional
	public Boolean getProvisioningErrorRequests(Integer reqDetailId) {
		Query qurey = null;
		StringBuilder builder = new StringBuilder();
		Date requestCompletionDate = null;
		Boolean flag = true;
		try {
			log.info("Started ProvisioningDaoImpl::getProvisioningErrorRequests() ");
			builder.append(
					"SELECT prd2.* FROM provisioning_requests p1 INNER JOIN provisioning_request_details prd2 ON p1.status = prd2.status WHERE p1.status NOT IN (19) AND prd2.REQUEST_DETAIL_ID = :reqDetailId ");
			qurey = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetails.class);
			qurey.setParameter("reqDetailId", reqDetailId);
			ProvisioningRequestDetails reqDetails = (ProvisioningRequestDetails) qurey.getSingleResult();
			builder = new StringBuilder();
			builder.append(
					"UPDATE provisioning_request_details SET STATUS = 11, ERROR_DESCRIPTION = NULL WHERE REQUEST_DETAIL_ID = :reqDetailId");
			qurey = getEntityManager().createNativeQuery(builder.toString());
			qurey.setParameter("reqDetailId", reqDetailId);
			qurey.executeUpdate();

			this.updateProvisioningRequest(reqDetails.getRequestID(), 11, "Sucess", requestCompletionDate);
			return flag;
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl::getProvisioningErrorRequests() ", e);
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProvReqResponseDetails> getProvRequestResponseDetails(BigInteger reqDtlID, BigInteger reqID) {
		log.info("Started ProvisioningDaoImpl::getProvRequestResponseDetails() ");
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		if (reqDtlID != null) {
			whereCause.append("WHERE REQUEST_DETAIL_ID in (:reqDtlID) ");
		}
		if (reqID != null) {
			whereCause.append("WHERE REQUEST_ID in (:reqID) ");
		}
		Query qurey = null;
		List<ProvReqResponseDetails> responseObj = new ArrayList<>();
		try {
			builder.append("SELECT * FROM  provisioning_request_response_details ").append(whereCause);
			qurey = getEntityManager().createNativeQuery(builder.toString(), ProvReqResponseDetails.class);
			if (reqDtlID != null) {
				qurey.setParameter("reqDtlID", reqDtlID);
			}
			if (reqID != null) {
				qurey.setParameter("reqID", reqID);
			}
			responseObj = qurey.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("An exception occured in ProvisioningDaoImpl::getProvRequestResponseDetails()  ", e);
		}
		return responseObj;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProvisioningRequestDetailsNsoBO> getProvisioningRequestDetalisToWfm(BigInteger workOderID,
			Integer status) {
		StringBuilder builder = new StringBuilder();
		Query qurey = null;
		List<ProvisioningRequestDetailsNsoBO> rDList = new ArrayList<>();
		try {
			builder.append("SELECT pr.TENANT_ID, prd.* FROM provisioning_request_details prd, ")
					.append("provisioning_requests pr WHERE ")
					.append("prd.REQUEST_ID = pr.REQUEST_ID AND pr.WORK_ORDER_ID in(:workOderID) ")
					.append("AND prd.STATUS = :status");
			qurey = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetailsNsoBO.class);
			qurey.setParameter("workOderID", workOderID).setParameter("status", status);
			rDList = qurey.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rDList;
	}

	@Transactional
	@Override
	public boolean updateProvReqDtlCompletionStatusByRespId(String reqDtlId, String status, String externalID,
			String requestDate) {
		log.info("Started ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId ()");
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		StringBuilder requestComplDate = new StringBuilder();
		Query query = null;
		Map<String, Object> setParamMap = new HashMap<>();

		try {
			if (StringUtils.hasText(reqDtlId)) {
				whereCause.append(" WHERE REQUEST_DETAIL_ID = :reqDtlId ");
				setParamMap.put("reqDtlId", reqDtlId);
			}
			if (StringUtils.hasText(externalID)) {
				whereCause.append(" WHERE REQ_SER_RESP_ID = :externalID ");
				setParamMap.put("externalID", externalID);
			}
			if (StringUtils.hasText(requestDate)) {
				requestComplDate.append("REQUEST_COMPLETION_DATE = '" + requestDate + "'");
			}
			if (!StringUtils.hasText(requestDate)) {
				requestComplDate.append("REQUEST_COMPLETION_DATE =NOW() ");
			}
			builder.append("UPDATE provisioning_request_details SET STATUS = :status , ").append(requestComplDate)
					.append(whereCause);
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("status", status);
			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString();
				query.setParameter(key, val);
			}
			int updatedRows = query.executeUpdate();
			log.info("Ended ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId () No of Rows update :{}",
					updatedRows);
		} catch (Exception e) {
			log.error("Error Occured in ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId() ", e);
			return false;
		}
		return true;
	}

	@Transactional
	@Override
	public boolean updateProvisioningRequestStatusByDetailId(BigInteger responseId, int status, String requestDate) {
		StringBuilder builder = new StringBuilder();
		Query query = null;
		StringBuilder requestComplDate = new StringBuilder();
		try {
			if (StringUtils.hasText(requestDate)) {
				requestComplDate.append("REQUEST_COMPLETION_DATE = '" + requestDate + "'");
			}
			if (!StringUtils.hasText(requestDate)) {
				requestComplDate.append("REQUEST_COMPLETION_DATE =NOW() ");
			}
			log.info("Started ProvisioningDaoImpl :: updateProvisioningRequestStatusByDetailId()");
			log.info(
					"ProvisioningDaoImpl :: updateProvisioningRequestStatusByDetailId() arguments: requestDetailID {}, status {}",
					responseId, status);

			builder.append("UPDATE provisioning_requests ");
			builder.append("SET STATUS = :status, ").append(requestComplDate);
			builder.append(" WHERE request_id IN (");
			builder.append("SELECT request_id FROM provisioning_request_details WHERE REQUEST_DETAIL_ID = :responseId");
			builder.append(")");

			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("status", status).setParameter("responseId", responseId);

			int updatedRows = query.executeUpdate();
			log.debug("Rows updated: {}", updatedRows);
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningRequestStatusByDetailId()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningRequestStatusByDetailId() ", e);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProvisioningRequestDetailsBO getRequestDtlIdByPassingExternalID(String externalId) {
		log.info("Started ProvisioningRequestDaoImpl :: getRequestDtlIdByPassingExternalID {} :: ", externalId);
		StringBuilder builder = new StringBuilder();
		ProvisioningRequestDetailsBO requestDetailObj = new ProvisioningRequestDetailsBO();
		Query qurey = null;
		try {
			if (StringUtils.hasText(externalId)) {
				builder.append("SELECT * FROM provisioning_request_details WHERE REQ_SER_RESP_ID =:externalId");
				qurey = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetailsBO.class);
				qurey.setParameter("externalId", externalId);
				List<ProvisioningRequestDetailsBO> resultList = qurey.getResultList();
				if (!CollectionUtils.isEmpty(resultList)) {
					requestDetailObj = resultList.get(0);
				} else {
					throw new ProvisioningException("No data founded for the provided externalId :: " + externalId);
				}

			} else {
				throw new ProvisioningException("Unable to process the request we did not recived any externalId");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("An exception Occured in ProvisioningRequestDaoImpl : : getRequestDeatilsByReqId {}", e);
			throw e;
		}
		return requestDetailObj;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProvisioningRequestDetailsNsoBO> getRequestIdsByReqDtlId(String reqDtlId, String externalId) {
		log.info("Started ProvisioningRequestDaoImpl :: getRequestIdsByyReqDtlId {} :: ", reqDtlId);
		StringBuilder builder = new StringBuilder();
		List<ProvisioningRequestDetailsNsoBO> rDList = new ArrayList<>();
		Query qurey = null;
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParamMap = new HashMap<>();
		try {
			if (StringUtils.hasText(reqDtlId)) {
				whereCause.append("where REQUEST_DETAIL_ID = :reqDtlId AND prd.STATUS NOT IN (:status)) ");
				setParamMap.put("reqDtlId", reqDtlId);
				setParamMap.put("status", Arrays.asList(19, 99));
			}
			if (StringUtils.hasText(externalId)) {
				whereCause.append("where REQ_SER_RESP_ID = :externalId AND prd.STATUS NOT IN (:status)) ");
				setParamMap.put("externalId", externalId);
				setParamMap.put("status", Arrays.asList(19, 99));
			}
			builder.append(
					"SELECT prd.*, pr.TENANT_ID FROM provisioning_request_details prd JOIN provisioning_requests pr ");
			builder.append(
					"ON prd.request_id = pr.request_id WHERE prd.request_id IN (SELECT request_id FROM provisioning_request_details ");
			builder.append(whereCause);
			qurey = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetailsNsoBO.class);

			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				qurey.setParameter(entry.getKey(), entry.getValue());
			}

			rDList = qurey.getResultList();
			log.info("Ended ProvisioningRequestDaoImpl :: getRequestIdsByyReqDtlId :: ");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("An exception Occured in ProvisioningRequestDaoImpl : : getRequestDeatilsByReqId {}", e);
		}
		return rDList;
	}

	@Transactional
	@Override
	public boolean updateProvReqResponseDtls(BigInteger reqDtlId, Integer respSeqNo, String responseObj) {
		log.info("Started ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId ()");
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			builder.append(
					"UPDATE provisioning_request_response_details SET RESPONSE_OBJECT = :responseObj, RESPONSE_SEQUENCE_NO= :respSeqNo,RESPONSE_RECEIVED_DATE = NOW() ")
					.append("WHERE REQUEST_DETAIL_ID = :reqDtlId ");
			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("responseObj", responseObj);
			query.setParameter("respSeqNo", respSeqNo);
			query.setParameter("reqDtlId", reqDtlId);
			int updatedRows = query.executeUpdate();
			log.info("Ended ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId () No of Rows update :{}",
					updatedRows);
		} catch (Exception e) {
			log.error("Error Occured in ProvisioningDaoImpl :: updateProvReqDtlCompletionStatusByRespId() ", e);
			return false;
		}
		return true;
	}

	public String getReqDtlSeqNumber(String id) {
		try {
			log.info("Sequence No creation for :- {}!!!", id);
			StoredProcedureQuery query = em.createStoredProcedureQuery("GET_SEQUENCE_ID");
			query.registerStoredProcedureParameter("P_IDNAME", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("P_NEXTID", String.class, ParameterMode.OUT);
			query.registerStoredProcedureParameter("P_RESULT", String.class, ParameterMode.OUT);
			query.setParameter("P_IDNAME", id);
			query.execute();
			String sequenceNo = (String) query.getOutputParameterValue("P_NEXTID");
			log.info("sequence Number :: {}", sequenceNo);
			return sequenceNo;
		} catch (Exception e) {
			log.error("Error from WorkOrderDaoImpl - getSeqNumber() ", e);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updateProvisioningNetwork(String reqDtlID, String workOrderId, String requestComplDate) {
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		Query query = null;
		boolean status = false;
		Map<String, Object> setParamMap = new HashMap<>();
		log.info("Start ProvisioningDaoImpl :: updateProvisioningNetwork() with DetailId {}", reqDtlID);
		try {
			if (StringUtils.hasText(reqDtlID)) {
				whereCause.append("where prd.REQUEST_DETAIL_ID = :reqDtlID ");
				setParamMap.put("reqDtlID", reqDtlID);
			}
			if (StringUtils.hasText(workOrderId)) {
				whereCause.append("where pr.work_order_id =:workOrderId ");
				setParamMap.put("workOrderId", workOrderId);
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String completionDate = StringUtils.hasText(requestComplDate) ? requestComplDate
					: LocalDateTime.now().format(formatter);
			String idleSta = cache.strConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT);
			String reqDtlSucess = cache.strConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
			String reqCreation = cache.strConfigVal(Constants.REQ_GEN_SUCCESS, Constants.GLOBAL_TENANT);
			builder.append("UPDATE provisioning_request_details prd INNER JOIN provisioning_requests pr ").append(
					"ON pr.request_id = prd.request_id SET prd.status =:reqDtlSucess, pr.status =:reqCreation, pr.REQUEST_COMPLETION_DATE =STR_TO_DATE (:completionDate, '%Y-%m-%d %H:%i:%s')")
					.append(whereCause).append("AND prd.status =:idleSta AND pr.status =:idleSta");
			query = em.createNativeQuery(builder.toString());
			query.setParameter("reqDtlSucess", Integer.parseInt(reqDtlSucess))
					.setParameter("reqCreation", Integer.parseInt(reqCreation))
					.setParameter("idleSta", Integer.parseInt(idleSta)).setParameter("completionDate", completionDate);
			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
			int updatedRows = query.executeUpdate();
			log.debug("Response Result From updateProvisioningNetwork() Updated Rows {}", updatedRows);
			if (updatedRows > 0) {
				log.info("Request details status updated sucessfully with DetailId {}", reqDtlID);
				status = true;
			}
			log.info("Ended ProvisioningDaoImpl :: updateProvisioningNetwork()");
		} catch (Exception e) {
			log.error("ProvisioningDaoImpl :: updateProvisioningNetwork() Exception", e);
		}

		log.info("End ProvisioningDaoImpl :: updateProvisioningNetwork()");

		return status;
	}

	@Override
	public List<ProvisioningRequestDetailsBO> getRequestIdRequestDtlIdByPassingWorkOrderId(int workOrderId) {
		log.info("Started ProvisioningRequestDaoImpl :: getRequestDtlIdByPassingWorkOrderId() workOrderId {}",
				workOrderId);
		List<ProvisioningRequestDetailsBO> detailsModel = null;
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT prd.* ");
			builder.append("FROM provisioning_requests pr ");
			builder.append("INNER JOIN provisioning_request_details prd ");
			builder.append("ON pr.REQUEST_ID = prd.REQUEST_ID ");
			builder.append("WHERE pr.WORK_ORDER_ID = :workOrderId ").append("AND prd.status =:status");
			Query query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestDetailsBO.class);
			query.setParameter("workOrderId", workOrderId);
			query.setParameter("status", cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT));
			detailsModel = query.getResultList();
			log.info("Ended ProvisioningRequestDaoImpl :: getRequestDtlIdByPassingWorkOrderId() workOrderId {}",
					workOrderId);
			return detailsModel;
		} catch (Exception e) {
			log.error("An Exception Occured in ProvisioningRequestDaoImpl :: getRequestDtlIdByPassingWorkOrderId()", e);
		}
		return detailsModel;
	}

	@Override
	@Transactional
	public boolean saveProvActivityConfig(ActivityConfig activityConfig) {
		log.info("ProvisioningDaoImpl :: saveProvActivityConfig() started");
		try {
			getEntityManager().merge(activityConfig);
			log.info("ProvisioningDaoImpl :: saveProvActivityConfig() saved successfully");
			return true;
		} catch (Exception e) {
			log.error("Exception in ProvisioningDaoImpl :: saveProvActivityConfig()", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ActivityConfig> getAllActvityConfigs(ActivityConfigDto dto) {
		log.info("Started ProvisioningRequestDaoImpl :: getAllActvityConfigs() ");
		StringBuilder builder = new StringBuilder();
		List<ActivityConfig> configList = new ArrayList<>();
		Query qurey = null;
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParamMap = new HashMap<>();
		try {
			if (StringUtils.hasText(dto.getTenantId())) {
				whereCause.append(" AND TENANT_ID = :tenantID ");
				setParamMap.put("tenantID", dto.getTenantId());
			}
			if (StringUtils.hasText(dto.getActivityId())) {
				whereCause.append(" AND ACTIVITY_ID = :activityID ");
				setParamMap.put("activityID", dto.getActivityId());
			}
			if (StringUtils.hasText(dto.getPriority())) {
				whereCause.append(" AND PRIORITY = :priority ");
				setParamMap.put("priority", dto.getPriority());
			}
			if (StringUtils.hasText(dto.getFromDate()) && StringUtils.hasText(dto.getToDate())) {
				whereCause.append(" AND CREATED_ON >= :fromDate ")
						.append(" AND CREATED_ON < DATE_ADD(:toDate, INTERVAL 1 DAY) ");
				setParamMap.put("fromDate", dto.getFromDate());
				setParamMap.put("toDate", dto.getToDate());
			}
			String baseCondition = " FROM provisioning_activity_priorities "
					+ " WHERE NOW() BETWEEN EFFECTIVE_FROM AND EFFECTIVE_TO " + whereCause;
			String countSql = "SELECT COUNT(*) " + baseCondition;
			Query countQuery = getEntityManager().createNativeQuery(countSql);
			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				countQuery.setParameter(entry.getKey(), entry.getValue());
			}
			long totalCount = ((Number) countQuery.getSingleResult()).longValue();
			builder.append("SELECT * ").append(baseCondition);
			qurey = getEntityManager().createNativeQuery(builder.toString(), ActivityConfig.class);
			for (Map.Entry<String, Object> entry : setParamMap.entrySet()) {
				qurey.setParameter(entry.getKey(), entry.getValue());
			}
			PaginationDTO paginationObj = dto.getPaginationObj();
			if (Objects.nonNull(paginationObj)) {
				int pageNumber = Integer.parseInt(paginationObj.getPageNumber());
				int pageSize = Integer.parseInt(paginationObj.getPageCount());
				paginationObj.setMinSize((pageNumber - 1) * pageSize);
				paginationObj.setMaxSize(pageSize);
				paginationObj.setTotalDisplayCount(String.valueOf(totalCount));
				dto.setPaginationObj(paginationObj);
				qurey.setFirstResult(paginationObj.getMinSize());
				qurey.setMaxResults(paginationObj.getMaxSize());
				ObjectMapper mapper = new ObjectMapper();
				log.info("Pagination object :: {}", mapper.writeValueAsString(paginationObj));

			} else {
				paginationObj = new PaginationDTO();
				paginationObj.setTotalDisplayCount(String.valueOf(totalCount));
				dto.setPaginationObj(paginationObj);
			}
			configList = qurey.getResultList();

			log.info("Total matching activity configurations: {}", totalCount);
			log.info("Ended ProvisioningRequestDaoImpl :: getAllActvityConfigs() ");

		} catch (Exception e) {
			log.error("Exception in ProvisioningRequestDaoImpl :: getAllActvityConfigs()", e);
			throw new ProvisioningException(e.getMessage());
		}

		return configList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ActivityConfig getActivityConfigByConfigId(String configId) {
		log.info("Started ProvisioningDaoImpl :: getActivityConfigByConfigId, configId: {}", configId);
		StringBuilder builder = new StringBuilder();
		try {
			builder.append("SELECT * FROM provisioning_activity_priorities WHERE ACTIVITY_RECORD_ID = :configId");
			ActivityConfig config = (ActivityConfig) getEntityManager()
					.createNativeQuery(builder.toString(), ActivityConfig.class).setParameter("configId", configId)
					.getResultStream().findFirst().orElse(null);
			log.info("Ended ProvisioningDaoImpl :: getActivityConfigByConfigId, configId: {}", configId);
			return config;
		} catch (Exception e) {
			log.error("Exception in ActivityConfigDaoImpl :: getActivityConfigByConfigId, configId: {}", configId, e);
			throw e;
		}
	}
	
	public boolean existsActivityCode(String activityCode, String configId) {
		log.info("Started ProvisioningDaoImpl :: existsActivityCode, activityCode: {}, configId: {}", activityCode,
				configId);
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT COUNT(*) ");
			sql.append("FROM provisioning_activity_priorities ");
			sql.append("WHERE UPPER(TRIM(ACTIVITY_CODE)) = UPPER(TRIM(:activityCode)) ");
			sql.append("AND EFFECTIVE_FROM <= :currentTime ");
			sql.append("AND EFFECTIVE_TO > :currentTime ");
			if (configId != null && !configId.trim().isEmpty()) {
				sql.append("AND ACTIVITY_RECORD_ID <> :configId ");
			}
			Query query = getEntityManager().createNativeQuery(sql.toString());
			LocalDateTime now = LocalDateTime.now();
			query.setParameter("activityCode", activityCode);
			query.setParameter("currentTime", now);
			if (configId != null && !configId.trim().isEmpty()) {
				query.setParameter("configId", configId);
			}
			Number count = (Number) query.getSingleResult();
			boolean exists = count != null && count.longValue() > 0;
			log.info("Ended ProvisioningDaoImpl :: existsActivityCode, exists: {}", exists);
			return exists;
		} catch (Exception e) {
			log.error("Exception in ProvisioningDaoImpl :: existsActivityCode", e);
			throw e;
		}
	}

}
