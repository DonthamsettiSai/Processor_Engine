package com.knot.provisioning.utill;

public class Constants {
	
	private Constants() {

	}
	
	public static final String GLOBAL_TENANT = "-1";
	public static final String PROC_IN_PROGRESS = "PROC_IN_PROG";
	public static final String PROC_SUCESS= "PROC_SUCESS";
	public static final int ACTIVE_STATUS = 11;
	public static final String KEY_SEP = ":";
	public static final String WORK_ORDER_INPROGRESS  ="WK_OD_PR";
	public static final String WORK_ORD_REMARKS ="WK_REMARKS";
	public static final String SLEEP_TIME = "SLEEP_TIME_PROC";
	public static final String THREADSIZE = "THREADSIZE_PROC";
	public static final String MAX_RECORDS_PER_CALL = "PRO_MAX_RECORDS";
	public static final String NSWO = "NSWO";
	public static final String RE_EKYC = "RE_EKYC";
	public static final String NS_ACTIVITY = "NS_ACTIVITY";
	public static final String OMS_URL = "OMS_URL";
	public static final String OMS_USERNAME = "OMS_USERNAME";
	public static final String OMS_PWD = "OMS_PWD";
	public static final String KYC_CHECK = "IS_KYC_CHK_REQ";
	public static final String CHAR_Y = "CHAR_Y";
	public static final String DEF_URL = "DEF_URL";
	public static final String DEF_UN = "DEF_UN";
	public static final String DEF_PWD = "DEF_PWD";
	public static final String SUCCESS = "SUCCESS";
	public static final String REQUEST_COMPLETION_SUCCESS = "Request updated successfully";
	public static final String REQUEST_FAILED= "Unable to process the request";
	public static final String API_SUCCESS_MESSAGE = "SUCCESS_MSG";
	public static final String CHAR_N = "CHAR_N";
	public static final String PROCESSOR_ADMIN = "PROC_ADMIN";
	public static final String RESPONSE_CODE_TYPE_SUCESS = "RES_CTYPE";
	public static final String RESULT_CODE = "RES_CODE";
	public static final String USERNAME = "USERNAME_PROCES";
	public static final String PASSWORD = "PASSWORD_PROCES";
	public static final String HEADER = "HEADER";
	public static final String RESP_TYPE = "RESP_TYPE";
	public static final String PROV_PROCESS_IN_PROGRESS = "PROC_INPROGR_RE";
	public static final String DOWN_STREAM_PARAM = "DOWN_STREAM_PRM";
	public static final String SUB_REQ_PARAM = "SUB_REQ_PARAM";
	public static final String BOTH = "BOTH_STREAMS";
	public static final String R_PIPE = "R_PIPE";
	public static final String ERROR_REMARKS = "ERROR_REMARKS";
	public static final String DOWN_STREAM_PARAM_REGEX = "DOWN_STRM_REGEX";
	public static final String SUB_REQ_PARAM_REGEX = "SUB_REQ_REGEX";
	public static final String BOTH_REGEX = "BOTH_REGEX";
	public static final String ERROR = "ERROR";
	public static final String SUCCESS_REMARKS = "SUCCESS_REMARKS";
	public static final String RESP_NOT_UPDATED = "RESP_NOT_UPDATE";
	public static final String ERROR_CODE = "ERROR_CODE";
	public static final String NO_RESP_FROM_SERVER = "NO_SERVER_RSPNS";
	public static final String PROV_REQ_GENERATED = "REQ_GEN_SUCCESS";
	public static final String ERROR_IN_EXEC = "ERROR_IN_EXEC";
	public static final String PROV_REQ_CORRECTED_AND_READY_TO_RE_PROV = "REQ_CORRECTED";
	public static final String INDEPENDANT = "INDEPENDANT";
	public static final String NEW_VERSION_AND_NOT_VALIDATED = "NEW_VERSION";
	public static final String PROV_IN_PROGRESS = "PROV_IN_PROGRES";
	public static final String LOB_PRIO = "LOB_PRIO";
	public static final String SERVICE_PRIO = "SERVICE_PRIO";
	public static final String ERROR_MAX_THREADS = "ERROR_THREADS";
	public static final String WAIT_FOR = "WAIT_FOR";
	public static final String WO_PROV_IN_STATUS = "PROCES_IN_PROGR";
	public static final String POST_PROV_IN_PROGRESS = "POST_IN_PROGRES";
	public static final String ALL_SUB_REQ_PROV_SUCCESSFULLY = "PROCESSOR_SUCCE";
	
