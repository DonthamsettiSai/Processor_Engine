package com.knot.provisioning.dao;

import java.util.List;

import com.knot.provioning.model.ProvisioningReqDetailsModel;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.dto.ProvisioningReqDTO;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;
import com.knot.provisioning.utill.ProvisionException;

/**
 * @author Lakshman
 *
 */

public interface ProvisioningRequestDao {

	public List<ProvisioningRequestsBO> searchProvisioningRequest(ProvisioningReqSearchDTO request) throws Exception;

	public List<ProvisioningReqDetailsBO> getProvisioningReqDetails(String requestID);
	
	public List<Long> getRatePlanIds(ProvisioningReqSearchDTO dto);

	public List<Object[]> getHowLOngRequests(ProvisioningReqSearchDTO request);

	public List<Object[]> requestSource(ProvisioningReqSearchDTO request);

	public List<ProvisioningRequestsBO> getViewHowLongRequest(ProvisioningReqSearchDTO dto);

	public List<Object[]> getRequestByLobs(ProvisioningReqSearchDTO request);

	public List<Object[]> getRequestByServices(ProvisioningReqSearchDTO request);

	public List<Object[]> getRequestsByServers(ProvisioningReqSearchDTO request);

	public ProvisioningReqDetailsModel getActiveProvisioningReqDetails(ProvisioningReqDTO request);

	public boolean updateProvisioningReqDetails(ProvisioningReqDTO request) throws Exception;

	public boolean updateProvisioningRequest(ProvisioningReqDTO request) throws Exception;

	public boolean saveProvisioningReqDetails(ProvisioningReqDetailsModel requestDetails);

	public List<ProvisioningDashBoadBO> getDashBoardDetails(ProvisioningReqSearchDTO request) throws Exception;

	public ProvisioningRequestBO getProvisioningRequest(String workOrderId);
	
	public Long getProvRequDtlById(String reqDtlId);

	ProvisioningRequestBO getWorkOrderByPassingRequDtlId(String reqDtlID, String externalID) throws ProvisionException;
}
