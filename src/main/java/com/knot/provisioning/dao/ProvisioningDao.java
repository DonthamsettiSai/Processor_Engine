package com.knot.provisioning.dao;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.knot.provioning.model.ActivityConfig;
import com.knot.provioning.model.ProvRespDetails;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provioning.model.ProvisioningRequestProcessingError;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.bo.ProvReqResponseDetails;
import com.knot.provisioning.bo.ProvisioningRequestDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestDetailsNsoBO;
import com.knot.provisioning.dto.ActivityConfigDto;

/**
 * @author Sai
 * 
 *
 */

public interface ProvisioningDao {

	/**
	 * public boolean updateWorkOrderStatus(Integer workOrderID,int status,String
	 * remarks);
	 */

	public boolean updateProvReqProgress(BigInteger requestID, int stat);

	public boolean updateProvReqDet4PostProv(int requestDetailID, int status);

	public List<ProvisioningRequest> getProvisioningRequests(String tenantId, String noOfRecords); // S

	public List<ProvisioningRequestDetails> getRequestDetailsGroups(BigInteger requestID); // S

	public List<ProvisioningRequest> getProvisioningErrorRequests(); // S

	public String getDependentResponse(int requestDetailID, int requestID); // S

	public boolean updateRequestObject(BigInteger requestDetailID, String requestObject);

	public boolean updateProvisioningRequestDetailCompletion(BigInteger requestDetailID, int status, int retryCount); // S

	public boolean saveProcessError(ProvisioningRequestProcessingError obj); // S

	public boolean saveProvisioningRequestResponseDetail(ProvisioningRequestResponseDetail obj); // S

	public boolean updateProvisioningRequestDetailCompletion(BigInteger requestDetailID, int status, int retryCount,
			String errorCode, String errorDescription); // S

	public boolean updateProvisioningRequestProgress(BigInteger requestID); // S

	public boolean updateProvisioningRequest(BigInteger requestID, int stats, String remarks,
			Date requestCompletionDate);

	public boolean updateProvisioningRequestDetails(BigInteger requestDetailId, BigInteger requestId);// S

	public boolean updateRequestDetailServerUrl(String url, BigInteger requestDetailID, String responseUniqueID);

	public List<ProvRespDetails> getProvisioningResponse(ProvisioningRequestDetails detail,
			ProvisioningRequest provisioningRequest);

	public Boolean getProvisioningErrorRequests(Integer reqDetailId);

	public List<ProvReqResponseDetails> getProvRequestResponseDetails(BigInteger requestID, BigInteger reqID);

	public List<ProvisioningRequestDetailsNsoBO> getProvisioningRequestDetalisToWfm(BigInteger workOderID,
			Integer status);

	boolean updateProvReqDtlCompletionStatusByRespId(String responseId, String status, String externallID,
			String requestDate);

	boolean updateProvisioningRequestStatusByDetailId(BigInteger responseId, int status, String requestDate);

	List<ProvisioningRequestDetailsNsoBO> getRequestIdsByReqDtlId(String responseID, String externalID);

	public boolean updateProvReqResponseDtls(BigInteger reqDtlId, Integer respSeqNo, String responseObj);

	public String getReqDtlSeqNumber(String id);

	public ProvisioningRequestDetailsBO getRequestDtlIdByPassingExternalID(String externalId);

	public List<ProvisioningRequestDetailsBO> getRequestIdRequestDtlIdByPassingWorkOrderId(int workOrderId);// Mounica M

	boolean updateProvisioningNetwork(String reqDtlID, String workOrderId, String requestComplDate);

	boolean saveProvActivityConfig(ActivityConfig activityConfig);
	
	
	ActivityConfig getActivityConfigByConfigId(String configID);

	public List<ActivityConfig> getAllActvityConfigs(ActivityConfigDto dto);
	
	public boolean existsActivityCode(String activityCode, String configId);

	/**
	 * public List<String> getUpdatedProvisioningRequestDetails(int requestID);
	 * public boolean updateErrorRequestDetails(String requestDetailIDs); public
	 * List<ProvisioningRequestDetails>
	 * getAllRequestDetailsGroupsForErrorRequest(int requestID); public boolean
	 * updateDetail(ProvisioningRequestDetails obj); public boolean
	 * saveCustomerAccountNetworkID(CustomerAccountNetworkIDBO obj); public boolean
	 * updateProvisioningRequestDetails(int requestDetailID); public List
	 * getDependentLOBStatus(ProvisioningRequestDetails obj); public
	 * List<ProvisioningRequestDetails> getRequestDetailAllPrioritiesParallel(int
	 * requestID); public List<ProvisioningRequestDetails>
	 * getRequestDetailByServicePriority(int requestID,int lobPriority,int
	 * servicePriority); public List<ProvisioningRequestDetails> getTopPriority(int
	 * requestID,int lobPriority,int servicePriority); public
	 * List<ProvisioningRequestDetails> getRequestDetails(int requestID,int
	 * lobPriority,int sequencePriority); public boolean
	 * updateProvisioningRequestDetailProgress(int requestID); public String
	 * getDependentStatus(ProvisioningRequestDetails obj); public String
	 * getSequenceRequestStatus(ProvisioningRequestDetails obj); public
	 * List<ProvisioningRequestDetails> getByRatePlanTopPriority(int requestID,int
	 * lobPriority); public List<ProvisioningRequestDetails> getRequestDetail(int
	 * requestID,int lobPriority); public boolean
	 * saveTopPriotiryDetail(List<ProvisioningRequestDetails> detailList); public
	 * List<ProvisioningRequestDetails> getByLobParallel(int requestID) ; public
	 * List<ProvisioningRequestDetails> getByLobSequence(int requestID) ; public
	 * boolean getDetailStatus(int ratePlanPriority,int requestID);
	 */
}
