package com.knot.provisioning.serviceimpl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.knot.provioning.model.ActivityConfig;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.bo.ProvisioningRequestBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsNsoBO;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dao.ProvisioningRequestDao;
import com.knot.provisioning.dto.ActivityConfigDto;
import com.knot.provisioning.dto.LifeCycleDto;
import com.knot.provisioning.dto.MsisdnDTO;
import com.knot.provisioning.dto.MsisdnDtoResponse;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.network.RefMasterNetwork;
import com.knot.provisioning.network.UmsNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisionErrorRequestService;
import com.knot.provisioning.service.ProvisionService;
import com.knot.provisioning.service.ProvisioningRequestService;
import com.knot.provisioning.service.ProvisioningService;
import com.knot.provisioning.soapclient.SoapClient;
import com.knot.provisioning.soapclient.SoapConnector;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.DateUtill;
import com.knot.provisioning.utill.MdcAwareRunnable;
import com.knot.provisioning.utill.ProvisionException;
import com.knot.redis.dto.TenantsDTO;

/**
 *
 * @author sai D
 *
 */
@Service
public class ProvisioningServiceImpl implements ProvisioningService {

	private static final Logger log = LogManager.getLogger(ProvisioningServiceImpl.class);

	ProvisioningDao dao;
	private OMSNetwork omsNet;
	private RefMasterNetwork defNet;
	private RestTemplate restTemplate;
	private SoapConnector connector;
	private SoapClient clientConnector;
	private CacheService cacheService;
	private UmsNetwork umsNet;
	private Gson gson;
	private ProvisioningDao provisioningDao;
	ProvisioningRequestDao provisioningRequestDao;
	private ProvisioningRequestService provisioningRequestService;
	
	
	@Autowired
	public void setProvisioningRequestService(ProvisioningRequestService provisioningRequestService) {
		this.provisioningRequestService = provisioningRequestService;
	}

	@Autowired
	public void setUmsNet(UmsNetwork umsNet) {
		this.umsNet = umsNet;
	}

	@Autowired
	public void setDefNet(RefMasterNetwork defNet) {
		this.defNet = defNet;
	}

	@Autowired
	public void setDao(ProvisioningDao dao) {
		this.dao = dao;
	}

	@Autowired
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Autowired
	public void setOmsNet(OMSNetwork omsNet) {
		this.omsNet = omsNet;
	}

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	public void setConnector(SoapConnector connector) {
		this.connector = connector;
	}

	@Autowired
	public void setClientConnector(SoapClient clientConnector) {
		this.clientConnector = clientConnector;
	}

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setProvisioningDao(ProvisioningDao provisioningDao) {
		this.provisioningDao = provisioningDao;
	}
	
	@Autowired
	public void setProvisioningRequestDao(ProvisioningRequestDao provisioningRequestDao) {
		this.provisioningRequestDao = provisioningRequestDao;
	}

