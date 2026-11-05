package com.knot.provisioning.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.knot.provioning.exception.ErrorCodes;
import com.knot.provioning.model.ActivityConfig;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.dto.ActivityConfigDto;
import com.knot.provisioning.dto.AysncRequestDto;
import com.knot.provisioning.dto.DashBoardSummaryDTO;
import com.knot.provisioning.dto.LifeCycleDto;
import com.knot.provisioning.dto.MsisdnDTO;
import com.knot.provisioning.dto.NSOServiceDto;
import com.knot.provisioning.dto.PageCount;
import com.knot.provisioning.dto.ProvisioningReqDTO;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;
import com.knot.provisioning.dto.Response;
import com.knot.provisioning.dto.ResponseDTO;
import com.knot.provisioning.dto.ResponseForWfm;
import com.knot.provisioning.dto.ResponseMode;
import com.knot.provisioning.dto.ServiceActDto;
import com.knot.provisioning.dto.TmfAysncRequestDto;
import com.knot.provisioning.dto.WFMDto;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.service.ProvisioningRequestService;
import com.knot.provisioning.service.ProvisioningService;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.ProvisionException;

import jakarta.validation.Valid;

/**
 * This class provides get all auto generated sequence no's for entire
 * application POST requests to a web server.
 * 
 * @author Sathish Ravula
 *
 */
/**
 * @Api(description = "A set of APIs that handles the order fallout management
 *                  releated information", tags = { "Provisioning Service" })
 */
@RestController
public class ProvisioningRequestController {

	private static final Logger logger = LogManager.getLogger(ProvisioningRequestController.class);

	ProvisioningRequestService provisioningRequestService;
	
	Gson gson;
	
	CacheService cache;

	ProvisioningService service;

	@Autowired
	public void setProvisioningRequestService(ProvisioningRequestService provisioningRequestService) {
		this.provisioningRequestService = provisioningRequestService;
	}

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@Autowired
	public void setS(ProvisioningService service) {
		this.service = service;
	}

