package com.knot.provisioning.serviceimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.knot.provioning.model.ProvisioningReqDetailsModel;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.bo.ProvReqResponseDetails;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsNsoBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dao.ProvisioningRequestDao;
import com.knot.provisioning.dto.AysncRequestDto;
import com.knot.provisioning.dto.ChartDataSummaryDTO;
import com.knot.provisioning.dto.DashBoardSummaryDTO;
import com.knot.provisioning.dto.DocumentDto;
import com.knot.provisioning.dto.NSOServiceDto;
import com.knot.provisioning.dto.ProvisioningReqDTO;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;
import com.knot.provisioning.dto.RatePlanDTO;
import com.knot.provisioning.dto.ServiceActDto;
import com.knot.provisioning.dto.ServiceDTO;
import com.knot.provisioning.dto.TmfAysncRequestDto;
import com.knot.provisioning.dto.WFMDto;
import com.knot.provisioning.network.CatNetwork;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisioningRequestService;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.FileUtil;
import com.knot.provisioning.utill.ProvisionException;

/**
 * @author sai D
 *
 */

@Service
public class ProvisioningRequestServiceImpl implements ProvisioningRequestService {

	private static final Logger logger = LogManager.getLogger(ProvisioningRequestServiceImpl.class);

	Gson gson;
	ProvisioningRequestDao provisioningRequestDao;
	ProvisioningDao provisioningDao;
	CacheService cache;
	CatNetwork catalogueService;
	OMSNetwork omsNet;

	@Autowired
	public void setOmsNet(OMSNetwork omsNet) {
		this.omsNet = omsNet;
	}

	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setProvisioningRequestDao(ProvisioningRequestDao provisioningRequestDao) {
		this.provisioningRequestDao = provisioningRequestDao;
	}

	@Autowired
	public void setCatalogueService(CatNetwork catalogueService) {
		this.catalogueService = catalogueService;
	}

	@Autowired
	public void setProvisioningDao(ProvisioningDao provisioningDao) {
		this.provisioningDao = provisioningDao;
	}

	public List<ProvisioningRequestsBO> searchProvisioningRequest(ProvisioningReqSearchDTO request) throws Exception {
		List<ProvisioningRequestsBO> requestList = new ArrayList<>();
		try {
			requestList = provisioningRequestDao.searchProvisioningRequest(request);

		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: seachProvisioningRequest() ", e);
			throw e;
		}
		return requestList;
	}

	public List<ProvisioningReqDetailsBO> getProvisioningReqDetails(String requestID) {
		List<ProvisioningReqDetailsBO> requestDetailsList = new ArrayList<>();
		try {
			requestDetailsList = provisioningRequestDao.getProvisioningReqDetails(requestID);
			if (requestDetailsList != null && !requestDetailsList.isEmpty()) {
				List<ServiceDTO> serviceList = catalogueService.getServices(requestDetailsList.get(0).getTenantId(),
						requestDetailsList.get(0).getPartnerId());
				requestDetailsList.forEach(request -> {
					ServiceDTO service = serviceList.stream()
							.filter(s -> s.getServiceID().equalsIgnoreCase(request.getServiceID())).findFirst()
							.orElse(null);
					if (service != null) {
						request.setServiceName(service.getServiceName());
						request.setServiceCode(service.getServiceCode());
					}

				});

			}

		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: getProvisioningReqDetails() ", e);
			throw e;
		}
		return requestDetailsList;
	}

