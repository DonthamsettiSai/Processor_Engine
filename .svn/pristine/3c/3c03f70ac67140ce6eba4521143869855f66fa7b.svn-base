package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Sathish Ravula
 */

@Entity
@Table(name = "provisioning_request_details")
@IdClass(ProvisioningReqDetailsPK.class)
public class ProvisioningReqDetailsModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "REQUEST_DETAIL_ID")
	private BigInteger requesDetailID;
	
	@Column(name = "REQUEST_ID")
	private BigInteger requestID;
	
	@Id
	@Column(name = "VERSION_NO")
	private Float versionNO;

	
	@Column(name = "LOB_ID")
	private BigInteger lobID;
	
	
	@Column(name = "SERVICE_ID")
	private BigInteger serviceID;
	
	@Column(name = "SERVICE_PACKAGE_ID")
	private BigInteger servicePackageID;
	
	@Column(name = "RATE_PLAN_ID")
	private BigInteger ratePlanID;
	

	@Column(name = "INTERFACE_TYPE")
	private String interfaceType;

	@Column(name = "SERVER_NAME")
	private String serverName;

	@Column(name = "SERVER_URL")
	private String serverURL;

	@Column(name = "TOKEN_ID")
	private String tokenID;

	@Column(name = "LOB_PRIORITY")
	private Integer lobPriority;

	@Column(name = "SERVICE_PRIORITY")
	private Integer servicePriority;

	@Column(name = "RATE_PLAN_PRIORITY")
	private Integer ratePLanPriority;

	@Column(name = "SEQUENCE_ID")
	private Integer seqID;

	@Column(name = "SUB_SEQUENCE_ID")
	private Integer subSeqID;

	@Column(name = "DEPENDENT_SEQUENCE_ID")
	private Integer depSeqID;

	@Column(name = "DEPENDENT_SUB_SEQUENCE_ID")
	private Integer depSubSeqID;

	@Column(name = "GROUP_NO")
	private Integer groupNo;

	@Column(name = "SUB_GROUP_NO")
	private Integer subGroupNO;

	@Column(name = "DEP_SUB_SEQ_PROCESSING_TYPE")
	private String subSeqProcessingType;

	@Column(name = "CAPTURE_DEP_SUB_SEQ_RESPONSE")
	private String subSeqResponse;

	@Column(name = "REQUEST_OBJECT")
	private String requestObj;

	@Column(name = "MAX_RETRIES")
	private int maxRetries;

	@Column(name = "TIME_GAP_BETWEEN_RETRY")
	private Integer gapBetweenRetry;

	@Column(name = "TIME_GAP_UNIT")
	private String timeGapUnit;

	@Column(name = "CURRENT_RETRY_COUNT")
	private int retryCount;

	@Column(name = "REQUEST_CREATION_DATE")
	private Date reqCreationDate;

	@Column(name = "REQUEST_PICKED_DATE")
	private Date reqPickedDate;

	@Column(name = "REQUEST_COMPLETION_DATE")
	private Date resCompletionDate;

	@Column(name = "RESPONSE_UPDATE_CATEGORY")
	private int resUpdateCategory;

	@Column(name = "SLA_TIME")
	private Integer slaTime;

	@Column(name = "SLA_UNIT")
	private String slaUnit;

	@Column(name = "ACTUAL_PROCESS_TIME")
	private Integer actualProcessTime;

	@Column(name = "SLA_VIOLATION_STATUS")
	private String slaViolationStatus;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "VERSION_DATE")
	private Date versionDate;

	@Column(name = "ERROR_CODE")
	private String errorCode;

	@Column(name = "PROV_OBJECT_ID")
	private Integer provObjectId;

	@Column(name = "REQUEST_METHOD")
	private String requestMethod;

	@Column(name = "IS_ASYNC")
	private String isAsync;

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	@Column(name = "ERROR_DESCRIPTION")
	private String errorDescription;

	public ProvisioningReqDetailsModel(ProvisioningReqDetailsModel detailModel) {
		this.requesDetailID = detailModel.getRequesDetailID();
		this.requestID = detailModel.getRequestID();
		this.lobID = detailModel.getLobID();
		this.serviceID = detailModel.getServiceID();
		this.servicePackageID = detailModel.getServicePackageID();
		this.ratePlanID = detailModel.getRatePlanID();
		this.interfaceType = detailModel.getInterfaceType();
		this.serverName = detailModel.getServerName();
		this.serverURL = detailModel.getServerURL();
		this.tokenID = detailModel.getTokenID();
		this.lobPriority = detailModel.getLobPriority();
		this.servicePriority = detailModel.getServicePriority();
		this.ratePLanPriority = detailModel.getRatePLanPriority();
		this.seqID = detailModel.getSeqID();
		this.subSeqID = detailModel.getSubSeqID();
		this.depSeqID = detailModel.getDepSeqID();
		this.depSubSeqID = detailModel.getDepSubSeqID();
		this.groupNo = detailModel.getGroupNo();
		this.subGroupNO = detailModel.getSubGroupNO();
		this.subSeqProcessingType = detailModel.getSubSeqProcessingType();
		this.subSeqResponse = detailModel.getSubSeqResponse();
		this.requestObj = detailModel.getRequestObj();
		this.maxRetries = detailModel.getMaxRetries();
		this.gapBetweenRetry = detailModel.getGapBetweenRetry();
		this.timeGapUnit = detailModel.getTimeGapUnit();
		this.retryCount = detailModel.getRetryCount();
		this.reqCreationDate = detailModel.getReqCreationDate();
		this.reqPickedDate = detailModel.getReqPickedDate();
		this.resCompletionDate = detailModel.getResCompletionDate();
		this.resUpdateCategory = detailModel.getResUpdateCategory();
		this.slaTime = detailModel.getSlaTime();
		this.slaUnit = detailModel.getSlaUnit();
		this.actualProcessTime = detailModel.getActualProcessTime();
		this.slaViolationStatus = detailModel.getSlaViolationStatus();
		this.errorCode = detailModel.getErrorCode();
		this.errorDescription = detailModel.getErrorDescription();
		this.provObjectId = detailModel.getProvObjectId();
		this.requestMethod = detailModel.getRequestMethod();
		this.isAsync = detailModel.getIsAsync();
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public ProvisioningReqDetailsModel() {

	}

	public BigInteger getRequesDetailID() {
		return requesDetailID;
	}

	public void setRequesDetailID(BigInteger requesDetailID) {
		this.requesDetailID = requesDetailID;
	}

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public Float getVersionNO() {
		return versionNO;
	}

	public void setVersionNO(Float versionNO) {
		this.versionNO = versionNO;
	}

	public String getIsAsync() {
		return isAsync;
	}

	public void setIsAsync(String isAsync) {
		this.isAsync = isAsync;
	}

	public BigInteger getLobID() {
		return lobID;
	}

	public void setLobID(BigInteger lobID) {
		this.lobID = lobID;
	}

	public BigInteger getServiceID() {
		return serviceID;
	}

	public void setServiceID(BigInteger serviceID) {
		this.serviceID = serviceID;
	}

	public BigInteger getServicePackageID() {
		return servicePackageID;
	}

	public void setServicePackageID(BigInteger servicePackageID) {
		this.servicePackageID = servicePackageID;
	}

	public BigInteger getRatePlanID() {
		return ratePlanID;
	}

	public void setRatePlanID(BigInteger ratePlanID) {
		this.ratePlanID = ratePlanID;
	}

	public String getInterfaceType() {
		return interfaceType;
	}

	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getTokenID() {
		return tokenID;
	}

	public void setTokenID(String tokenID) {
		this.tokenID = tokenID;
	}

	public Integer getLobPriority() {
		return lobPriority;
	}

	public void setLobPriority(Integer lobPriority) {
		this.lobPriority = lobPriority;
	}

	public Integer getServicePriority() {
		return servicePriority;
	}

	public void setServicePriority(Integer servicePriority) {
		this.servicePriority = servicePriority;
	}

	public Integer getRatePLanPriority() {
		return ratePLanPriority;
	}

	public void setRatePLanPriority(Integer ratePLanPriority) {
		this.ratePLanPriority = ratePLanPriority;
	}

	public Integer getSeqID() {
		return seqID;
	}

	public void setSeqID(Integer seqID) {
		this.seqID = seqID;
	}

	public Integer getSubSeqID() {
		return subSeqID;
	}

	public void setSubSeqID(Integer subSeqID) {
		this.subSeqID = subSeqID;
	}

	public Integer getDepSeqID() {
		return depSeqID;
	}

	public void setDepSeqID(Integer depSeqID) {
		this.depSeqID = depSeqID;
	}

	public Integer getDepSubSeqID() {
		return depSubSeqID;
	}

	public void setDepSubSeqID(Integer depSubSeqID) {
		this.depSubSeqID = depSubSeqID;
	}

	public Integer getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(Integer groupNo) {
		this.groupNo = groupNo;
	}

	public Integer getSubGroupNO() {
		return subGroupNO;
	}

	public void setSubGroupNO(Integer subGroupNO) {
		this.subGroupNO = subGroupNO;
	}

	public String getSubSeqProcessingType() {
		return subSeqProcessingType;
	}

	public void setSubSeqProcessingType(String subSeqProcessingType) {
		this.subSeqProcessingType = subSeqProcessingType;
	}

	public String getSubSeqResponse() {
		return subSeqResponse;
	}

	public void setSubSeqResponse(String subSeqResponse) {
		this.subSeqResponse = subSeqResponse;
	}

	public String getRequestObj() {
		return requestObj;
	}

	public void setRequestObj(String requestObj) {
		this.requestObj = requestObj;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public Integer getGapBetweenRetry() {
		return gapBetweenRetry;
	}

	public void setGapBetweenRetry(Integer gapBetweenRetry) {
		this.gapBetweenRetry = gapBetweenRetry;
	}

	public String getTimeGapUnit() {
		return timeGapUnit;
	}

	public void setTimeGapUnit(String timeGapUnit) {
		this.timeGapUnit = timeGapUnit;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Date getReqCreationDate() {
		return reqCreationDate;
	}

	public void setReqCreationDate(Date reqCreationDate) {
		this.reqCreationDate = reqCreationDate;
	}

	public Date getReqPickedDate() {
		return reqPickedDate;
	}

	public void setReqPickedDate(Date reqPickedDate) {
		this.reqPickedDate = reqPickedDate;
	}

	public Date getResCompletionDate() {
		return resCompletionDate;
	}

	public void setResCompletionDate(Date resCompletionDate) {
		this.resCompletionDate = resCompletionDate;
	}

	public int getResUpdateCategory() {
		return resUpdateCategory;
	}

	public void setResUpdateCategory(int resUpdateCategory) {
		this.resUpdateCategory = resUpdateCategory;
	}

	public Integer getSlaTime() {
		return slaTime;
	}

	public void setSlaTime(Integer slaTime) {
		this.slaTime = slaTime;
	}

	public String getSlaUnit() {
		return slaUnit;
	}

	public void setSlaUnit(String slaUnit) {
		this.slaUnit = slaUnit;
	}

	public Integer getActualProcessTime() {
		return actualProcessTime;
	}

	public void setActualProcessTime(Integer actualProcessTime) {
		this.actualProcessTime = actualProcessTime;
	}

	public String getSlaViolationStatus() {
		return slaViolationStatus;
	}

	public void setSlaViolationStatus(String slaViolationStatus) {
		this.slaViolationStatus = slaViolationStatus;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public Integer getProvObjectId() {
		return provObjectId;
	}

	public void setProvObjectId(Integer provObjectId) {
		this.provObjectId = provObjectId;
	}

}
