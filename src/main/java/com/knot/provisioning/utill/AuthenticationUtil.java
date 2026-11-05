package com.knot.provisioning.utill;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.knot.provioning.model.ProvisioningRequestDetails;

/**
 * 
 * @author Lakshman
 *
 */

public class AuthenticationUtil {

	private static final String HEADER_KEY = "header.key";
	private static final String AUTHORIZATION = "Authorization";

	private AuthenticationUtil() {

	}

	private static final Logger loggerError = LogManager.getLogger(AuthenticationUtil.class);

	public static HttpEntity<String> getHttpEntity(String username, String pwd) {
		HttpHeaders headers = new HttpHeaders();
		String plainCreds = "";
		byte[] plainCredsBytes = null;
		byte[] base64CredsBytes = null;
		String base64Creds = "";
		Properties perProperties = loadProperties();
		try {
			plainCreds = username + ":" + pwd;
			plainCredsBytes = plainCreds.getBytes();
			base64CredsBytes = Base64.encodeBase64(plainCredsBytes, false);
			base64Creds = new String(base64CredsBytes);
			headers.add(AUTHORIZATION, "Basic " + base64Creds);
			headers.add(perProperties.getProperty(HEADER_KEY), "true");
			headers.setContentType(MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			loggerError.error(":::UmsRestController -- getHttpEntity :::", e);
			Arrays.asList(e.getStackTrace()).stream().forEach(loggerError::error);
		}
		return new HttpEntity<>(headers);
	}

	
	
	public static <T> HttpEntity<T> getHttpEntity(String username, String pwd, T t) {
		Properties perProperties = loadProperties();
		String plainCreds = username + ":" + pwd;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes, false);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add(AUTHORIZATION, "Basic " + base64Creds);
		headers.add(perProperties.getProperty(HEADER_KEY), "true");
		return new HttpEntity<>(t, headers);
	}

	public static <T> HttpEntity<T> getHeaderWithTokenAndObject(String token, T t,ProvisioningRequestDetails requestDtl) {
		Properties perProperties = loadProperties();
		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.hasText(token))
			headers.add(AUTHORIZATION, "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(perProperties.getProperty(HEADER_KEY), "true");
		if(requestDtl !=null && requestDtl.getServerName().equalsIgnoreCase("ekyc")) {
			headers.add("X-Source-System", "RAPTR");	
		}
		return new HttpEntity<>(t, headers);
	}

	 public static <T> HttpEntity<T> getHeaderWithTokenAndFormData(T t, String token) {
	        Properties perProperties = loadProperties();
	        HttpHeaders headers = new HttpHeaders();
	        if (StringUtils.hasText(token))
	            headers.add(AUTHORIZATION, "Bearer " + token);
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	        headers.add(perProperties.getProperty(HEADER_KEY), "true");
	        return new HttpEntity<>(t, headers);
	    }
	
	public static HttpEntity<String> getHeaderWithToken(String token) {
		Properties perProperties = loadProperties();
		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.hasText(token))
			headers.add(AUTHORIZATION, "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(perProperties.getProperty(HEADER_KEY), "true");
		return new HttpEntity<>(headers);
	}

	public static HttpHeaders getHeaderKeys(ProvisioningRequestDetails requestDetails) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		headers.set("X-Login", "xgate");
		headers.set("X-Password", "sql");
		return headers;
	}
	public static Properties loadProperties() {
		Properties properties = new Properties();

		try (InputStream inputStream = AuthenticationUtil.class.getClassLoader()
				.getResourceAsStream("application.properties")) {
			if (inputStream != null) {
				properties.load(inputStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}
	
	public static <T> HttpEntity<T> getHeaderWithTokenObjectWithCorrelationID(String token, T t, String correlationId) {
		Properties perProperties = loadProperties();
		HttpHeaders headers = new HttpHeaders();
		if (StringUtils.hasText(token))
			headers.add(AUTHORIZATION, "Bearer " + token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(perProperties.getProperty(HEADER_KEY), "true");
		if (StringUtils.hasText(correlationId)) {
			headers.add("correlationId", correlationId);
		}else {
			loggerError.info("Unable to add correlationId in headers because correlationId {} got", correlationId );
		}
		return new HttpEntity<>(t, headers);
	}
	
	
}
