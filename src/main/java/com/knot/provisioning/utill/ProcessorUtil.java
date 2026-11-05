package com.knot.provisioning.utill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.knot.provisioning.service.CacheService;

public class ProcessorUtil {
	
	private static final Logger log = LogManager.getLogger(ProcessorUtil.class);

	private ProcessorUtil()
	{
		
	}
	public static HttpMethod findHTTPMethodType(CacheService cache, String actionID) throws ProvisionException {
		if(actionID.equalsIgnoreCase(cache.strConfigVal(Constants.GET, Constants.GLOBAL_TENANT))) {
			return HttpMethod.GET;
		} else if(actionID.equalsIgnoreCase(cache.strConfigVal(Constants.POST, Constants.GLOBAL_TENANT))) {
			return HttpMethod.POST;
		} else if(actionID.equalsIgnoreCase(cache.strConfigVal(Constants.PUT, Constants.GLOBAL_TENANT))) {
			return HttpMethod.PUT;
		} else if(actionID.equalsIgnoreCase(cache.strConfigVal(Constants.DELETE, Constants.GLOBAL_TENANT))) {
			return HttpMethod.DELETE;
		}
		throw new ProvisionException("Unalbe to identify HTTP method for the Action ID: "+ actionID);
	}
	
	public static String handlePrimitive(String key, String[] pipes, String expectedJSON) {
		for(String pipe : pipes) {
			if(key != null && expectedJSON.contains(pipe+key)) {
				return pipe+key;
			}
		}
		return key;
	}
	
	@SuppressWarnings("unchecked")
	public static Object handleList(List<Object> listData, String[] pipes, String expectedJSON) {
		List<Object> list = new ArrayList<>();
		for(Object obj : listData) {
			if(obj == null) {
				log.error("Found null value for in List: ");
				continue;
			}
			Class<? extends Object> objClass = obj.getClass();
			if(objClass.isPrimitive() || objClass.equals(String.class)) {
				list.add(obj);
			} else if(objClass.equals(ArrayList.class)) {
				Object handledList = handleList((ArrayList<Object>)obj, pipes, expectedJSON);
				list.add(handledList);
			} else if(objClass.equals(LinkedHashMap.class)) {
				Map<String, Object> handledMap = handleMap((Map<String, Object>) obj, pipes, expectedJSON);
				list.add(handledMap);
			} else {
				list.add(obj);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> handleMap(Map<String, Object> mapData, String[] pipes, String expectedJSON) {
		Map<String, Object> replacedMapData = new HashMap<>();
		for(Entry<String, Object> key : mapData.entrySet()) {
			Object value = key.getValue();
			if(value == null) {
				log.error("Found null value for Key: {}" , key);
				continue;
			}
			Class<? extends Object> valueClass = value.getClass();
			if (valueClass.equals(ArrayList.class)) {
				value = handleList((ArrayList<Object>) value, pipes, expectedJSON);
				replacedMapData.put(key.getKey(), value);
			} else if (valueClass.equals(LinkedHashMap.class)) {
				value = handleMap((Map<String, Object>) value, pipes, expectedJSON);
				replacedMapData.put(key.getKey(), value);
			} else {
				String keyIn = handlePrimitive(key.getKey(), pipes, expectedJSON);
				replacedMapData.put(keyIn, value);
			}
		}
		return replacedMapData;
	}
	
	
//	#########################################Below Methods are used to replace values in child/dependent request.#################################################################
	
	@SuppressWarnings("unchecked")
	public static String handleData(Class<? extends Object> valueClass, Object value, String request, String key, CacheService cache, String tenantID) throws JsonProcessingException {
		if (valueClass.equals(ArrayList.class)) {
			request = handlechildList((List<Object>) value, request, cache, tenantID);
		} else if (valueClass.equals(LinkedHashMap.class)) {
			request = handleChildMap((Map<String, Object>)value, request, cache, tenantID);
		} else {
			request = handleChildPlainData(key, request, value, cache, tenantID);
		}
		return request;
	}
	
	@SuppressWarnings("unchecked")
	public static String handlechildList(List<Object> listData, String requestJson,  CacheService cache, String tenantID) throws JsonProcessingException {
		for(Object obj: listData) {
			Class<? extends Object> objClass = obj.getClass();
			if(objClass.equals(ArrayList.class)) {
				requestJson = handlechildList((List<Object>)obj, requestJson, cache, tenantID);
			} else if (objClass.equals(LinkedHashMap.class)) {
				requestJson = handleChildMap((Map<String,Object>) obj, requestJson, cache, tenantID);
			} else {
				log.debug("Found List of primitives");
//				List<PrimitiveDataType or String> cant be handled now..if need to be handled then we can try it out.
			}
		}
		return requestJson;
	}
	
	
	public static String handleChildMap(Map<String, Object> mapData, String requestJson, CacheService cache, String tenantID) throws JsonProcessingException {
		for(Entry<String, Object> key : mapData.entrySet()) {
			Object value =key.getValue();
			Class<? extends Object> valueClass = value.getClass();
			requestJson = handleData(valueClass, value, requestJson, key.getKey(), cache, tenantID);
		}
		return requestJson;
	}
	
	public static String handleChildPlainData(String key, String requestJson, Object value, CacheService cache, String tenantID) {
		String toReplace = "";
		if (key.contains( cache.strConfigVal(Constants.SUB_REQ_PARAM, tenantID))) {
			key = key.replace( cache.strConfigVal(Constants.SUB_REQ_PARAM, tenantID), "");
			toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID)+key;
			requestJson = requestJson.replace(toReplace, value.toString());
		}
		if (key.contains( cache.strConfigVal(Constants.BOTH, tenantID))) {
			key = key.replace( cache.strConfigVal(Constants.BOTH, tenantID), "");
			toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID)+key;
			requestJson = requestJson.replace(toReplace, value.toString());
		}
		if (key.contains( cache.strConfigVal(Constants.DOWN_STREAM_PARAM, tenantID))) {
			key = key.replace( cache.strConfigVal(Constants.DOWN_STREAM_PARAM, tenantID), "");
			toReplace = cache.strConfigVal(Constants.R_PIPE, tenantID)+key;
			requestJson = requestJson.replace(toReplace, value.toString());
		}
		return requestJson;
	}

//	#########################################above Methods are used to replace values in child/dependent request.#################################################################
	
}
