/**
 * 
 */
package com.knot.provioning.exception;

/**
 * @author KNOT
 *
 */
public class ErrorCodes {

	
	private ErrorCodes() {
		
	}
	
	public static final String CANNOT_CONNECT_TO_DATABASE = "99101";
	public static final String USEID_PASSWORD_INCORRECT = "99102";
	public static final String ACCESS_DENIED_FOR_USER = "99103";
	public static final String NO_SPACE_IN_DATABASE = "99104";

	public static final String DUPLICATE_ENTRY = "99105";
	public static final String FAILED_TO_UPDATE_IN_A_TABLE = "99106";
	public static final String FAILED_TO_READ_FROM_A_TABLE = "99107";
	public static final String TABLE_DOESNT_EXIST = "99108";

	public static final String DATA_IS_NOT_AVAILABLE = "99109";
	public static final String CANNOT_LOCK_FOR_UPDATE = "99110";
	public static final String CANNOT_UNLOCK = "99111";
	public static final String UNKNOWN_COLUMN = "99115";

	public static final String SQL_SYNTAX = "99116";
	public static final String NULL_POINTER_EXCEPTION = "99117";
	
	public static final String NUM_FOR_EXCEPTION = "99118";
	public static final String INVALID_RES_USG_EXC = "99119";
	public static final String ARRAY_INDEX_EXC = "99120";
	public static final String DATA_INTE_VIO_EXC = "99121";
	public static final String EXCEPTION = "99122";
	
	public static final String MIN_BAL_EXCEPTION = "99674";
	public static final String NO_BAL_LEFT_EXCEPTION = "99673";
	
	public static final String REDDEM_STANDLONE = "99720";
	public static final String VOUCHER_LOB_NA = "99721";
	public static final String CAT_NO_PLAN = "99722";
	public static final String SELL_PREPAID_ONLY = "99725";
	
	public static final String METHOD_ARUGUMENT_EXCEPTION = "99726";
	public static final String CUSTOM_EXCPTION = "99727";

}
