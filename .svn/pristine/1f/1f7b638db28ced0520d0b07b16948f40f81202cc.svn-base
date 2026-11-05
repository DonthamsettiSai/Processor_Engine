package com.knot.provioning.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "provisioning_request_details")
@IdClass(ProvisioningRequestDetailPK.class)

public class ProvisioningRequestDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	Map response;

	@Transient
	String soapresponse;

	@Transient
	String responseString;

	@Id

	@Column(name = "REQUEST_DETAIL_ID")
	private BigInteger requestDetailID;

	@Id
	@Column(name = "VERSION_NO")
	private float version;

	@Column(name = "REQUEST_ID")
	private BigInteger requestID;

	@Column(name = "LOB_ID")
	private BigInteger lobID;

	@Column(name = "SERVICE_ID")
	private BigInteger serviceID;

	@Column(name = "TOKEN_ID")
	private String tokenID;

	@Column(name = "SERVICE_PACKAGE_ID")
	private BigInteger servicePackageID;

	@Column(name = "RATE_PLAN_ID")
	private BigInteger ratePlanID;

	@Column(name = "LOB_PRIORITY")
	private Integer lobPriority;

	@Column(name = "RATE_PLAN_PRIORITY")
	private Integer ratePlanPriority;

	@Column(name = "SEQUENCE_ID")
	private int sequenceID;

	@Column(name = "SUB_SEQUENCE_ID")
	private int subSequenceID;

	@Column(name = "DEPENDENT_SEQUENCE_ID")
	private int dependentSequenceID;

	@Column(name = "DEPENDENT_SUB_SEQUENCE_ID")
	private int dependentSubSequenceID;

	@Column(name = "GROUP_NO")
	private Integer group;

	@Column(name = "SUB_GROUP_NO")
	private Integer subGroup;

	@Column(name = "SERVICE_PRIORITY")
	private Integer servicePriority;

	@Column(name = "INTERFACE_TYPE")
	private String interfaceType;

	@Column(name = "SERVER_URL")
	private String serverURL;

	@Column(name = "REQUEST_OBJECT")
	private String requestObject;

	@Column(name = "SERVER_NAME")
	private String serverName;

	@Column(name = "REQUEST_METHOD")
	private String requestMethod;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "REQUEST_CREATION_DATE")
	private Date requestCreationDate;

	@Column(name = "REQUEST_COMPLETION_DATE")
	private Date requestCompletionDate;

	@Column(name = "REQUEST_PICKED_DATE")
	private Date requestPickedDate;

	@Column(name = "ACTUAL_PROCESS_TIME")
	private Integer actualProcessTime;

	@Column(name = "MAX_RETRIES")
	private Integer maxRetries;

	@Column(name = "TIME_GAP_BETWEEN_RETRY")
	private Integer timeGapBetweenRetry;

	@Column(name = "VERSION_DATE")
	private Date versionDate;

	@Column(name = "CURRENT_RETRY_COUNT")
	private Integer cureentRetryCount;

	@Column(name = "RESPONSE_UPDATE_CATEGORY")
	private Integer responseUpdateCategory;

	@Column(name = "PROV_OBJECT_ID")
	private Integer provObjectId;

	@Column(name = "IS_ASYNC")
	private String isAsync;

	@Column(name = "PROCESS_TYPE")
	private String processType;

	@Column(name = "REQ_SER_RESP_ID")
	private String reqServerRespID;

	@Column(name = "ADDL_COL1")
	private String addColumnOne;

	public String getReqServerRespID() {
		return reqServerRespID;
	}

	public void setReqServerRespID(String reqServerRespID) {
		this.reqServerRespID = reqServerRespID;
	}

	public Integer getProvObjectId() {
		return provObjectId;
	}

	public void setProvObjectId(Integer provObjectId) {
		this.provObjectId = provObjectId;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public Integer getCureentRetryCount() {
		return cureentRetryCount;
	}

	public void setCureentRetryCount(Integer cureentRetryCount) {
		this.cureentRetryCount = cureentRetryCount;
	}

	public Integer getResponseUpdateCategory() {
		return responseUpdateCategory;
	}

	public void setResponseUpdateCategory(Integer responseUpdateCategory) {
		this.responseUpdateCategory = responseUpdateCategory;
	}

	public BigInteger getRequestDetailID() {
		return requestDetailID;
	}

	public void setRequestDetailID(BigInteger requestDetailID) {
		this.requestDetailID = requestDetailID;
	}

	public BigInteger getRequestID() {
		return requestID;
	}

	public void setRequestID(BigInteger requestID) {
		this.requestID = requestID;
	}

	public BigInteger getServiceID() {
		return serviceID;
	}

	public void setServiceID(BigInteger serviceID) {
		this.serviceID = serviceID;
	}

	public String getTokenID() {
		return tokenID;
	}

	public void setTokenID(String tokenID) {
		this.tokenID = tokenID;
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

	public int getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}

	public Integer getServicePriority() {
		return servicePriority;
	}

	public void setServicePriority(Integer servicePriority) {
		this.servicePriority = servicePriority;
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

	public String getRequestObject() {
		return requestObject;
	}

	public void setRequestObject(String requestObject) {
		this.requestObject = requestObject;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getRequestCreationDate() {
		return requestCreationDate;
	}

	public void setRequestCreationDate(Date requestCreationDate) {
		this.requestCreationDate = requestCreationDate;
	}

	public float getVersion() {
		return version;
	}

	public void setVersion(float version) {
		this.version = version;
	}

	public Date getVersionDate() {
		return versionDate;
	}

	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}

	public BigInteger getLobID() {
		return lobID;
	}

	public void setLobID(BigInteger lobID) {
		this.lobID = lobID;
	}

	public Integer getLobPriority() {
		return lobPriority;
	}

	public void setLobPriority(Integer lobPriority) {
		this.lobPriority = lobPriority;
	}

	public Integer getRatePlanPriority() {
		return ratePlanPriority;
	}

	public void setRatePlanPriority(Integer ratePlanPriority) {
		this.ratePlanPriority = ratePlanPriority;
	}

	public int getSubSequenceID() {
		return subSequenceID;
	}

	public void setSubSequenceID(int subSequenceID) {
		this.subSequenceID = subSequenceID;
	}

	public int getDependentSequenceID() {
		return dependentSequenceID;
	}

	public void setDependentSequenceID(int dependentSequenceID) {
		this.dependentSequenceID = dependentSequenceID;
	}

	public int getDependentSubSequenceID() {
		return dependentSubSequenceID;
	}

	public void setDependentSubSequenceID(int dependentSubSequenceID) {
		this.dependentSubSequenceID = dependentSubSequenceID;
	}

	public Date getRequestCompletionDate() {
		return requestCompletionDate;
	}

	public void setRequestCompletionDate(Date requestCompletionDate) {
		this.requestCompletionDate = requestCompletionDate;
	}

	public Date getRequestPickedDate() {
		return requestPickedDate;
	}

	public void setRequestPickedDate(Date requestPickedDate) {
		this.requestPickedDate = requestPickedDate;
	}

	public Integer getActualProcessTime() {
		return actualProcessTime;
	}

	public void setActualProcessTime(Integer actualProcessTime) {
		this.actualProcessTime = actualProcessTime;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public Integer getSubGroup() {
		return subGroup;
	}

	public void setSubGroup(Integer subGroup) {
		this.subGroup = subGroup;
	}

	public Integer getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(Integer maxRetries) {
		this.maxRetries = maxRetries;
	}

	public Integer getTimeGapBetweenRetry() {
		return timeGapBetweenRetry;
	}

	public void setTimeGapBetweenRetry(Integer timeGapBetweenRetry) {
		this.timeGapBetweenRetry = timeGapBetweenRetry;
	}

	public Map getResponse() {
		return response;
	}

	public void setResponse(Map response) {
		this.response = response;
	}

	public String getSoapresponse() {
		return soapresponse;
	}

	public void setSoapresponse(String soapresponse) {
		this.soapresponse = soapresponse;
	}

	public String getResponseString() {
		return responseString;
	}

	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

	public String getIsAsync() {
		return isAsync;
	}

	public void setIsAsync(String isAsync) {
		this.isAsync = isAsync;
	}

	public String getProcessType() {
		return processType;
	}

	public void setProcessType(String processType) {
		this.processType = processType;
	}

	public String getAddColumnOne() {
		return addColumnOne;
	}

	public void setAddColumnOne(String addColumnOne) {
		this.addColumnOne = addColumnOne;
	}

}
