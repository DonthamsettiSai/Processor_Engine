package com.knot.provisioning.soapServiceimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.knot.provisioning.dto.MsisdnDTO;
import com.knot.provisioning.service.ProvisioningService;
import com.knot.provisioning.soapService.ActiveFirstService;
import com.knot.tring.procesprov.schema.ActiveFirstRequest;

@Service
public class ActiveFirstServiceImpl implements ActiveFirstService {

	
	@Autowired
	private ProvisioningService provisioningService;
	
	private static final Logger log = LogManager.getLogger(ActiveFirstServiceImpl.class);

	@Override
	public boolean processActiveFirst(ActiveFirstRequest request) throws Exception {
		log.info("Started ActiveFirstServiceImpl :: processActiveFirst()");
		boolean activeFirstResource = false;
		try {
			validateRequest(request);
			String msisdn = request.getMsisdn();
			String source = request.getSource();
			MsisdnDTO msisdnDto = new MsisdnDTO(request);
			log.info("Processing ActiveFirst request. msisdn={}, source={}", msisdn, source);
			 activeFirstResource = provisioningService.activeFirstResource(msisdnDto);

		} catch (Exception e) {
			log.error("An Exception Occured in ActiveFirstServiceImpl ::  processActiveFirst() request", e);
			activeFirstResource = false;
			throw e;
		}
		return activeFirstResource;
	}

	private void validateRequest(ActiveFirstRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request cannot be null");
		}
		if (!StringUtils.hasText(request.getMsisdn())) {
			throw new IllegalArgumentException("MSISDN cannot be empty");
		}
		if (!StringUtils.hasText(request.getSource())) {
			throw new IllegalArgumentException("Source cannot be empty");
		}
	}
}
