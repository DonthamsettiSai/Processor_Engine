package com.knot.provisioning.daoimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.knot.provioning.model.ProvRespDetails;
import com.knot.provisioning.bo.ConfigBo;
import com.knot.provisioning.utill.Constants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class ConfigDaoImpl {
	
	private static final Logger log = LogManager.getLogger(ConfigDaoImpl.class);
	
	private EntityManager em;

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigBo> getAllConfigs(String tenantId) {
		StringBuilder builder = new StringBuilder();
		try {
			log.info("Started ConfigDaoImpl :: getAllConfigs()");
			builder.append("Select CONFIG_ID,TENANT_ID,EFFECTIVE_FROM,CODE,CONFIG_VALUE from prov_configurations where current_date between effective_from and effective_to AND status = "+Constants.ACTIVE_STATUS);
			boolean hasText = StringUtils.hasText(tenantId);
			builder.append(tenantId !=null && hasText ? " and tenant_id=:tenantId":" ");
			Query q = em.createNativeQuery(builder.toString(), ConfigBo.class);
			if(hasText)
			{
			q.setParameter("tenantId",tenantId);
			}
			log.info("Ended ConfigDaoImpl :: getAllConfigs()");
			return q.getResultList();
		} catch (Exception e) {
			log.info("CustomerCreationDaoImpl :: getAllConfigs() ", e);
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ProvRespDetails> getProvResponseDetails() {
		StringBuilder builder = new StringBuilder();
		List<ProvRespDetails> provRespDetailslist = new ArrayList<>();
		Query query=null;
		try {
			log.info("Started ConfigDaoImpl :: getProvResponseDetails()");
		builder.append("Select PROVISIONING_RESP_ID,PROVISIONING_REQUEST_ID,SERVER_URL,RESPONSE_CODES,DESCRIPTION,API_OBJECT,")
		.append("EFFECTIVE_FROM,EFFECTIVE_TO,STATUS,RESPONSE_CODE_TYPE,CREATED_BY,CREATED_IPADDR,CREATED_ON,MODIFIED_BY,MODIFIED_ON,")
		.append("MODIFIED_IPADDR from provisioning_response_details");
		query=em.createNativeQuery(builder.toString(),ProvRespDetails.class);
		provRespDetailslist=query.getResultList();
		log.debug("Response From ConfigDaoImpl :: getProvResponseDetails() provRespDetailslist size {}",provRespDetailslist.size());
		log.info("Ended ConfigDaoImpl :: getProvResponseDetails()");
		}
		catch (Exception e) {
			log.error("ConfigDaoImpl :: getProvResponseDetails()" , e);
			
		}
		return provRespDetailslist;
	}

	
	

}
