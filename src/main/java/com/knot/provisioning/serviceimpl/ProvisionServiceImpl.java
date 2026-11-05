package com.knot.provisioning.serviceimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
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
	
	Map<String, ProvisioningRequestDetails> map = new HashMap<>();

	ProvisioningDao dao = null;
	
	UmsNetwork umsNet = null;
	
	RefMasterNetwork defNet = null ;
	
	OMSNetwork omsNet = null;
	

	public Future processProvisioningRequest(ProvisioningRequest provisioningRequest, ProvisioningDao dao,
			ExecutorService executor, CacheService cache, OMSNetwork omsNet, RestTemplate restTemplate,
			SoapConnector connector, SoapClient clientConnector, UmsNetwork umsNet, RefMasterNetwork defNet) {
		log.info("In ProvisioningServiceImpl :: processProvisioningRequest ");
		this.dao = dao;
		this.umsNet = umsNet;
		this.defNet = defNet;
		this.omsNet = omsNet;
		Future task = null;
		try {
			ProvisioningMonitoringThread prThread = new ProvisioningMonitoringThread(provisioningRequest, cache,
					omsNet, restTemplate, connector, clientConnector, defNet);
			task = executor.submit(new MdcAwareRunnable(prThread));
			log.info("In ProvisioningServiceImpl :: ProvisioningMonitoringThread After task excuetion is done");
			/**
			 * task.get(); task = null;
			 */
		} catch (Exception e) {
			log.error("ProvisioningServiceImpl :: processProvisioningRequest{}{}", provisioningRequest.getRequestID(),
					e.getMessage());
			log.error("", e);
		}
		return task;
	}

	private class ProvisioningMonitoringThread implements Runnable {

		private ProvisioningRequest provisioningRequest;

		private CacheService cache;
		private OMSNetwork omsNet;
		private RestTemplate restTemplate;
		private SoapConnector connector;
		private SoapClient clientConnector;
		private RefMasterNetwork defNet;

		ProvisioningMonitoringThread(ProvisioningRequest provisioningRequest, CacheService cache, OMSNetwork omsNet,
				RestTemplate restTemplate, SoapConnector connector, SoapClient clientConnector, RefMasterNetwork defNet) {
			this.provisioningRequest = provisioningRequest;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.omsNet = omsNet;
			this.restTemplate = restTemplate;
			this.cache = cache;
			this.connector = connector;
			this.clientConnector = clientConnector;
			this.defNet = defNet;
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
			/**
			 * In future requirment need to write manualList thread 
			 */
			List<ProvisioningRequestDetails> manualList = new ArrayList<>();
			try {
				requestID = provisioningRequest.getRequestID();
				log.info("ProvisioningMonitoringThread run method started. with Thread Name: {}, Thread ID: {}, requestID {}",Thread.currentThread().getName(), Thread.currentThread().getId(), requestID);
			    requestCompletionDate = provisioningRequest.getRequestCompletionDate();
			    /** For Provisoing Order Multiple Thread executing same record so we skip this logic **/
				/**if (provisioningRequest.getStatus() !=  cache.intConfigVal(Constants.PROV_IN_PROGRESS,
						Constants.GLOBAL_TENANT)) {
					dao.updateProvisioningRequestProgress(requestID);
					this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),
							cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT),
							cache.strConfigVal(Constants.PROV_PROCESS_IN_PROGRESS, Constants.GLOBAL_TENANT), tenantID);
				}**/
					List<ActivityDTO> activityInfo = defNet.getActivityInfo(null, provisioningRequest.getTenantID().toString(), provisioningRequest.getActivityID());
					for(ActivityDTO activityDto : activityInfo) {
						activityCode = activityDto.getActivityCode();
						if(activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT)) || activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.RE_EKYC, Constants.GLOBAL_TENANT))) {
						  break;
						}
					}
				String traceID = omsNet.getTraceID(provisioningRequest.getWorkOrderID(), provisioningRequest.getTenantID(), cache);
				   if(StringUtils.hasText(traceID)) {
				    log.info("TraceID received: {}, mapping this TraceID to the existing CorrelationId.", traceID);
				    ThreadContext.put("TRX_ID", traceID);
				}
				List<ProvisioningRequestDetails> rdList = dao.getRequestDetailsGroups(requestID);
				if (!rdList.isEmpty()) {
					executor = Executors.newFixedThreadPool(rdList.size());
					int groupNo = 0;
					int subGrpNo = 0;
					List<ProvisioningRequestDetails> parallelList = new ArrayList<>();
					List<ProvisioningRequestDetails> seqList = new ArrayList<>();
					log.info("Processing Details  ");
					outer: 
						for (ProvisioningRequestDetails obj : rdList) {
						log.info("Processing request details :: with status={}, requestID={}, requestDetailID={}",obj.getStatus(), obj.getRequestID(), obj.getRequestDetailID());
						key = obj.getServerName() + ":" + obj.getSequenceID() + ":" + obj.getSubSequenceID();
                      if(obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT) && (StringUtils.hasText(obj.getProcessType()) && obj.getProcessType().equalsIgnoreCase(cache.strConfigVal(Constants.PROCESS_TYPE, Constants.GLOBAL_TENANT)))) {
                    	  manualList.add(obj);
                               
                      }else	if (obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT)
								|| obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV, Constants.GLOBAL_TENANT)
								|| obj.getStatus() ==  cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							if (groupNo != obj.getGroup() && !parallelList.isEmpty()) {
								log.info("Inside Main Thread triggered");
								ProvisionParellelMainThread prThread = new ProvisionParellelMainThread(parallelList,
										provisioningRequest, cache, restTemplate, connector, clientConnector);
								Future<Boolean> task = executor.submit(new MdcAwareCallable<>(prThread));
								task.get(); /** Mandatory to wait until thread returns.*/
								for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
									statusCode = entry.getValue().getStatus();
									if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
											|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS,
													Constants.GLOBAL_TENANT)) {
										errorFlag = true;
										break outer;
									}
								}
								parallelList = new ArrayList<>();
							}
							if (groupNo != obj.getGroup() && !seqList.isEmpty()) {
								ProvisionSequential prThread = new ProvisionSequential(seqList, provisioningRequest,
										cache, omsNet, restTemplate,activityCode);
								log.info("ProvisionSequential Thread checking");
								Future<Boolean> task = executor.submit(new MdcAwareCallable<>(prThread));
								task.get(); /** Mandatory to wait until thread returns.*/
								for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
									statusCode = entry.getValue().getStatus();
									if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
											|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS,
													Constants.GLOBAL_TENANT)) {
										errorFlag = true;
										break outer;
									}
								}
								seqList = new ArrayList<>();
							}

							if (obj.getDependentSequenceID() == cache.intConfigVal(Constants.INDEPENDANT,
									Constants.GLOBAL_TENANT)
									&& obj.getDependentSubSequenceID() == cache.intConfigVal(Constants.INDEPENDANT,
											Constants.GLOBAL_TENANT)) {
								parallelList.add(obj);
							} else if (obj.getDependentSequenceID() != cache.intConfigVal(Constants.INDEPENDANT,
									Constants.GLOBAL_TENANT)
									|| obj.getDependentSubSequenceID() != cache.intConfigVal(Constants.INDEPENDANT,
											Constants.GLOBAL_TENANT)) {
								seqList.add(obj);
							}

							if (groupNo != obj.getGroup()) {
								groupNo = obj.getGroup();
							}
							if (subGrpNo != obj.getSubGroup()) {
								subGrpNo = obj.getSubGroup();
							}
						} else if (obj.getStatus() == cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY,
								Constants.GLOBAL_TENANT) ||  obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							key = obj.getSequenceID() + ":" + obj.getSubSequenceID();
							map.put(key, obj);
						} else if (obj.getStatus() == cache.intConfigVal(Constants.ERROR_IN_EXEC,
								Constants.GLOBAL_TENANT)) {
							errorFlag = true;
							break outer;
						}
					}

					if (!errorFlag && !parallelList.isEmpty()) {
						ProvisionParellelMainThread prThread = new ProvisionParellelMainThread(parallelList,
								provisioningRequest, cache, restTemplate, connector, clientConnector);
						log.info("Entered into  ProvisionParellelMainThread :: Processing parallell Thread  list with size={}", parallelList.size());
						Future<Boolean> task = executor.submit(prThread);
						task.get(); /*** Mandatory to wait until thread returns.*/
						log.info("In ProvisioningServiceImpl :: ProvisionParellelMainThread After task excuetion is done");
						for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
							statusCode = entry.getValue().getStatus();
							if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS,Constants.GLOBAL_TENANT)  
									|| statusCode ==  cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT) 
									|| statusCode == cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS,Constants.GLOBAL_TENANT)) {
								errorFlag = true;
								updateCpeStatus = cache.intConfigVal(Constants.PROV_ERROR_WO_STATUS, Constants.GLOBAL_TENANT);
								/**
								 * if(!this.tenantID.contains(Constants.TFIBER_TNT)) { updateCpeStatus =
								 * cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT); break;
								 * }  
								 */
								if(statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT) ) {
									updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
									break;
								}
							}
						}
					}
					if (!errorFlag && !seqList.isEmpty()) {
						ProvisionSequential prThread = new ProvisionSequential(seqList, provisioningRequest, cache,
								omsNet, restTemplate,activityCode);
						log.info("Entered into  ProvisionSequential :: Processing Sequential Thread  list with size={}", seqList.size());
						Future<Boolean> task = executor.submit(prThread);
						task.get(); /*** Mandatory to wait until thread returns.*/
						log.info("In ProvisioningServiceImpl ::ProvisionSequentialMainThread After task excuetion is done");
						
						for (Map.Entry<String, ProvisioningRequestDetails> entry : map.entrySet()) {
							statusCode = entry.getValue().getStatus();
							for(ProvisioningRequestDetails seq : seqList) {
								if(seq.getServerName().equalsIgnoreCase(Constants.OPTIVA)) {
									statusCode = seq.getStatus();
									accountType = seq.getAddColumnOne();
									requestDtlID = seq.getRequestDetailID();
									serverName = seq.getServerName();
									break;
								}if(seq.getServerName().equalsIgnoreCase(Constants.EKYC)) {/**if serverName Contains ekyc need to update status as 19  */
									statusCode = seq.getStatus();
									statusCode = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
									break;
								}
								
							}
							if (statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS,Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS,Constants.GLOBAL_TENANT)
									|| statusCode == cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS,Constants.GLOBAL_TENANT)) {
								errorFlag = true;
								updateCpeStatus = cache.intConfigVal(Constants.PROV_ERROR_WO_STATUS, Constants.GLOBAL_TENANT);
								/**
								 * if(!this.tenantID.contains(Constants.TFIBER_TNT)) { updateCpeStatus =
								 * cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT); break;
								 * }
								 */
								if(statusCode ==  cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
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
						if (executor.awaitTermination(5, TimeUnit.MINUTES)) {
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
						if(statusCode == cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT)) {
							status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
							updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
							remarks = Constants.WFM_REMARKS;
						}else {
							remarks =  Constants.ERROR_IN_PROCESSOR;
						}
						dao.updateProvisioningRequest(requestID, status, remarks,requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),updateCpeStatus,remarks, tenantID);
					} else if(!manualList.isEmpty()) {
						for(ProvisioningRequestDetails reqObj : manualList) {
							status = cache.intConfigVal(Constants.MANUAL_STATUS, Constants.GLOBAL_TENANT);
							remarks = Constants.MANUAL_ACT_REMARKS;
							updateCpeStatus = cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT);
							dao.updateProvisioningRequestDetailCompletion(reqObj.getRequestDetailID(), status, 0);
						}
						dao.updateProvisioningRequest(requestID, status, remarks, requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),updateCpeStatus,remarks, tenantID);	
					}else if(StringUtils.hasText(activityCode) && activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT))
							&& serverName.equalsIgnoreCase(Constants.OPTIVA) && accountType.equalsIgnoreCase(Constants.PRE_PAID_ACCOUNT_TYPE)) {
						status = cache.intConfigVal(Constants.CUST_IDLE_STAT, Constants.GLOBAL_TENANT);
						dao.updateProvisioningRequest(requestID, status, remarks,requestCompletionDate);
						dao.updateProvisioningRequestDetailCompletion(requestDtlID, status, 0);
						remarks = "Waiting for ExternalApi call";
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(), status, remarks,tenantID);
					}
					else {
						status = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
						remarks = cache.strConfigVal(Constants.SUCCESS_REMARKS, Constants.GLOBAL_TENANT);
						remarks = cache.strConfigVal(Constants.PROC_SUCESS, Constants.GLOBAL_TENANT);
						dao.updateProvisioningRequest(requestID, status, remarks,requestCompletionDate);
						this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),cache.intConfigVal(Constants.PROV_SUCCESS_WO_STATUS, Constants.GLOBAL_TENANT),remarks, tenantID);
					}
