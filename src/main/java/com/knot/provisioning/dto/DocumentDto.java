package com.knot.provisioning.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.knot.provisioning.bo.ProvisioningRequestBO;

public class DocumentDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String tenantID;

	private String partnerID;

	private String customerID;

	private String customerAccountID;

	@JsonIgnore
	private MultipartFile file;

	private String customerProofType;

	private String documentID;

	private String documentType;

	private String documentName;

	private String documentPath;

	private String documentContent;

	private String status;

	private String createdBy;

	private String createdIPAddr;

	private String modifiedBy;

	private String modifiedIPAddr;

	private String isMainTable;

	public String getTenantID() {
		return tenantID;
	}

	public void setTenantID(String tenantID) {
		this.tenantID = tenantID;
	}

	public String getPartnerID() {
		return partnerID;
	}

	public void setPartnerID(String partnerID) {
		this.partnerID = partnerID;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getCustomerAccountID() {
		return customerAccountID;
	}

	public void setCustomerAccountID(String customerAccountID) {
		this.customerAccountID = customerAccountID;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getCustomerProofType() {
		return customerProofType;
	}

	public void setCustomerProofType(String customerProofType) {
		this.customerProofType = customerProofType;
	}

	public String getDocumentID() {
		return documentID;
	}

	public void setDocumentID(String documentID) {
		this.documentID = documentID;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentPath() {
		return documentPath;
	}

	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public String getDocumentContent() {
		return documentContent;
	}

	public void setDocumentContent(String documentContent) {
		this.documentContent = documentContent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedIPAddr() {
		return createdIPAddr;
	}

	public void setCreatedIPAddr(String createdIPAddr) {
		this.createdIPAddr = createdIPAddr;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifiedIPAddr() {
		return modifiedIPAddr;
	}

	public void setModifiedIPAddr(String modifiedIPAddr) {
		this.modifiedIPAddr = modifiedIPAddr;
	}

	public String getIsMainTable() {
		return isMainTable;
	}

	public void setIsMainTable(String isMainTable) {
		this.isMainTable = isMainTable;
	}

	public DocumentDto(ProvisioningRequestBO request,MultipartFile file) {
		super();
		this.tenantID =  String.valueOf(request.getTenantID());
		this.partnerID = String.valueOf(request.getPartnerID());
		this.customerID = String.valueOf(request.getCustomerID());
		this.customerAccountID =  String.valueOf(request.getCustomerAccountID());
		this.file = file;
		this.customerProofType = null;
		this.documentID = null;
		this.documentType = "SAC";
		this.documentName = file.getOriginalFilename();
		this.documentPath = null;
		this.documentContent = null;
		this.status = null;
		this.createdBy = request.getCreatedBy();
		this.createdIPAddr = request.getCreatedIpaddr();
		this.modifiedBy = request.getModifiedBy();
		this.modifiedIPAddr = request.getModifiedIpaddr();
		this.isMainTable = "Y";
	}

}
