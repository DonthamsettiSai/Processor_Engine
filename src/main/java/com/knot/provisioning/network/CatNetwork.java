package com.knot.provisioning.network;

import java.math.BigInteger;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.knot.provisioning.dto.RatePlanDTO;
import com.knot.provisioning.dto.Response;
import com.knot.provisioning.dto.ServiceDTO;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.AuthenticationUtil;
import com.knot.provisioning.utill.Constants;
import com.knot.redis.dto.OcsRPMapData;

@Component
public class CatNetwork {

	/**
	 * private static final Logger logger = LogManager.getLogger(CatNetwork.class);
	 */

	private static final Logger log = LogManager.getLogger(CatNetwork.class);

	RestTemplate restTemplate;
	UmsNetwork umsNetwork;
	CacheService cache;

	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@Autowired
	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	public void setUmsNetwork(UmsNetwork umsNetwork) {
		this.umsNetwork = umsNetwork;
	}

	public List<ServiceDTO> getServices(String tenantID, String partnerID) {
		List<ServiceDTO> serviceList = null;
		ServiceDTO serviceDTO = new ServiceDTO();
		String token = umsNetwork.generateSuperUserToken();
		HttpEntity<ServiceDTO> httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, serviceDTO, null);
		String servicesUrl = cache.strConfigVal(Constants.CAT_URL, Constants.GLOBAL_TENANT)
				+ "getAllServices?tenantID=" + tenantID + "&partnerID=" + partnerID;
		Response<ServiceDTO> resServiceDTO = restTemplate.exchange(servicesUrl, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<Response<ServiceDTO>>() {
				}).getBody();
		serviceList = resServiceDTO != null ? resServiceDTO.getResult() : new ArrayList<>();
		return serviceList;
	}

	public List<RatePlanDTO> getRatePlanTypes(List<Long> ratePlanIds) {
		log.info("Started  CatNetwork : - getRatePlanTypes {}");
		List<RatePlanDTO> list = new ArrayList<>();
		try {
			String rateplanId = ratePlanIds.toString().replaceAll("\\[|\\]", "");
			String url = cache.strConfigVal(Constants.CAT_URL, Constants.GLOBAL_TENANT)
					+ "getAllRatePlanIDs?ratePlanID=" + rateplanId;
			log.info("CAT URL : - getRatePlanTypes {}", url);
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			Response<RatePlanDTO> ordersResp = restTemplate
					.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Response<RatePlanDTO>>() {
					}).getBody();
			if (ordersResp != null && ordersResp.getResult() != null && !ordersResp.getResult().isEmpty()) {
				list = ordersResp.getResult();
				log.info("Response From Catlouge Network  : - getRatePlanTypes {} " + list.size());
			}
			log.info("Ended CatNetwork  : - getRatePlanTypes {}");
		} catch (Exception e) {
			log.error("An Exception Occured in CatNetwork : - getRatePlanTypes {}", e);
		}
		return list;

	}

	@Cacheable(value = "PROV_CONFIGURATIONS", key = "'OCS_RATE_PLANS'")
	public List<OcsRPMapData> loadOCSData() {
		String url = "";
		try {
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			url = cache.strConfigVal(Constants.CAT_URL, Constants.GLOBAL_TENANT) + "getAllOcsRatePlanMappings";
			log.info("Catalogue URL:- {}", url);
			Response<OcsRPMapData> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity,
					new ParameterizedTypeReference<Response<OcsRPMapData>>() {
					}).getBody();
			if (response != null && response.getResult() != null) {
				return response.getResult();
			} else
				throw new ProviderException("PROBLEM_FETCHING_PARAMETERS ");
		} catch (Exception e) {
			log.error("CatalogueNetwork :: loadOCSData", e);
			throw e;
		}
	}

}
