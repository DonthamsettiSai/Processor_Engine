package com.knot.provisioning.network;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.knot.provisioning.dto.DocumentDto;
import com.knot.provisioning.dto.MsisdnDTO;
import com.knot.provisioning.dto.MsisdnDtoResponse;
import com.knot.provisioning.dto.Response;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.AuthenticationUtil;
import com.knot.provisioning.utill.Constants;
import com.knot.tring.procesprov.schema.ActiveFirstRequest;

@Component
public class OMSNetwork {

	private static final Logger log = LogManager.getLogger(OMSNetwork.class);
	
	
	private RestTemplate restTemplate;
	private CacheService cache;
	UmsNetwork umsNetwork;
	private Gson gson;
	
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

	public boolean updateWorkOrderStatus(BigInteger workOrderID, int status, String remarks, String tenantID) {
		boolean flag = false;
		log.info("Started OMSNetwork :: updateWorkOrderStatus() :: ");
		try {
			String url = cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT) + "updateWorkOrder";
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
			.queryParam("modifiedBy", cache.strConfigVal(Constants.PROCESSOR_ADMIN, tenantID))
			.queryParam("status", status)
			.queryParam("workorderID", workOrderID)
			.queryParam("tenantID", tenantID)
			.queryParam("remarks", remarks);
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			String uriString = builder.build().encode().toUriString();
			log.info("URL :- {}",uriString);
			Response<Boolean> ordersResp = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Response<Boolean>>() {
			}).getBody();
			if(ordersResp != null) {
				flag = true;
				/** return ordersResp.getResult().get(0); */
			}
		} catch (Exception e) {
			log.error("OMSNetwork :: updateWorkOrderStatus() :: ",e);
			flag = false;
		}
		return flag;
	}
	
	
	 public String saveDocument(DocumentDto documentDto) {
	        log.info("Started OMSNetwork :: saveDocument() ");
	        ObjectMapper mapper = new ObjectMapper();
	        String ordersResp = null;
	        try {
	        	
	            String url = cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT) + "saveDocument";
	        	
	        	String token = umsNetwork.generateSuperUserToken();
	            log.info("Request url :: " + url + " request body ::" + mapper.writeValueAsString(documentDto));
	            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
	            formData.add("file", new ByteArrayResource(documentDto.getFile().getBytes()) {
	                @Override
	                public String getFilename() {
	                    return documentDto.getFile().getOriginalFilename();
	                }
	            });
	            formData.add("tenantID", documentDto.getTenantID());
	            formData.add("partnerID", documentDto.getPartnerID());
	            formData.add("customerID", documentDto.getCustomerID());
	            formData.add("customerAccountID", documentDto.getCustomerAccountID());
	            formData.add("customerProofType", documentDto.getCustomerProofType());
	            formData.add("documentID", documentDto.getDocumentID());
	            formData.add("documentType", documentDto.getDocumentType());
	            formData.add("documentName", documentDto.getDocumentName());
	            formData.add("documentPath", documentDto.getDocumentPath());
	            formData.add("status", documentDto.getStatus());
	            formData.add("createdBy", documentDto.getCreatedBy());
	            formData.add("createdIPAddr", documentDto.getCreatedIPAddr());
	            formData.add("modifiedBy", documentDto.getModifiedBy());
	            formData.add("modifiedIPAddr", documentDto.getModifiedIPAddr());
	            formData.add("isMainTable", documentDto.getIsMainTable());

	            HttpEntity<MultiValueMap<String, Object>> requestEntity = AuthenticationUtil
	                    .getHeaderWithTokenAndFormData(formData, token);

	            ordersResp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class).getBody();
	            if (ordersResp.contains("200")) {
	                log.info("Response from Omsnetwork " + ordersResp + " Ended OMSNetwork :: saveDocument() ");
	                return ordersResp;
	            } else {
	                ordersResp = null;
	                log.info("Response from Omsnetwork " + ordersResp + " Ended OMSNetwork :: saveDocument() ");
	            }
	        } catch (Exception e) {
	            log.error("Ended OMSNetwork :: saveDocument() :: ", e);
	        }
	        return ordersResp;
	    }
	 
//	 public boolean sendServerLvlDtls(ServiceLvlDetailsDTO dto) {
//			log.info("Stared OmsNetwork :: sendServerLvlDtls():: ");
//			String temp ="";
//			try {
//				String url = cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT)+ "updateServLvlDtls";
//				String token = umsNetwork.generateSuperUserToken();
//				HttpEntity<ServiceLvlDetailsDTO> httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token,dto);
//				log.info("OMS URL for  sendServerLvlDtls :- {}", url);
//				temp = gson.toJson(dto);
//				log.info("Input for sendServerLvlDtls dto {}", temp);
//				Response<String> ordersResp = restTemplate
//						.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<Response<String>>() {
//						}).getBody();
//				temp = gson.toJson(ordersResp);
//				log.info("Response From OmsNetwork :: sendServerLvlDtls {}",temp);
//				if (ordersResp != null) {
//					log.info(temp);
//					return true;
//				} else {
//					log.error("No response from OmsNetwork :: sendServerLvlDtls API.");
//					return false;
//				}	
//			} catch (Exception e) {
//				log.error("Error in sendServerLvlDtls() :: ", e);
//			}
//			log.info("Stared OmsNetwork :: sendServerLvlDtls():: ");
//			
//			return false;
//		}
	 
	 
	 public String getTraceID(BigInteger workOrderID, BigInteger tenantID, CacheService cache) {
			log.info("Stared OmsNetwork :: getTraceID():: with workOrderID {}, tenantID {}",workOrderID, tenantID);
      String correlationId = "";
			String url = cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT) + "getTraceIDByWorkOrderID";
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
					.queryParam("workOrderID", workOrderID).queryParam("tenantID", tenantID);
			String uriString = builder.build().encode().toUriString();
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			log.info("URL :- {}",uriString);
			Response<String> ordersResp = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, httpEntity, new ParameterizedTypeReference<Response<String>>() {
			}).getBody();
			if( ordersResp.getStatusCode().equalsIgnoreCase("200")) {
				correlationId = ordersResp.getResult().get(0);
			}
			log.info("Ended OmsNetwork :: getTraceID():: ");
			return correlationId;
		}

	 public MsisdnDtoResponse getWorkOrderByMsisdn(MsisdnDTO dto) {
			log.info("Started OmsNet :: getWorkOrderByMsisdn ");
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<MsisdnDTO> httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, dto, null);
			String url =  cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT)+"external/getWorkOrderByMsisdn";
			log.info("OmsNetwork :: getWorkOrderByMsisdn :: omsNetwork url {},MsisdnDTO {}",url,gson.toJson(dto));
			return restTemplate.exchange(url, HttpMethod.POST, httpEntity, new ParameterizedTypeReference<MsisdnDtoResponse>() {
			}).getBody();
		
	 }
	 
	 public String updateActiveFirst(MsisdnDTO dto) {
			log.info("Started OmsNet :: updateActiveFirst ");
			String token = umsNetwork.generateSuperUserToken();
			HttpEntity<String> httpEntity = AuthenticationUtil.getHeaderWithToken(token);
			String url = cache.strConfigVal(Constants.OMS_URL, Constants.GLOBAL_TENANT)
					+ "external/updateActiveFirst?msisdn="+dto.getMsisdn();
			log.info("OmsNetwork :: updateActiveFirst :: omsNetwork url {},MsisdnDTO {}", url, gson.toJson(dto));
			return restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<String>() {
			}).getBody();
		}
}