//					dao.updateProvisioningRequest(requestID, status, remarks);
//					this.omsNet.updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),
//							!errorFlag ? cache.intConfigVal(Constants.PROV_SUCCESS_WO_STATUS, Constants.GLOBAL_TENANT)
//									: updateCpeStatus,
//							remarks, tenantID);
				} catch (Exception e) {
					log.error("Request ID {} {}", provisioningRequest.getRequestID(), e.getMessage());
					log.error("", e);
				}
			}
			log.info("Exit ProvisioningMonitoringThread run method ended. with Thread Name: {}, Thread ID: {}, requestID {}",Thread.currentThread().getName(), Thread.currentThread().getId(), requestID);

		}
	}

	private class ProvisionParellelMainThread implements Callable<Boolean> {

		private List<ProvisioningRequestDetails> detailList;
		private ProvisioningRequest provisioningRequest;
		private CacheService cache;
		private RestTemplate restTemplate;
		private SoapConnector connector;
		private SoapClient clientConnector;

		ProvisionParellelMainThread(List<ProvisioningRequestDetails> detailList,
				ProvisioningRequest provisioningRequest, CacheService cache, RestTemplate restTemplate,
				SoapConnector connector, SoapClient clientConnector) {
			this.restTemplate = restTemplate;
			this.detailList = detailList;
			this.cache = cache;
			this.provisioningRequest = provisioningRequest;
			this.connector = connector;
			this.clientConnector = clientConnector;

		}

		public Boolean call() throws Exception {
			ExecutorService executor = Executors.newFixedThreadPool(detailList.size());
			Exception exception = null;
			try {
				for (ProvisioningRequestDetails detail : detailList) {
					log.info(
							"ProvisionParellelMainThread :: Submitting task :: ThreadName={}, RequestDetailID={}, RequestID={}, Status={}",
							Thread.currentThread().getName(), detail.getRequestDetailID(), detail.getRequestID(), detail.getStatus());
					ProvisionParallel prThread = new ProvisionParallel(detail, provisioningRequest, cache,
							restTemplate, connector, clientConnector);
					/** executor.execute(prThread); */
					Future task = executor.submit(new MdcAwareCallable<>(prThread));
					log.info("In ProvisioningServiceImpl :: ProvisionParallel After task excuetion is done with detailID={}, RequestID={} status={}", detail.getRequestDetailID(),detail.getRequestID(),detail.getStatus());
					log.info(task.get());

				}
			} catch (Exception e) {
				exception = e;
				log.error("ProvisionParellelMainThread :: call() || ", e);
			} finally {
				try {
					log.info("Waiting for the parallel Threads Executor to terminate !!!");
					executor.shutdown();
					while (!executor.isTerminated()) {
						executor.shutdown();
						if (executor.awaitTermination(10, TimeUnit.MINUTES)) {
							executor.shutdownNow();
						}
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

		private ProvisioningRequestDetails detail;
		private CacheService cache;
		private RestTemplate restTemplate;
		private SoapConnector connector;
		private SoapClient clientConnector;
		private ProvisioningRequest provisioningRequest;

		ProvisionParallel(ProvisioningRequestDetails detail, ProvisioningRequest provisioningRequest,
				CacheService cache, RestTemplate restTemplate, SoapConnector connector, SoapClient clientConnector) {
			this.detail = detail;
			this.restTemplate = restTemplate;
			this.cache = cache;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.connector = connector;
			this.clientConnector = clientConnector;
			this.provisioningRequest = provisioningRequest;
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
			boolean isSend = false;
			int codeValue =0;
			ResponseEntity<String> responseEntity = null;
			try {
				log.info(
						"Started ProvisionParallel call method. Thread Name: {}, Thread ID: {}, Request ID: {}, Request Detail ID: {}, Status: {}",
						Thread.currentThread().getName(), Thread.currentThread().getId(), detail.getRequestID(),
						detail.getRequestDetailID(), detail.getStatus());
				retries = 0;
				dao.updateProvisioningRequestDetails(detail.getRequestDetailID(), detail.getRequestID());
				ProvisioningRequestResponseDetail reqResponseObj = new ProvisioningRequestResponseDetail();
				ProvisioningRequestProcessingError errorObj = new ProvisioningRequestProcessingError();
				String  customRespCode  = FileUtil.getProvisioningResponseCode(detail,cache);
				String  customRespReqObj  = FileUtil.getProvisioningResponseObject(detail,cache);
				String responseUnqiueID =null;
				
				for (int i = retries; i <= detail.getMaxRetries(); i++) {
					try {
						retries++;
						retries = i;
						if(detail.getServerName().contains("cisco")) {
							responseEntity = FileUtil.ciscoApiCall(detail, cache, restTemplate);
							codeValue = responseEntity.getStatusCodeValue();
							response  = responseEntity.getBody();
						}else {
							responseEntity = FileUtil.sendReq(detail, cache, tenantID, umsNet, restTemplate, connector,dao);
							response = responseEntity.getBody(); 
							codeValue = responseEntity.getStatusCodeValue();
						}if(StringUtils.hasText(response)) {
/**							log.info("After successFull Request ::  Response From Server :: "+  detail.getServerName() + response + " With requestDetailID {}, requestUrl {}, server Name {} ",detail.getRequestDetailID(), detail.getServerURL() , detail.getServerName());*/
							break;
						}
					}catch (ResourceAccessException e) {
						errorCode = 503;/**Service Unavailable*/
						errorDescription = e.getMessage();
						 log.error("An Exception Occured ProvisionParallel, Response From serever {}, errorCode {}, exception {}", detail.getServerName(),errorCode, e.getMessage());
						 log.info("Response From :: "+detail.getServerName()  + response);
					}catch (HttpServerErrorException | HttpClientErrorException e ) {
						   errorCode =  e.getStatusCode().value();
						   log.error("An Exception Occured ProvisionParallel, Response From serever {}, errorCode {}, exception {}", detail.getServerName(),errorCode, e.getMessage());
						 if(StringUtils.hasText(customRespCode)) {
							   Integer valueOf = Integer.valueOf(customRespCode);
						     if(errorCode.intValue() == valueOf.intValue() && retries == 1) {
							     if(responseEntity !=null && responseEntity.getBody() != null ) {
							        response  = responseEntity.getBody();
							        log.info("Successful response after matching custom code: {}", response);
							        codeValue = Constants.SUCESS_CODE;
							        break;
							   }else {
								  log.error("Response entity responseEntitty.getBody() :: null Has no Body :: cannot process the request {}", responseEntity.getBody());
							   }
						     }else {
							    	log.error("We Cannot Process the request because errorCode does not matched the customCode Retrying again with Numb of retries " + retries);
							    }
						 }else {
						    	log.error("CustomResponseCode from ProvisioningResponseDetails got null :: {}", customRespCode );
						    }
						    errorDescription = e.getMessage();
						    response = e.getResponseBodyAsString();
						 log.error("Exception occured From Response Server : " + e.getMessage());
						 log.error("An Exception Occured Fom ProvisionParallel, url {} requestBody {}, server Name {} ", detail.getServerURL() , detail.getRequestObject(), detail.getServerName(),  e);
					}
				}
				log.info("After successFull Request :: Response From Server :: " + detail.getServerName() + response +" With requestDetailID {}, requestUrl {}, server Name {} . Number Of Maximum Retreis :: {} for requestDetailId :: {} Date {}",
				         detail.getRequestDetailID(), detail.getServerURL(), detail.getServerName(),
				         retries, detail.getRequestDetailID(), new Date());
	
				reqResponseObj = FileUtil.setProvisioningRequestResponseDetailsData(mapper, detail, cache, response, tenantID, retries);
				
				/**
				 * This if block is used to save ProvisioningRequestProcessorErrors :: 
				 */	
				if (!StringUtils.hasText(response) &&  codeValue != 200) {	
					errorObj = FileUtil.saveProvisioningRequestProcessorErrors(String.valueOf(errorCode), response, detail, errorDescription, cache);
					dao.saveProcessError(errorObj);
					dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(), errorObj.getStatus(), retries, StringUtils.hasText(String.valueOf(errorCode))? String.valueOf(errorCode) : Constants.SUCCESS, errorDescription);
					status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
					reqResponseObj.setErrorDescription(cache.strConfigVal(Constants.NO_RESP_FROM_SERVER, tenantID) + reqResponseObj.getRequestDetailID());
					reqResponseObj.setErrorDescription(errorDescription);
					reqResponseObj.setRetrySubmitDate(new Date());
					reqResponseObj.setErrorCode(String.valueOf(errorCode));
					reqResponseObj.setStatus(status);
					detail.setStatus(status);
					map.put(key, detail);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries, String.valueOf(errorCode),errorDescription);
					dao.saveProvisioningRequestResponseDetail(reqResponseObj);
					log.error("{} is not processing ", detail.getRequestDetailID());
					throw new ProvisionException("Unable to make the request for this RequestDetailID");
				}else if(response.contains("status")){
					if(response.contains("20") || response.contains("OK")) {
							if (StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
								status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
								 detail.setStatus(status);
							}else {
								status = getStatus(reqResponseObj);	
							}
					}else {
						status = cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS, Constants.GLOBAL_TENANT);
					}
					reqResponseObj.setStatus(status);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				}else if(detail.getServerURL().contains("tmf") || detail.getServerURL().contains("oce")) {
					/*** customRespReqObj this if block is used to store responses unique id r|keys which is coming from the external call api:*/
			      if(customRespReqObj.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
			    	  log.info("Founded customRespReqObj responseMaping to map Response with r| notation :: ");
			    	  List<String> responseRegexValue = FileUtil.foundResponseReplaceValues(customRespReqObj);
			    	  log.info("ResponseRegexValues founded responseMaping to map "+ responseRegexValue); 
			    	  Map<String,String> responseObjMap = mapper.readValue(response, Map.class);
				
				      /** In coming sprints we need to un comment convertResponseJsonToMap() to do replace dynamic response replacement
					  *
					  * Map<String, String> resonseJsonMap = FileUtil.convertResponseJsonToMap(response);
					  */
				    for(String val : responseRegexValue) {
				      responseUnqiueID = responseObjMap.get(val);
				      log.info("Response  UnqiueID {} ", responseUnqiueID); 
				    }
				  detail.setReqServerRespID(responseUnqiueID);
				  dao.updateRequestDetailServerUrl(detail.getServerURL(), detail.getRequestDetailID(), responseUnqiueID);
				  responseObjMap.clear();
				 
			  }	if(StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
						status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
					}else {
					  status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
					}
					reqResponseObj.setStatus(status);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				}else if (StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
					status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				}else {
					  status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
					  dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries);
				}
				reqResponseObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT));
				dao.saveProvisioningRequestResponseDetail(reqResponseObj);			
				detail.setStatus(status);
				Map<?, ?> mapResponse = FileUtil.getJsonOrXmlMap(replacedResonse);
				detail.setResponse(mapResponse);
				detail.setSoapresponse(response);
				detail.setResponseString(response);
				key =  detail.getSequenceID() + ":" + detail.getSubSequenceID();
				map.put(key, detail);
				log.info(
						"Ended ProvisionParallel call method. Thread Name: {}, Thread ID: {}, Request ID: {}, Request Detail ID: {}, Status: {}",
						Thread.currentThread().getName(), Thread.currentThread().getId(), detail.getRequestID(),
						detail.getRequestDetailID(), detail.getStatus());
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
					status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
				} else {
					status = cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV, Constants.GLOBAL_TENANT);
					dao.updateProvisioningRequestDetailCompletion(reqResponseObj.getRequestDetailID(), status, retries,
							cache.strConfigVal(Constants.ERROR_CODE, Constants.GLOBAL_TENANT),
							cache.strConfigVal(Constants.RESP_NOT_UPDATED, Constants.GLOBAL_TENANT));
				}
			} else {
				status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
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
				mapResponse = mapper.readValue(response,Map.class);		
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
				List<Map<String, String>> l = new ArrayList<>();
				l.add(finalResponse);
				return mapper.writeValueAsString(finalResponse);

			} catch (JsonSyntaxException e) {
				log.error("{} {}", detail.getRequestDetailID(), e.getMessage());
				log.error("ProvisionParallel :: replaceResponseParams() ", e);
				return null;
			}

		}

	}

	private class ProvisionSequential implements Callable<Boolean> {

		private List<ProvisioningRequestDetails> detailList;
		private CacheService cache;
		private RestTemplate restTemplate;
		private OMSNetwork omsNet;
		private ProvisioningRequest provisioningRequest;
		private String activityCode;

		ProvisionSequential(List<ProvisioningRequestDetails> detailList, ProvisioningRequest provisioningRequest,
				CacheService cache, OMSNetwork omsNet, RestTemplate restTemplate, String activityCode) {
			this.restTemplate = restTemplate;
			this.detailList = detailList;
			this.cache = cache;
			this.omsNet = omsNet;
			this.tenantID = provisioningRequest.getTenantID().toString();
			this.provisioningRequest = provisioningRequest;
			this.activityCode = activityCode;
		}

		String tenantID = null;
		int retries = 0;
		/**
		 * Map<?, ?> mapResponse; ObjectMapper mapper = new ObjectMapper();
		 * String result = null;
		 * SoapClient clientConnector = new SoapClient();
		 */
		JSONObject jsonObject = null;
		String description = "";
		String code = "";
		String strVal = "";

		public Boolean call() throws Exception {
			int status = 0;
			String keyToFind;
			String key;
			ProvisioningRequestDetails dependentObj = new ProvisioningRequestDetails();
			ProvisioningRequestDetails detailTemp = null;
			ObjectMapper mapper = new ObjectMapper();
			String errorDescription = "";
			Integer errorCode = null;
			String response = "";
			ResponseEntity<String> responseEntity = null;
			String responseUnqiueID =null;
			String responsejson = null;
			try {
				retries = 0;
				for (ProvisioningRequestDetails detail : detailList) {
					log.info(
							"Started ProvisionSequential call method. Thread Name: {}, Thread ID: {}, Request ID: {}, Request Detail ID: {}, Status: {}",
							Thread.currentThread().getName(), Thread.currentThread().getId(), detail.getRequestID(),
							detail.getRequestDetailID(), detail.getStatus());
					dao.updateProvisioningRequestDetails(detail.getRequestDetailID(), detail.getRequestID());
					String  customRespCode  = FileUtil.getProvisioningResponseCode(detail,cache);
					String  customRespReqObj  = FileUtil.getProvisioningResponseObject(detail,cache);
					log.info("{} is being processed with activityCode {}", detail.getRequestDetailID(), activityCode);
					/**String customRespCode = FileUtil.getProvisioningResponseCode(detail,cache); */
					String request = null;
					/**
					 * detailTemp = detail; key =
					 * detail.getServiceID()+":"+detail.getGroup()+":"+detail.getSequenceID()+":"+
					 * detail.getSubSequenceID(); keyToFind =
					 * detail.getServiceID()+":"+detail.getGroup()+":"+detail.getDependentSequenceID
					 * ()+":"+detail.getDependentSubSequenceID();
					 */
					key =  detail.getSequenceID() + ":" + detail.getSubSequenceID();
					keyToFind =  detail.getDependentSequenceID() + ":" + detail.getDependentSubSequenceID();
					dependentObj = map.get(keyToFind);
					log.info("keyToFind for dependentObject {} ", keyToFind);
					List<ProvReqResponseDetails> responseDtllist  = new ArrayList<>();
					/**
					 * if(detail.getProcessType().equalsIgnoreCase("ManualUpdate")) {
					 * responseDtllist = dao.getProvRequestResponseDetails(null,
					 * detail.getRequestID());
					 * 
					 * }
					 */
					if(dependentObj !=null) {
					 responseDtllist = dao.getProvRequestResponseDetails(dependentObj.getRequestDetailID(), null);
					 if(responseDtllist.isEmpty()) {
						 log.info("Response Details got null seraching with requestId {}", dependentObj.getRequestID());
						 responseDtllist = dao.getProvRequestResponseDetails(null, dependentObj.getRequestID());
						 for(ProvReqResponseDetails responseObj : responseDtllist) {
							 String responseobject = responseObj.getResponseobject();
							 if(StringUtils.hasText(responseobject) && responseobject.contains("cvlan")) {
								 dependentObj.setResponseString(responseobject);
								 log.info("Dependent Response Object Founded responseObj {}", responseobject);
								 break;
							 }
						 }
					 }else {
						 dependentObj.setResponseString(responseDtllist.get(0).getResponseobject());
						 log.info("Dependent Response Object Founded responseObj {}, dependentObjRequestDtlId {}, reponseRequestDetailID {}",responseDtllist.get(0).getResponseobject(),dependentObj.getRequestDetailID(), responseDtllist.get(0).getRequestDetailID());
					 }
					}
					if (dependentObj !=null && dependentObj.getStatus() == cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY,Constants.GLOBAL_TENANT) && detail.getResponseUpdateCategory().equals(2)) {				
						if (detail.getServerURL().contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
							Map<String, String> convertResponseJsonToMap = FileUtil.convertResponseJsonToMap(dependentObj.getResponseString());
							String requestUrl = detail.getServerURL();
							List<String> respnseRegExp = FileUtil.getResponseRegexPipes(requestUrl);
							for( String regexKey : respnseRegExp ) {
								requestUrl = requestUrl.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID), convertResponseJsonToMap.get(regexKey));
								detail.setServerURL(requestUrl);
							}
							
							/**
							 * String stringJson = mapper.writeValueAsString(dependentObj.getResponse());
							 * Map<Object,Object> map = mapper.readValue(stringJson.getBytes(), Map.class);
							 * String replacedUrl =
							 * FileUtil.getUrlParametrsReplacement(detail.getServerURL(),Constants.R_REGX,
							 * cache.strConfigVal(Constants.R_PIPE, this.tenantID), map);
							 * detail.setServerURL(replacedUrl);
							 */
							}
							request = detail.getRequestObject();
							if(StringUtils.hasText(request)) { 
								strVal = cache.strConfigVal(Constants.R_PIPE, this.tenantID);
								if(request.contains(strVal)) {
								     request = replaceRequestParams(dependentObj, request);
								   }
							if (!request.contains(strVal)) {
								dao.updateRequestObject(detail.getRequestDetailID(), request);
								detail.setRequestObject(request);
							} else {
								detail.setStatus(cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV,Constants.GLOBAL_TENANT));
								map.put(key, detail);
								dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),cache.intConfigVal(Constants.PROV_DETAIL_FAILED_TO_PROV, Constants.GLOBAL_TENANT),
										retries, "", strVal + cache.strConfigVal(Constants.R_PIPE_NOT_REPLACES,
												Constants.GLOBAL_TENANT));
								log.error("{} {}  parameter is not replaced, response parameter from the dependent is not available !",detail.getRequestDetailID(), strVal);
								break;
							 }
						}
						for (int i = retries; i <= detail.getMaxRetries(); i++) {
							retries++;
							retries = i;
							try {
								if(detail.getServerName().contains("cisco")) {
									responseEntity = FileUtil.ciscoApiCall(detail, cache, restTemplate);
									/** int codeValue = responseEntity.getStatusCode().value(); */
									response  = responseEntity.getBody();
								}else {
								responseEntity = FileUtil.sendReq(detail, cache, tenantID, umsNet, restTemplate, null, dao);/** call Network API*/
								response = responseEntity.getBody();
								}
								if(StringUtils.hasText(response)) {
									log.info("SendRequest response as Some Text, breaking loop ");
/**									log.info("After successFull Request :: Response From Server :: "+  detail.getServerName() + response + " With requestDetailID {}, requestUrl {}, server Name {} ",detail.getRequestDetailID(), detail.getServerURL() , detail.getServerName());*/
								     break;	
								}	
								/**
								 * sendRequest(detail); // call Network API isSend = true; break;
								 */
							} catch (ResourceAccessException e) {
								errorDescription = e.getMessage();
								 log.error("An Exception Occured ProvisionSequential, Response From serever ", e);
							}catch (HttpClientErrorException | HttpServerErrorException e) {
								   errorCode = e.getStatusCode().value();
								  if(StringUtils.hasText(customRespCode)) {
									    Integer valueOf = Integer.valueOf(customRespCode);
									  if(errorCode.intValue() == valueOf.intValue() && retries == 1) {
										   if(responseEntity != null && responseEntity.getBody() != null) {
											  response  = responseEntity.getBody();
											  errorCode = Constants.SUCESS_CODE;
											  log.info("Successful response after matching custom code: {}", response);
											  break;
										 }else {
										   log.error("Response entity responseEntitty.getBody() :: got null :: cannot process the request " + responseEntity.getBody());
										  }
									 }else {
										 log.error("We Cannot Process the request because errorCode does not matched the customCode Retrying again with Numb of retries " + retries);
									    }
								  }else{
									  log.error("Unable to find Process The Request " + e.getMessage());
								  }
								   errorDescription = e.getMessage();
								    responsejson = e.getResponseBodyAsString();
								    response = e.getResponseBodyAsString();
								   log.error("An Exception Occured Fom ProvisionSequential, url {} requestBody {}, server Name {} ", detail.getServerURL() , detail.getRequestObject(), detail.getServerName(),  e);
							}
						}
						log.info("After successFull Request :: Response From Server :: " + detail.getServerName() + response +" With requestDetailID {}, requestUrl {}, server Name {} . Number Of Maximum Retreis :: {} for requestDetailId :: {} Date {}",
						         detail.getRequestDetailID(), detail.getServerURL(), detail.getServerName(),
						         retries, detail.getRequestDetailID(), new Date());
						detail.setCureentRetryCount(retries);
						/**This If Block is used to save ProvisioningRequestProcessingError iw response got error:: 
						 */				 
						ProvisioningRequestResponseDetail requestResponseObj = FileUtil.setProvisioningRequestResponseDetailsData(mapper, detail, cache, response, this.tenantID, retries);
						
						if(!StringUtils.hasText(response)) {
							ProvisioningRequestProcessingError errorRequestObj = FileUtil.saveProvisioningRequestProcessorErrors(String.valueOf(errorCode), response, detail,errorDescription, cache);	
							dao.saveProcessError(errorRequestObj);
							dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(), errorRequestObj.getStatus(), retries, StringUtils.hasText(String.valueOf(errorCode)) ? String.valueOf(errorCode) : Constants.SUCCESS, errorDescription);			
							requestResponseObj.setRetrySubmitDate(new Date());
							requestResponseObj.setErrorCode(String.valueOf(errorCode));
							requestResponseObj.setResponseObject(responsejson);
							 status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);;
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries,code, description);
							dao.saveProvisioningRequestResponseDetail(requestResponseObj);
							detail.setStatus(status);
							map.put(key, detail);
							log.error("Unable to make the request for RequestDetailID={}", detail.getRequestDetailID());
							throw new ProvisionException("Unable to make the request for this RequestDetailID");
						}else if(response.contains("status")){
							 if(response.contains("20") || response .contains("OK")) {
									 if(StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes")) ) {
										 status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS,Constants.GLOBAL_TENANT);
										 dependentObj.setStatus(status);
										 detail.setStatus(status);
											/**
											 * if(response.contains("id")) { Map<String, String> finalMap = new
											 * HashMap<>(); finalMap = FileUtil.convertResponseJsonToMap(response);
											 * String responseId = finalMap.get("id"); //
											 * detail.setReqSerRespId(responseId); }
											 */
								    }else{
										  status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
										  dependentObj.setStatus(status); 
								     }
							}else {
								status = cache.intConfigVal(Constants.RESPONSE_NOT_CONTAIN_SUCESS_STATUS,Constants.GLOBAL_TENANT);
							}
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
						}else if(detail.getServerURL().contains("tmf") || detail.getServerURL().contains("oce")) {
							/*** customRespReqObj this if block is used to store responses unique id r|keys which is coming from the external call api:*/
						      if(customRespReqObj.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))) {
						    	  log.info("Founded customRespReqObj responseMaping to map Response with r| notation :: ");
						    	  List<String> responseRegexValue = FileUtil.foundResponseReplaceValues(customRespReqObj);
						    	  log.info("ResponseRegexValues founded responseMaping to map "+ responseRegexValue); 
						    	  Map<String,String> responseObjMap = mapper.readValue(response, Map.class);
							
							      /** In coming sprints we need to un comment convertResponseJsonToMap() to do replace dynamic response replacement
								  *
								  * Map<String, String> resonseJsonMap = FileUtil.convertResponseJsonToMap(response);
								  */
							    for(String val : responseRegexValue) {
							      responseUnqiueID = responseObjMap.get(val);
							      log.info("Response  UnqiueID {} ", responseUnqiueID); 
							    }
							  detail.setReqServerRespID(responseUnqiueID);
							  dao.updateRequestDetailServerUrl(detail.getServerURL(), detail.getRequestDetailID(), responseUnqiueID);
							  responseObjMap.clear();
							 
						  }	if(StringUtils.hasText(detail.getIsAsync()) && (detail.getIsAsync().equalsIgnoreCase("y") || detail.getIsAsync().equalsIgnoreCase("yes"))) {
									status = cache.intConfigVal(Constants.PROV_REQ_AYSNC_STATUS, Constants.GLOBAL_TENANT);
								}else {
								  status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT);
								}
						        requestResponseObj.setStatus(status);
								dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
							}else if (StringUtils.hasText(response)) {
								if(detail.getAddColumnOne().equalsIgnoreCase(Constants.PRE_PAID_ACCOUNT_TYPE) &&
										detail.getServerName().equalsIgnoreCase(Constants.OPTIVA) && activityCode.equalsIgnoreCase(cache.strConfigVal(Constants.NSWO, Constants.GLOBAL_TENANT))) {
									status = cache.intConfigVal(Constants.CUST_IDLE_STAT,Constants.GLOBAL_TENANT);
									
								}else {
			         			status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,
										Constants.GLOBAL_TENANT);
								}
							dao.updateProvisioningRequestDetailCompletion(requestResponseObj.getRequestDetailID(), status, retries);
							log.info("{} is processed successfully ", detail.getRequestDetailID());
						}
						requestResponseObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS,Constants.GLOBAL_TENANT));
						dao.saveProvisioningRequestResponseDetail(requestResponseObj);
						detail.setStatus(status);
						/** detail.setResponse(res.getResult()); */
						Map mapResponse = FileUtil.getJsonOrXmlMap(response);
						detail.setResponse(mapResponse);
						detail.setSoapresponse(response);
						detail.setResponseString(response);
						map.put(key, detail);
					} else {
						log.error(
								"Unable to process request :: Dependent request status does not match the required completion status. RequestDetailID={}, RequiredCompletionStatus={}",
								detail.getRequestDetailID(), cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY,Constants.GLOBAL_TENANT));
						detail.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT));
						map.put(key, detail);
						/** detail.setResponse(res.getResult()); */
