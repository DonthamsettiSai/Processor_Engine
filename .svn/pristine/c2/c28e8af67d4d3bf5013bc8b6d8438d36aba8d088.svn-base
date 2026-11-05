package com.knot.provisioning.soapclient;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class SoapConnector extends WebServiceGatewaySupport{
	
	public Object callWebService(String url, Object requestPayload) {
		return getWebServiceTemplate().marshalSendAndReceive(url, requestPayload);
	}

	public boolean callWebService(String url,Source requestPayload,Result responseResult) {
		return getWebServiceTemplate().sendSourceAndReceiveToResult(url, requestPayload, responseResult);
	}

}
