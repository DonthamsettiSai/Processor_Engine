package com.knot.provisioning.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.dto.AysncRequestDto;
import com.knot.provisioning.dto.DashBoardSummaryDTO;
import com.knot.provisioning.dto.MsisdnDTO;
import com.knot.provisioning.dto.NSOServiceDto;
import com.knot.provisioning.dto.ProvisioningReqDTO;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;
import com.knot.provisioning.dto.ServiceActDto;
import com.knot.provisioning.dto.TmfAysncRequestDto;
import com.knot.provisioning.dto.WFMDto;

/**
 * @author Lakshman
 *
 */

public interface ProvisioningRequestService {

	public List<ProvisioningRequestsBO> searchProvisioningRequest(ProvisioningReqSearchDTO request) throws Exception;

	public List<ProvisioningReqDetailsBO> getProvisioningReqDetails(String requestID);

	public boolean updateProvisioningRequest(ProvisioningReqDTO request);

	public List<ProvisioningRequestsBO> getViewHowLongRequest(ProvisioningReqSearchDTO dto);

	public List<DashBoardSummaryDTO> requestSource(ProvisioningReqSearchDTO request);

	public List<DashBoardSummaryDTO> getRequestByServers(ProvisioningReqSearchDTO request);

	public List<DashBoardSummaryDTO> getRequestByServices(ProvisioningReqSearchDTO request);

	public List<DashBoardSummaryDTO> getHowLongRequest(ProvisioningReqSearchDTO request);

	public List<DashBoardSummaryDTO> getRequestByLobs(ProvisioningReqSearchDTO dto);

	public List<DashBoardSummaryDTO> getRequestCount(ProvisioningReqSearchDTO dto);

	public List<ProvisioningDashBoadBO> getDashBoardDetails(ProvisioningReqSearchDTO request) throws Exception;

	public List<DashBoardSummaryDTO> getRatePlanIds(ProvisioningReqSearchDTO dto);
	
	public boolean updateServiceActivationStatus(NSOServiceDto dto);
	
	public boolean getProvisioningRequest(String workOrderId, MultipartFile file);
   
//	public boolean workFlowManagement(ProvisioningReqDTO dto);
	
	public boolean serviceProvisioniActivation(ServiceActDto dto);
	
	public boolean getProvisioningRequestDtlsById(String reqDtlId);

	boolean getProvisioningRequestDtlsById(String responseId, AysncRequestDto dto);
	
	boolean aysncApiResponseReqDtls(TmfAysncRequestDto dto) throws Exception;

	boolean workFlowManagement(WFMDto dto);

	boolean workFlowManagements(ProvisioningRequestBO dto);
	
	public List<ProvisioningRequest> getProvisioningRequests(String tenantID, String maxNoOfrecords);
	
}