//						detail.setResponse(mapResponse);
//						map.put(key, detail);
//						return true;
					}
					log.info(
							"Ended ProvisionSequential call method. Thread Name: {}, Thread ID: {}, Request ID: {}, Request Detail ID: {}, Status: {}",
							Thread.currentThread().getName(), Thread.currentThread().getId(), detail.getRequestID(),
							detail.getRequestDetailID(), detail.getStatus());
				}
			} catch (Exception e) {
				log.error("An exception occured in ProvisionSequential :: ProvisionServiceImpl {} is not processed , got error :  {}",
						dependentObj != null ? dependentObj.getRequestDetailID() : "", e.getMessage());
				dependentObj.setStatus(cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT));
				key =  dependentObj.getSequenceID() + ":" + dependentObj.getSubSequenceID();
				map.put(key, dependentObj);
				log.error("ProvisionSequential :: call() :: ", e);
			}
			return true;
		}
//    	boolean sendRequest(ProvisioningRequestDetails detail) throws Exception {
//			String request = detail.getRequestObject();
//			/** String token = umsNetwork.generateSuperUserToken(); */
//			if (detail.getInterfaceType().equalsIgnoreCase(Constants.REST_CALL)) { 	
//				String objectType = FileUtil.getRequestObjectType(request);
//				if(objectType.equalsIgnoreCase("json")) {
//				String token = umsNet.generateSuperUserToken();
//				HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, request);
//					
////				HttpEntity<String> httpEntity = AuthenticationUtil.getHttpEntity(cache.strConfigVal(Constants.USERNAME, Constants.GLOBAL_TENANT),cache.strConfigVal(Constants.PASSWORD, Constants.GLOBAL_TENANT), request);		
//			String response=restTemplate.exchange(detail.getServerURL(),HttpMethod.valueOf(detail.getRequestMethod()), httpEntity, String.class).getBody();
//				log.info("Response from network server is : {}", mapResponse);
//
//				/**
//				 * HttpMethod httpMethod
//				 * =config.getHttpMethod(provisioningRequest.getActionID());
//				 */
//				mapResponse = restTemplate.exchange(detail.getServerURL(),HttpMethod.valueOf(detail.getRequestMethod()), httpEntity, Map.class).getBody();
//				log.info("Response from network server is : {}", mapResponse);
//				log.info("{} {} {}", detail.getRequestDetailID(), detail.getServerURL(), request);
//				}
//				else if(objectType.equalsIgnoreCase("xml") || objectType.isEmpty()){
//					HttpHeaders headerKeys = AuthenticationUtil.getHeaderKeys(detail);
//					HttpEntity<String> httpEntity = new HttpEntity<String>(detail.getRequestObject(),headerKeys);
//					result = restTemplate.exchange(detail.getServerURL(), HttpMethod.valueOf(detail.getRequestMethod()),
//							httpEntity, String.class).getBody();
//					log.info("Response From Server :: " +result);
//					return true;
//				}
//			}
//			if (detail.getInterfaceType().equalsIgnoreCase(Constants.SOAP_CALL)) {
//				ProvisioningRequestResponseDetail obj = new ProvisioningRequestResponseDetail();
//				HttpHeaders headerKeys = AuthenticationUtil.getHeaderKeys(detail);
//				HttpEntity<String> httpEntity = new HttpEntity<>(detail.getRequestObject(),headerKeys);
//				result = restTemplate.exchange(detail.getServerURL(), HttpMethod.valueOf(detail.getRequestMethod()),
//						httpEntity, String.class).getBody();
//				obj.setResponseObject(result);
//				return true;
//			}
//
//			return validateMapResponse(detail);
//
//		}

