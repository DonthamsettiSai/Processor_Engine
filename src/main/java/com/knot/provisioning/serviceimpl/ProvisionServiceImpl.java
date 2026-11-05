package com.knot.provisioning.serviceimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import com.knot.provioning.model.ProvRespDetails;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provioning.model.ProvisioningRequestProcessingError;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.bo.ProvReqResponseDetails;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dto.ActivityDTO;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.network.RefMasterNetwork;
import com.knot.provisioning.network.UmsNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisionService;
import com.knot.provisioning.soapclient.SoapClient;
import com.knot.provisioning.soapclient.SoapConnector;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.DynamicXmlValueExtractor;
import com.knot.provisioning.utill.FileUtil;
import com.knot.provisioning.utill.MdcAwareCallable;
import com.knot.provisioning.utill.MdcAwareRunnable;
import com.knot.provisioning.utill.ProvisionException;

/**
 *
 * @author Sai D
 *
 */

@Service
public class ProvisionServiceImpl implements ProvisionService {

	private static final Logger log = LogManager.getLogger(ProvisionServiceImpl.class);

	public Future processProvisioningRequest(ProvisioningRequest provisioningRequest, ProvisioningDao dao,
			ExecutorService executor, CacheService cache, OMSNetwork omsNet, RestTemplate restTemplate,
			SoapConnector connector, SoapClient clientConnector, UmsNetwork umsNet, RefMasterNetwork defNet) {
		log.info("In ProvisioningServiceImpl :: processProvisioningRequest ");
		Map<String, ProvisioningRequestDetails> taskMap = new ConcurrentHashMap<>();
		Future task = null;
		try {
			ProvisioningMonitoringThread prThread = new ProvisioningMonitoringThread(provisioningRequest, cache,
					omsNet, restTemplate, connector, clientConnector, defNet, dao, umsNet, taskMap);
			task = executor.submit(new MdcAwareRunnable(prThread));
			log.info("In ProvisioningServiceImpl :: ProvisioningMonitoringThread After task excuetion is done");
		} catch (Exception e) {
			log.error("ProvisioningServiceImpl :: processProvisioningRequest{}{}", provisioningRequest.getRequestID(),
					e.getMessage());
			log.error("", e);
		}
		return task;
	}

	private class ProvisioningMonitoringThread implements Runnable {

		private final ProvisioningRequest provisioningRequest;
		private final CacheService cache;
		private final OMSNetwork omsNet;
		private final RestTemplate restTemplate;
		private final SoapConnector connector;
		private final SoapClient clientConnector;
		private final RefMasterNetwork defNet;
		private final ProvisioningDao dao;
		private final UmsNetwork umsNet;
		private final Map<String, ProvisioningRequestDetails> map;

		ProvisioningMonitoringThread(ProvisioningRequest provisioningRequest, CacheService cache, OMSNetwork omsNet,
				RestTemplate restTemplate, SoapConnector connector, SoapClient clientConnector, RefMasterNetwork defNet,
				ProvisioningDao dao, UmsNetwork umsNet, Map<String, ProvisioningRequestDetails> map) {
			this.provisioningRequest = provisioningRequest;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.omsNet = omsNet;
			this.restTemplate = restTemplate;
			this.cache = cache;
			this.connector = connector;
			this.clientConnector = clientConnector;
			this.defNet = defNet;
			this.dao = dao;
			this.umsNet = umsNet;
			this.map = map;
		}

		String tenantID = null;
		BigInteger requestID;
		int status;
		int statusCode;
		int updateCpeStatus;
		String activityCode = "";