	public static final String UPDATE_CPE_WORK_ORDER = "UPDATE_CPE";
	public static final String CUST_IDLE_STAT = "CUST_IDLE_STAT";
	
	public static final String PROV_SUCCESS_WO_STATUS = "PROV_SUCCESS_WO";
	public static final String PROV_ERROR_WO_STATUS = "PROV_ERROR_WO_";
	public static final String PROV_REQ_DTL_PROVISION_SUCCESS = "REQ_DTL_SUCCESS";
	public static final String PROV_DETAIL_FAILED_TO_PROV = "REQ_DTL_FAILED";
	public static final String PROV_REQ_DTL_ERROR = "PRO_REQ_DTL_ERR";
	public static final String PROV_REQ_PROCESS_ERROR_STATUS = "PROC_ERROR_STAT";
	public static final String PROV_REQ_RE_PROCESS_IN_PROGRESS = "RE_PROCESSING";
	public static final String READY_TO_RE_PROCESS = "READY_TO_PROCES";
	public static final String FAILED_WHILE_RETRY = "FAILED_IN_RETRY";
	public static final String GLOBAL_FLAG = "GLOBAL_FLAG";
	public static final String PROCESSOR_DONE = "PROCESSOR_DONE";
	public static final String R_PIPE_NOT_REPLACES = "R_PIPE_NOT_REPL";
	public static final String SUPER_UN = "SUPER_UN";
	public static final String SUPER_PWD = "SUPER_PWD";
	public static final String UMS_URL = "UMS_URL";
	public static final String GLB_1 = "-1";
	public static final String CAT_URL = "CAT_URL";
	public static final String POST_PROV_FAIL = "POST_PROV_FAIL";
	public static final String POST_PROV_SUCCE = "POST_PROV_SUCCE";
	public static final String POST_PROV_SUCE = "POST_PROV_SUCE";
	public static final String ACT_STATUS = "ACT_STATUS";
	public static final String RIP_STATUS = "RIP_STATUS";
	public static final String TIP_STATUS = "TIP_STATUS";
	public static final String ASC_STATUS = "ASC_STATUS";
	public static final String CORP_TYPE = "CORP_TYPE";
	public static final String RF_STATUS = "RF_STATUS";
	public static final String FLAG = "FLAG";
	public static final String MSG = "MSG";
	public static final String FAILURE_CODE = "FAIL_CODE";
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String DELETE= "DELETE";
	public static final String LABEL = "label";
	public static final String DATA = "data";
	//for soap call purpose 
	public static final String REST_CALL= "RESTFUL";
	public static final String SOAP_CALL= "SOAP";
	public static final String LOCAL_CALL= "LOCALHOST";
	
	//new changes in dshboard used to compare no of days
	public static final String LABEL_REQ= "LABEL_REQ";
	public static final String TOTAL_REQ= "TOTAL_REQ";
	public static final String DAYS= "DAYS";
	public static final String RPD= "RPD";
	public static final String PLD= "PLD";
	public static final String subscriptionId= "subscriptionId";
	public static final String result= "result";
	public static final String resultObject= "resultObject";
	public static final String clientId= "clientId";
	
	
	