//		public boolean validateMapResponse(ProvisioningRequestDetails detail) throws Exception {
//			boolean status = false;
//			ProvRespDetails obj = cache.getResponseDetailsMap(detail.getProvObjectId());
//			String requsetObject = obj.getApiObject();
//			Map map = mapper.readValue(requsetObject.getBytes(), Map.class);
//			String replace = null;
//			Object value = null;
//			String key = null;
//			Iterator it = map.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry entry = (Map.Entry) it.next();
//				key = entry.getKey().toString();
//				if (key.contains(cache.strConfigVal(Constants.RESULT_CODE, this.tenantID))) {
//					replace = key.replace(cache.strConfigVal(Constants.RESULT_CODE, this.tenantID), "");
//					value = mapResponse.get(replace);
//				}
//				if (key.contains(cache.strConfigVal(Constants.SUB_REQ_PARAM, this.tenantID))) {
//					return true;
//				}
//			}
//			if (obj.getResponseCode().trim().equals(value != null ? value.toString() : "")) {
//				status = true;
//				return status;
//			} else {
//				status = false;
//			}
//			return status;
//		}
		
		String replaceRequestParams(ProvisioningRequestDetails dependentObj, String request) throws Exception  {
			try {
				Map parentJson = dependentObj.getResponse();
				log.info("In ProvisionServiceImpl started :: replaceRequestParamsMethod processing dependentObj with dependentObjDetailID {} RequestBody {}",dependentObj.getRequestDetailID(), parentJson);
				/**
				 * long coId;  long customerId;String key; String coIdPub = "";List<String> subscriptionIds = new ArrayList<>();
				 */
				String objectType=FileUtil.getRequestObjectType(request);
				if (dependentObj.getInterfaceType().equalsIgnoreCase(Constants.SOAP_CALL) || dependentObj.getInterfaceType().equalsIgnoreCase(Constants.LOCAL_CALL)) {
					String soapresponse = dependentObj.getSoapresponse();
					List<String> regexValues = FileUtil.foundResponseReplaceValues(request);
					log.info("Request of responseParameters foundResponseReplaceValues: {}", regexValues);
					String rPipe = cache.strConfigVal(Constants.R_PIPE, this.tenantID);
					if (!regexValues.isEmpty()) {
					    Map<String, String> responseValues = DynamicXmlValueExtractor.getValues(soapresponse, Constants.XML_DYNAMIC_OPERATION, Constants.XML_DYNAMIC_MODIFIER, regexValues);
					    for (String val : regexValues) {
					        String replaceValue = rPipe + val;
					        if (request.contains(replaceValue)) {
					            request = request.replace(replaceValue, responseValues.get(val));
					        }
					    }
					    responseValues.clear();
					}	 
					/**This line of code is commented because as of protei serever is 
					 * Map<Object,Object> resultObjMap = (Map<Object, Object>)((Map<Object, Object>)
					 * parentJson.get(Constants.result)).get(Constants.resultObject);
					 * if(request.contains(cache.strConfigVal(Constants.R_PIPE,
					 * this.tenantID)+Constants.clientId)) { Object object = resultObjMap.get("id");
					 * request = request.replace(cache.strConfigVal(Constants.R_PIPE,
					 * this.tenantID)+Constants.clientId, resultObjMap.get("id").toString()); return
					 * request; }else if(request.contains(cache.strConfigVal(Constants.R_PIPE,
					 * this.tenantID)+Constants.subscriptionId)) { request =
					 * getReplacedResponseSubscrptionIds(subscriptionIds, resultObjMap,
					 * request,dependentObj.getRatePlanID().toString()); }*/
			  } else if(dependentObj.getServerURL().contains("step3")) {
					String response = dependentObj.getResponseString();
					 Map<String, String> finalMap = new HashMap<>();
					 List<String> regexValues = null;
					 try {
						 finalMap = FileUtil.convertResponseJsonToMap(response);
						 regexValues = FileUtil.foundResponseReplaceValues(request);
						 for(String val : regexValues) {
								if(request.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID)+val)) {
									request = request.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID)+val, finalMap.get(val));
								} 
						 }
						 
					   } catch (Exception e) {
						e.printStackTrace();
					}
					 finalMap.clear();
				}else if(objectType.equalsIgnoreCase("json") && dependentObj.getServerURL().contains("createSubscriber")) {
					Iterator<?> it = parentJson.entrySet().iterator();			
					/*** getReplacedRequestParams used to replace the requestParmas */				
					request = FileUtil.getReplacedRequestParams(request,it,cache, this.tenantID);
				}else if(objectType.equalsIgnoreCase("json") && !dependentObj.getServerURL().contains("createSubscriber")) {
					String response = dependentObj.getResponseString();
					 Map<String, String> finalMap = new HashMap<>();
					 List<String> regexValues = null;
					 try {
						 finalMap = FileUtil.convertResponseJsonToMap(response);
						 log.info("finalMap: {}", finalMap);
						 regexValues = FileUtil.foundResponseReplaceValues(request);
						 for(String val : regexValues) {
								if(request.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID)+val)) {
									request = request.replace(cache.strConfigVal(Constants.R_PIPE, this.tenantID)+val, finalMap.get(val));
								} 
						 }
						 
					   } catch (Exception e) {
						e.printStackTrace();
					}
					 finalMap.clear();
				}
					
			/*** This if block is used for Soap callS To make every soap call as dynamic replacement */
				 
				/**As of now we are not using this soapResponse from protei.
				 * String soapresponse = dependentObj.getSoapresponse(); jsonObject =
				 * XML.toJSONObject(soapresponse); if (request.contains("r|customerId")) {
				 * customerId =
				 * jsonObject.getJSONObject("SOAP-ENV:Envelope").getJSONObject("SOAP-ENV:Body")
				 * .getJSONObject("customerCreateResponse").getJSONObject("customerNew")
				 * .getJSONObject("csId").getLong("content"); } else if (request.contains("r|"))
				 * { jsonObject =
				 * jsonObject.getJSONObject("SOAP-ENV:Envelope").getJSONObject("SOAP-ENV:Body")
				 * .getJSONObject("contractCreateResponse").getJSONObject("contractNew"); coId =
				 * jsonObject.getJSONObject("coId").getLong("content"); coIdPub =
				 * jsonObject.getJSONObject("coIdPub").getString("content"); request =
				 * request.replace("r|contractID", String.valueOf(coId)); request =
				 * request.replace("r|contractPubId", coIdPub);
				 * 
				 * }
				 */					
				log.info("In ProvisionServiceImpl ended :: replaceRequestParamsMethod After replacement of dependentRequest requestJson {}", request);
				return request;
			} catch (Exception e) {
				log.error("{} Got Error while reading dependnent response {}", dependentObj.getRequestDetailID(),e.getMessage());
				log.error("replaceRequestParams(ProvisioningRequestDetails dependentObj, String request)", e);
				throw e;
			}

		}

		public String getReplacedResponseSubscrptionIds(List<String> subscriptionIds, Map<Object, Object> resultObjMap,String request,String ratePlanId) {
			String ocsPalnIdFound;
			String replaceRequest = "";
			jsonObject = FileUtil.getMapToJsonObject(resultObjMap);
			if(jsonObject.get("productRest") instanceof JSONArray) {
				JSONArray productRestArray = jsonObject.getJSONArray("productRest");
				  for (int i = 0; i < productRestArray.length(); i++) {
					 JSONObject productRest = productRestArray.getJSONObject(i);
					 Object productId = productRest.get("productId");

					 ocsPalnIdFound = cache.getOcsPlanData(productId.toString());
					 if(ocsPalnIdFound.equalsIgnoreCase(ratePlanId)) {
						 Object subscriptionId = productRest.get("subscriptionId");
							String replaceString = "<long>"+subscriptionId.toString()+"</long>";
							subscriptionIds.add(replaceString);
							i = productRestArray.length();
					 }
					 else {
						 log.info("No RatePlan Id Fount For Particular Product Id :: "+productId+"--------------"+ratePlanId); 
					 }
				  }
			}
			else if(jsonObject.get("productRest") instanceof JSONObject) {
				JSONObject productRest = jsonObject.getJSONObject("productRest");
				Object productId = productRest.get("productId");
				 ocsPalnIdFound = cache.getOcsPlanData(productId.toString());
				 if(ocsPalnIdFound.equalsIgnoreCase(ratePlanId)) {
					 Object subscriptionId = productRest.get("subscriptionId");
						String replaceString = "<long>"+subscriptionId.toString()+"</long>";
						subscriptionIds.add(replaceString);
				 }
				 else {
						log.info("No RatePlan Id Fount For Particular Product Id :: "+productId+ocsPalnIdFound);
					}
				
			}
			replaceRequest = subscriptionIds.toString();
			replaceRequest = replaceRequest.replace("[", "").replace("]", "").replace(",", "");
			request = request.replace("r|subscriptionId", replaceRequest);
			 log.info("request =================== :: "+request);
			return request;
		}

	}
}
