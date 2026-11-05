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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provioning.model.ProvisioningRequestProcessingError;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dto.Responce;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisionErrorRequestService;
import com.knot.provisioning.utill.AuthenticationUtil;
import com.knot.provisioning.utill.Constants;

/**
 *
 * @author Sameena
 *
 */

@Service
public class ProvisionServiceErrorRequestImpl implements ProvisionErrorRequestService{

	
	private OMSNetwork omsNet;
	private CacheService cache;
	
	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@Autowired
	public void setOmsNet(OMSNetwork omsNet) {
		this.omsNet = omsNet;
	}

	private static final Logger logger = LogManager.getLogger(ProvisionServiceErrorRequestImpl.class);

	Map<String, ProvisioningRequestDetails> map = new HashMap<>();	

	ProvisioningDao dao = null;
	
	Gson gson = new Gson();

	@SuppressWarnings("rawtypes")
	public Future processProvisioningErrorRequest(ProvisioningRequest provisioningRequest,ProvisioningDao dao,ExecutorService executor) {

		logger.info("In ProvisioningServiceImpl :: processProvisioningRequest ");
		this.dao = dao;
		Future task = null ;		
		try {				
			ProvisioningMonitoringThread prThread = new ProvisioningMonitoringThread(provisioningRequest);
			task   = executor.submit(prThread);
		} catch (Exception e) {				
			logger.error("{}{}",provisioningRequest.getRequestID(),e.getMessage());
		}		
		return task;
	}

	private class ProvisioningMonitoringThread implements Runnable {

		private ProvisioningRequest provisioningRequest ;
		
