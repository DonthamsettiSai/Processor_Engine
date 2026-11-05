package com.knot.provisioning.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.knot.provisioning.soapclient.SoapConnector;

@Configuration
public class ConfigLoader {


	@Bean
	@Order(1)
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	SoapConnector soapConnector() {
		SoapConnector connector = new SoapConnector();
		connector.getWebServiceTemplate();
		connector.setDefaultUri("http://localhost:8080/ws");
		return connector;
	}

	@Bean
	@Order(2)
	Gson gson() {
		return new Gson();
	}
}
