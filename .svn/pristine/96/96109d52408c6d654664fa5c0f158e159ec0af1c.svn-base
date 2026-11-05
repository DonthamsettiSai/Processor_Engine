package com.knot.provisioning.soapController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.knot.provisioning.controller.ProvisioningRequestController;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.soapService.ActiveFirstService;
import com.knot.provisioning.utill.Constants;
import com.knot.tring.procesprov.schema.ActiveFirstRequest;
import com.knot.tring.procesprov.schema.ActiveFirstResponse;

@Endpoint
public class ActiveFirstEndpoint {
	private static final Logger logger = LogManager.getLogger(ProvisioningRequestController.class);
	private static final String NAMESPACE_URI = "http://procesprov.com/";

	@Autowired
	private  ActiveFirstService activeFirstService;
	
	@Autowired
	CacheService cache;

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "activeFirstRequest")
	@ResponsePayload
	public ActiveFirstResponse activeFirst(@RequestPayload ActiveFirstRequest request) {
		logger.info("Started ActiveFirstEndpoint ActiveFirst SOAP request received. msisdn={}, source={}", request.getMsisdn(),
				request.getSource());
		ActiveFirstResponse response = new ActiveFirstResponse();
		try {
			boolean processActiveFirst = activeFirstService.processActiveFirst(request);
			if (processActiveFirst == true) {
				response.setStatusCode(cache.strConfigVal(Constants.SUCCESS, Constants.GLOBAL_TENANT));
				response.setStatusMessage(Constants.REQUEST_COMPLETION_SUCCESS);
				logger.info("Ended ActiveFirstEndpoint :: activeFirstResource() ");
			}
		} catch (Exception e) {
			response.setStatusCode(String.valueOf(HttpStatus.NO_CONTENT.value()));
			response.setStatusMessage(e.getMessage());
			logger.error("An Exception Occured in ActiveFirstEndpoint :: activeFirstResource() ", e);
		}
		return response;

	}
}
