package com.knot.provisioning.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.knot.provisioning.dto.ActivityDTO;
import com.knot.provisioning.dto.ErrorCodeDTO;
import com.knot.provisioning.dto.ReferenceErrorCodeDTO;
import com.knot.provisioning.dto.Response;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.AuthenticationUtil;
import com.knot.provisioning.utill.Constants;
import com.knot.redis.dto.TenantsDTO;

@Component
public class RefMasterNetwork {

	private static final Logger log = LogManager.getLogger(RefMasterNetwork.class);

	RestTemplate restTemplate;
	UmsNetwork umsNetwork;
	CacheService cache;
	 Gson gson;
	
	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}
	@Autowired
	public void setUmsNetwork(UmsNetwork umsNetwork) {
		this.umsNetwork = umsNetwork;
	}
	
	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Cacheable(value="PROV_CONFIGURATIONS",key = "'getErrorCodes'")
	public Map<String, ErrorCodeDTO> getAllErrorCode() {
		Map<String, ErrorCodeDTO> map = new HashMap<>();
		try {
			String reqParam = "B";
			String token = umsNetwork.generateSuperUserToken();
			String url = cache.strConfigVal(Constants.DEF_URL, Constants.GLOBAL_TENANT);
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			String definitionURL = url + "getReferenceDataByKey?key=" + reqParam;
			log.info("Definition_URL :- {}" , definitionURL);
			ReferenceErrorCodeDTO errorDto = restTemplate
					.exchange(definitionURL, HttpMethod.GET, httpEntity, ReferenceErrorCodeDTO.class).getBody();
			if (errorDto!=null&&errorDto.getResult() != null && !errorDto.getResult().isEmpty()) {
				map = errorDto.getResult();
			}
		} catch (Exception e) {
			log.error("The Exception is Runner :: getAllErrorCode() " , e);
		}
		return map;
	}
	
	@Cacheable(value="PROV_CONFIGURATIONS",key = "'allTenants'")
	public List<TenantsDTO> getAllTenants() {
		log.info("The Start of  ReferenceMasterNetwork:: getAllTenants()");
		log.info("Cache LOG FOR -- Get ALL Tenants");
		String token = umsNetwork.generateSuperUserToken();
		HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
		String url = cache.strConfigVal(Constants.PMS_URL, Constants.GLOBAL_TENANT) + "/getAllTenants";
		log.info("GET All Tenants URL==  :: {}" , url);
		try {
			Response<TenantsDTO> result = restTemplate
					.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Response<TenantsDTO>>() {
					}).getBody();
			return Optional.ofNullable(result).map(Response::getResult).orElse(Collections.emptyList());
		
		} catch (Exception e) {
			log.error("Erro {}", e.getMessage());
		}
		return Collections.emptyList();
	}
	
	public List<ActivityDTO> getActivityInfo(String activityCode, String tenantID, String activityID) {
		try {
			ActivityDTO in = new ActivityDTO();
			in.setActivityCode(activityCode);
			in.setTenantID(tenantID);
			in.setActivityID(activityID);
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<ActivityDTO> requestEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, in, null);
			String url = cache.strConfigVal(Constants.DEF_URL, Constants.GLOBAL_TENANT) + "getAllActivities";
			log.info("URL {}", url);
			String json = gson.toJson(in);
			log.info("Input :- {}", json);
			Response<ActivityDTO> defResp = restTemplate.exchange(url, HttpMethod.POST, requestEntity,
					new ParameterizedTypeReference<Response<ActivityDTO>>() {
					}).getBody();
			if (defResp != null && defResp.getResult() != null && !defResp.getResult().isEmpty()) {
				return defResp.getResult();
			}
		} catch (Exception e) {
			log.error("Unable to fetch Activities for proovided input from definition master activityCode {} {} {}",
					activityCode, tenantID, activityID);
		}
		return new ArrayList<>();
	}
	
}
