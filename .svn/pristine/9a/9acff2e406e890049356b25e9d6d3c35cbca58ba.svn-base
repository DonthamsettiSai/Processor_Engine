package com.knot.provisioning.soapclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.knot.provisioning.dto.Response;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

@Component
public class SoapClient {
	private static final Logger log = LogManager.getLogger(SoapClient.class);
	
	public Response<String> callSoapService(String soapRequestObject, String soapUrl) {
		Response<String> response = new Response<>();
		List<String> responseList = new ArrayList<>();
		try {
			MessageFactory messageFactory = MessageFactory.newInstance();
			SOAPMessage soapMessage = messageFactory.createMessage(new MimeHeaders(),
					new ByteArrayInputStream(soapRequestObject.getBytes()));
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			SOAPMessage soapResponse = soapConnection.call(soapMessage, soapUrl);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			responseList.add(new String(out.toByteArray()));
			response.setResult(responseList);			
			String res = response.getResult().get(0);
			if (soapResponse.getSOAPPart().getEnvelope().getBody().getFault() == null) {
				response.setStatusCode("Sucess");
				log.info("Sucess Response from Soap network server is {}",res);
			}else {
				response.setStatusCode("Failure");
				log.info("Failed Response from Soap network server server is {}",res);
				}
			soapConnection.close();
		} catch (SOAPException | IOException e) {
			responseList.add(e.getMessage());
			response.setStatusCode("Failure");
			response.setResult(responseList);
		}

		return response;

	}

}
