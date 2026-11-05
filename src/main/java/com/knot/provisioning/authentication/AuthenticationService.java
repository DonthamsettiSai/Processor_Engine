package com.knot.provisioning.authentication;
/**
 * 
 * 
 * import java.io.IOException; import java.util.StringTokenizer;
 * 
 * import org.apache.logging.log4j.LogManager; import
 * org.apache.logging.log4j.Logger; import
 * org.apache.tomcat.util.codec.binary.Base64; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.stereotype.Component;
 * 
 * import com.knot.provisioning.utill.Constants; import
 * com.knot.provisioning.utill.StoreConfigs;
 * 
 */
/**
	 * 
	 * @author Lakshman
	 *
	 */
/**
		 * 
		 * @Component public class AuthenticationService {
		 * 
		 * private static final Logger loggerError =
		 * LogManager.getLogger(AuthenticationService.class);
		 * 
		 * @Autowired private StoreConfigs config;
		 * 
		 * public boolean authenticate(String authCredentials) {
		 * 
		 * if (null == authCredentials) return false; final String encodedUserPassword =
		 * authCredentials.replaceFirst("Basic" + " ", ""); String usernameAndPassword =
		 * null; try { byte[] decodedBytes = Base64.decodeBase64(encodedUserPassword);
		 * usernameAndPassword = new String(decodedBytes, "UTF-8"); } catch (IOException
		 * e) {
		 * loggerError.error("The Exception is AuthenticationService :: authenticate" +
		 * e); } final StringTokenizer tokenizer = new
		 * StringTokenizer(usernameAndPassword, ":"); final String token1 =
		 * tokenizer.nextToken(); final String token2 = tokenizer.nextToken();
		 * 
		 * return config.getStrVal(Constants.USERNAME,
		 * Constants.GLOBAL_TENANT).equals(token1) &&
		 * config.getStrVal(Constants.PASSWORD, Constants.GLOBAL_TENANT).equals(token2);
		 * }
		 * 
		 * }
		 */