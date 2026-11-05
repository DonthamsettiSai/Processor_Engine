package com.knot.provisioning.daoimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.knot.provioning.model.ProvRespDetails;
import com.knot.provisioning.dao.CacheDao;
import com.knot.provisioning.utill.Constants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Repository
public class CacheDaoImpl implements CacheDao {
	
	private static final Logger log = LogManager.getLogger(CacheDaoImpl.class);


	private static final String TENANT_ID = "tenantID";
	private static final String CODE = "code";
	private static final String NA = "NA";
	private EntityManager em;

	@Autowired
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Cacheable(value="PROV_CONFIGURATIONS", key = "#code + '::' + #tenantID")
	@Override
	public String strConfigVal(final String code, final String tenantID) {
		
		log.info("Started CacheDaoImpl Cache LOG FOR -- CODE $ {} AND TENANT $ {}", code, tenantID);
		

		String returnVal = NA;
		StringBuilder builder = new StringBuilder();
		builder.append("Select CONFIG_VALUE from prov_configurations ")
				.append(" where current_date() between effective_from and effective_to")
				.append(" and tenant_id = :tenantID  and code = :code ");
		try {
			Query q = em.createNativeQuery(builder.toString());
			q.setParameter(TENANT_ID, tenantID);
			q.setParameter(CODE, code);
			@SuppressWarnings("unchecked")
			List<String> resultList = q.getResultList();
			returnVal = resultList.isEmpty() ? NA : resultList.get(0);
			log.debug("Response From PROV_CONFIGURATIONS Cache :: returnVal {}", returnVal);
			log.info("Ended CacheDaoImpl Cache LOG FOR -- CODE $ {} AND TENANT $ {}", code, tenantID);
		} catch (Exception e) {
			log.error(" Error {}", e.getMessage());
		}
	
		return returnVal;
	}
	
	@Override
	public Integer intConfigVal(String code, String tenantID) {
		String strVal = strConfigVal(code, tenantID);
		if (strVal != null) {
			return Integer.parseInt(strVal);
		}
		throw new RuntimeException(code + Constants.KEY_SEP + tenantID + " is not configured. ");
	}
	
	@Override
	public Long longConfigVal(String code, String tenantID) {
		String strVal = strConfigVal(code, tenantID);
		if (strVal != null) {
			return Long.parseLong(strVal);
		}
		throw new RuntimeException(code + Constants.KEY_SEP + tenantID + " is not configured. ");
	}
	
	@SuppressWarnings("unchecked")
	@Cacheable(value="PROV_CONFIGURATIONS", key = "'getProvResponseDetails'")
	@Override
	public List<ProvRespDetails> getProvResponseDetails() {
		StringBuilder builder = new StringBuilder();
		List<ProvRespDetails> provRespDetailslist = new ArrayList<>();
		Query query=null;
		try {
		builder.append("Select  * from provisioning_response_details ");
		query=em.createNativeQuery(builder.toString(),ProvRespDetails.class);
		provRespDetailslist=query.getResultList();
		}
		catch (Exception e) {
			log.info("ConfigDaoImpl :: getProvResponseDetails()" , e);
			
		}
		return provRespDetailslist;
	}

	@Override	
	   public String configValByCodeForGraceFullStop(final String code, final String tenantID) {
			log.info("Started Cache CacheDaoImpl :: configValByCode() LOG FOR -- CODE $ {} AND TENANT $ {}", code, tenantID);
			log.debug(" Entered in CacheDaoImpl :: configValByCode() CODE  {} AND TENANT  {}", code, tenantID);
			String returnVal = NA;
			if (!StringUtils.hasText(code) || !StringUtils.hasText(tenantID)) {
				returnVal = NA;
			}
			StringBuilder builder = new StringBuilder();
			builder.append("Select CONFIG_VALUE from prov_configurations ")
					.append(" where current_date() between effective_from and effective_to")
					.append(" and tenant_id = :tenantID  and code = :code ");
			try {
				Query q = em.createNativeQuery(builder.toString());
				q.setParameter(TENANT_ID, tenantID);
				q.setParameter(CODE, code);
				@SuppressWarnings("unchecked")
				List<String> resultList = q.getResultList();
				returnVal = resultList.isEmpty() ? NA : resultList.get(0);
				log.debug("Response From prov_configurations :: CONFIG_VALUE  ",returnVal);
				log.info("Ended CacheDaoImpl :: configValByCode()");
			} catch (Exception e) {
				log.error(" Error {}", e.getMessage());
			}
		
			return returnVal;
		}

}
