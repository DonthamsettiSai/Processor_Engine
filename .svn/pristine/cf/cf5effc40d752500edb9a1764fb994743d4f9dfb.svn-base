package com.knot.provisioning.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.Constants;

/**
 * @author Sameena
 *
 */

public class Response<T> {

	/**
	 * 
	 */
	private String statusCode;

	@JsonInclude(Include.NON_NULL)
	private String statusMessage;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String totalDisplayCount;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<T> result;

	/** private boolean status; */

	public Response() {

	}

	public Response(String code, String message) {
		this.statusCode = code;
		this.statusMessage = message;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getTotalDisplayCount() {
		return totalDisplayCount;
	}

	public void setTotalDisplayCount(String totalDisplayCount) {
		this.totalDisplayCount = totalDisplayCount;
	}

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public void prepareSuccessMessageForList(List<T> list, CacheService cache) {
		this.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
		this.setStatusMessage(
				list == null || list.isEmpty() ? cache.strConfigVal(Constants.MSG, Constants.GLOBAL_TENANT)
						: cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
		this.setResult(list);
	}

	public void prepareFailureMessageForList(List<T> list, Exception e, CacheService cache) {
		this.setStatusCode(cache.strConfigVal(Constants.FAILURE_CODE, Constants.GLOBAL_TENANT));
		this.setStatusMessage(e.getMessage());
		this.setResult(list);
	}

	public void prepareSuccessMessageForObject(T t, CacheService cache) {
		this.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
		this.setStatusMessage(t == null ? cache.strConfigVal(Constants.MSG, Constants.GLOBAL_TENANT)
				: cache.strConfigVal(Constants.FLAG, Constants.GLOBAL_TENANT));
		this.setResult(Arrays.asList(t));
	}

	public void prepareFailureMessageForObject(T t, Exception e, CacheService cache) {
		this.setStatusCode(cache.strConfigVal(Constants.FAILURE_CODE, Constants.GLOBAL_TENANT));
		this.setStatusMessage(e.getMessage());
		this.setResult(t != null ? Arrays.asList(t) : null);
	}
}
