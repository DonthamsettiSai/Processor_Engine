package com.knot.provisioning.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FailDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

	private String deviceid;

	private String port;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPlaybookStatus() {
		return playbookStatus;
	}

	public void setPlaybookStatus(String playbookStatus) {
		this.playbookStatus = playbookStatus;
	}

	@JsonProperty("playbook_status")
	private String playbookStatus;

	public FailDto(FailDto dto) {
		super();
		this.id = dto.getId();
		this.deviceid = dto.getDeviceid();
		this.port = dto.getPort();
		this.playbookStatus = dto.getPlaybookStatus();
	}

	public FailDto() {
		super();
		// TODO Auto-generated constructor stub
	}

}
