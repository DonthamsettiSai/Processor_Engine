package com.knot.provisioning.utill;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knot.provioning.model.ProvRespDetails;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provioning.model.ProvisioningRequestProcessingError;
import com.knot.provioning.model.ProvisioningRequestResponseDetail;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.dto.CreditionalsDTO;
import com.knot.provisioning.dto.ResponseDTO;
import com.knot.provisioning.network.UmsNetwork;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.soapclient.SoapConnector;

/**
 * @author Sai D
 *
 */

public class FileUtil {

	private static final Logger logger = LogManager.getLogger(FileUtil.class);

	static List<String> listOfValues = new ArrayList<>();

	static Map<String, String> finalResponseMap = new HashMap<>();

	private FileUtil() {

	}

	public static boolean checkEmptyNull(String val) {
		try {
			if (Objects.nonNull(val) && !val.trim().isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("The Exception is CheckEmptyNull :: checkEmptyNull()", e);
		}
		return false;
	}

	public static boolean getBooleanValue(String val) {
		try {
			if (Objects.nonNull(val) && !val.trim().isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("The Exception is CheckEmptyNull :: getBooleanValue()", e);
		}
		return false;
	}

	public static String getStrValue(String val) {
		try {
			if (Objects.nonNull(val) && !val.trim().isEmpty()) {
				return val;
			}
		} catch (Exception e) {
			logger.error("The Exception is checkStrEmptyNull :: getStrValue()", e);
		}
		return Constants.GLOBAL_FLAG;
	}

	public static Integer getIntValue(String val) {
		try {
			if (Objects.nonNull(val) && !val.isEmpty()) {
				return Integer.parseInt(val.trim());
			}
		} catch (Exception e) {
			logger.error("The Exception is checkStrEmptyNull :: checkStrEmptyNull()", e);
		}
		return Integer.parseInt(Constants.GLOBAL_FLAG);
	}

	public static int intVal(String val) {
		try {
			if (Objects.nonNull(val) && !val.isEmpty()) {
				return Integer.parseInt(val.trim());
			}
		} catch (Exception e) {
			logger.error("The Exception is checkStrEmptyNull :: checkStrEmptyNull()", e);
		}
		return 0;
	}

	public static BigDecimal getBigDecimalValue(String val) {
		try {
			if (Objects.nonNull(val)) {
				return new BigDecimal(val.trim());
			}
		} catch (Exception e) {
			logger.error("The Exception is checkIntEmptyNull :: checkIntEmptyNull()", e);
		}
		return new BigDecimal("0.00");
	}

	public static String getInQueryValue(String value) {
		String result = "";
		try {
			String[] array = value.split(",");
			StringBuilder builder = new StringBuilder();
			int count = 0;
			for (String val : array) {
				count++;
				if (count != array.length) {
					builder.append("'").append(val).append("'").append(",");
				} else {
					builder.append("'").append(val).append("'");
				}
			}
			result = builder.toString();
		} catch (Exception e) {
			logger.error("The Exception is FileUtil :: getInQueryValue ", e);
		}
		return result;
	}

	public static List<String> getCommaSeparateStringInQuery(List<Object[]> contractIDsList) {
		StringBuilder builder = new StringBuilder();
		StringBuilder builder1 = new StringBuilder();
		List<String> resultList = new ArrayList<>();
		String date = "";
		try {
			for (Object[] obj : contractIDsList) {
				date = obj[2].toString();
				if (builder.length() > 0) {
					builder.append(',');
				}
				builder.append(obj[0].toString());

				if (builder1.length() > 0) {
					builder1.append(',');
				}
				builder1.append(obj[1].toString());
			}
			resultList.add(getInQueryValue(builder.toString()));
			resultList.add(builder1.toString());
			resultList.add(date);
		} catch (Exception e) {
			logger.error("The Exception is FileUtil :: getCommaSeparateStringInQuery ", e);
		}
		return resultList;
	}

	/**
	 * public static String getUrlParametrsReplacement(String serverUrl, String
	 * regExp, String prameter, Map<Object, Object> map) {
	 * logger.info("Enterd into FileUtil :: getUrlParametrsReplacement :: " +
	 * serverUrl); try { Map<Object, Object> map2 = (Map<Object, Object>)
	 * ((Map<Object, Object>) map.get("result")) .get("resultObject"); Pattern
	 * pattern = Pattern.compile(regExp); Matcher matcher =
	 * pattern.matcher(serverUrl);
	 * 
	 * while (matcher.find()) { if (map2.containsKey("code")) { serverUrl =
	 * serverUrl.replace(prameter + matcher.group(1) + "|",
	 * map2.get(matcher.group(1)).toString()); } serverUrl =
	 * serverUrl.replace(prameter + matcher.group(1) + "|",
	 * map2.get(matcher.group(1)).toString()); }
	 * logger.info("Ended FileUtil :: getUrlParametrsReplacement :: " + serverUrl);
	 * 
	 * } catch (Exception e) {
	 * logger.error("The Exception is FileUtil :: getUrlParametrsReplacement ", e);
	 * } return serverUrl; }
	 */

	/** This method is used to get the map */

	public void getMapResopnse(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		Map jsonMap = new HashMap<>();
		try {
			jsonMap = mapper.readValue(responseBody, Map.class);
		} catch (Exception e) {
			logger.error("Ended FileUtil :: getMapResopnse :: " + jsonMap);
		}
	}

	public static Map getJsonOrXmlMap(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		Map jsonMap = new HashMap<>();
		String responseType = getResponseType(responseBody);
		try {
			if (responseType.equalsIgnoreCase("json")) {
				jsonMap = mapper.readValue(responseBody, Map.class);
			} else {
				String convertXmlToJson = convertXmlToJson(responseBody);
				jsonMap = mapper.readValue(convertXmlToJson, Map.class);
			}
		} catch (Exception e) {
		}
		return jsonMap;

	}

	public static String getRequestObjectType(String requestObject) {
		if (requestObject != null && (requestObject.startsWith("{") || requestObject.startsWith("["))) {
			return "JSON";
		} else if (requestObject != null && requestObject.startsWith("<")) {
			return "XML";
		} else {
			return "";
		}
	}

	public static String getResponseType(String responseBody) {
		if (responseBody != null && (responseBody.startsWith("{") || responseBody.startsWith("["))) {
			return "JSON";
		} else if (responseBody != null && responseBody.startsWith("<")) {
			return "XML";
		} else {
			return "Unknown";
		}
	}

	public static String convertXmlToJson(String xml) {
		JSONObject jsonObject = new JSONObject();
		try {
			if (StringUtils.hasText(xml)) {
				jsonObject = XML.toJSONObject(xml);
			}
			return jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static JSONObject getMapToJsonObject(Map<Object, Object> map) {
		ObjectMapper mapper = new ObjectMapper();
		JSONObject jsonObj = null;
		String jsonString = "";
		try {
			jsonString = mapper.writeValueAsString(map);
			jsonObj = new JSONObject(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj;

	}

	public static ResponseEntity<String> sendReq(ProvisioningRequestDetails reqDetail, CacheService cache,
			String tenantID, UmsNetwork umsNet, RestTemplate restTemplate, SoapConnector connector, ProvisioningDao dao) throws Exception {
		logger.info("Entred in  FileUtil :: sendReq () ");
		String requestObj = reqDetail.getRequestObject();
		String token = "";
		HttpEntity<String> httpEntity = null;
		
		/** String soapResponse = "";
		 * requestObj =
		 * requestObj.replaceAll(cache.strConfigVal(Constants.DOWN_STREAM_PARAM_REGEX,
		 * tenantID), ""); requestObj =
		 * requestObj.replaceAll(cache.strConfigVal(Constants.SUB_REQ_PARAM_REGEX,
		 * tenantID), ""); requestObj =
		 * requestObj.replaceAll(cache.strConfigVal(Constants.BOTH_REGEX, tenantID),
		 * "");
		 */
		String requestObjectType = FileUtil.getRequestObjectType(requestObj);
		ResponseEntity<String> response = null;

		if (reqDetail.getInterfaceType().equalsIgnoreCase(Constants.REST_CALL)) {
			logger.info("Entred in  FileUtil :: sendReq () reqDetail is Rest call ");
			if (requestObjectType.equalsIgnoreCase("json")) {
				if (reqDetail.getServerURL().contains("webhook")) {
					httpEntity = new HttpEntity<>(requestObj);
				} else if ((reqDetail.getServerURL().contains("tmf-api"))) {
					token = umsNet.generateExternalSystemToken(reqDetail);
				} else if ((reqDetail.getServerName().equalsIgnoreCase("ProteiSDPserver"))) {
					httpEntity = AuthenticationUtil.getHttpEntity("admin", "sql");
				} else if ((reqDetail.getServerName().equalsIgnoreCase("wfm"))) {
					token = umsNet.generateTokenWFM();
				}else if(reqDetail.getServerName().equalsIgnoreCase("ekyc")) {
					token = getAccessToken(cache);
				} else {
					token = umsNet.generateSuperUserToken();
				}
				if (!StringUtils.hasText(token)) {
					return response;
				}
				httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, requestObj, reqDetail);
				logger.info("Sending request to Server "+ reqDetail.getServerName() +" URL :: "+reqDetail.getServerURL() +" HttpEntity {}, requestObj{} ",httpEntity.toString(), requestObj);
				response = restTemplate.exchange(reqDetail.getServerURL(),
						HttpMethod.valueOf(reqDetail.getRequestMethod()), httpEntity, String.class);
			} else {
				HttpHeaders headers = AuthenticationUtil.getHeaderKeys(reqDetail);
				HttpEntity<String> entity = new HttpEntity<>(reqDetail.getRequestObject(), headers);
				response = restTemplate.exchange(reqDetail.getServerURL(),
						HttpMethod.valueOf(reqDetail.getRequestMethod()), entity, String.class);
			}
		} else if (reqDetail.getInterfaceType().equalsIgnoreCase(Constants.SOAP_CALL)) {
			logger.info("Entred in  FileUtil :: sendReq () reqDetail is Soap call ");
			String headerParam = cache.strConfigVal(Constants.HEADER_PARAM, Constants.GLB_1);
			String soapReqObj = reqDetail.getRequestObject();
			if (StringUtils.hasText(soapReqObj) && soapReqObj.contains("Header")) {
			    List<String> headersRegexPipes = FileUtil.getHeadersRegexPipes(soapReqObj);
			    for (String header : headersRegexPipes) {
			        String value = cache.strConfigVal(header, Constants.GLB_1);
			        if (StringUtils.hasText(value)) {
			            soapReqObj = soapReqObj.replace(headerParam + header, value);
			        }
			    }
			}
			reqDetail.setRequestObject(soapReqObj);
			logger.info("Updating soapReqObj  requestDetailObject = {}", reqDetail.getRequestObject());
			dao.updateRequestObject(reqDetail.getRequestDetailID(), soapReqObj);
			logger.info("SOAP call url = {}, requestDetailObject = {}", reqDetail.getServerURL(), soapReqObj);
			response = callSoapService(reqDetail.getServerURL(), soapReqObj);
			logger.info("SOAP response:{} ", response);
			/** HttpHeaders headers = AuthenticationUtil.getHeaderKeys(reqDetail); */
			/**
			 * HttpHeaders headers = new HttpHeaders(); HttpEntity<String> entity = new
			 * HttpEntity<>(reqDetail.getRequestObject(), headers); response =
			 * restTemplate.exchange(reqDetail.getServerURL(),
			 * HttpMethod.valueOf(reqDetail.getRequestMethod()), entity, String.class);
			 *
			 * This line used for completed soap calls
			StreamSource source = new StreamSource(new StringReader(soapReqObj));
			StringWriter swResponse = new StringWriter();
			Result responseResult = new StreamResult(swResponse);
			if (connector != null) {
			  /** callWebService Sends it to a SOAP web service 
				*boolean flag = connector.callWebService(reqDetail.getServerURL(), source, responseResult);
				*if (flag) {
					logger.info("After callWebService got sucess in  FileUtil :: sendReq () reqDetail is Soap call flag {}", flag);
					  soapResponse = swResponse.toString();
					  response = ResponseEntity.ok(soapResponse);
				}
				logger.info("SOAP success flag = {}", flag);
				logger.info("SOAP response:{} ", response);
			}**/
		}else if (reqDetail.getInterfaceType().equalsIgnoreCase(Constants.LOCAL_CALL)) {
			token = umsNet.generateSuperUserToken();
			httpEntity = AuthenticationUtil.getHeaderWithTokenAndObject(token, requestObj, reqDetail);
			logger.info("Sending request to Server "+ reqDetail.getServerName() +" URL :: "+reqDetail.getServerURL() +" HttpEntity {}, requestObj{} ",httpEntity.toString(), requestObj);
			response = restTemplate.exchange(reqDetail.getServerURL(),
					HttpMethod.valueOf(reqDetail.getRequestMethod()), httpEntity, String.class);
		}
		return response;
	}
		

	/** *@apiNote This API is used for T-fiber Integration */
	public static ResponseEntity<String> ciscoApiCall(ProvisioningRequestDetails reqDetail, CacheService cache,
			RestTemplate restTemplate) {
		HttpEntity<String> httpEntity;
		ResponseEntity<String> response = null;
		String ciscoUser = "";
		String ciscoPassw = "";
		String ciscoUrl = "";
		ResponseDTO responseBody = new ResponseDTO();
		HttpEntity<String> entityWithHeaders = null;
		HttpEntity<String> entityWithBody = null;

		if (!StringUtils.hasText(reqDetail.getTokenID()) || !reqDetail.getTokenID().equalsIgnoreCase("y")) {
			ciscoUser = cache.strConfigVal(Constants.CISCO_USER_NAME, Constants.GLB_1);
			ciscoPassw = cache.strConfigVal(Constants.CISCO_PWD, Constants.GLB_1);
			httpEntity = AuthenticationUtil.getHttpEntity(ciscoUser, ciscoPassw);
			entityWithHeaders = new HttpEntity<>(reqDetail.getRequestObject(), httpEntity.getHeaders());
			logger.info("Sending request to Server "+ reqDetail.getServerName() +" URL :: "+reqDetail.getServerURL() + " HttpEntity {}, requestObj{} ",httpEntity.toString(), reqDetail.getRequestObject());
			response = restTemplate.exchange(reqDetail.getServerURL(), HttpMethod.valueOf(reqDetail.getRequestMethod()),
					entityWithHeaders, String.class);
		}
		if (StringUtils.hasText(reqDetail.getTokenID()) && reqDetail.getTokenID().equalsIgnoreCase("y")) {
			ciscoUser = cache.strConfigVal(Constants.CISCO_USER_NAME, Constants.GLB_1);
			ciscoPassw = cache.strConfigVal(Constants.CISCO_PWD, Constants.GLB_1);
			ciscoUrl = cache.strConfigVal(Constants.CISCO_URL, Constants.GLB_1);
			CreditionalsDTO dto = new CreditionalsDTO(ciscoPassw, ciscoUser);
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<CreditionalsDTO> httpEntityDto = new HttpEntity<>(dto, headers);
			ResponseEntity<ResponseDTO> barrerToken = restTemplate.exchange(ciscoUrl, HttpMethod.POST, httpEntityDto,ResponseDTO.class);
			responseBody = barrerToken.getBody();
			String token = responseBody.getAccess_token();
			entityWithBody = AuthenticationUtil.getHeaderWithTokenAndObject(token, reqDetail.getRequestObject(),reqDetail);
			logger.info("Sending request to Server "+ reqDetail.getServerName() +" URL :: "+reqDetail.getServerURL() +" HttpEntity {}, requestObj{} ",entityWithBody.toString(), reqDetail.getRequestObject());
			response = restTemplate.exchange(reqDetail.getServerURL(), HttpMethod.valueOf(reqDetail.getRequestMethod()),
					entityWithBody, String.class);
		}
		return response;
	}

	public static ProvisioningRequestProcessingError saveProvisioningRequestProcessorErrors(String errorCode,
			String response, ProvisioningRequestDetails detail, String errorDesc, CacheService cache) {
		ProvisioningRequestProcessingError obj = new ProvisioningRequestProcessingError();
		Integer status = null;
		obj.setRequestID(detail.getRequestID());
		/** obj.setVersionNo(detail.getVersion()); */
		obj.setRequestDetailID(detail.getRequestDetailID());
		obj.setErrorFoundDate(new Date());
		obj.setStatus(15);
		obj.setErrorCode(errorCode != null ? errorCode : null);
		obj.setRemarks(Constants.TOKEN_ERR);
		status = cache.intConfigVal(Constants.FAILED_WHILE_RETRY, Constants.GLOBAL_TENANT);
		obj.setStatus(status);
		obj.setErrorDescription(!errorDesc.isBlank() ? errorDesc : Constants.NO_RESP_FROM_SERVER);
		return obj;
	}

	public static ProvisioningRequestResponseDetail setProvisioningRequestResponseDetailsData(ObjectMapper mapper,
			ProvisioningRequestDetails detail, CacheService cache, String response, String tenantID, int retries) {
		ProvisioningRequestResponseDetail obj = new ProvisioningRequestResponseDetail();
		obj.setRequestDetailID(detail.getRequestDetailID());
		obj.setRequestID(detail.getRequestID());
		obj.setVersion(1);
		obj.setResponseObjectType(cache.strConfigVal(Constants.RESP_TYPE, tenantID));
		obj.setResponseReceivedDate(new Date());
		obj.setResponseObject(response);
		obj.setResponseSequenceNo(retries);
		return obj;
	}

	public static String getReplacedRequestParams(String request, Iterator<?> it, CacheService cache, String tenantID) {

		String key;
		String toReplace;

		while (it.hasNext()) {
			Map.Entry<?, ?> entry7 = (Map.Entry<?, ?>) it.next();
			key = entry7.getKey().toString();

			if (key.contains(cache.strConfigVal(Constants.SUB_REQ_PARAM, tenantID))) {
				key = key.replace(cache.strConfigVal(Constants.SUB_REQ_PARAM, tenantID), "");
				if (request.contains(key)) {
					toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID) + key;

					request = request.replace(toReplace, entry7.getValue().toString());
				}
			}
			if (key.contains(cache.strConfigVal(Constants.BOTH, tenantID))) {
				key = key.replace(cache.strConfigVal(Constants.BOTH, tenantID), "");
				if (request.contains(key)) {
					toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID) + key;
					request = request.replace(toReplace, entry7.getValue().toString());
				}
			}
			if (key.contains(cache.strConfigVal(Constants.DOWN_STREAM_PARAM, tenantID))) {
				key = key.replace(cache.strConfigVal(Constants.DOWN_STREAM_PARAM, tenantID), "");
				if (request.contains(key)) {
					toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID) + key;
					request = request.replace(toReplace, entry7.getValue().toString());
				}
			}
		}
		return request;
	}

	public static synchronized Map<String, String> convertResponseJsonToMap(String responseString) throws Exception {
		finalResponseMap.clear();
		JSONObject jsonObject = new JSONObject(responseString);
		if (jsonObject != JSONObject.NULL) {
			toMap(jsonObject);
		}
	    return new HashMap<>(finalResponseMap);
	}

	public static Map<String, Object> toMap(JSONObject object) throws Exception {
		Map<String, Object> map = new HashMap<>();
		Iterator<String> keysItr = object.keys();
		
		logger.info("start toMap listOfValues: {} replace JSON {}", listOfValues,object);

		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof String) {
				String stringValue = (String) value;
				if (stringValue.startsWith("{") || stringValue.startsWith("[")) {
					try {
						if (stringValue.startsWith("{")) {
							value = toMap(new JSONObject(stringValue));
						} else if (stringValue.startsWith("[")) {
							value = toList(new JSONArray(stringValue), key);
						}
					} catch (Exception e) {
						listOfValues.add(stringValue.trim());
						updateFinalResponseMap(key, stringValue.trim());
					}
				} else {
					listOfValues.add(stringValue.trim());
					updateFinalResponseMap(key, stringValue.trim());
				}
			} else if (value instanceof JSONArray) {
				value = toList((JSONArray) value, key);
			} else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			} else {
				listOfValues.add(value.toString().trim());
				updateFinalResponseMap(key, value.toString().trim());
			}

			logger.info("End toMap listOfValues: {}", listOfValues);
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array, String key) throws Exception {
		List<Object> list = new ArrayList<>();

		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value, key);
			} else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			} else if (value instanceof String
					&& (((String) value).startsWith("{") || ((String) value).startsWith("["))) {
				try {
					String stringValue = (String) value;
					if (stringValue.startsWith("{")) {
						value = toMap(new JSONObject(stringValue));
					} else {
						value = toList(new JSONArray(stringValue), key);
					}
				} catch (Exception e) {
					listOfValues.add(value.toString().trim());
					updateFinalResponseMap(key, value.toString().trim());
				}
			} else {
				listOfValues.add(value.toString().trim());
				updateFinalResponseMap(key, value.toString().trim());
			}

			list.add(value);
		}
		return list;
	}

	private static void updateFinalResponseMap(String key, String value) {
	        if (finalResponseMap.containsKey(key)) {
	            finalResponseMap.put(key.trim(), finalResponseMap.get(key) + "," + value);
	        } else {
	            finalResponseMap.put(key.trim(), value);
	        }
	}

	public static List<String> foundResponseReplaceValues(String jsonString) {
		List<String> rValues = new ArrayList<>();
		/** Pattern pattern = Pattern.compile("r\\|([^\\s\",]+)"); */
		Pattern pattern = Pattern.compile("r\\|([a-zA-Z0-9_]+)");
		Matcher matcher = pattern.matcher(jsonString);
		while (matcher.find()) {
			rValues.add(matcher.group(1).trim());
		}

		return rValues;
	}

	public static List<String> getResponseRegexPipes(String jsonString) {
		List<String> rValues = new ArrayList<>();
		 Pattern pattern = Pattern.compile("r\\|([^&/]+)");
		Matcher matcher = pattern.matcher(jsonString);
		while (matcher.find()) {
			rValues.add(matcher.group(1));
		}
		return rValues;
	}
	
	
	public static List<String> getHeadersRegexPipes(String jsonString) {
		List<String> oValues = new ArrayList<>();
		Pattern pattern = Pattern.compile("H\\|([\\w-]+)");
		Matcher matcher = pattern.matcher(jsonString);
		while (matcher.find()) {
			oValues.add(matcher.group(1));
		}
		return oValues;
	}

	public static String getProvisioningResponseCode(ProvisioningRequestDetails detailObj, CacheService cache) {
		logger.info("Started  ProvisioningServiceImpl :: getProvisioningResponseCode () ::  ");
		String responseCode = "";
		try {
			ProvRespDetails responseDetailsMap = cache.getResponseDetailsMap(detailObj.getProvObjectId());
			responseCode = responseDetailsMap.getResponseCode();
		} catch (Exception e) {
			logger.error("An Exception Occured in FileUtil :: getProvisioningResponseCode() : {}", e);
		}
		logger.info(
				"Ended  ProvisioningServiceImpl :: getProvisioningResponseCode () :: Response code from ProvRespDetails is ",
				responseCode);
		return responseCode;
	}
	
	public static String getProvisioningResponseObject(ProvisioningRequestDetails detailObj, CacheService cache) {
		logger.info("Started  ProvisioningServiceImpl :: getProvisioningResponseObject () ::  ");
		String responseObj = "";
		try {
			ProvRespDetails responseDetailsMap = cache.getResponseDetailsMap(detailObj.getProvObjectId());
			responseObj = responseDetailsMap.getApiObject();
		} catch (Exception e) {
			logger.error("An Exception Occured in FileUtil :: getProvisioningResponseObject() : {}", e);
		}
		logger.info(
				"Ended  ProvisioningServiceImpl :: getProvisioningResponseObject () :: Response code from ProvRespDetails is ",responseObj);
		return responseObj;
	}
	
	public static Map<String, String> convertResponseIntoMap(ProvisioningRequest request, ObjectMapper mapper, String response) {
		Map<String, String> responseFinalMap = new HashMap<>();
		try {
			responseFinalMap = FileUtil.convertResponseJsonToMap(response);
			responseFinalMap.put("workOrderID", request.getWorkOrderID().toString());
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		} catch (Exception e) {
			logger.info("An error Occured in  TFiberDynamicServiceImpl getResponseFinalMap() :: "+ e);
	 }
		return responseFinalMap;

	}
	
	public static ResponseEntity<String> callSoapService(String url, String soapRequest) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST,requestEntity,String.class);
        return response;
	}
	
	
	public static String getAccessToken(CacheService cache) {
		try {
			logger.info("EKYC Auth ::  getAccessToken :: Starting token generation");		
			String tokenUrl = cache.strConfigVal(Constants.EKYC_URL, Constants.GLB_1);
			String ekycPassword = cache.strConfigVal(Constants.EKYC_PASSOWRD, Constants.GLB_1);
			String ekycUsername = cache.strConfigVal(Constants.EKYC_USERNAME, Constants.GLB_1);
			HttpHeaders headers = new HttpHeaders();
			CreditionalsDTO dto = new CreditionalsDTO(ekycPassword, ekycUsername);
	        headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreditionalsDTO> entity = new HttpEntity<>(dto, headers);
			logger.info("Ekyc HTTP Request - Headers: {}, Body: {}",  entity.getHeaders(), entity.getBody());
			logger.debug("EKYC Auth :: Calling token API");
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
			logger.info("EKYC Auth :: Token API response received | HTTP Status={}", response.getStatusCode());
			String responseBody = response.getBody();
			if (responseBody == null || responseBody.isBlank()) {
				logger.error("EKYC Auth :: Empty response received from token API");
				throw new RuntimeException("Empty response from token API");
			}
			logger.debug("EKYC Auth :: Token API responseCode extracted from response");
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(responseBody);
			String status = rootNode.path("status").asText();
			String responseCode = rootNode.path("responseCode").asText();
			if (!"SUCCESS".equalsIgnoreCase(status)) {
				logger.error("EKYC Auth :: Token generation failed | status={} | responseCode={}", status, responseCode);
				throw new RuntimeException("Token generation failed");
			}
			String accessToken = rootNode.path("data").path("accessToken").asText();
			if (accessToken == null || accessToken.isBlank()) {
				logger.error("EKYC Auth :: accessToken not found in response");
				throw new RuntimeException("accessToken not found in token response");
			}
			logger.info("EKYC Auth :: Access token generated successfully");
			return accessToken;
		} catch (Exception e) {
			logger.error("EKYC Auth :: Exception while generating access token", e);
			throw new RuntimeException("Unable to generate access token", e);
		}
	}
}
