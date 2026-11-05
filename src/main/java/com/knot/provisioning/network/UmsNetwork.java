package com.knot.provisioning.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.knot.jwt.security.JwtService;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provisioning.dto.CreditionalsDTO;
import com.knot.provisioning.dto.GenerateTokenResponse;
import com.knot.provisioning.dto.ResponseDTO;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.Constants;

@Component
public class UmsNetwork {

	private static final Logger logger = LogManager.getLogger(UmsNetwork.class);
	
	RestTemplate restTemplate;
	
	CacheService cache;
	
	private JwtService jwtService;
	
	Gson gson;   

	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@Autowired
	public void setJwtService(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	public String generateSuperUserToken() {
		return jwtService.generateToken("Super", null);
	}

	public String generateExternalSystemToken(ProvisioningRequestDetails detail) throws Exception {
		CreditionalsDTO dto = null;
		String extUserName = cache.strConfigVal(Constants.EXT_SUPER_UN, Constants.GLB_1);
		String extPassword = cache.strConfigVal(Constants.EXT_SUPER_PWD, Constants.GLB_1);
		String headerKey = cache.strConfigVal(Constants.HEADER_KEY, Constants.GLB_1);
		String headerValue = cache.strConfigVal(Constants.HEADER_VALUE, Constants.GLB_1);
		String url = "";
		HttpHeaders headers = new HttpHeaders();
		ResponseDTO responseBody = new ResponseDTO();
		if (detail.getServerName().contains("SDPserver")) {
			dto = new CreditionalsDTO("sql", "admin");
			HttpEntity<CreditionalsDTO> httpEntity = new HttpEntity<>(dto);
			return httpEntity.toString();
		} else {
			dto = new CreditionalsDTO(extPassword, extUserName);
			url = cache.strConfigVal(Constants.AUTH_URL, Constants.GLB_1);
			headers.add(headerKey, headerValue);
			HttpEntity<CreditionalsDTO> httpEntity = new HttpEntity<>(dto, headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<ResponseDTO> barrerToken = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
					ResponseDTO.class);
			responseBody = barrerToken.getBody();
		}
		logger.info("Auth request details - URL: [{}], Headers: [{}], Credentials DTO: [dto {}], responseBody {}", url, headers, gson.toJson(dto), responseBody.toString());
		return StringUtils.hasText(responseBody.getAccess_token()) ? responseBody.getAccess_token() : "";
	}

	public String generateTokenWFM() {
		String url = cache.strConfigVal(Constants.WFM_URL, Constants.GLB_1) + "api/GenerateToken";
		/** Prepare headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		/** Prepare body using MultiValueMap for form data */
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("Username", Constants.WFM_USERNAME);
		requestBody.add("grant_type", Constants.WFM_GRANTTYPE);
		requestBody.add("Password", Constants.WFM_PASSWORD);
		requestBody.add("Source", Constants.WFM_SOURCE);
		/** Build the request */
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
		/** Send request */
		ResponseEntity<GenerateTokenResponse> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
				requestEntity, GenerateTokenResponse.class);
		return responseEntity.getBody().getAccess_token();
	}

}
