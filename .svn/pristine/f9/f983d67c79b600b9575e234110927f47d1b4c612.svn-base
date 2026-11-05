package com.knot.provisioning.bo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * @author Sathish Ravula
 */

@Entity
public class ProvisioningDashBoadBO implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "total")
	@JsonProperty("Total")
	private String total;

	@Column(name = "success")
	@JsonProperty("Success")
	private String success;

	@Column(name = "Inprogress")
	@JsonProperty("Inprogress")
	private String inProgress;

	@Column(name = "subRequest")
	@JsonProperty("Subrequest")
	private String subRequest;

	@Column(name = "Failed")
	@JsonProperty("Failed")
	private String failed;

	/**
	 * @return the total
	 */
	public String getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(String total) {
		this.total = total;
	}

	/**
	 * @return the success
	 */
	public String getSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(String success) {
		this.success = success;
	}

	/**
	 * @return the failed
	 */
	public String getFailed() {
		return failed;
	}

	/**
	 * @param failed the failed to set
	 */
	public void setFailed(String failed) {
		this.failed = failed;
	}

	/**
	 * @return the subRequest
	 */
	public String getSubRequest() {
		return subRequest;
	}

	/**
	 * @param subRequest the subRequest to set
	 */
	public void setSubRequest(String subRequest) {
		this.subRequest = subRequest;
	}

	public String getInProgress() {
		return inProgress;
	}

	public void setInProgress(String inProgress) {
		this.inProgress = inProgress;
	}

}