	public static final String R_REGX= "r\\|(.*?)\\|";
	public static final String PMS_URL = "PMS_URL";
	public static final String MAX_TENANTS = "MAX_TENANTS";
	
	
	public static final String AUTH_URL = "AUTH_URL";
	public static final String EKYC_URL = "EKYC_URL";
	public static final String EKYC_USERNAME = "EKYC_USERNAME";
	public static final String EKYC_PASSOWRD = "EKYC_PASSOWRD";
	public static final String TOKEN_ERR = "NO Token returend from the server";
	
	public static final String EXT_SUPER_UN = "EXT_SUPER_UN";
	public static final String EXT_SUPER_PWD = "EXT_SUPER_PWD";
	
	public static final String HEADER_KEY = "HEADER_KEY";
	public static final String HEADER_VALUE = "HEADER_VALUE";
	
	public static final String NEW_VERSION = "Ready To Re Progress !!";
	
	public static final String CISCO_USER_NAME = "CISCO_USER_NAME";
	public static final String CISCO_PWD = "CISCO_PWD";
	public static final String CISCO_URL = "CISCO_URL";
	
	
	public static final String WFM_URL = "WFM_URL";
	public static final String WFM_USERNAME = "YXBpX3VzZXI=";
	public static final String WFM_PASSWORD = "YXBpdXNlckAxMjM=";
	public static final String WFM_GRANTTYPE = "password";
	public static final String WFM_SOURCE = "HOBS";
	
	public static final String NM_TNT_ID = "NM_TNT_ID";
	
	public static final String PROV_REQ_AYSNC_STATUS = "AYSNC_STATUS";
	public static final String MANUAL_STATUS = "MANUAL_STATUS";
	public static final String RESPONSE_NOT_CONTAIN_SUCESS_STATUS = "RES_FAIL_STATUS";
	
    public static final Integer SUCESS_CODE = 200;
	
	
	public static final String WFM_REMARKS = "Waiting for API Conformation";
	public static final String ERROR_IN_PROCESSOR = "Error in proviosining processor";
	
	public static final Integer WORK_ODR_SUCESS = 45;
	public static final Integer PROCESOOR_SUCESS = 19;
	public static final Integer WORK_ODR_FAIL_SUCESS = 44;
	public static final Integer PROCERSER_FAIL = 73;
	public static final Integer PROCERSER_INPROFRESS = 13;
	public static final Integer WORK_ODR_INPROGRESS_SUCESS = 41;
	public static final Integer PROCERSER_CREATION = 11;
	
	public static final String TFIBER_TNT = "117";
	public static final String MANUAL_ACT_REMARKS = "Manual Activation Done";
	public static final String PROCESS_TYPE = "PROCESS_TYPE";
	public static final String PRO_PROCESS_STOP = "PROPROCESS_STOP";
	
	public static final String PROV_REQ_DTL_SEQ_NAME = "REQ_DTL_ID";
	public static final String SERVICE_ID = "service_id";
	public static final String OCE_USERNAME = "OCE_USERNAME";
	public static final String OCE_PASSWORD = "OCE_PASSWORD";
	public static final String HEADER_PARAM = "HEADER_PARAM";
	public static final String PRE_PAID_ACCOUNT_TYPE = "11";
	public static final String OPTIVA = "optiva";
	public static final String POST_PROV_SUCCESS = "POST_PROV_SUCCE";
	public static final String POST_DT_IN_SUSS = "POST_DT_IN_SUSS";
	
	public static final String PROV_SUCCESS_WO = "PROV_SUCCESS_WO";
	public static final String REQ_GEN_SUCCESS = "REQ_GEN_SUCCESS";
	public static final String EKYC = "ekyc";
	public static final String ORDER_VALIDATION_FAILED = "422";
	public static final String MISSING_MANDTORY_FILED = "400";
	
	public static final String XML_DYNAMIC_OPERATION = "Read";
	public static final String XML_DYNAMIC_MODIFIER = "ROP";
	
	
}