	@Transactional
	public boolean updateProvisioningRequest(ProvisioningReqDTO request) {
		boolean result = true;
		BigInteger seqNo;
		String sequenceNo;
		try {
			ProvisioningReqDetailsModel detailModel = provisioningRequestDao.getActiveProvisioningReqDetails(request);
			if (detailModel != null) {
				detailModel.setVersionDate(new Date());
				detailModel.setVersionNO(detailModel.getVersionNO() + 1);
				detailModel.setStatus(11);

				if (FileUtil.checkEmptyNull(request.getServerURL())) {
					detailModel.setServerURL(request.getServerURL());
				}
				if (FileUtil.checkEmptyNull(request.getTokenID())) {
					detailModel.setTokenID(request.getTokenID());
				}
				if (FileUtil.checkEmptyNull(request.getRequestObject())) {
					detailModel.setRequestObj(request.getRequestObject());
				}
				sequenceNo = provisioningDao. getReqDtlSeqNumber(cache.strConfigVal(Constants.PROV_REQ_DTL_SEQ_NAME, Constants.GLOBAL_TENANT));
				seqNo = new BigInteger(sequenceNo);
				detailModel.setRequesDetailID(seqNo);
				provisioningRequestDao.saveProvisioningReqDetails(detailModel);
				provisioningRequestDao.updateProvisioningReqDetails(request);
				provisioningRequestDao.updateProvisioningRequest(request);
			} else {
				result = false;
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: updateProvisioningRequest() ", e);
		}
		return result;
	}

	public List<ProvisioningDashBoadBO> getDashBoardDetails(ProvisioningReqSearchDTO request) throws Exception {
		List<ProvisioningDashBoadBO> requestList = new ArrayList<>();
		try {
			requestList = provisioningRequestDao.getDashBoardDetails(request);
			if (!CollectionUtils.isEmpty(requestList) && requestList.get(0) == null) {
				requestList = new ArrayList<>();
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: getDashBoardDetails() ", e);
			throw e;
		}
		return requestList;
	}

	@Override
	public List<DashBoardSummaryDTO> getHowLongRequest(ProvisioningReqSearchDTO request) {
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		try {
			logger.info("Started ProvisioningRequestServiceImpl :: getHowLongRequest() ");
			List<Object[]> list = provisioningRequestDao.getHowLOngRequests(request);
			if (!CollectionUtils.isEmpty(list)) {
				DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
				List<String> labelList = new ArrayList<>();
				String chartType = "line";
				for (Object[] data : list) {
					if (data[0].toString().equalsIgnoreCase(Constants.LABEL)) {
						obj.setChartType(chartType);
						obj.setTitle(request.getChartName());
						labelList.addAll(Arrays.asList(data[1].toString(), data[2].toString(), data[3].toString(),
								data[4].toString(), data[5].toString()));
						obj.setRootLabel(labelList);
					} else if (data[0].toString().equalsIgnoreCase(Constants.DATA)) {
						List<String> dataList = new ArrayList<>();
						ChartDataSummaryDTO obj1 = new ChartDataSummaryDTO();
						dataList.addAll(Arrays.asList(data[1].toString(), data[2].toString(), data[3].toString(),
								data[4].toString(), data[5].toString()));
						obj1.setData(dataList);
						obj1.setLabel(labelList.get(0));
						obj1.setType(chartType);
						chartDataSets.add(obj1);
					}
				}
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
			} else {
				return resTypeList;
			}
		} catch (Exception e) {
			logger.error("Started ProvisioningRequestServiceImpl :: getHowLongRequest() {}", e.getMessage());
		}
		return resTypeList;
	}

	@Override
	public List<DashBoardSummaryDTO> requestSource(ProvisioningReqSearchDTO request) {
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();

		try {
			logger.info("ProvisioningRequestServiceImpl :: requestSource()");
			List<Object[]> list = provisioningRequestDao.requestSource(request);
			if (!CollectionUtils.isEmpty(list)) {
				DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
				List<String> labelList = new ArrayList<>();
				List<String> dataList = new ArrayList<>();
				ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
				obj.setChartType("bar");
				obj.setTitle(request.getChartName());
				obj.setRootLabel(labelList);
				for (Object[] data : list) {
					labelList.add(data[0].toString());
					dataList.add(String.valueOf(data[1]));
				}
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);

			} else {
				return resTypeList;
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: requestSource() {} ", e.getMessage());
		}

		return resTypeList;

	}

	@Override
	public List<ProvisioningRequestsBO> getViewHowLongRequest(ProvisioningReqSearchDTO dto) {
		List<ProvisioningRequestsBO> list = new ArrayList<>();

		try {
			logger.info("Started ProvisioningRequestServiceImpl :: getViewHowLongRequest() {} ");
			list = provisioningRequestDao.getViewHowLongRequest(dto);
			logger.info("Ended ProvisioningRequestServiceImpl :: getViewHowLongRequest() {} ");
		} catch (Exception e) {
			logger.error("ProvisioningRequestServiceImpl :: requestSource() {} ", e);
		}
		return list;
	}

	@Override
	public List<DashBoardSummaryDTO> getRequestByLobs(ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: getRequestByLobs() {} ");
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		try {
			List<Object[]> list = provisioningRequestDao.getRequestByLobs(dto);
			if (!CollectionUtils.isEmpty(list)) {
				DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
				List<String> labelList = new ArrayList<>();
				List<String> dataList = new ArrayList<>();
				ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
				obj.setChartType("bar");
				obj.setTitle(dto.getChartName());
				obj.setRootLabel(labelList);
				for (Object[] data : list) {
					labelList.add(String.valueOf(data[0]));
					dataList.add(String.valueOf(data[1]));
				}
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
			} else {
				return resTypeList;
			}
			logger.info("Ended ProvisioningRequestServiceImpl :: getRequestByLobs() {} ");
		} catch (Exception e) {
			logger.error("Exception Occured in ProvisioningRequestServiceImpl :: getRequestByLobs() {} ", e);
		}

		return resTypeList;
	}

	@Override
	public List<DashBoardSummaryDTO> getRequestByServers(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestServiceImpl :: getRequestByServers() {} ");
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		try {
			List<Object[]> list = provisioningRequestDao.getRequestsByServers(request);
			if (!CollectionUtils.isEmpty(list)) {
				DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
				List<String> labelList = new ArrayList<>();
				List<String> dataList = new ArrayList<>();
				ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
				obj.setChartType("bar");
				obj.setTitle("Server wise Requests");
				obj.setRootLabel(labelList);
				for (Object[] data : list) {
					labelList.add(String.valueOf(data[0]));
					dataList.add(String.valueOf(data[1]));
				}
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
			} else {
				return resTypeList;
			}
			logger.info("Ended ProvisioningRequestServiceImpl :: getRequestByServers() {} ");
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestServiceImpl :: getRequestByServers() {}", e);
		}
		return resTypeList;
	}

	@Override
	public List<DashBoardSummaryDTO> getRequestByServices(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestServiceImpl :: getRequestByServices() {} ");
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		try {
			List<Object[]> list = provisioningRequestDao.getRequestByServices(request);
			if (!CollectionUtils.isEmpty(list)) {
				DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
				List<String> labelList = new ArrayList<>();
				List<String> dataList = new ArrayList<>();
				ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
				obj.setChartType("pie");
				obj.setTitle(request.getChartName());
				obj.setRootLabel(labelList);
				for (Object[] data : list) {
					labelList.add(String.valueOf(data[0]));
					dataList.add(String.valueOf(data[1]));
				}
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
				logger.info("Ended ProvisioningRequestServiceImpl :: getRequestByServices() {} ");
			} else {
				return resTypeList;
			}
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestServiceImpl :: getRequestByServices() {}", e);
		}
		return resTypeList;
	}

	@Override
	public List<DashBoardSummaryDTO> getRatePlanIds(ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: getRatePlanIds() ");
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<RatePlanDTO> ratePlanTypeList = new ArrayList<>();
		try {
			List<Long> ratePlanIds = provisioningRequestDao.getRatePlanIds(dto);
			ratePlanTypeList = catalogueService.getRatePlanTypes(ratePlanIds);
			Map<String, String> responseMap = new HashMap<>();
			Map<String, Integer> countMap = new HashMap<>();

			countMap = getCountMap(ratePlanIds, ratePlanTypeList, responseMap);
			DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
			ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
			List<String> labelList = new ArrayList<>();
			List<String> dataList = new ArrayList<>();
			if (!CollectionUtils.isEmpty(countMap)) {
				for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
					String planType = entry.getKey();
					int count = entry.getValue();
					labelList.add(planType);
					dataList.add(String.valueOf(count));
					logger.info(planType + " : " + count);
				}

				obj.setChartType("bar");
				obj.setRootLabel(labelList);
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
			}

		} catch (Exception e) {
			logger.error("An Exception Occured in  ProvisioningRequestServiceImpl :: getRatePlanIds() ", e);
		}

		return resTypeList;

	}

	private Map<String, Integer> getCountMap(List<Long> ratePlanIds, List<RatePlanDTO> ratePlanTypeList,
			Map<String, String> responseMap) {
		Map<String, Integer> countMap = new HashMap<>();
		String planType = "";
		for (RatePlanDTO ratePlanDTO : ratePlanTypeList) {
			responseMap.put(ratePlanDTO.getRatePlanID(), ratePlanDTO.getRatePlantype());
		}
		for (Long ratePlanId : ratePlanIds) {

			if (responseMap.containsKey(String.valueOf(ratePlanId))) {
				planType = responseMap.get(String.valueOf(ratePlanId));
				if (countMap.containsKey(planType)) {
					countMap.put(planType, countMap.get(planType) + 1);
				} else {
					countMap.put(planType, 1);
				}
			}

			else {
				logger.info("This " + ratePlanId + " rate PlanId not EXIT in catlouge: ");
			}
		}
		return countMap;
	}

	@Override
	public List<DashBoardSummaryDTO> getRequestCount(ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: getRequestCount() {} ");
		List<DashBoardSummaryDTO> resTypeList = new ArrayList<>();
		List<ChartDataSummaryDTO> chartDataSets = new ArrayList<>();
		try {
			List<String> labelList = new ArrayList<>();
			List<String> dataList = new ArrayList<>();
			DashBoardSummaryDTO obj = new DashBoardSummaryDTO();
			List<ProvisioningDashBoadBO> dashBoardDetails = provisioningRequestDao.getDashBoardDetails(dto);
			if (!CollectionUtils.isEmpty(dashBoardDetails)) {
				ChartDataSummaryDTO chart = new ChartDataSummaryDTO();
				ProvisioningDashBoadBO requestObj = dashBoardDetails.get(0);
				List<String> totalRequests = Arrays.asList("Total", "Sucess", "InProgress", "Fail");
				List<String> totalValues = Arrays.asList(requestObj.getTotal(), requestObj.getSuccess(),
						requestObj.getInProgress(), requestObj.getFailed());
				for (String request : totalRequests) {
					labelList.add(request);
				}
				for (String value : totalValues) {
					dataList.add(value);
				}
				obj.setChartType("bar");
				obj.setRootLabel(labelList);
				chart.setData(dataList);
				chartDataSets.add(chart);
				obj.setChartDataSets(chartDataSets);
				resTypeList.add(obj);
			} else {
				return resTypeList;
			}
			logger.info("Ended ProvisioningRequestServiceImpl :: getRequestCount() {} ");

		} catch (Exception e) {
			logger.error("An Exception Occcured in ProvisioningRequestServiceImpl :: getRequestCount() :: {}", e);
		}

		return resTypeList;
	}

	@Override
	public boolean updateServiceActivationStatus(NSOServiceDto dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: updateServiceActivationStatus() {} ");
		boolean flag = false;
		try {
			if (dto != null && dto.getStatus().contains("200")) {
				ObjectMapper mapper = new ObjectMapper();
				String jsonString = mapper.writeValueAsString(dto);
				ProvisioningRequestResponseDetail responseObj = new ProvisioningRequestResponseDetail();
				responseObj.setResponseObject(jsonString);
				responseObj.setStatus(73);
				responseObj.setResponseSequenceNo(0);
				List<ProvisioningRequestDetailsNsoBO> list = provisioningDao
						.getProvisioningRequestDetalisToWfm(new BigInteger(dto.getServiceOrderId()), 71);

				for (ProvisioningRequestDetailsNsoBO obj : list) {
					provisioningDao.updateProvisioningRequest(obj.getRequestID(), 11,
							"NSO Service Activated Waiting For WFM", null);
					provisioningDao.updateProvisioningRequestDetailCompletion(obj.getRequestDetailID(), 73, 0);
					flag = omsNet.updateWorkOrderStatus(
					        new BigInteger(dto.getServiceOrderId()),
					        43,
					        "NSO Service Activated Waiting For WFM",
					        obj.getTenantID().toString());
					if (!flag) {
						responseObj.setRequestDetailID(obj.getRequestDetailID());
						responseObj.setRequestID(obj.getRequestID());
					}
					flag = provisioningDao.saveProvisioningRequestResponseDetail(responseObj);
					if (flag) {
						logger.info("Provisioning request Response details saved sucessfully  ");
					} else {
						logger.info("An error Occured to save request Response details  ");
					}

				}
			} else {
				logger.info("unable to perfom call back api :: check status code :: {} code must be 200",
						dto.getStatus());
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
		return flag;
	}

	@Override
	public boolean workFlowManagements(ProvisioningRequestBO dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: updateServiceActivationStatus() {} ");
		boolean flag = false;
		try {
			List<ProvisioningRequestDetailsNsoBO> list = provisioningDao
					.getProvisioningRequestDetalisToWfm(dto.getWorkOrderID(), 73);
			for (ProvisioningRequestDetailsNsoBO obj : list) {
				provisioningDao.updateProvisioningRequest(obj.getRequestID(), 19,
						"Customer Acceptance response received", null);
				List<ProvisioningRequestDetailsNsoBO> checkReqObjIsDependentOrNot = provisioningDao.getRequestIdsByReqDtlId(String.valueOf(obj.getRequestID()), null);
				if (checkReqObjIsDependentOrNot != null && !checkReqObjIsDependentOrNot.isEmpty()) {
				provisioningDao.updateProvisioningRequest(obj.getRequestID(), 11,
							"Dependent requests are being processed",null);
				}
				provisioningDao.updateProvisioningRequestDetailCompletion(obj.getRequestDetailID(), 19, 1);
				flag = omsNet.updateWorkOrderStatus(dto.getWorkOrderID(), 45,
						"Customer Acceptance response received", obj.getTenantID().toString());
			}
			if (!flag) {
				logger.info("workFlowManagement() Updated SucessFully  ");
			} else {
				flag = true;
			}
			return flag;
		} catch (Exception e) {
			logger.error("An error Ocurred ProvisioningRequestServiceImpl workFlowManagement() {}", e);
		}
		return flag;
	}

	@Override
	public boolean serviceProvisioniActivation(ServiceActDto dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: serviceProvisioniActivation() {} ");
		boolean flag = false;
		try {
			List<ProvisioningRequestDetailsNsoBO> list = provisioningDao
					.getProvisioningRequestDetalisToWfm(new BigInteger(dto.getWorkOrderId()), 73);
			for (ProvisioningRequestDetailsNsoBO obj : list) {
				provisioningDao.updateProvisioningRequest(obj.getRequestID(), 19,
						"Service Provision Activation completed ", null);
				provisioningDao.updateProvisioningRequestDetailCompletion(obj.getRequestDetailID(), 19, 1);
				flag = omsNet.updateWorkOrderStatus(new BigInteger(dto.getWorkOrderId()), 41,
						"Service Provision Activation completed wating for wfm ", obj.getTenantID().toString());
			}
			if (!flag) {
				logger.info("serviceProvisioniActivation() Updated SucessFully  ");
			} else {
				flag = true;
			}
			return flag;
		} catch (Exception e) {
			logger.error("An error Ocuured in ProvisioningRequestServiceImpl :: serviceProvisioniActivation() {} ", e);
		}
		return flag;
	}

	@Override
	public boolean getProvisioningRequest(String workOrderId, MultipartFile file) {
		logger.info("Started ProvisioningRequestServiceImpl :: getProvisioningRequest() {}", workOrderId);
		ProvisioningRequestBO provisioningRequest = null;
		WFMDto wfmDto = new WFMDto();
		boolean flag = true;
		try {
			provisioningRequest = provisioningRequestDao.getProvisioningRequest(workOrderId);
			DocumentDto documentDto = new DocumentDto(provisioningRequest, file);
			String response = omsNet.saveDocument(documentDto);
			if (response != null) {
				wfmDto.setJobOrderId(workOrderId);
				boolean responseFlag = this.workFlowManagements(provisioningRequest);
				if (!responseFlag) {
					logger.info("workFlowManagement() Updated SucessFully  ");
					return flag;
				} else {
					logger.error("An error Occured while executing workFlowManagement()  ");
					flag = false;
				}
			}

		} catch (Exception e) {
			logger.error("An error Ocurred ProvisioningRequestServiceImpl getProvisioningRequest() {}", e);
		}
		logger.info("Ended ProvisioningRequestServiceImpl :: getProvisioningRequest() {}", workOrderId);
		return flag;
	}

	@Override
	public boolean getProvisioningRequestDtlsById(String reqDtlId) {
		logger.info("Started ProvisioningRequestServiceImpl :: getProvisioningRequestDtlsById() {}", reqDtlId);
		WFMDto wfmDto = new WFMDto();
		boolean flag = true;
		try {
			Long provRequDtlById = provisioningRequestDao.getProvRequDtlById(reqDtlId);
			wfmDto.setJobOrderId(provRequDtlById.toString());
			boolean response = this.workFlowManagement(wfmDto);
			if (!response) {
				logger.info("workFlowManagement() Updated SucessFully  ");
				return flag;
			} else {
				logger.error("An error Occured while executing workFlowManagement()  ");
				flag = false;
			}
		} catch (Exception e) {
			logger.error("An error Ocurred ProvisioningRequestServiceImpl getProvisioningRequest() {}", e);
		}
		logger.info("Ended ProvisioningRequestServiceImpl :: getProvisioningRequestDtlsById() {}", reqDtlId);
		return flag;
	}

	@Transactional
	@Override
	public boolean getProvisioningRequestDtlsById(String responseId, AysncRequestDto dto) {
		logger.info("Started ProvisioningRequestServiceImpl :: getProvisioningRequestDtlsById() {}", responseId);
		WFMDto wfmDto = new WFMDto();
		boolean flag = true;
		try {
			if (!ObjectUtils.isEmpty(dto)) {
				responseId = StringUtils.hasText(dto.getReqDtlID()) ? dto.getReqDtlID() : null;
			}
			boolean response = this.checkAndReturnResult(wfmDto, dto);
			if (response) {
				logger.info("workFlowManagement() Updated SucessFully  ");
				return flag;
			} else {
				logger.error("An error Occured while executing workFlowManagement()  ");
				flag = false;
			}
		} catch (Exception e) {
			logger.error("An error Ocurred ProvisioningRequestServiceImpl getProvisioningRequest() {}", e);
		}
		logger.info("Ended ProvisioningRequestServiceImpl :: getProvisioningRequestDtlsById() {}", responseId);
		return flag;
	}

	@Transactional
	public boolean checkAndReturnResult(WFMDto dto, AysncRequestDto asyncDto) {
		logger.info("Started ProvisioningRequestServiceImpl :: checkAndReturnResult");
		boolean flag = false;
		boolean provFlag = false;
		try {
			Integer workOrderStatus = Constants.WORK_ODR_SUCESS;
			Integer provRequestStatus = Constants.PROCESOOR_SUCESS;
			ProvisioningRequestResponseDetail responseObj = new ProvisioningRequestResponseDetail();
			ProvisioningRequestBO reqObj = new ProvisioningRequestBO();

			if (!ObjectUtils.isEmpty(asyncDto) && !asyncDto.getStatusCode().contains("200")) {
				workOrderStatus = Constants.WORK_ODR_INPROGRESS_SUCESS;
				provRequestStatus = Constants.PROCERSER_FAIL;
				logger.info("Invalid Response Status Code: {} - unable to process the request",
						asyncDto.getStatusCode());
				provisioningDao.updateProvReqDtlCompletionStatusByRespId(asyncDto.getReqDtlID(),String.valueOf(provRequestStatus),null, asyncDto.getRequestDate());
				provisioningDao.updateProvisioningRequestStatusByDetailId(new BigInteger(asyncDto.getReqDtlID()),provRequestStatus,asyncDto.getRequestDate());
			} else {
				provisioningDao.updateProvReqDtlCompletionStatusByRespId(asyncDto.getReqDtlID(),String.valueOf(provRequestStatus),null, asyncDto.getRequestDate());
				List<ProvisioningRequestDetailsNsoBO> checkReqObjIsDependentOrNot = provisioningDao.getRequestIdsByReqDtlId(asyncDto.getReqDtlID(), null);
				reqObj = provisioningRequestDao.getWorkOrderByPassingRequDtlId(asyncDto.getReqDtlID(),null);
				if (!CollectionUtils.isEmpty(checkReqObjIsDependentOrNot)) {
					int status = 0;
					for (ProvisioningRequestDetailsNsoBO deatilObj : checkReqObjIsDependentOrNot) {
						boolean manualReqIsYesOrNo = handleAsyncProcessTypeY(asyncDto, deatilObj);
						logger.info("manualReqIsYesOrNo {}",manualReqIsYesOrNo);
						status = Constants.PROCERSER_CREATION;
						if (!manualReqIsYesOrNo) {
							if (FileUtil.checkEmptyNull(deatilObj.getProcessType())
									&& deatilObj.getProcessType().equalsIgnoreCase(
											cache.strConfigVal(Constants.PROCESS_TYPE, Constants.GLOBAL_TENANT))) {
								if (StringUtils.hasText(deatilObj.getDepSubProcessingType())
										&& !deatilObj.getDepSubProcessingType().equalsIgnoreCase("1")) {
									status = cache.intConfigVal(Constants.MANUAL_STATUS, Constants.GLOBAL_TENANT);
									workOrderStatus = Constants.WORK_ODR_INPROGRESS_SUCESS;
								} else {
									status = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY,
											Constants.GLOBAL_TENANT);
									workOrderStatus = Constants.WORK_ODR_SUCESS;
								}
							} else {
								status = Constants.PROCERSER_CREATION;
								workOrderStatus = Constants.WORK_ODR_INPROGRESS_SUCESS;
							}
							provisioningDao.updateProvisioningRequestDetailCompletion(deatilObj.getRequestDetailID(),
									status, 1);
							provisioningDao.updateProvisioningRequestStatusByDetailId(
									new BigInteger(asyncDto.getReqDtlID()), status, asyncDto.getRequestDate());
						}
				}
					}
					else {
					provisioningDao.updateProvisioningRequestStatusByDetailId(new BigInteger(asyncDto.getReqDtlID()),
							provRequestStatus, asyncDto.getRequestDate());
				}
				flag = omsNet.updateWorkOrderStatus(reqObj.getWorkOrderID(), workOrderStatus,
						"Customer Acceptance response received", reqObj.getTenantID().toString());
			logger.info(provFlag ? "Provisioning request response details saved successfully"
						: "Error occurred while saving request response details");
			}
			reqObj = provisioningRequestDao.getWorkOrderByPassingRequDtlId(asyncDto.getReqDtlID(),null);
			responseObj.setResponseObject(StringUtils.hasText(asyncDto.getResponse()) ? asyncDto.getResponse() : null);
			responseObj.setRequestDetailID(new BigInteger(asyncDto.getReqDtlID()));
			responseObj.setRequestID(reqObj.getRequestID());
			responseObj.setResponseSequenceNo(1);
			List<ProvReqResponseDetails> provRequestObjisAvailabale = provisioningDao.getProvRequestResponseDetails(new BigInteger(asyncDto.getReqDtlID()),null);
			if (provRequestObjisAvailabale.isEmpty() || provRequestObjisAvailabale == null) {
				provisioningDao.saveProvisioningRequestResponseDetail(responseObj);
				logger.info("Provisioning request Response details saved sucessfully  ");
			} else {
				provisioningDao.updateProvReqResponseDtls(responseObj.getRequestDetailID(),
						responseObj.getResponseSequenceNo(), responseObj.getResponseObject());
			}
			if (flag) {
				logger.info("Workflow management updated successfully");
			}
			return flag;
		} catch (Exception e) {
			logger.error("Error occurred in ProvisioningRequestServiceImpl checkAndReturnResult: {}", e.getMessage(),
					e);
			return flag;
		}
	}

	@Override
	public boolean aysncApiResponseReqDtls(TmfAysncRequestDto dto) throws Exception {
		logger.info("Started ProvisioningRequestServiceImpl :: aysncApiResponseReqDtls() :: AysncRequestDto {}",gson.toJson(dto));
		boolean flag = true;
		ProvisioningRequestBO reqObj = new ProvisioningRequestBO();
		int provReqStatus = 0;
		try {
			Integer provSuccessWoStatus = cache.intConfigVal(Constants.PROV_SUCCESS_WO_STATUS, Constants.GLOBAL_TENANT);
			Integer provRequestStatus = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
			Integer postProvSucess = cache.intConfigVal(Constants.POST_PROV_SUCCESS, Constants.GLOBAL_TENANT);
			Integer postProvDtlSucess = cache.intConfigVal(Constants.POST_DT_IN_SUSS, Constants.GLOBAL_TENANT);
			ProvisioningRequestResponseDetail responseObj = new ProvisioningRequestResponseDetail();
			reqObj = provisioningRequestDao.getWorkOrderByPassingRequDtlId(null,dto.getServiceOrderId());
			flag = omsNet.updateWorkOrderStatus(reqObj.getWorkOrderID(), provSuccessWoStatus,"Customer Acceptance response received", reqObj.getTenantID().toString());
			if (flag) {
				logger.info("WorkOrder Updated SucessFully with OrderID {} ", reqObj.getWorkOrderID());
			  }else {
				  throw new ProvisionException("Unable to process WorkOrder :: Something went wrong From OMS");
			  }
			ProvisioningRequestDetailsBO reqDtlsBo = provisioningDao.getRequestDtlIdByPassingExternalID(dto.getServiceOrderId());
			Integer reqDtlstatus = Integer.parseInt(reqDtlsBo.getStatus());
			if(reqDtlstatus == provRequestStatus || reqDtlstatus == postProvSucess || reqDtlstatus == postProvDtlSucess ) {
				logger.error("Unable to make the request !! request already Processed with reqDtlID {}", reqDtlsBo.getRequestDetailID());
				throw new ProvisionException("Unable to make the request !!  request already processed with the provided externalID " +dto.getServiceOrderId());
			}else {
			 responseObj.setRequestDetailID(reqDtlsBo.getRequestDetailID());
			 responseObj.setRequestID(reqDtlsBo.getRequestID());
			 responseObj.setStatus(Constants.PROCESOOR_SUCESS);
			 responseObj.setResponseReceivedDate(new Date());
			 responseObj.setResponseObjectType("JSON");
			 responseObj.setResponseSequenceNo(1);
			 responseObj.setVersion(2.0f);
			 responseObj.setResponseObject(new ObjectMapper().writeValueAsString(dto));
			 provisioningDao.updateProvReqDtlCompletionStatusByRespId(null,String.valueOf(provRequestStatus),dto.getServiceOrderId(), null);
			 provReqStatus = Constants.PROCERSER_CREATION;
			 provSuccessWoStatus =Constants.WORK_ODR_INPROGRESS_SUCESS;
		     provisioningDao.updateProvisioningRequestStatusByDetailId(reqDtlsBo.getRequestDetailID(),provReqStatus, null);
			 responseObj.setStatus(Constants.PROCESOOR_SUCESS);
			 provisioningDao.saveProvisioningRequestResponseDetail(responseObj);
			}	
			logger.info("Ended ProvisioningRequestServiceImpl :: aysncApiResponseReqDtls() :: ");
			return flag;	
		} catch (Exception e) {
			flag = false;
			logger.error("Error occurred in ProvisioningRequestServiceImpl aysncApiResponseReqDtls(): {}" );
			throw e;
		}
	}
	
	private Boolean handleAsyncProcessTypeY(AysncRequestDto asyncDto, ProvisioningRequestDetailsNsoBO detailObj) {
	    if (StringUtils.hasText(asyncDto.getProcessType()) && asyncDto.getProcessType().equalsIgnoreCase("Y")) {
	    	logger.info("Started  ProvReqServImp :: handleAsyncProcessTypeY() with processType {}", asyncDto.getProcessType());
	       Integer status = cache.intConfigVal(Constants.ALL_SUB_REQ_PROV_SUCCESSFULLY, Constants.GLOBAL_TENANT);
	       logger.info("handleAsyncProcessTypeY detailObj.getRequestDetailID() {}",detailObj.getRequestDetailID(),status);
	        provisioningDao.updateProvisioningRequestDetailCompletion(detailObj.getRequestDetailID(), status, 1);
	        if(detailObj.getServerName().equalsIgnoreCase("wfm")) {
	        	status = Constants.PROCERSER_CREATION;
	        	logger.info("Entered ProvReqServImp :: handleAsyncProcessTypeY() - serverName: {}, updating status: {}", detailObj.getServerName(), status);
	        	provisioningDao.updateProvisioningRequestStatusByDetailId(new BigInteger(asyncDto.getReqDtlID()),status, asyncDto.getRequestDate());
	        	provisioningDao.updateProvisioningRequestDetailCompletion(detailObj.getRequestDetailID(), status, 1);
	        }
	        return true;
	    }
		return false;
	}

	@Override
	public boolean workFlowManagement(WFMDto dto) {
		return false;
	}

	@Override
	@Transactional
	public List<ProvisioningRequest> getProvisioningRequests(String tenantID, String maxNoOfrecords) {
		logger.info("Started ProvisioningRequestServiceImpl :: getProvisioningRequests()");
		List<ProvisioningRequest> list=new ArrayList<>();
		try {
			list=provisioningDao.getProvisioningRequests(tenantID, maxNoOfrecords);
			for(ProvisioningRequest request:list) {
				logger.info("getProvisioningRequests Request ID {} Work Order ID {} status {}",request.getRequestID(),request.getWorkOrderID(),request.getStatus());
				provisioningDao.updateProvisioningRequestProgress(request.getRequestID());
				this.omsNet.updateWorkOrderStatus(request.getWorkOrderID(),
						cache.intConfigVal(Constants.WO_PROV_IN_STATUS, Constants.GLOBAL_TENANT),
						cache.strConfigVal(Constants.PROV_PROCESS_IN_PROGRESS, Constants.GLOBAL_TENANT), tenantID);
			}
			logger.info("End ProvisioningRequestServiceImpl :: getProvisioningRequests()");
		}catch (Exception e) {
			logger.error("Error occurred in ProvisioningRequestServiceImpl getProvisioningRequests()" );
			throw e;
		}
		return list;
	}
	
	
	


}