	public void processProvisioningRequest() throws Exception {
		log.info(
				"Entered  ProvisioningServiceImpl looking for Provisioning requests to process :: processProvisioningRequests() ");
		String isGraceFullStopNeeded = cacheService.configValByCodeForGraceFullStop(Constants.PRO_PROCESS_STOP, Constants.GLOBAL_TENANT);
		if(StringUtils.hasText(isGraceFullStopNeeded)  && isGraceFullStopNeeded.equalsIgnoreCase("Y")){
			log.info("Application has been configured to stop processing.");
		}else {
		List<TenantsDTO> allTenants = defNet.getAllTenants();
		int size = allTenants.isEmpty() ? 1 : allTenants.size();
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(size);
		try {
			for (TenantsDTO dto : allTenants) {
				String tenantID = dto.getTenantID();
				ProcessProvisioningPraentThread praentThread = new ProcessProvisioningPraentThread(tenantID);
				executor.scheduleAtFixedRate(new MdcAwareRunnable(praentThread), 0, 20, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			log.error("Error Occured to ", e);
		 }
		}	
	}

	private class ProcessProvisioningPraentThread implements Runnable {
		String tenantId;
		String noOfMaxRecords;
		ExecutorService executor = null;

		public ProcessProvisioningPraentThread(String tenantId) throws Exception {
			super();
			this.tenantId = tenantId;
			executor = Executors
					.newFixedThreadPool(cacheService.intConfigVal(Constants.THREADSIZE, Constants.GLOBAL_TENANT));
		}

		@Override
		public void run() {
			try {
				log.info("ProcessProvisioningParentThread run method started. with Thread Name: {}, Thread ID: {}",Thread.currentThread().getName(), Thread.currentThread().getId());
				noOfMaxRecords = cacheService.strConfigVal(Constants.MAX_TENANTS, tenantId);
				noOfMaxRecords = noOfMaxRecords.equalsIgnoreCase("NA") || noOfMaxRecords.equalsIgnoreCase("-1")  ? "3" : noOfMaxRecords;
				log.info("noOfMaxRecords {}",noOfMaxRecords);
				/** For T2 Tenant we have update this logic when multple thread sceanrio processing multiple records**/
				/**List<ProvisioningRequest> provRequest = dao.getProvisioningRequests(tenantId, noOfMaxRecords);**/
				List<ProvisioningRequest> provRequest = provisioningRequestService.getProvisioningRequests(tenantId, noOfMaxRecords);
				if (provRequest != null && !provRequest.isEmpty()) {
					log.info("requests founded for tenant {} Processing the provisioning requests !!! ", tenantId);
					for (ProvisioningRequest provisioningRequest : provRequest) {
						log.info("ProcessProvisioningParentThread run method started. Thread Name: {}, Thread ID: {}",Thread.currentThread().getName(), Thread.currentThread().getId());
						log.info("ProvisioningRequest JSON {}",new JSONObject(provisioningRequest));
						ProvisionService obj = new ProvisionServiceImpl();
						obj.processProvisioningRequest(provisioningRequest, dao, executor, cacheService, omsNet,
								restTemplate, connector, clientConnector, umsNet, defNet);
					}
				} else {
					log.info("No requests  to be  processed For  Tenant  {}", tenantId);
				}
				log.info("ProcessProvisioningParentThread run method Ended. with Thread Name: {}, Thread ID: {}",Thread.currentThread().getName(), Thread.currentThread().getId());
			} catch (Exception e) {
				log.info("Failed to start the run() method :: ");
			}
		}

	}

	public boolean processProvisioningErrorRequests() throws Exception {

		log.info("In ProvisioningServiceImpl :: processProvisioningErrorRequests ");

		ExecutorService executor = Executors
				.newFixedThreadPool(cacheService.intConfigVal(Constants.ERROR_MAX_THREADS, Constants.GLOBAL_TENANT));

		List<ProvisioningRequest> provRequest = dao.getProvisioningErrorRequests();

		for (ProvisioningRequest provisioningRequest : provRequest) {
			try {
				ProvisionErrorRequestService obj = new ProvisionServiceErrorRequestImpl();
				obj.processProvisioningErrorRequest(provisioningRequest, dao, executor);
			} catch (Exception e) {
				log.error("{}{} ", provisioningRequest.getRequestID(), e);
			}
		}
		try {
			log.info("Waiting for the Threads Executor to terminate !!!");
			executor.shutdown();
			if (executor.awaitTermination(cacheService.intConfigVal(Constants.WAIT_FOR, Constants.GLOBAL_TENANT),
					TimeUnit.MINUTES)) {
				executor.shutdownNow();
			}
		} catch (Exception e) {

			log.error("ProvisioningServiceImpl :: processProvisioningErrorRequests ", e);
			throw e;
		}
		log.info("Done cleaning");
		return true;
	}

	@Override
	public Boolean getProvisioningRequestErrors(Integer tenantId) {
		log.info(" Started ProvisioningServiceImpl :: getProvisioningRequestErrors ");
		try {
			Boolean provisioningErrorRequests = dao.getProvisioningErrorRequests(tenantId);
			if (provisioningErrorRequests == true) {
				return true;
			}
		} catch (Exception e) {
			log.error("ProvisioningServiceImpl :: getProvisioningRequestErrors ", e);
		}
		log.info(" Ended ProvisioningServiceImpl :: getProvisioningRequestErrors ");
		return false;

	} 
	

	@Override
	public boolean updateLifeCycleStatus(LifeCycleDto dto) throws Exception {
		log.info("Started ProvisioningServiceImpl :: updateLifeCycleStatus ExternalDto {}",gson.toJson(dto));	
		ProvisioningRequestResponseDetail responseObj = new ProvisioningRequestResponseDetail();
		boolean saveProvisioningReqResponse;
		boolean responseFlag = true;
		try {
			ProvisioningRequestBO reqObj = new ProvisioningRequestBO();
			reqObj = provisioningRequestDao.getWorkOrderByPassingRequDtlId(dto.getReqDtlID(),null);
			if(dto.getStatusCode().equalsIgnoreCase(cacheService.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT))) {
				provisioningDao.updateProvReqDtlCompletionStatusByRespId(dto.getReqDtlID(),String.valueOf(cacheService.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT)),null, dto.getRequestCompletionTime());
				List<ProvisioningRequestDetailsNsoBO> checkReqObjIsDependentOrNot = provisioningDao.getRequestIdsByReqDtlId(dto.getReqDtlID(), null);
				if(!CollectionUtils.isEmpty(checkReqObjIsDependentOrNot)) {
					provisioningDao.updateProvisioningRequestStatusByDetailId(new BigInteger(dto.getReqDtlID()), cacheService.intConfigVal(Constants.REQ_GEN_SUCCESS, Constants.GLOBAL_TENANT), dto.getRequestCompletionTime());
					omsNet.updateWorkOrderStatus(reqObj.getWorkOrderID(), cacheService.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT), "", reqObj.getTenantID().toString());
				}else {
					provisioningDao.updateProvisioningRequestStatusByDetailId(
							new BigInteger(dto.getReqDtlID()), cacheService.intConfigVal(Constants.PROV_REQ_DTL_PROVISION_SUCCESS, Constants.GLOBAL_TENANT), dto.getRequestCompletionTime());
					omsNet.updateWorkOrderStatus(reqObj.getWorkOrderID(), Constants.WORK_ODR_SUCESS, "", reqObj.getTenantID().toString());
				}
			}else {
				provisioningDao.updateProvReqDtlCompletionStatusByRespId(dto.getReqDtlID(),String.valueOf(cacheService.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT)),null, dto.getRequestCompletionTime());
				provisioningDao.updateProvisioningRequestStatusByDetailId(new BigInteger(dto.getReqDtlID()), cacheService.intConfigVal(Constants.PROV_REQ_DTL_ERROR, Constants.GLOBAL_TENANT), dto.getRequestCompletionTime());
				omsNet.updateWorkOrderStatus(reqObj.getWorkOrderID(), cacheService.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT), "", reqObj.getTenantID().toString());
				responseFlag = false;
			}
			 saveProvisioningReqResponse = saveProvisioningReqResponse(dto, responseObj, null, reqObj, null);
			if(saveProvisioningReqResponse) {
				log.info("Provisioning Request Response Objects saved SuccuessFully with requestDtlID {}",dto.getReqDtlID());
			}else {
				log.info("Provisioning Request Response Objects unable to save ",dto.getReqDtlID());
			}
			log.info("Ended ProvisioningServiceImpl :: updateLifeCycleStatus ExternalDto {}",gson.toJson(dto));
		} catch (ProvisionException e) {
			log.error("An Exception ExternalServiceImpl activeFirstResource ", e);
			responseFlag = false;
			throw e;
		}
		return responseFlag;
	}
	
	
	@Override
	public boolean activeFirstResource(MsisdnDTO dto) throws Exception  {
		log.info("Started ProvisioningServiceImpl :: activeFirstResource ExternalDto {}", gson.toJson(dto));
		boolean responseFlag = true;
		ProvisioningRequestResponseDetail responseObj = new ProvisioningRequestResponseDetail();
		/** dto.setFlag("Y"); */
		try {

			if (StringUtils.hasText(dto.getSource()) && dto.getSource().equalsIgnoreCase("OCS")) {
				MsisdnDtoResponse omsResp = omsNet.getWorkOrderByMsisdn(dto);
				if (!omsResp.getStatusCode().equalsIgnoreCase("200")) {
					log.info("An error occured on oms network getWorkOrderByMsisdn Response From Oms Network :: "
							+ omsResp.getStatusMessage());
					throw new ProvisionException(omsResp.getStatusMessage());
				} else {
					try {
						ProvisioningRequestDetailsBO reqDtlsBo = dao.getRequestIdRequestDtlIdByPassingWorkOrderId(
								Integer.parseInt(omsResp.getWorkOrderID())).get(0);
						if (reqDtlsBo != null) {
							boolean saveProvisioningReqResponse = saveProvisioningReqResponse(null, responseObj, reqDtlsBo, null,dto);
							if (saveProvisioningReqResponse) {
								log.info("Provisioning Request Response saved SuccuessFully with requestDtlID {}",
										reqDtlsBo.getRequestDetailID());
								dao.updateProvisioningNetwork(null, omsResp.getWorkOrderID(),
										dto.getRequestCompletionTime());
								Integer workOrderInProgress = cacheService.intConfigVal(Constants.WO_PROV_IN_STATUS,
										Constants.GLOBAL_TENANT);
								omsNet.updateWorkOrderStatus(new BigInteger(omsResp.getWorkOrderID()),
										workOrderInProgress, "", omsResp.getTenantID());
							}
						} else {
							throw new ProvisionException("Unable to find provisioning requests for workOrderID: "
									+ omsResp.getWorkOrderID());

						}
					} catch (Exception e) {
						log.error("Exception Occured in Oms unable to update WorkOrderIn OMS Component", e);
					}
				}
			} else {
				String omsResponse = omsNet.updateActiveFirst(dto);
				if (omsResponse.contains("200")) {
					MsisdnDtoResponse omsResp = omsNet.getWorkOrderByMsisdn(dto);
					if (omsResp.getStatusCode().equalsIgnoreCase("200")) {
						Integer workOrderInProgress = cacheService.intConfigVal(Constants.WO_PROV_IN_STATUS,
								Constants.GLOBAL_TENANT);
						omsNet.updateWorkOrderStatus(new BigInteger(omsResp.getWorkOrderID()), workOrderInProgress,
								"", omsResp.getTenantID());
						log.info("ExternalServiceImpl :: updateActiveFirst omsResponse {}", omsResponse);
					} else {
						log.info("An error occured on oms network getWorkOrderByMsisdn Response From Oms Network :: "
								+ omsResp.getStatusMessage());
						throw new ProvisionException(omsResp.getStatusMessage());
					}
				} else {
					log.info("An error occured on oms network getWorkOrderByMsisdn Response From Oms Network :: "
							+ omsResponse);
					throw new ProvisionException("unable to update the status Response from oms {}" + omsResponse);
				}
			}

			log.info("Ended ExternalServiceImpl :: activeFirstResource ExternalDto {}");
		} catch (Exception e) {
			log.error("An Exception ExternalServiceImpl activeFirstResource ", e);
			responseFlag = false;
			throw e;
		}
		log.info("Ended ExternalServiceImpl :: activeFirstResource ");
		return responseFlag;
	}

	public boolean saveProvisioningReqResponse(LifeCycleDto dto, ProvisioningRequestResponseDetail responseObj,
			ProvisioningRequestDetailsBO reqDtlsBo, ProvisioningRequestBO reqObj, MsisdnDTO msisdnDto) throws JsonProcessingException {
		responseObj.setRequestDetailID(dto !=null ? new BigInteger(dto.getReqDtlID()) : reqDtlsBo.getRequestDetailID());
		responseObj.setRequestID(reqObj != null ? reqObj.getRequestID() : reqDtlsBo.getRequestID());
		responseObj.setStatus(Constants.PROCESOOR_SUCESS);
		responseObj.setResponseReceivedDate(new Date());
		responseObj.setResponseObjectType("JSON");
		responseObj.setResponseSequenceNo(1);
		responseObj.setVersion(2.0f);
		if(dto !=null)
		responseObj.setResponseObject(new ObjectMapper().writeValueAsString(dto));
		else
			responseObj.setResponseObject(new ObjectMapper().writeValueAsString(msisdnDto));
		return dao.saveProvisioningRequestResponseDetail(responseObj);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveProvActivityConfig(List<ActivityConfigDto> activityConfig) throws Exception {
		log.info("Started ProvisioningServiceImpl :: saveProvActivityConfig");
		if (activityConfig == null || activityConfig.isEmpty()) {
			throw new ProvisionException("Activity configuration list cannot be empty");
		}
		try {
			for (ActivityConfigDto dto : activityConfig) {
				if (dto == null) {
					throw new ProvisionException("Activity configuration cannot be null");
				}
				String activityCode = dto.getActivityCode();
				String configId = dto.getConfigId();
				if (activityCode == null || activityCode.trim().isEmpty()) {
					throw new ProvisionException("Activity code cannot be empty");
				}
				ActivityConfig config = new ActivityConfig(dto);
				if (configId == null || configId.trim().isEmpty()) {
					/**
					 * New configuration. Reject duplicate active activity codes.
					 */
					if (dao.existsActivityCode(activityCode.trim(), null)) {
						throw new ProvisionException("Activity code already exists: " + activityCode);
					}
					config.setEffectiveTo(DateUtill.getDefaultEndDateTime());
					boolean saved = dao.saveProvActivityConfig(config);
					if (!saved) {
						throw new ProvisionException("Failed to save activity configuration: " + activityCode);
					}
				} else {
					/** Modify existing configuration. */
					ActivityConfig oldConfig = dao.getActivityConfigByConfigId(configId);

					if (oldConfig == null) {
						throw new ProvisionException("Activity configuration not found for configId: " + configId);
					}
					/** active configuration. */
					if (dao.existsActivityCode(activityCode.trim(), configId)) {
						throw new ProvisionException("Activity code already exists: " + activityCode);
					}
					LocalDateTime now = LocalDateTime.now();
					/** Close the old version. */
					oldConfig.setEffectiveTo(now.minusSeconds(1));
					oldConfig.setModifiedOn(now);
					boolean oldRecordUpdated = dao.saveProvActivityConfig(oldConfig);
					if (!oldRecordUpdated) {
						throw new ProvisionException("Failed to close the existing activity configuration");
					}
					/** A new primary key is required for a separate row. */
					config.setConfigId(null);
					config.setEffectiveFrom(now);
					config.setEffectiveTo(DateUtill.getDefaultEndDateTime());
					config.setModifiedOn(now);
					/**  Insert the new version. */
					boolean saved = dao.saveProvActivityConfig(config);
					if (!saved) {
						throw new ProvisionException(
								"Failed to insert new activity configuration version: " + activityCode);
					}
				}
			}
			log.info("Ended ProvisioningServiceImpl :: saveProvActivityConfig");
			return true;
		} catch (Exception e) {
			log.error("Exception occurred in ProvisioningServiceImpl :: saveProvActivityConfig", e);
			throw e;
		}
	}

	@Override
	public List<ActivityConfig> getAllActvityConfigs(ActivityConfigDto dto) throws Exception {
		log.info("Started ProvisioningServiceImpl :: getAllActvityConfigs() with dto {}", gson.toJson(dto));
		List<ActivityConfig> allActvityConfigs = new ArrayList<>();
		try {
			allActvityConfigs = dao.getAllActvityConfigs(dto);
			log.info("Ended ProvisioningServiceImpl :: getAllActvityConfigs() ");
		} catch (Exception e) {
			log.error("An exception Ocuured in ProvisioningServiceImpl :: getAllActvityConfigs {} Unable to find the requests ",e);
			throw e;
		}
		return allActvityConfigs;
	}

	
	


}
