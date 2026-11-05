package com.knot.provisioning.bo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ConfigBo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "CONFIG_ID")
	private String configID;
	
	@Column(name = "TENANT_ID")
	private String tenantID;

	@Id	
	@Column(name = "EFFECTIVE_FROM")
	private String effectiveFrom;

	@Column(name = "CODE")
	private String code;

	@Column(name = "CONFIG_VALUE")
	private String value;

	public String getConfigID() {
		return configID;
	}

	public void setConfigID(String configID) {
		this.configID = configID;
	}

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	public String getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(String effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
