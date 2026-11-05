package com.knot.provisioning.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;

@Configuration
public class WsSecurityConfig {

	@Value("${OCE_USERNAME}")
	private String oceUsername;

	@Value("${OCE_PASSWORD}")
	private String ocePassword;

	@Bean
	public Wss4jSecurityInterceptor wsSecurityInterceptor() {

		Wss4jSecurityInterceptor interceptor = new Wss4jSecurityInterceptor();

		/** Require UsernameToken in incoming SOAP request */
		interceptor.setValidationActions("UsernameToken");

		/** Username/password validation */
		SimplePasswordValidationCallbackHandler callbackHandler = new SimplePasswordValidationCallbackHandler();

		Map<String, String> map = new HashMap<>();

		/** Add username and password from application.properties */
		map.put(oceUsername, ocePassword);
		callbackHandler.setUsersMap(map);

		interceptor.setValidationCallbackHandler(callbackHandler);

		return interceptor;
	}
}
