package com.knot.provisioning.utill;

/**
 * import java.io.Serializable;
 * 
 * import org.springframework.beans.factory.annotation.Value; import
 * org.springframework.context.annotation.Configuration; import
 * org.springframework.context.annotation.PropertySource; import
 * org.springframework.stereotype.Component;
 * 
 *//**
	 * @author Lakshman
	 *
	 *//**
		 * 
		 * @Component
		 * 
		 * @Configuration
		 * 
		 * @PropertySource(value = "file:${app.home}") public class
		 * ReadConfigurationFile implements Serializable {
		 * 
		 * private static final long serialVersionUID = 1L;
		 * 
		 * @Value("${jdbc.driverClassName}") private String driverClassName;
		 * 
		 * @Value("${jdbc.url}") private String url;
		 * 
		 * @Value("${jdbc.userName}") private String userName;
		 * 
		 * @Value("${jdbc.password}") private String password;
		 * 
		 * @Value("${jdbc.dialect}") private String dialect;
		 * 
		 * @Value("${logging.config}") private String logFile;
		 * 
		 * @Value("${SLEEP-TIME}") private String sleepTime;
		 * 
		 * @Value("${BATCH-RECORDS}") private String batchSize;
		 * 
		 * 
		 * 
		 * @Value("${COMMONS_URL}") private String commonsUrl;
		 * 
		 * @Value("${COMMONS_USERNAME}") private String commonsUserName;
		 * 
		 * @Value("${COMMONS_PWD}") private String commonsPassword;
		 * 
		 * @Value("${CAT_URL}") private String catUrl;
		 * 
		 * @Value("${CAT_USERNAME}") private String catUserName;
		 * 
		 * @Value("${CAT_PWD}") private String catPassword;
		 * 
		 * @Value("${TMS_URL}") private String tmsUrl;
		 * 
		 * @Value("${TMS_USERNAME}") private String tmsUserName;
		 * 
		 * @Value("${TMS_PWD}") private String tmsPassword;
		 * 
		 * @Value("${BILLING_URL}") private String billingUrl;
		 * 
		 * @Value("${BILLING_PWD}") private String billingUserName;
		 * 
		 * @Value("${TMS_PWD}") private String billingPassword;
		 * 
		 * @Value("${DOCUMENT_UPLOAD_PATH}") private String contractPath;
		 * 
		 * 
		 * public String getBatchSize() { return batchSize; }
		 * 
		 * public void setBatchSize(String batchSize) { this.batchSize = batchSize; }
		 * 
		 * public String getDriverClassName() { return driverClassName; }
		 * 
		 * public String getSleepTime() { return sleepTime; }
		 * 
		 * public void setSleepTime(String sleepTime) { this.sleepTime = sleepTime; }
		 * 
		 * public void setDriverClassName(String driverClassName) { this.driverClassName
		 * = driverClassName; }
		 * 
		 * public String getUrl() { return url; }
		 * 
		 * public void setUrl(String url) { this.url = url; }
		 * 
		 * public String getUserName() { return userName; }
		 * 
		 * public void setUserName(String userName) { this.userName = userName; }
		 * 
		 * public String getPassword() { return password; }
		 * 
		 * public void setPassword(String password) { this.password = password; }
		 * 
		 * public String getDialect() { return dialect; }
		 * 
		 * public void setDialect(String dialect) { this.dialect = dialect; }
		 * 
		 * public String getLogFile() { return logFile; }
		 * 
		 * public void setLogFile(String logFile) { this.logFile = logFile; }
		 * 
		 * }
		 */