		ProvisioningMonitoringThread(ProvisioningRequest provisioningRequest){
			this.provisioningRequest = provisioningRequest;
			tenantID = provisioningRequest.getTenantID().toString();
		}
		private String tenantID = null;
		BigInteger requestID;	
		int statusCode = 0;
		public void run(){		

			ExecutorService executor = null;
			boolean errorFlag = false;				
			String key;
			try {
				logger.info("Entered ProvisioningMonitoringThread ... ");

				requestID = provisioningRequest.getRequestID();

				dao.updateProvReqProgress(requestID,cache.intConfigVal(Constants.PROV_REQ_RE_PROCESS_IN_PROGRESS, Constants.GLOBAL_TENANT));
				
				List<ProvisioningRequestDetails> rdList =  dao.getRequestDetailsGroups(requestID);		
				
				if(!rdList.isEmpty()){
					executor = Executors.newFixedThreadPool(rdList.size());						
					int groupNo = 0 ;					
					int subGrpNo = 0;

					List<ProvisioningRequestDetails> parallelList = new ArrayList<>();
					List<ProvisioningRequestDetails> seqList = new ArrayList<>();	
					
					outer:
						for (ProvisioningRequestDetails obj : rdList) {
							key = obj.getGroup()+":"+obj.getSequenceID()+":"+obj.getSubSequenceID();
							if(obj.getStatus() == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT) || obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT)  || obj.getStatus() == cache.intConfigVal(Constants.READY_TO_RE_PROCESS, Constants.GLOBAL_TENANT)) {
								if(groupNo != obj.getGroup() && !parallelList.isEmpty() ){
									ProvisionParellelMainThread	prThread = new ProvisionParellelMainThread(parallelList,provisioningRequest);
									Future<Boolean> task  = executor.submit(prThread);							
									task.get();
									for (Map.Entry<String,ProvisioningRequestDetails> entry : map.entrySet()){  
										statusCode = entry.getValue().getStatus();
										if(statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT) || statusCode ==cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT) || statusCode == cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT )){		
											errorFlag = true;
											break outer;
										}
									} 							
									parallelList = new ArrayList<>();	
								}
								if(groupNo != obj.getGroup() && !seqList.isEmpty()){	
									for (Map.Entry<String,ProvisioningRequestDetails> entry : map.entrySet()){  
										statusCode = entry.getValue().getStatus();
										logger.info("Key = {}, Value = {}" ,entry.getKey() ,entry.getValue().getStatus()); 					            
										if(statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT) || statusCode ==cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT) || statusCode == cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT)){		
											errorFlag = true;
											break outer;
										}
									} 									
									seqList = new ArrayList<>();
								}
								
								if(obj.getDependentSequenceID() == cache.intConfigVal(Constants.GLOBAL_FLAG, tenantID) && obj.getDependentSubSequenceID() == cache.intConfigVal(Constants.GLOBAL_FLAG, tenantID)){
									parallelList.add(obj);
								}
								else if(obj.getDependentSequenceID() != cache.intConfigVal(Constants.GLOBAL_FLAG, tenantID) || obj.getDependentSubSequenceID() != cache.intConfigVal(Constants.GLOBAL_FLAG, tenantID) ){							
									seqList.add(obj);	
								}

								if(groupNo != obj.getGroup()){
									groupNo = obj.getGroup();
								}
								if(subGrpNo != obj.getSubGroup()){
									subGrpNo = obj.getSubGroup();
								}
							}
							else if(obj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)){
								/** obj.setResponse(res.getResult()); */
								map.put(key, obj);								
							}							
							
						}	

					if(!errorFlag && !parallelList.isEmpty()){
						ProvisionParellelMainThread	prThread = new ProvisionParellelMainThread(parallelList,provisioningRequest);
						Future<Boolean> task  = executor.submit(prThread);							
						logger.info("Parallel Main returned  {}", task.get());
						for (Map.Entry<String,ProvisioningRequestDetails> entry : map.entrySet()){  
							statusCode = entry.getValue().getStatus();
							if(statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT) || statusCode ==cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT) || statusCode == cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT)){		
								errorFlag = true;								
							}
						} 
					}
					if(!errorFlag && !seqList.isEmpty()){	
						ProvisionSequential	prThread = new ProvisionSequential(seqList,provisioningRequest);
						Future<Boolean> task  = executor.submit(prThread);							
						logger.info("Sequential Thread returned  {}", task.get());												
						for (Map.Entry<String,ProvisioningRequestDetails> entry : map.entrySet()){  
							statusCode = entry.getValue().getStatus();
							if(statusCode == cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT) || statusCode ==cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT) || statusCode == cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT)){		
								errorFlag = true;								
							}
						} 	
						
					}
							
				}
				String remarks=cache.strConfigVal(Constants.PROC_SUCESS, Constants.GLOBAL_TENANT);
				int status = 0;
				if(errorFlag){
					status =cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
				}else{
					omsNet. updateWorkOrderStatus(provisioningRequest.getWorkOrderID(),cache.intConfigVal(Constants.PROV_SUCCESS_WO_STATUS, Constants.GLOBAL_TENANT),cache.strConfigVal(Constants.PROCESSOR_DONE, Constants.GLOBAL_TENANT), tenantID);
				}
				dao.updateProvisioningRequest(requestID,status,remarks,null);

			} catch (Exception e) {	
				int status = 0;
				status =cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
				dao.updateProvisioningRequest(requestID,status,"Errors in Exceution ", null);
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			finally{				
				if(executor != null){
					executor.shutdown();				
					try {
						if (executor.awaitTermination(5, TimeUnit.MINUTES)) {
							executor.shutdownNow();
							logger.info("Shut down Now ..........");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			logger.info("Exit ProvisioningMonitoringThread ... ");
			
		}
	}

	// SSSSSSS
	private class ProvisionParellelMainThread implements Callable<Boolean>  {

		private List<ProvisioningRequestDetails> detailList;
		private ProvisioningRequest provisioningRequest;

		ProvisionParellelMainThread(List<ProvisioningRequestDetails>  detailList,ProvisioningRequest provisioningRequest){
			this.detailList = detailList;	
			this.provisioningRequest =  provisioningRequest;
		}

		public Boolean call() throws Exception {
			ExecutorService executor = Executors.newFixedThreadPool(detailList.size());				
			Exception exception = null;
			try {
				for (ProvisioningRequestDetails detail : detailList) {
					ProvisionParallel	prThread = new ProvisionParallel(detail, provisioningRequest);
					executor.execute(prThread);												
				}
			} catch (Exception e) {
				exception = e;
				e.printStackTrace();
			}
			finally{
				try {
					logger.info("Waiting for the parallel Threads Executor to terminate !!!");						
					executor.shutdown();
					while(!executor.isTerminated()){
						executor.shutdown();
						if (executor.awaitTermination(cache.intConfigVal(Constants.WAIT_FOR, Constants.GLOBAL_TENANT), TimeUnit.MINUTES)) {
							executor.shutdownNow();					
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			if(exception != null){
				throw exception;
			}
			return true;
		}
	}	

	/** SSSSSSS */
	private class ProvisionParallel implements Runnable  {		

		private ProvisioningRequestDetails detail;	
		
		ProvisionParallel(ProvisioningRequestDetails  detail,ProvisioningRequest provisioningRequest){
			this.detail = detail;
			this.tenantID = provisioningRequest.getTenantID().toString();
		}

		RestTemplate restTemplate = new RestTemplate();
		
		String tenantID = null;
		String responseStatus ;
		String key;		
		int retries = 0;
		Map mapResponse;
		ObjectMapper mapper = new ObjectMapper();
		Responce res;
		Gson gson = new Gson();
		String errorDescription = "";	
		String errorCode = "" ;
		String replacedResonse = null;
		public void run(){	
			int status = 0;	
			
			try {				
				int maxRetries = detail.getMaxRetries();
				TimeUnit time = TimeUnit.MILLISECONDS; 				
				int timeGap = detail.getTimeGapBetweenRetry();
				long sleepTime = time.convert(timeGap,TimeUnit.MINUTES);	
				key = detail.getGroup()+":"+detail.getSequenceID()+":"+detail.getSubSequenceID();
				retries = 0;
				ProvisioningRequestResponseDetail obj = null;
				while(retries < maxRetries ){
					retries++;
					responseStatus = sendRequest(); // call Network API	
					obj = new ProvisioningRequestResponseDetail();
					obj.setRequestDetailID(detail.getRequestDetailID());
					obj.setRequestID(detail.getRequestID());
					obj.setVersion(detail.getVersion());	
					obj.setResponseObjectType(cache.strConfigVal(Constants.RESP_TYPE, this.tenantID));
					obj.setResponseReceivedDate(new Date());				
					String jsonString = gson.toJson(res);
					obj.setResponseObject(jsonString);	
					obj.setResponseSequenceNo(retries);
					if(responseStatus.equalsIgnoreCase(cache.strConfigVal(Constants.API_SUCCESS_MESSAGE, Constants.GLOBAL_TENANT))){				
						status = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);								
					}else{						
						status = cache.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT);						
						obj.setRetrySubmitDate(new Date());
						obj.setErrorCode(errorCode);
						obj.setErrorDescription(errorDescription);			
						
					}	
					obj.setStatus(status);											
					if(status == cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT)){
						break;
					}
					dao.saveProvisioningRequestResponseDetail(obj);	
					Thread.sleep(sleepTime); 
				}				
				if(status == cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)){
					if(detail.getResponseUpdateCategory().equals(1)){
						replacedResonse = replaceResponseParams(detail.getRequestObject());
						if(replacedResonse != null){
							status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);							
							obj.setResponseObject(replacedResonse);
							dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),status,retries);					

						}else{
							status = cache.intConfigVal(Constants.POST_PROV_IN_PROGRESS, Constants.GLOBAL_TENANT);
							dao.updateProvisioningRequestDetailCompletion(obj.getRequestDetailID(),status,retries, ""+cache.intConfigVal(Constants.GLOBAL_FLAG, tenantID), cache.strConfigVal(Constants.RESP_NOT_UPDATED, Constants.GLOBAL_TENANT));
						}											

					}else{
						status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);
						dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),status,retries);					

					}
					obj.setStatus(status);
					dao.saveProvisioningRequestResponseDetail(obj);		
				}else{
					if(retries == maxRetries){
						status = cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT);
						errorDescription="Failed While Retrying";
					}
					this.saveProcessError();
					dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),status,retries,errorCode,errorDescription);					
				}	
				detail.setStatus(status);
				if(detail.getInterfaceType().equalsIgnoreCase("SOAP1")) {
					logger.info("IMplementation pending...");
				}
			   Map m=	mapper.readValue(replacedResonse.getBytes(), Map.class);
				detail.setResponse(m);
				map.put(key, detail);
			} catch (Exception e) {
				try {
					detail.setStatus(cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT));
				} catch (Exception e1) {
					logger.error("Error while setting status for requestDetail.",e);
				}
				map.put(key, detail);
			}	
			
		}

		String replaceResponseParams(String request) throws JsonProcessingException{
			try {
				/**
				 * List parentJson = res.getResult(); String str = gson.toJson(parentJson); str
				 * = str.replace("[", ""); str = str.replace("]", ""); Map map =
				 * gson.fromJson(str, Map.class); Map<String,String> finalResponse = new
				 * HashMap<>(); Iterator it = map.entrySet().iterator();
				 */
				Map<String, String> finalResponse = new HashMap<>();
				Iterator it = mapResponse.entrySet().iterator();
				String key = null;	
				String tempKep;	
				boolean isFound=false;
				while (it.hasNext()) {										
					Map.Entry entry7 = (Map.Entry)it.next(); 
					String[] types = new String[3];
					types[0] =  cache.strConfigVal(Constants.DOWN_STREAM_PARAM, this.tenantID);
					types[1] =  cache.strConfigVal(Constants.SUB_REQ_PARAM, this.tenantID);
					types[2] =  cache.strConfigVal(Constants.BOTH, this.tenantID);
					for (int i = 0; i < types.length; i++) {
						key = types[i] + entry7.getKey().toString();
						
						if(request.contains(key)){
							isFound=true;
							tempKep = key;
							finalResponse.put(tempKep, entry7.getValue().toString());
							break;	
						}
					}					
					/** logger.info(tempKep + " ::: "+entry7.getValue()); */
					if(!isFound) {

						finalResponse.put(entry7.getKey().toString(), entry7.getValue().toString());
					}
				}				
				/**
				 * List l = new ArrayList(); l.add(finalResponse); res.setResult(l);
				 */
				logger.info("RRR {}",finalResponse);
				return mapper.writeValueAsString(finalResponse);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		String sendRequest(){			
			String request = detail.getRequestObject();
			logger.info("Before request {}",request);			
			request  = request.replaceAll(cache.strConfigVal(Constants.DOWN_STREAM_PARAM_REGEX, this.tenantID), "");
			request = request.replaceAll(cache.strConfigVal(Constants.SUB_REQ_PARAM_REGEX, this.tenantID), "");
			request = request.replaceAll(cache.strConfigVal(Constants.BOTH_REGEX, this.tenantID), "");
			logger.info("After request {}",request);
			/** String token = umsNetwork.generateSuperUserToken(); */
			HttpEntity<String> httpEntity = AuthenticationUtil.getHttpEntity(cache.strConfigVal(Constants.USERNAME, Constants.GLOBAL_TENANT),cache.strConfigVal(Constants.PASSWORD, Constants.GLOBAL_TENANT));
			mapResponse = restTemplate.exchange(detail.getServerURL(), HttpMethod.GET, httpEntity, Map.class).getBody();
			/**
			 * res = restTemplate.exchange(detail.getServerURL(), HttpMethod.GET,
			 * httpEntity, Responce.class).getBody(); String status =
			 * res.getStatusMessage(); errorDescription = res.getStatusMessage(); errorCode
			 * = res.getStatusCode();
			 */
			return cache.strConfigVal(Constants.API_SUCCESS_MESSAGE, Constants.GLOBAL_TENANT);			
		}
		
		public boolean saveProcessError(){
			try {				
				ProvisioningRequestProcessingError obj = new ProvisioningRequestProcessingError();					
				obj.setRequestID(detail.getRequestID());				
				obj.setRequestDetailID(detail.getRequestDetailID());
				obj.setErrorFoundDate(new Date());
				obj.setStatus(11);
				obj.setErrorCode(errorCode);
				obj.setErrorDescription(errorDescription);	
				dao.saveProcessError(obj);
				return true;
			} catch (Exception e) {			
				e.printStackTrace();
				return false;
			}
		}

	}

	// SSSSSSS
	private class ProvisionSequential implements Callable<Boolean>  {

		private List<ProvisioningRequestDetails> detailList;
		ProvisionSequential(List<ProvisioningRequestDetails>  detailList,ProvisioningRequest provisioningRequest){
			this.detailList = detailList;
			this.tenantID = provisioningRequest.getTenantID().toString();
		}
		String tenantID = null;
		String responseStatus = "";
		int retries = 0;
		RestTemplate restTemplate = new RestTemplate();
		Gson gson = new Gson();
		Map mapResponse;
		String description = "";	
		String code = ""; 
		int maxRetries;
		TimeUnit time = TimeUnit.MILLISECONDS; 				
		int timeGap;
		long sleepTime;	
		public Boolean call() throws Exception { 
			
			int status = 0;
			String keyToFind;
			String key;
			ProvisioningRequestDetails dependentObj;	
		
					
			try {
				retries = 0;
				for (ProvisioningRequestDetails detail : detailList) {					
					String request = null;		
					key = detail.getGroup()+":"+detail.getSequenceID()+":"+detail.getSubSequenceID();
					keyToFind = detail.getGroup()+":"+detail.getDependentSequenceID()+":"+detail.getDependentSubSequenceID();
					dependentObj = map.get(keyToFind);
					if(dependentObj.getStatus() == cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)){												
						try {
							if((detail.getResponseUpdateCategory().equals(2)) && detail.getRequestObject().contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))){								
								request = replaceRequestParams( dependentObj, detail.getRequestObject());
								if(!request.contains(cache.strConfigVal(Constants.R_PIPE, this.tenantID))){
									detail.setRequestObject(request);
									dao.updateRequestObject(detail.getRequestDetailID(), request);
								}else{
									detail.setStatus(23);								
									map.put(key, detail);
									break;
								}		
							}
							retries = 0;
						    maxRetries = detail.getMaxRetries();
						    timeGap = detail.getTimeGapBetweenRetry();
							sleepTime = time.convert(timeGap,TimeUnit.SECONDS);	
							/**
							 * key =
							 * detail.getGroup()+":"+detail.getSequenceID()+":"+detail.getSubSequenceID();
							 */
							while(retries < maxRetries){
								retries++;
								responseStatus = sendRequest(detail); // call Network API									
								ProvisioningRequestResponseDetail obj = new ProvisioningRequestResponseDetail();
								obj.setRequestDetailID(detail.getRequestDetailID());
								obj.setRequestID(detail.getRequestID());
								obj.setVersion(detail.getVersion());	
								obj.setResponseObjectType("JSON");
								obj.setResponseReceivedDate(new Date());				
								/** jsonString = gson.toJson(res); */
								obj.setResponseObject(gson.toJson(mapResponse));	
								obj.setResponseSequenceNo(retries);
								if(responseStatus.equalsIgnoreCase("Success")){				
									status = cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT);								
								}else{
									if(retries == maxRetries){
										status = cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT);
									}
									else { 
										status = cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT);
									}
									obj.setRetrySubmitDate(new Date());
									obj.setErrorCode(code);
									obj.setErrorDescription(description);										
								}	
								obj.setStatus(status);					
								dao.saveProvisioningRequestResponseDetail(obj);	
								
								if(status == cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)){
									break;
								}
								Thread.sleep(sleepTime); 
							}				
							if(status == cache.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)){					
								dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),status,retries);					
							}else{	
								this.saveProcessError(detail);
								dao.updateProvisioningRequestDetailCompletion(detail.getRequestDetailID(),status,retries,code,description);					
							}	
							detail.setStatus(status);					
							detail.setResponse(mapResponse);
							map.put(key, detail);
						} catch (Exception e) {
							detail.setStatus(cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT));
							map.put(key, detail);
						}	
					
					}else{
						responseStatus = cache.strConfigVal(Constants.ERROR, Constants.GLOBAL_TENANT);
						detail.setStatus(cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT));
						map.put(key, detail);
						break;
					}
				}
			} catch (Exception e) {
				logger.error("Error in call method", e);
			}					
			return true;
		}
		String sendRequest(ProvisioningRequestDetails detail){
			
			HttpEntity<String> httpEntity = AuthenticationUtil.getHttpEntity(cache.strConfigVal(Constants.USERNAME, Constants.GLOBAL_TENANT),cache.strConfigVal(Constants.PASSWORD, Constants.GLOBAL_TENANT));		
			
			mapResponse = restTemplate.exchange(detail.getServerURL(), HttpMethod.GET, httpEntity, Map.class).getBody();
			/**
			 * String token = umsNetwork.generateSuperUserToken();
			 * res = restTemplate.exchange(detail.getServerURL(), HttpMethod.GET,
			 * httpEntity, Responce.class).getBody();
			 * 
			 * String status = res.getStatusMessage(); description = res.getStatusMessage();
			 * code = res.getStatusCode();
			 */
			return  cache.strConfigVal(Constants.API_SUCCESS_MESSAGE, Constants.GLOBAL_TENANT);			
		}

		public boolean saveProcessError(ProvisioningRequestDetails detail){
			try {				
				ProvisioningRequestProcessingError obj = new ProvisioningRequestProcessingError();					
				obj.setRequestID(detail.getRequestID());				
				/** obj.setVersionNo(detail.getVersion()); */
				obj.setRequestDetailID(detail.getRequestDetailID());
				obj.setErrorFoundDate(new Date());
				obj.setStatus(11);
				obj.setErrorCode(code);
				obj.setErrorDescription(description);	
				dao.saveProcessError(obj);
				return true;
			} catch (Exception e) {			
				e.printStackTrace();
				return false;
			}
		}

		/**String replaceResponseParams(ProvisioningRequestDetails dependentObj,String request){
			try {
				List parentJson = dependentObj.getResponse();
				String str = gson.toJson(parentJson);
				str = str.replace("[", "");				
				str = str.replace("]", "");
				Map map =  gson.fromJson(str, Map.class);	
				Iterator it = map.entrySet().iterator();
				String key;				
				while (it.hasNext()) {										
					Map.Entry entry7 = (Map.Entry)it.next(); 									   
					key = "r|"+entry7.getKey().toString();	
					logger.info(key + " "+entry7.getValue());
					if(request.contains(key)){
						request = request.replace(key, entry7.getValue().toString());
					}
				}	
				return request;
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return null;
			}

		}*/
		String toReplace ;
		String replaceRequestParams(ProvisioningRequestDetails dependentObj,String request){
			try {
				Map parentJson = dependentObj.getResponse();
				/**
				 * String str = gson.toJson(parentJson); str = str.replace("[", ""); str =
				 * str.replace("]", ""); Map map = gson.fromJson(str, Map.class);
				 */
				Iterator it = parentJson.entrySet().iterator();
				String key;				
				while (it.hasNext()) {										
					Map.Entry entry7 = (Map.Entry)it.next(); 									   
					key = entry7.getKey().toString();	
					if(key.contains(cache.strConfigVal(Constants.SUB_REQ_PARAM, this.tenantID))){
						key = key.replace(cache.strConfigVal(Constants.SUB_REQ_PARAM, this.tenantID), "");
						if(request.contains(key)){
							toReplace = cache.strConfigVal(Constants.R_PIPE, this.tenantID)+key;
							logger.info("{}{} torepalce {}",key ,entry7.getValue(), toReplace);
							request = request.replace(toReplace, entry7.getValue().toString());
						}
					}
					if(key.contains(cache.strConfigVal(Constants.BOTH, this.tenantID))){
						key = key.replace(cache.strConfigVal(Constants.BOTH, this.tenantID), "");
						if(request.contains(key)){
							toReplace = cache.strConfigVal(Constants.R_PIPE, this.tenantID)+key;
							logger.info("{}{} torepalce {} ",key , entry7.getValue() , toReplace);
							request = request.replace(toReplace, entry7.getValue().toString());
						}
					}
				}	
				return request;
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return null;
			}

		}


	}

	/**public String getCurrentHourMinutes(){
			DateFormat dateFormat = new SimpleDateFormat("HH:mm");
			String formattedDate=dateFormat. format(new Date());	    
			return formattedDate;    
		}
		public String getCurrentMinutes(){
			DateFormat dateFormat = new SimpleDateFormat("mm");
			String formattedDate=dateFormat. format(new Date());	    
			return formattedDate;    
		}
		public String getCurrentHour(){
			DateFormat dateFormat = new SimpleDateFormat("HH");
			String formattedDate=dateFormat. format(new Date());	    
			return formattedDate;   	
		}*/
	
	
}