		public void run() {
			String accountType = "";
			BigInteger requestDtlID = null;
			String serverName = "";
			ExecutorService executor = null;
			boolean errorFlag = false;
			String key;
			String remarks = "";
			Date requestCompletionDate = null;
			List<ProvisioningRequestDetails> manualList = new ArrayList<>();
			try {
				requestID = provisioningRequest.getRequestID();
				log.info("ProvisioningMonitoringThread run method started. with Thread Name: {}, Thread ID: {}, requestID {}",
						Thread.currentThread().getName(), Thread.currentThread().getId(), requestID);
				requestCompletionDate = provisioningRequest.getRequestCompletionDate();

				List<ActivityDTO> activityInfo = defNet.getActivityInfo(null, provisioningRequest.getTenantID().toString(),
						provisioningRequest.getActivityID());
				for (ActivityDTO activityDto : activityInfo) {
					activityCode = activityDto.getActivityCode();
					if (activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT))
							|| activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.RE_EKYC, Constants.GLOBAL_TENANT))) {
						break;
					}
				}
				String traceID = omsNet.getTraceID(provisioningRequest.getWorkOrderID(), provisioningRequest.getTenantID(), cache);
				if (StringUtils.hasText(traceID)) {
					log.info("TraceID received: {}, mapping this TraceID to the existing CorrelationId.", traceID);
					ThreadContext.put("TRX_ID", traceID);
				}

				List<ProvisioningRequestDetails> rdList = dao.getRequestDetailsGroups(requestID);
				if (!rdList.isEmpty()) {
					executor = Executors.newFixedThreadPool(rdList.size());
					int groupNo = 0;
					List<ProvisioningRequestDetails> parallelList = new ArrayList<>();
					List<ProvisioningRequestDetails> seqList = new ArrayList<>();
					for (ProvisioningRequestDetails obj : rdList) {
						log.info("Processing request details :: with status={}, requestID={}, requestDetailID={}",
								obj.getStatus(), obj.getRequestID(), obj.getRequestDetailID());
						key = obj.getServerName() + ":" + obj.getSequenceID() + ":" + obj.getSubSequenceID();
						if (obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT)
								&& StringUtils.hasText(obj.getProcessType())
								&& obj.getProcessType().equalsIgnoreCase(Constants.MANUAL_STATUS)) {
							manualList.add(obj);
						} else if (obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT)
								|| obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV, Constants.GLOBAL_TENANT)
								|| obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							if (groupNo != obj.getGroup() && !parallelList.isEmpty()) {
								log.info("Inside Main Thread triggered");
								ProvisionParellelMainThread prThread = new ProvisionParellelMainThread(parallelList,
										provisioningRequest, cache, restTemplate, connector, clientConnector, dao, umsNet, map);
								Future<Boolean> task = executor.submit(new MdcAwareCallable<>(prThread));
								task.get();
								for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
									statusCode = entry.getValue().getStatus();
									if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
											|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT)) {
										errorFlag = true;
										break;
									}
								}
								parallelList = new ArrayList<>();
							}
							if (groupNo != obj.getGroup() && !seqList.isEmpty()) {
								ProvisionSequential prThread = new ProvisionSequential(seqList, provisioningRequest, cache,
										omsNet, restTemplate, activityCode, dao, umsNet, map);
								Future<Boolean> task = executor.submit(new MdcAwareCallable<>(prThread));
								task.get();
								for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
									statusCode = entry.getValue().getStatus();
									if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
											|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT)) {
										errorFlag = true;
										break;
									}
								}
								seqList = new ArrayList<>();
							}

							if (obj.getDependentSequenceID() == cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT)
									&& obj.getDependentSubSequenceID() == cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT)) {
								parallelList.add(obj);
							} else if (obj.getDependentSequenceID() != cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT)
									|| obj.getDependentSubSequenceID() != cache.intConfigVal(Constants.INDEPENDANT, Constants.GLOBAL_TENANT)) {
								seqList.add(obj);
							}
							if (groupNo != obj.getGroup()) {
								groupNo = obj.getGroup();
							}
						} else if (obj.getStatus() == cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT)
								|| obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							key = obj.getSequenceID() + ":" + obj.getSubSequenceID();
							map.put(key, obj);
						} else if (obj.getStatus() == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)) {
							errorFlag = true;
							break;
						}
					}

					if (!errorFlag && !parallelList.isEmpty()) {
						ProvisionParellelMainThread prThread = new ProvisionParellelMainThread(parallelList,
								provisioningRequest, cache, restTemplate, connector, clientConnector, dao, umsNet, map);
						Future<Boolean> task = executor.submit(prThread);
						task.get();
						for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
							statusCode = entry.getValue().getStatus();
							if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS, Constants.GLOBAL_TENANT)) {
								errorFlag = true;
								updateCpeStatus = cache.intConfigVal(Constants.PROV_ERROR_WO_STATUS, Constants.GLOBAL_TENANT);
								if (statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
									updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
									break;
								}
							}
						}
					}
					if (!errorFlag && !seqList.isEmpty()) {
						ProvisionSequential prThread = new ProvisionSequential(seqList, provisioningRequest, cache,
								omsNet, restTemplate, activityCode, dao, umsNet, map);
						Future<Boolean> task = executor.submit(prThread);
						task.get();
						for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
							statusCode = entry.getValue().getStatus();
							for (ProvisioningRequestDetails seq : seqList) {
								if (seq.getServerName().equalsIgnoreCase(Constants.OPTIVA)) {
									statusCode = seq.getStatus();
									accountType = seq.getAddColumnOne();
									requestDtlID = seq.getRequestDetailID();
									serverName = seq.getServerName();
									break;
								}
								if (seq.getServerName().equalsIgnoreCase(Constants.EKYC)) {
									statusCode = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
									break;
								}
							}
							if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS, Constants.GLOBAL_TENANT)) {
								errorFlag = true;
								updateCpeStatus = cache.intConfigVal(Constants.PROV_ERROR_WO_STATUS, Constants.GLOBAL_TENANT);
								if (statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
									updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
									break;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Request ID {}  {}", provisioningRequest.getRequestID(), e.getMessage());
				log.error("ProvisioningMonitoringThread :: run()", e);
				errorFlag = true;
			} finally {
				map.clear();
				if (executor != null) {
					executor.shutdown();
					try {
						if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
							executor.shutdownNow();
						}
					} catch (Exception e) {
						log.error("ProvisioningMonitoringThread :: run() || ", e);
					}
				}

				try {
					if (errorFlag) {
						updateCpeStatus = cache.intConfigVal(Constants.PROV_ERROR_WO_STATUS, Constants.GLOBAL_TENANT);
						status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
						if (statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
							updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
							remarks = Constants.WFM_REMARKS;
						} else {
							remarks = Constants.ERROR_IN_PROCESSOR;
						}
						dao.updateProvisioningRequest(requestID, status, remarks, requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(), updateCpeStatus, remarks, tenantID);
					} else if (!manualList.isEmpty()) {
						for (ProvisioningRequestDetails reqObj : manualList) {
							status = cache.intConfigVal(Constants.MANUAL_STATUS, Constants.GLOBAL_TENANT);
							remarks = Constants.MANUAL_ACT_REMARKS;
							updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
							dao.updateProvisioningRequestDetailCompletion(reqObj.getRequestDetailID(), status, 0);
						}
						dao.updateProvisioningRequest(requestID, status, remarks, requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(), updateCpeStatus, remarks, tenantID);
					} else if (StringUtils.hasText(activityCode)
							&& activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT))
							&& serverName.equalsIgnoreCase(Constants.OPTIVA)
							&& accountType.equalsIgnoreCase(Constants.PRE_PAID_ACCOUNT_TYPE)) {
						status = cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT);
						dao.updateProvisioningRequest(requestID, status, remarks, requestCompletionDate);
						dao.updateProvisioningRequestDetailCompletion(requestDtlID, status, 0);
						remarks = "Waiting for ExternalApi call";
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(), status, remarks, tenantID);
					} else {
						status = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
						remarks = cache.strConfigVal(Constants.SUCCESS_REMARKS, Constants.GLOBAL_TENANT);
						remarks = cache.strConfigVal(Constants.PROC_SUCESS, Constants.GLOBAL_TENANT);
						dao.updateProvisioningRequest(requestID, status, remarks, requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),
								cache.intConfigVal(Constants.PROV_SUCCESS_WO_STATUS, Constants.GLOBAL_TENANT), remarks, tenantID);
					}
				} catch (Exception e) {
					log.error("Request ID {} {}", provisioningRequest.getRequestID(), e.getMessage());
					log.error("", e);
				}
			}
			log.info("Exit ProvisioningMonitoringThread run method ended. with Thread Name: {}, Thread ID: {}, requestID {}",
					Thread.currentThread().getName(), Thread.currentThread().getId(), requestID);
		}
	}

	private class ProvisionParellelMainThread implements Callable<Boolean> {

		private final List<ProvisioningRequestDetails> detailList;
		private final ProvisioningRequest provisioningRequest;
		private final CacheService cache;
		private final RestTemplate restTemplate;
		private final SoapConnector connector;
		private final SoapClient clientConnector;
		private final ProvisioningDao dao;
		private final UmsNetwork umsNet;
		private final Map<String, ProvisioningRequestDetails> map;

		ProvisionParellelMainThread(List<ProvisioningRequestDetails> detailList,
				ProvisioningRequest provisioningRequest, CacheService cache, RestTemplate restTemplate,
				SoapConnector connector, SoapClient clientConnector, ProvisioningDao dao, UmsNetwork umsNet,
				Map<String, ProvisioningRequestDetails> map) {
			this.restTemplate = restTemplate;
			this.detailList = detailList;
			this.cache = cache;
			this.provisioningRequest = provisioningRequest;
			this.connector = connector;
			this.clientConnector = clientConnector;
			this.dao = dao;
			this.umsNet = umsNet;
			this.map = map;
		}

		public Boolean call() throws Exception {
			ExecutorService executor = Executors.newFixedThreadPool(Math.max(detailList.size(), 1));
			Exception exception = null;
			List<Future<Boolean>> futures = new ArrayList<>();
			try {
				for (ProvisioningRequestDetails detail : detailList) {
					ProvisionParallel prThread = new ProvisionParallel(detail, provisioningRequest, cache,
							restTemplate, connector, clientConnector, dao, umsNet, map);
					Future<Boolean> task = executor.submit(new MdcAwareCallable<>(prThread));
					futures.add(task);
				}
				for (Future<Boolean> task : futures) {
					task.get();
				}
			} catch (Exception e) {
				exception = e;
				log.error("ProvisionParellelMainThread :: call() || ", e);
			} finally {
				try {
					executor.shutdown();
					if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
						executor.shutdownNow();
					}
				} catch (Exception e) {
					log.error("ProvisionParellelMainThread :: call() finally block. || ", e);
				}
			}
			if (exception != null) {
				throw exception;
			}
			return true;
		}
	}

	private class ProvisionParallel implements Callable<Boolean> {

		private final ProvisioningRequestDetails detail;
		private final CacheService cache;
		private final RestTemplate restTemplate;
		private final SoapConnector connector;
		private final SoapClient clientConnector;
		private final ProvisioningRequest provisioningRequest;
		private final ProvisioningDao dao;
		private final UmsNetwork umsNet;
		private final Map<String, ProvisioningRequestDetails> map;

		ProvisionParallel(ProvisioningRequestDetails detail, ProvisioningRequest provisioningRequest,
				CacheService cache, RestTemplate restTemplate, SoapConnector connector, SoapClient clientConnector,
				ProvisioningDao dao, UmsNetwork umsNet, Map<String, ProvisioningRequestDetails> map) {
			this.detail = detail;
			this.restTemplate = restTemplate;
			this.cache = cache;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.connector = connector;
			this.clientConnector = clientConnector;
			this.provisioningRequest = provisioningRequest;
			this.dao = dao;
			this.umsNet = umsNet;
			this.map = map;
		}

		String tenantID = null;
		String key;
		int retries = 0;
		Map mapResponse;
		ObjectMapper mapper = new ObjectMapper();
		String errorDescription = "";
		Integer errorCode = null;
		String replacedResonse = null;
		String result = null;
		String response = "";

		@SuppressWarnings("null")
		public Boolean call() throws Exception {
			Integer status = null;
			int codeValue = 0;
			ResponseEntity<String> responseEntity = null;
			try {
				log.info("Started ProvisionParallel call method. Thread Name: {}, Thread ID: {}, Request ID: {}, Request Detail ID: {}, Status: {}",
						Thread.currentThread().getName(), Thread.currentThread().getId(), detail.getRequestID(),
						detail.getRequestDetailID(), detail.getStatus());
				retries = 0;
				dao.updateProvisioningRequestDetails(detail.getRequestDetailID(), detail.getRequestID());
				ProvisioningRequestResponseDetail reqResponseObj = new ProvisioningRequestResponseDetail();
				ProvisioningRequestProcessingError errorObj = new ProvisioningRequestProcessingError();
				String customRespCode = FileUtil.getProvisioningResponseCode(detail, cache);
				String customRespReqObj = FileUtil.getProvisioningResponseObject(detail, cache);
				String responseUniqueID = null;
				for (int i = retries; i <= detail.getMaxRetries(); i++) {
					try {
						retries = i;
						if (detail.getServerName().contains("cisco")) {
							responseEntity = FileUtil.ciscoApiCall(detail, cache, restTemplate);
							codeValue = responseEntity.getStatusCodeValue();
							response = responseEntity.getBody();
						} else {
							responseEntity = FileUtil.sendReq(detail, cache, tenantID, umsNet, restTemplate, connector, dao);
							response = responseEntity.getBody();
							codeValue = responseEntity.getStatusCodeValue();
						}
						if (StringUtils.hasText(response)) {
							break;
						}
					} catch (ResourceAccessException e) {
						errorCode = 503;
						errorDescription = e.getMessage();
						log.error("An Exception Occured ProvisionParallel, Response From serever {}, errorCode {}, exception {}", detail.getServerName(), errorCode, e.getMessage());
					} catch (HttpServerErrorException | HttpClientErrorException e) {
						errorCode = e.getStatusCode().value();
						log.error("An Exception Occured ProvisionParallel, Response From serever {}, errorCode {}, exception {}", detail.getServerName(), errorCode, e.getMessage());
						if (StringUtils.hasText(customRespCode)) {
							Integer valueOf = Integer.valueOf(customRespCode);
							if (errorCode.intValue() == valueOf.intValue() && retries == 1 && responseEntity != null && responseEntity.getBody() != null) {
								response = responseEntity.getBody();
								codeValue = Constants.SUCESS_CODE;
								break;
							}
						}
						errorDescription = e.getMessage();
						response = e.getResponseBodyAsString();
					}
				}

				reqResponseObj = FileUtil.setProvisioningRequestResponseDetailsData(mapper, detail, cache, response, tenantID, retries);
				if (!StringUtils.hasText(response) && codeValue != 200) {
					errorObj = FileUtil.saveProvisioningRequestProcessorErrors(String.valueOf(errorCode), response, detail, errorDescription, cache);
					dao.saveProcessError(errorObj);
					dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(), errorObj.getStatus(), retries,
							StringUtils.hasText(String.valueOf(errorCode)) ? String.valueOf(errorCode) : Constants.ERROR_CODE,
							errorDescription);
					status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
					reqResponseObj.setErrorDescription(cache.strConfigVal(Constants.NO_RESP_FROM_SERVER, tenantID) + reqResponseObj.getRequestDetailID());
					reqResponseObj.setErrorDescription(errorDescription);
					reqResponseObj.setRetrySubmitDate(new Date());
					reqResponseObj.setErrorCode(String.valueOf(errorCode));
					reqResponseObj.setStatus(status);
					detail.setStatus(status);
					key = detail.getSequenceID() + ":" + detail.getSubSequenceID();
					map.put(key, detail);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries, String.valueOf(errorCode), errorDescription);
					dao.saveProvisioningRequestResponseDetail(reqResponseObj);
					throw new ProvisionException("Unable to make the request for this RequestDetailID");
				} else if (StringUtils.hasText(response) && response.contains("status")) {
					if (response.contains("20") || response.contains("OK")) {
						if (StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
							status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
							detail.setStatus(status);
						} else {
							status = getStatus(reqResponseObj);
						}
					} else {
						status = cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS, Constants.GLOBAL_TENANT);
					}
					reqResponseObj.setStatus(status);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				} else if (detail.getServerURL().contains("tmf") || detail.getServerURL().contains("oce")) {
					if (StringUtils.hasText(customRespReqObj) && customRespReqObj.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
						List<String> responseRegexValue = FileUtil.foundResponseReplaceValues(customRespReqObj);
						Map<String, String> responseObjMap = mapper.readValue(response, Map.class);
						for (String val : responseRegexValue) {
							responseUniqueID = responseObjMap.get(val);
						}
						detail.setReqServerRespID(responseUniqueID);
						dao.updateRequestDetailServerUrl(detail.getServerURL(), detail.getRequestDetailID(), responseUniqueID);
						responseObjMap.clear();
					}
					status = StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))
							? cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)
							: cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
					reqResponseObj.setStatus(status);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				} else if (StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
					status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				} else {
					status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				}
				reqResponseObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT));
				dao.saveProvisioningRequestResponseDetail(reqResponseObj);
				detail.setStatus(status);
				Map<?, ?> mapResponse = FileUtil.getJsonOrXmlMap(StringUtils.hasText(replacedResonse) ? replacedResonse : response);
				detail.setResponse(mapResponse);
				detail.setSoapresponse(response);
				detail.setResponseString(response);
				key = detail.getSequenceID() + ":" + detail.getSubSequenceID();
				map.put(key, detail);
			} catch (Exception e) {
				detail.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT));
				detail.setResponse(mapResponse);
				map.put(key, detail);
				log.error("{} is not processed , got error : {} ", detail.getRequestDetailID(), e.getMessage());
				log.error("", e);
			}
			return true;
		}

		private Integer getStatus(ProvisioningRequestResponseDetail reqResponseObj) throws JsonProcessingException {
			Integer status;
			if (detail.getResponseUpdateCategory().equals(1) || detail.getResponseUpdateCategory().equals(3)) {
				ProvRespDetails responseDetailsMap = cache.getResponseDetailsMap(detail.getProvObjectId());
				replacedResonse = replaceResponseParams(responseDetailsMap.getApiObject(), detail, response);
				if (replacedResonse != null) {
					reqResponseObj.setResponseObject(response);
					status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
				} else {
					status = cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV, Constants.GLOBAL_TENANT);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries,
							cache.strConfigVal(Constants.ERROR_CODE, Constants.GLOBAL_TENANT),
							cache.strConfigVal(Constants.RESP_NOT_UPDATED, Constants.GLOBAL_TENANT));
				}
			} else {
				status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
			}
			log.info("{} is processing successfully   ", detail.getRequestDetailID());
			return status;
		}

		@SuppressWarnings("unchecked")
		String replaceResponseParams(String request, ProvisioningRequestDetails details, String response)
				throws JsonProcessingException {
			try {
				String responseType = FileUtil.getResponseType(response);
				if (responseType.equalsIgnoreCase("xml")) {
					return response;
				}
				mapResponse = mapper.readValue(response, Map.class);
				Map<String, String> finalResponse = new HashMap<>();
				Iterator<?> it = mapResponse.entrySet().iterator();
				String key = null;
				String tempKep;
				while (it.hasNext()) {
					Map.Entry entry7 = (Map.Entry) it.next();
					String[] types = new String[3];
					types[0] = cache.strConfigVal(Constants.DOWN_STREAM_PARAM, this.tenantID);
					types[1] = cache.strConfigVal(Constants.SUB_REQ_PARAM, this.tenantID);
					types[2] = cache.strConfigVal(Constants.BOTH, this.tenantID);
					boolean isFound = false;
					for (int i = 0; i < types.length; i++) {
						key = types[i] + entry7.getKey().toString();
						if (request.contains(key)) {
							isFound = true;
							tempKep = key;
							finalResponse.put(tempKep, entry7.getValue().toString());
							break;
						}
					}
					if (!isFound) {
						finalResponse.put(entry7.getKey().toString(), entry7.getValue().toString());
					}
				}
				return mapper.writeValueAsString(finalResponse);
			} catch (JsonSyntaxException e) {
				log.error("{} {}", detail.getRequestDetailID(), e.getMessage());
				log.error("ProvisionParallel :: replaceResponseParams() ", e);
				return null;
			}
		}
	}

	private class ProvisionSequential implements Callable<Boolean> {

		private final List<ProvisioningRequestDetails> detailList;
		private final CacheService cache;
		private final RestTemplate restTemplate;
		private final OMSNetwork omsNet;
		private final ProvisioningRequest provisioningRequest;
		private final String activityCode;
		private final ProvisioningDao dao;
		private final UmsNetwork umsNet;
		private final Map<String, ProvisioningRequestDetails> map;

		ProvisionSequential(List<ProvisioningRequestDetails> detailList, ProvisioningRequest provisioningRequest,
				CacheService cache, OMSNetwork omsNet, RestTemplate restTemplate, String activityCode,
				ProvisioningDao dao, UmsNetwork umsNet, Map<String, ProvisioningRequestDetails> map) {
			this.restTemplate = restTemplate;
			this.detailList = detailList;
			this.cache = cache;
			this.omsNet = omsNet;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.provisioningRequest = provisioningRequest;
			this.activityCode = activityCode;
			this.dao = dao;
			this.umsNet = umsNet;
			this.map = map;
		}

		String tenantID = null;
		int retries = 0;
		JSONObject jsonObject = null;
		String description = "";
		String code = "";
		String strVal = "";

		public Boolean call() throws Exception {
			int status = 0;
			String keyToFind;
			String key;
			ProvisioningRequestDetails dependentObj = new ProvisioningRequestDetails();
			ObjectMapper mapper = new ObjectMapper();
			String errorDescription = "";
			Integer errorCode = null;
			String response = "";
			ResponseEntity<String> responseEntity = null;
			String responseUniqueID = null;
			String responsejson = null;
			try {
				retries = 0;
				for (ProvisioningRequestDetails detail : detailList) {
					key = detail.getSequenceID() + ":" + detail.getSubSequenceID();
					keyToFind = detail.getDependentSequenceID() + ":" + detail.getDependentSubSequenceID();
					dependentObj = map.get(keyToFind);
					List<ProvReqResponseDetails> responseDtllist = new ArrayList<>();
					if (dependentObj != null) {
						responseDtllist = dao.getProvRequestResponseDetails(dependentObj.getRequestDetailID(), null);
						if (responseDtllist.isEmpty()) {
							responseDtllist = dao.getProvRequestResponseDetails(null, dependentObj.getRequestID());
							for (ProvReqResponseDetails responseObj : responseDtllist) {
								String responseobject = responseObj.getResponseobject();
								if (StringUtils.hasText(responseobject) && responseobject.contains("cvlan")) {
									dependentObj.setResponseString(responseobject);
									break;
								}
							}
						} else {
							dependentObj.setResponseString(responseDtllist.get(0).getResponseobject());
						}
					}
					if (dependentObj != null && dependentObj.getStatus() == cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT)
							&& detail.getResponseUpdateCategory().equals(2)) {
						if (detail.getServerURL().contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
							Map<String, String> convertResponseJsonToMap = FileUtil.convertResponseJsonToMap(dependentObj.getResponseString());
							String requestUrl = detail.getServerURL();
							List<String> responseRegexPipes = FileUtil.getResponseRegexPipes(requestUrl);
							for (String regexKey : responseRegexPipes) {
								requestUrl = requestUrl.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID), convertResponseJsonToMap.get(regexKey));
								detail.setServerURL(requestUrl);
							}
						}
						String request = detail.getRequestObject();
						if (StringUtils.hasText(request)) {
							strVal = cache.strConfigVal(Constants.R_PIPE, this.tenantID);
							if (request.contains(strVal)) {
								request = replaceRequestParams(dependentObj, request);
							}
							if (!request.contains(strVal)) {
								dao.updateRequestObject(detail.getRequestDetailID(), request);
								detail.setRequestObject(request);
							} else {
								detail.setStatus(cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV, Constants.GLOBAL_TENANT));
								map.put(key, detail);
								dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),
										cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV, Constants.GLOBAL_TENANT),
										retries, "", strVal + cache.strConfigVal(Constants.R_PIPE_NOT_REPLACES, Constants.GLOBAL_TENANT));
								break;
							}
						}
						for (int i = retries; i <= detail.getMaxRetries(); i++) {
							retries = i;
							try {
								if (detail.getServerName().contains("cisco")) {
									responseEntity = FileUtil.ciscoApiCall(detail, cache, restTemplate);
									response = responseEntity.getBody();
								} else {
									responseEntity = FileUtil.sendReq(detail, cache, tenantID, umsNet, restTemplate, null, dao);
									response = responseEntity.getBody();
								}
								if (StringUtils.hasText(response)) {
									break;
								}
							} catch (ResourceAccessException e) {
								errorDescription = e.getMessage();
							} catch (HttpClientErrorException | HttpServerErrorException e) {
								errorCode = e.getStatusCode().value();
								errorDescription = e.getMessage();
								responsejson = e.getResponseBodyAsString();
								response = e.getResponseBodyAsString();
							}
						}
						ProvisioningRequestResponseDetail requestResponseObj = FileUtil.setProvisioningRequestResponseDetailsData(mapper, detail, cache, response, this.tenantID, retries);
						if (!StringUtils.hasText(response)) {
							ProvisioningRequestProcessingError errorRequestObj = FileUtil.saveProvisioningRequestProcessorErrors(String.valueOf(errorCode), response, detail, errorDescription, cache);
							dao.saveProcessError(errorRequestObj);
							dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(), errorRequestObj.getStatus(), retries,
									StringUtils.hasText(String.valueOf(errorCode)) ? String.valueOf(errorCode) : Constants.ERROR_CODE,
									errorDescription);
							requestResponseObj.setRetrySubmitDate(new Date());
							requestResponseObj.setErrorCode(String.valueOf(errorCode));
							requestResponseObj.setResponseObject(responsejson);
							status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
							dao.saveProvisioningRequestResponseDetail(requestResponseObj);
							detail.setStatus(status);
							map.put(key, detail);
							throw new ProvisionException("Unable to make the request for this RequestDetailID");
						} else if (StringUtils.hasText(response) && response.contains("status")) {
							if (response.contains("20") || response.contains("OK")) {
								if (StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
									status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
									dependentObj.setStatus(status);
									detail.setStatus(status);
								} else {
									status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
									dependentObj.setStatus(status);
								}
							} else {
								status = cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS, Constants.GLOBAL_TENANT);
							}
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
						} else if (detail.getServerURL().contains("tmf") || detail.getServerURL().contains("oce")) {
							if (StringUtils.hasText(customRespReqObj) && customRespReqObj.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
								List<String> responseRegexValue = FileUtil.foundResponseReplaceValues(customRespReqObj);
								Map<String, String> responseObjMap = mapper.readValue(response, Map.class);
								for (String val : responseRegexValue) {
									responseUniqueID = responseObjMap.get(val);
								}
								detail.setReqServerRespID(responseUniqueID);
								dao.updateRequestDetailServerUrl(detail.getServerURL(), detail.getRequestDetailID(), responseUniqueID);
								responseObjMap.clear();
							}
							status = StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))
									? cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)
									: cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
							requestResponseObj.setStatus(status);
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
						} else if (StringUtils.hasText(response)) {
							if (detail.getAddColumnOne().equalsIgnoreCase(Constants.PRE_PAID_ACCOUNT_TYPE)
									&& detail.getServerName().equalsIgnoreCase(Constants.OPTIVA)
									&& activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT))) {
								status = cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT);
							} else {
								status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
							}
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
						}
						requestResponseObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT));
						dao.saveProvisioningRequestResponseDetail(requestResponseObj);
						detail.setStatus(status);
						Map mapResponse = FileUtil.getJsonOrXmlMap(response);
						detail.setResponse(mapResponse);
						detail.setSoapresponse(response);
						detail.setResponseString(response);
						map.put(key, detail);
					} else {
						log.error("Unable to process request :: Dependent request status does not match the required completion status. RequestDetailID={}, RequiredCompletionStatus={}",
								detail.getRequestDetailID(), cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT));
						detail.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT));
						map.put(key, detail);
					}
				}
			} catch (Exception e) {
				log.error("An exception occured in ProvisionSequential :: ProvisionServiceImpl {} is not processed , got error :  {}",
						dependentObj != null ? dependentObj.getRequestDetailID() : "", e.getMessage());
				dependentObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT));
				key = dependentObj.getSequenceID() + ":" + dependentObj.getSubSequenceID();
				map.put(key, dependentObj);
				log.error("ProvisionSequential :: call() :: ", e);
			}
			return true;
		}

		String replaceRequestParams(ProvisioningRequestDetails dependentObj, String request) throws Exception {
			try {
				Map parentJson = dependentObj.getResponse();
				String objectType = FileUtil.getRequestObjectType(request);
				if (dependentObj.getInterfaceType().equalsIgnoreCase(Constants.SOAP_CALL)
						|| dependentObj.getInterfaceType().equalsIgnoreCase(Constants.LOCAL_CALL)) {
					String soapresponse = dependentObj.getSoapresponse();
					List<String> regexValues = FileUtil.foundResponseReplaceValues(request);
					String rPipe = cache.strConfigVal(Constants.R_PIPE, this.tenantID);
					if (!regexValues.isEmpty()) {
						Map<String, String> responseValues = DynamicXmlValueExtractor.getValues(soapresponse, Constants.XML_DYNAMIC_OPERATION,
								Constants.XML_DYNAMIC_MODIFIER, regexValues);
						for (String val : regexValues) {
							String replaceValue = rPipe + val;
							if (request.contains(replaceValue)) {
								request = request.replace(replaceValue, responseValues.get(val));
							}
						}
						responseValues.clear();
					}
				} else if (dependentObj.getServerURL().contains("step3")) {
					String response = dependentObj.getResponseString();
					Map<String, String> finalMap = new HashMap<>();
					List<String> regexValues = null;
					try {
						finalMap = FileUtil.convertResponseJsonToMap(response);
						regexValues = FileUtil.foundResponseReplaceValues(request);
						for (String val : regexValues) {
							if (request.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID) + val)) {
								request = request.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID) + val, finalMap.get(val));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					finalMap.clear();
				} else if (objectType.equalsIgnoreCase("json") && dependentObj.getServerURL().contains("createSubscriber")) {
					Iterator<?> it = parentJson.entrySet().iterator();
					request = FileUtil.getReplacedRequestParams(request, it, cache, this.tenantID);
				} else if (objectType.equalsIgnoreCase("json") && !dependentObj.getServerURL().contains("createSubscriber")) {
					String response = dependentObj.getResponseString();
					Map<String, String> finalMap = new HashMap<>();
					List<String> regexValues = null;
					try {
						finalMap = FileUtil.convertResponseJsonToMap(response);
						regexValues = FileUtil.foundResponseReplaceValues(request);
						for (String val : regexValues) {
							if (request.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID) + val)) {
								request = request.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID) + val, finalMap.get(val));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					finalMap.clear();
				}
				return request;
			} catch (Exception e) {
				log.error("{} Got Error while reading dependnent response {}", dependentObj.getRequestDetailID(), e.getMessage());
				log.error("replaceRequestParams(ProvisioningRequestDetails dependentObj, String request)", e);
				throw e;
			}
		}

		public String getReplacedResponseSubscrptionIds(List<String> subscriptionIds, Map<Object, Object> resultObjMap, String request, String ratePlanId) {
			String ocsPalnIdFound;
			String replaceRequest = "";
			jsonObject = FileUtil.getMapToJsonObject(resultObjMap);
			if (jsonObject.get("productRest") instanceof JSONArray) {
				JSONArray productRestArray = jsonObject.getJSONArray("productRest");
				for (int i = 0; i < productRestArray.length(); i++) {
					JSONObject productRest = productRestArray.getJSONObject(i);
					Object productId = productRest.get("productId");
					ocsPalnIdFound = cache.getOcsPlanData(productId.toString());
					if (ocsPalnIdFound.equalsIgnoreCase(ratePlanId)) {
						Object subscriptionId = productRest.get("subscriptionId");
						String replaceString = "<long>" + subscriptionId.toString() + "</long>";
						subscriptionIds.add(replaceString);
						i = productRestArray.length();
					} else {
						log.info("No RatePlan Id Fount For Particular Product Id :: " + productId + "--------------" + ratePlanId);
					}
				}
			} else if (jsonObject.get("productRest") instanceof JSONObject) {
				JSONObject productRest = jsonObject.getJSONObject("productRest");
				Object productId = productRest.get("productId");
				ocsPalnIdFound = cache.getOcsPlanData(productId.toString());
				if (ocsPalnIdFound.equalsIgnoreCase(ratePlanId)) {
					Object subscriptionId = productRest.get("subscriptionId");
					String replaceString = "<long>" + subscriptionId.toString() + "</long>";
					subscriptionIds.add(replaceString);
				} else {
					log.info("No RatePlan Id Fount For Particular Product Id :: " + productId + ocsPalnIdFound);
				}
			}
			replaceRequest = subscriptionIds.toString();
			replaceRequest = replaceRequest.replace("[", "").replace("]", "").replace(",", "");
			request = request.replace("r|subscriptionId", replaceRequest);
			log.info("request =================== :: " + request);
			return request;
		}
	}
}