	@GetMapping("/reStartErrorProvRequest")
	public ResponseDTO reStartErrorProvRequest(@RequestParam(required = false) Integer tenantID) {
		logger.info("The Start ProvisioningRequestController :: reStartErrorProvRequest() {}", new Date());
		ResponseDTO response = new ResponseDTO();
		try {
			Boolean flag = service.getProvisioningRequestErrors(tenantID);
			if (flag == true) {
				response.setStatusCode(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				response.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
			} else {
				response.setStatusCode(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				response.setStatusMessage("Something went wrong");
			}
		} catch (Exception e) {
			logger.error("An exception Ocuured in ProvisioningRequestController ::reStartErrorProvRequest ()", e);
		}
		logger.info("Ended ProvisioningRequestController :: reStartErrorProvRequest() {}", new Date());
		return response;

	}

	@PostMapping(path = "/searchProvisioningRequest")
	public Response<ProvisioningRequestsBO> searchProvisioningRequest(@RequestBody ProvisioningReqSearchDTO request)
			throws Exception {
		Response<ProvisioningRequestsBO> responseObject = new Response<>();
		String status = cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT);

		logger.info("The Start ProvisioningRequestController :: seachProvisioningRequest(){}", new Date().getTime());
		logger.debug(
				" Response Recived  ProvisioningRequestController :: searchProvisioningRequest() :: ProvisioningReqSearchDTO DTO {}",
				gson.toJson(request));
		List<ProvisioningRequestsBO> list = provisioningRequestService.searchProvisioningRequest(request);

		if (!list.isEmpty() && status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(status);
			responseObject.setTotalDisplayCount(
					Objects.nonNull(request.getPaginationObj()) ? request.getPaginationObj().getTotalDisplayCount()
							: null);
			responseObject.setResult(list);
		} else if (status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(cache.strConfigVal(Constants.MSG, Constants.GLOBAL_TENANT));
			responseObject.setTotalDisplayCount(request.getPaginationObj().getTotalDisplayCount());
			responseObject.setResult(list);
		}
		logger.debug(
				"Response From ProvisioningRequestController seachProvisioningRequest {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
				responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
		logger.info("The End ProvisioningRequestController :: seachProvisioningRequest(){}", new Date().getTime());
		return responseObject;
	}

	@GetMapping(path = "/getProvisioningReqDetails")
	public Response<ProvisioningReqDetailsBO> getProvisioningReqDetails(
			@RequestParam(value = "requestID", required = true) String requestID,
			@RequestParam(value = "tenantID", required = true) String tenantID) {
		logger.info("The Start ProvisioningRequestController :: getProvisioningReqDetails(){}", new Date().getTime());
		logger.debug(
				"Enterd in ProvisioningRequestController :: getProvisioningReqDetails() Arguments requestID {}, tenantID {}",
				requestID, tenantID);
		Response<ProvisioningReqDetailsBO> responseObject = new Response<>();
		String status = cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT);
		List<ProvisioningReqDetailsBO> list = provisioningRequestService.getProvisioningReqDetails(requestID);
		if (!list.isEmpty() && status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(status);
			responseObject.setResult(list);
		} else if (status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(cache.strConfigVal(Constants.MSG, Constants.GLOBAL_TENANT));
			responseObject.setResult(list);
		}
		logger.debug(
				"Response From ProvisioningRequestController getProvisioningReqDetails {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
				responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
		logger.info("The End ProvisioningRequestController :: getProvisioningReqDetails(){}", new Date().getTime());
		return responseObject;
	}

	@PostMapping(path = "/getDashBoardDetails")
	public Response<ProvisioningDashBoadBO> getDashBoardDetails(@RequestBody ProvisioningReqSearchDTO request)
			throws Exception {
		Response<ProvisioningDashBoadBO> responseObject = new Response<>();
		String status = cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT);
		logger.info("The Start ProvisioningRequestController :: getDashBoardDetails(){}", new Date().getTime());
		logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getDashBoardDetails() DTO {}",
				gson.toJson(request));
		List<ProvisioningDashBoadBO> list = provisioningRequestService.getDashBoardDetails(request);
		if (!list.isEmpty() && status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(status);
			/**
			 * responseObject.setTotalDisplayCount(request.getPaginationObj().
			 * getTotalDisplayCount());
			 */
			responseObject.setResult(list);
		} else if (status.equalsIgnoreCase(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT))) {
			responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
			responseObject.setStatusMessage(cache.strConfigVal(Constants.MSG, Constants.GLOBAL_TENANT));
			/**
			 * responseObject.setTotalDisplayCount(request.getPaginationObj().
			 * getTotalDisplayCount());
			 */
			responseObject.setResult(list);
		}
		logger.info("The End ProvisioningRequestController :: getDashBoardDetails(){}", new Date().getTime());
		logger.debug(
				"Response From ProvisioningRequestController getDashBoardDetails {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
				responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
		return responseObject;
	}

	@PostMapping("/howLongRequests")
	public Response<DashBoardSummaryDTO> getHowLongRequests(@RequestBody ProvisioningReqSearchDTO request) {
		List<DashBoardSummaryDTO> requestsList = null;
		Response<DashBoardSummaryDTO> response = new Response<>();
		logger.info("Started ProvisioningRequestController :: getHowLongRequests");
		logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getHowLongRequests() DTO {}",
				gson.toJson(request));
		try {
			requestsList = provisioningRequestService.getHowLongRequest(request);
			if (requestsList != null && !requestsList.isEmpty()) {
				response.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				response.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				response.setResult(requestsList);
			} else {
				response.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				response.setStatusMessage("No Data Found :: Max duration is 60 Days ");
				response.setResult(requestsList);
			}
			logger.debug(
					"Response From ProvisioningRequestController getHowLongRequests {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					response.getStatusCode(), response.getStatusCode(), response.getResult().size());
		} catch (Exception e) {
			logger.error("An Exception ProvisioningRequestController :: getHowLongRequests ", e);
		}
		logger.info("The end ProvisioningRequestController :: getHowLongRequests");
		return response;

	}

