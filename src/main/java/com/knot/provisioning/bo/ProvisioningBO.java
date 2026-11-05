package com.knot.provisioning.bo;

import java.util.List;

import com.knot.provioning.model.ProvisioningRequestDetails;

public class ProvisioningBO  {

	List<ProvisioningRequestDetails> parallelList = null;
	
	List<ProvisioningRequestDetails> sequenceList = null;

	public List<ProvisioningRequestDetails> getParallelList() {
		return parallelList;
	}

	public void setParallelList(List<ProvisioningRequestDetails> parallelList) {
		this.parallelList = parallelList;
	}

	public List<ProvisioningRequestDetails> getSequenceList() {
		return sequenceList;
	}

	public void setSequenceList(List<ProvisioningRequestDetails> sequenceList) {
		this.sequenceList = sequenceList;
	}

}