	@PostMapping("/getViewHowLongRequest")
	public Response<ProvisioningRequestsBO> getViewHowLongRequest(@RequestBody ProvisioningReqSearchDTO dto) {
		List<ProvisioningRequestsBO> viewHowLongRequest = null;
		Response<ProvisioningRequestsBO> response = new Response<>();
		logger.info("Started ProvisioningRequestController :: getViewHowLongRequest");
		logger.debug(
				"Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getViewHowLongRequest() DTO {}",
				gson.toJson(dto));
		try {
			viewHowLongRequest = provisioningRequestService.getViewHowLongRequest(dto);
			if (!CollectionUtils.isEmpty(viewHowLongRequest)) {
				response.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				response.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				response.setResult(viewHowLongRequest);
			} else {
				response.setStatusCode("200");
				response.setStatusMessage("Sucess");
				response.setResult(viewHowLongRequest);

			}
			logger.debug(
					"Response From ProvisioningRequestController getViewHowLongRequest {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					response.getStatusCode(), response.getStatusCode(), response.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getViewHowLongRequest");
		} catch (Exception e) {
			logger.error("An Exception occured  ProvisioningRequestController :: getViewHowLongRequest", e);
		}
		return response;

	}

	@PostMapping("/getRequestsByActivity")
	public Response<DashBoardSummaryDTO> requestResource(@RequestBody ProvisioningReqSearchDTO dto) {
		List<DashBoardSummaryDTO> requestList = null;
		Response<DashBoardSummaryDTO> responseObject = new Response<>();
		try {
			logger.info("Started ProvisioningRequestController :: requestResource()");
			logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: requestResource() DTO {}",
					gson.toJson(dto));
			requestList = provisioningRequestService.requestSource(dto);
			if (!CollectionUtils.isEmpty(requestList)) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				responseObject.setResult(requestList);
			} else {
				responseObject.setStatusCode("200");
				responseObject.setStatusMessage("No Data Found :: Max duration is 60 Days ");
				responseObject.setResult(requestList);
			}
		} catch (Exception e) {
			logger.info("An Exception  ProvisioningRequestController :: requestResource()");
		}
		logger.debug(
				"Response From ProvisioningRequestController requestResource {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
				responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
		logger.info("Ended ProvisioningRequestController :: requestResource()");
		return responseObject;

	}

	@PostMapping("/getRequestByLobs")
	public Response<DashBoardSummaryDTO> getRequestByLobs(@RequestBody ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestController :: gerRequestByLobs()");
		logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getRequestByLobs() DTO {}",
				gson.toJson(dto));
		Response<DashBoardSummaryDTO> responseObject = new Response<>();
		try {
			List<DashBoardSummaryDTO> list = new ArrayList<>();
			list = provisioningRequestService.getRequestByLobs(dto);
			if (!CollectionUtils.isEmpty(list)) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				responseObject.setResult(list);
			} else {
				responseObject.setStatusCode("200");
				responseObject.setStatusMessage("No Data Found :: Max duration is 60 Days ");
				responseObject.setResult(list);
			}
			logger.debug(
					"Response From ProvisioningRequestController getRequestByLobs {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
			logger.info("Ended ProvisioningRequestController :: gerRequestByLobs()");
		} catch (Exception e) {
			logger.error("An Exception occured ProvisioningRequestController :: getRequestsByServers() ", e);
		}
		return responseObject;

	}

	@PostMapping("/getRequestByServices")
	public Response<DashBoardSummaryDTO> getRequestByServices(@RequestBody ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestController :: getRequestByServices()");
		logger.debug(
				"Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getRequestByServices() DTO {}",
				gson.toJson(dto));
		Response<DashBoardSummaryDTO> responseObject = new Response<>();
		List<DashBoardSummaryDTO> list = new ArrayList<>();
		try {
			list = provisioningRequestService.getRequestByServices(dto);
			if (!CollectionUtils.isEmpty(list)) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				responseObject.setResult(list);
			} else {
				responseObject.setStatusCode("200");
				responseObject.setStatusMessage("No Data Found :: Max duration is 60 Days ");
				responseObject.setResult(list);
			}
			logger.debug(
					"Response From ProvisioningRequestController getRequestByServices {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getRequestByServices()");
		} catch (Exception e) {
			logger.error("An Exception occured ProvisioningRequestController :: getRequestsByServers() ", e);
		}
		return responseObject;

	}

	@PostMapping("/getRequestsByServers")
	public Response<DashBoardSummaryDTO> getRequestsByServers(@RequestBody ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestController :: getRequestsByServers()");
		logger.debug(
				"Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getRequestsByServers() DTO {}",
				gson.toJson(dto));
		Response<DashBoardSummaryDTO> responseObject = new Response<>();
		try {
			List<DashBoardSummaryDTO> requestList = null;
			requestList = provisioningRequestService.getRequestByServers(dto);
			if (!CollectionUtils.isEmpty(requestList)) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				responseObject.setResult(requestList);
			} else {
				responseObject.setStatusCode("200");
				responseObject.setStatusMessage("No Data Found");
				responseObject.setResult(requestList);
			}
			logger.debug(
					"Response From ProvisioningRequestController getRequestsByServers {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getRequestsByServers()");
		} catch (Exception e) {
			logger.error("An Exception occured ProvisioningRequestController :: getRequestsByServers() ", e);
		}
		return responseObject;

	}

	@PostMapping("/getRequestsCount")
	public Response<DashBoardSummaryDTO> getRequestCount(@RequestBody ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestController :: getRequestCount()");
		logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getRequestCount() DTO {}",
				gson.toJson(dto));
		Response<DashBoardSummaryDTO> responseObject = new Response<>();
		try {
			List<DashBoardSummaryDTO> requestList = null;
			requestList = provisioningRequestService.getRequestCount(dto);
			if (!CollectionUtils.isEmpty(requestList)) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage(cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
				responseObject.setResult(requestList);
			} else {
				responseObject.setStatusCode("200");
				responseObject.setStatusMessage("No Data Found :: Max duration is 60 Days ");
				responseObject.setResult(requestList);
			}
			logger.debug(
					"Response From ProvisioningRequestController getRequestCount {} StatusCode {}, StatusMsg {}, ResponseListSize {} ::",
					responseObject.getStatusCode(), responseObject.getStatusCode(), responseObject.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getRequestCount()");
		} catch (Exception e) {
			logger.info("An Exception Occured in  ProvisioningRequestController :: getRequestCount()");
		}
		return responseObject;

	}

	@GetMapping("/getPageDurationcount")
	public Response<PageCount> getPageDurationcount() {
		logger.info("Started ProvisioningRequestController :: getPageDurationcount()");
		Response<PageCount> response = new Response<>();
		try {
			PageCount pag = new PageCount();
			pag.setPLD(cache.strConfigVal(Constants.PLD, Constants.GLB_1));
			pag.setRPD(cache.strConfigVal(Constants.RPD, Constants.GLB_1));
			List<PageCount> durationList = Arrays.asList(pag);
			if (!CollectionUtils.isEmpty(durationList)) {
				response.setStatusCode("200");
				response.setStatusMessage("Sucess");
				response.setResult(durationList);
			} else {
				response.setStatusCode("200");
				response.setStatusMessage("Sucess");
				response.setResult(durationList);
			}
			logger.debug(
					"Response From ProvisioningRequestController getPageDurationcount {} StatusCode {}, StatusMsg {}, ResponsePageCountList {} ::",
					response.getStatusCode(), response.getStatusCode(), response.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getPageDurationcount()");
		} catch (Exception e) {
			logger.error("An Excepton Occured in ProvisioningRequestController :: getPageDurationcount()");
		}
		return response;

	}

	@PostMapping("/getRatePlanTypeWiseRequets")
	public Response<DashBoardSummaryDTO> getRatePlanTypes(@RequestBody ProvisioningReqSearchDTO dto) {
		logger.info("ProvisioningRequestController :: getRatePlanTypes ");
		logger.debug("Received ProvisioningReqSearchDTO  ProvisioningRequestController :: getRatePlanTypes() DTO {}",
				gson.toJson(dto));
		Response<DashBoardSummaryDTO> response = new Response<>();
		List<DashBoardSummaryDTO> list = new ArrayList<>();
		try {
			list = provisioningRequestService.getRatePlanIds(dto);
			if (!CollectionUtils.isEmpty(list)) {
				response.setStatusCode("200");
				response.setStatusMessage("Success");
				response.setResult(list);
			} else {
				response.setStatusCode("200");
				response.setStatusMessage("No Data Found for Provided Date");
			}
			logger.debug(
					"Response From ProvisioningRequestController getRatePlanTypes {} StatusCode {}, StatusMsg {}, ResponseList {} ::",
					response.getStatusCode(), response.getStatusCode(), response.getResult().size());
			logger.info("Ended ProvisioningRequestController :: getRatePlanTypes ");
		} catch (Exception e) {
			logger.error("An Exception Occured  ProvisioningRequestController : getRatePlanTypes ", e);
		}
		return response;

	}

	@PostMapping(path = "/updateProvisioningRequest")
	public ResponseDTO updateProvisioningRequest(@RequestBody ProvisioningReqDTO request) {
		logger.info("Started ProvisioningRequestController :: updateProvisioningRequest ");
		logger.debug(
				"Received ProvisioningReqDTOResponse  ProvisioningRequestController :: updateProvisioningRequest DTO {}",
				gson.toJson(request));
		boolean status = true;
		String resultJson = "";
		ResponseDTO responseObject = new ResponseDTO();
		resultJson = gson.toJson(request);
		logger.info("The Provisioning Request Input Json is :" + resultJson);
		try {
			status = provisioningRequestService.updateProvisioningRequest(request);
			if (status) {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage("Provisioning Request Updated Successfully.");
			} else {
				responseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				responseObject.setStatusMessage("Provisioning Request Failed to Updated .");
			}
			logger.debug("Response From updateProvisioningRequest() responseObject {}::", gson.toJson(responseObject));
			logger.info("Ended ProvisioningRequestController :: updateProvisioningRequest ");
		} catch (Exception e) {
			logger.error("An Exception Occured  ProvisioningRequestController : updateProvisioningRequest ", e);
		}

		return responseObject;
	}

	@PostMapping("/serviceActivationConfirmation")
	public Response<Boolean> updateNetWorkRelatedChanges(@RequestBody NSOServiceDto dto) {
		logger.info("Started ProvisioningRequestController :: updateNetWorkRelatedChanges ");
		Response<Boolean> response = new Response<>();
		boolean flag = provisioningRequestService.updateServiceActivationStatus(dto);
		if (flag) {
			response.setStatusCode("200");
			response.setStatusMessage("serviceActivationConfirmed Waiting For Wfm ");
			response.setResult(Arrays.asList(flag));
		} else {
			response.setStatusCode("500");
			response.setStatusMessage("Something went wrong ");
			response.setResult(Arrays.asList(flag));
		}
		logger.info("Ended ProvisioningRequestController :: updateNetWorkRelatedChanges ");
		return response;
	}

	@PostMapping("/customer-acceptance")
	public ResponseForWfm cutsomerAcceptancee(@RequestBody WFMDto dto) {
		logger.info("Started ProvisioningRequestController :: cutsomerAcceptance () ");
		ResponseForWfm response = new ResponseForWfm();
		boolean flag = provisioningRequestService.workFlowManagement(dto);
		if (!flag) {
			response.setMessage("Customer acceptance response received.");
			response.setStatus("true");
		} else {
			response.setMessage("Something went wrong ");
			response.setStatus("false");
		}
		logger.info("Ended ProvisioningRequestController :: cutsomerAcceptance () ");
		return response;
	}

	@PostMapping("/v2/customer-acceptance")
	public ResponseMode uploadFiles(@RequestParam("file") MultipartFile[] files,
			@RequestParam("jobOrderId") String jobOrderId) {
		logger.info("Started ProvisioningRequestController :: uploadFiles with jobOrderId {}",jobOrderId);
		ResponseMode response = new ResponseMode();
		boolean flag = false;
		for (MultipartFile file : files) {
			flag = provisioningRequestService.getProvisioningRequest(jobOrderId, file);
		}
		if (!flag) {
			response.setStatusCode("200");
			response.setStatusMessage("Customer acceptance response received.");
		} else {
			response.setStatusCode("200");
			response.setStatusMessage("Something went wrong");
		}
		logger.info("Ended ProvisioningRequestController :: uploadFiles ");
		return response;
	}

	@PostMapping("/asynApi1")
	public ResponseMode asynApi(@RequestParam String reqDtlId) {
		logger.info("Started ProvisioningRequestController :: uploadFiles ");
		boolean flag = true;
		ResponseMode response = new ResponseMode();
		flag = provisioningRequestService.getProvisioningRequestDtlsById(reqDtlId);
		if (flag) {
			response.setStatusCode("200");
			response.setStatusMessage("Updated SucsesFully ");
		} else {
			response.setStatusCode("200");
			response.setStatusMessage("Something went wrong");
		}
		logger.info("Ended ProvisioningRequestController :: uploadFiles ");
		return response;
	}

	@PostMapping("/serviceProvisioniActivation")
	public ResponseForWfm serviceProvisioniActivation(@RequestBody ServiceActDto dto) {
		logger.info("Started ProvisioningRequestController :: serviceProvisioniActivation () ");
		ResponseForWfm response = new ResponseForWfm();
		boolean flag = provisioningRequestService.serviceProvisioniActivation(dto);
		if (!flag) {
			response.setMessage("Service Provisioning Activation is Done ");
			response.setStatus("true");
		} else {
			response.setMessage("Something went wrong ");
			response.setStatus("false");
		}
		logger.info("Ended ProvisioningRequestController :: serviceProvisioniActivation () ");
		return response;
	}

	@PostMapping("/serviceConformationActivation")
	public ResponseMode serviceConformationActivation(@RequestBody AysncRequestDto dto) {
		ResponseMode response = new ResponseMode();
		logger.info("Started ProvisioningRequestController :: serviceActivation ");
		logger.info("Received AysncRequestDto  ProvisioningRequestController :: serviceConformationActivation() DTO {}",
				gson.toJson(dto));

		try {
			boolean flag = true;
			flag = provisioningRequestService.getProvisioningRequestDtlsById(null, dto);
			if (flag) {
				response.setStatusCode("200");
				response.setStatusMessage("Request updated successfully. ");
			} else {
				response.setStatusCode("500");
				response.setStatusMessage("Something went wrong");
			}
			logger.info("Ended ProvisioningRequestController :: uploadFiles ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	@PostMapping("/serviceOrderResponse")
	public ResponseMode serviceOrderResponse(@Valid @RequestBody TmfAysncRequestDto dto) throws Exception {
		logger.info("Started ProvisioningRequestController :: serviceOrderResponse ");
		ResponseMode response = new ResponseMode();
		String statusCode = cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT);
			    provisioningRequestService.aysncApiResponseReqDtls(dto);
				response.setStatusCode(statusCode);
				response.setStatusMessage("Request updated successfully. ");
			logger.info("Ended ProvisioningRequestController :: serviceOrderResponse ");
		return response;

	}
	
	@PostMapping("/activeFirst")
	public ResponseMode activeFirstResource(@RequestBody MsisdnDTO dto) {
		logger.info("Started ProvisioningRequestController :: activeFirstResource() ");
		ResponseMode repsonseObject = new ResponseMode();
		try {
			boolean activeFirstResource = service.activeFirstResource(dto);
			if (activeFirstResource == true) {
				repsonseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				repsonseObject.setStatusMessage(Constants.REQUEST_COMPLETION_SUCCESS);
				logger.info("Ended ConfigController :: activeFirstResource() ");
			}
		} catch (Exception e) {
			repsonseObject.setStatusCode(String.valueOf(HttpStatus.NO_CONTENT.value()));
			repsonseObject.setStatusMessage(e.getMessage());
			logger.error("An Exception Occured in ConfigController :: activeFirstResource() ", e);
		}
		return repsonseObject;
	}
	
	
	@PostMapping("/lifeCycleStatus")
	public ResponseMode lifeCycleStatus(@RequestBody LifeCycleDto dto) throws Exception {
		logger.info("Started ProvisioningRequestController :: lifeCycleStatus() ");
		ResponseMode repsonseObject = new ResponseMode();
		try {
			boolean activeFirstResource = service.updateLifeCycleStatus(dto);
			if (activeFirstResource == true) {
				repsonseObject.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				repsonseObject.setStatusMessage(Constants.REQUEST_COMPLETION_SUCCESS);
				logger.info("Ended ConfigController :: activeFirstResource() ");
			}else {
				repsonseObject.setStatusCode(Constants.MISSING_MANDTORY_FILED);
				repsonseObject.setStatusMessage(Constants.REQUEST_FAILED);
			}
			logger.info("Ended ProvisioningRequestController :: lifeCycleStatus() ");
		} catch (ProvisionException e) {
			repsonseObject.setStatusCode(e.getErrorCode());
			repsonseObject.setStatusMessage(e.getMessage());
			logger.error("An Exception Occured in ProvisioningRequestController :: lifeCycleStatus() ", e);
		}
		return repsonseObject;
	}
	
	
	@PostMapping("/saveProvActivityConfig")
	public ResponseMode saveProvActivityConfig(@RequestBody List<ActivityConfigDto> dto) {
		logger.info("Started ProvisioningRequestController saveProvActivityConfig :: ");
		ResponseMode response = new ResponseMode();
		String statusCode = cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT);
		try {
			service.saveProvActivityConfig(dto);
			response.setStatusCode(statusCode);
			response.setStatusMessage("saved successfully. ");
		} catch (Exception e) {
			response.setStatusCode(ErrorCodes.CUSTOM_EXCPTION);
			response.setStatusMessage("Unable to save activity configuration "+ e.getMessage());
		}
		logger.info("Ended ProvisioningRequestController :: saveProvActivityConfig ");
		return response;
	}
	
	
	@PostMapping("/searchAllActvityConfigs")
	public Response<ActivityConfig> getAllActvityConfigs(@RequestBody  ActivityConfigDto dto) {
		logger.info("Started ProvisioningRequestController :: getAllActvityConfigs ");
		Response<ActivityConfig> response = new Response<>();
		String statusCode = cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT);
		try {
			List<ActivityConfig> configList = service.getAllActvityConfigs(dto);
			response.setStatusCode(statusCode);
			response.setResult(configList);
			String totalDisplayCount =Objects.nonNull(dto.getPaginationObj()) ? dto.getPaginationObj().getTotalDisplayCount(): null;
			response.setTotalDisplayCount(totalDisplayCount);
			logger.info("Ended ProvisioningRequestController :: getAllActvityConfigs ");
		} catch (Exception e) {
			response.setStatusCode(ErrorCodes.CUSTOM_EXCPTION);
			response.setStatusMessage("No data founded for provided configurations ");
		}
		return response;
	}
}
