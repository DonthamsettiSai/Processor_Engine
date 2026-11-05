package com.knot.provisioning.daoimpl;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.knot.provioning.model.ProvisioningReqDetailsModel;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.dao.ProvisioningRequestDao;
import com.knot.provisioning.dto.PaginationDTO;
import com.knot.provisioning.dto.ProvisioningReqDTO;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;
import com.knot.provisioning.service.CacheService;
import com.knot.provisioning.utill.Constants;
import com.knot.provisioning.utill.DateUtill;
import com.knot.provisioning.utill.FileUtil;
import com.knot.provisioning.utill.ProvisionException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * @author Sai
 *
 */

@Repository
public class ProvisioningRequestDaoImpl implements ProvisioningRequestDao {

	private static final Logger logger = LogManager.getLogger(ProvisioningRequestDaoImpl.class);

	private EntityManager em;

	CacheService cache;
	
	private Gson gson;

	
	@Autowired
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	@Autowired
	public void setCache(CacheService cache) {
		this.cache = cache;
	}

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	private EntityManager getEntityManager() {
		return em;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.knot.provisioning.daoimpl.oms.DaoImpl.ProvisioningRequestDaoImpl#saveProvRequestObjects(String)
	 *      This Method Used For Storing In provisioning_request_objects Table
	 * @return ResponseDTO
	 */
	public boolean saveProvisioningRequest(ProvisioningRequest provisioningRequest) {
		boolean status = false;
		try {
			getEntityManager().merge(provisioningRequest);
			status = true;
		} catch (Exception e) {
			logger.error("The Exception is ProvisioningRequestDaoImpl :: saveProvisioningRequest()", e);
			throw e;
		}
		return status;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningRequestsBO> searchProvisioningRequest(ProvisioningReqSearchDTO request) throws Exception {
		logger.info(" Started ProvisioningRequestDaoImpl :: searchProvisioningRequest() request :: {}",gson.toJson(request));
		logger.debug(" Entred in  ProvisioningRequestDaoImpl :: searchProvisioningRequest() :: ProvisioningReqSearchDTO {}", gson.toJson(request));
		List<ProvisioningRequestsBO> requestList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		boolean descOrderBY = true;
		Query query = null;
		try {
			if (!request.getFlag().contains("Days")) {
				logger.debug(
						" request.getFlag() :: Not Equal To the  Days :: Started to fetch Flag Based ProvRequests {}",
						request.getFlag());
				if (FileUtil.checkEmptyNull(request.getFlag())) {
					switch (request.getFlag()) {
					case "S":
						whereClause.append(" and pr.STATUS = :postSucessStatus ");
						setParam.put("postSucessStatus",
								cache.intConfigVal(Constants.POST_PROV_SUCCE, Constants.GLOBAL_TENANT));
						break;
					case "F":
						whereClause.append(" and pr.STATUS = :postFailStatus ");
						setParam.put("postFailStatus",
								cache.intConfigVal(Constants.POST_PROV_FAIL, Constants.GLOBAL_TENANT));
						break;
					case "G":
						whereClause.append(" and pr.STATUS = :provGeneratedStatus ");
						setParam.put("provGeneratedStatus",
								cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT));
						break;
					case "I":
						whereClause.append(
								" and  pr.STATUS in (:provGeneratedStatus , :actviceStatus , :ripStatus , :ascStatus , :tipStatus ) ");
						setParam.put("provGeneratedStatus",
								cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT));
						setParam.put("actviceStatus",
								cache.intConfigVal(Constants.ACT_STATUS, Constants.GLOBAL_TENANT));
						setParam.put("ripStatus", cache.intConfigVal(Constants.RIP_STATUS, Constants.GLOBAL_TENANT));
						setParam.put("ascStatus", cache.intConfigVal(Constants.ASC_STATUS, Constants.GLOBAL_TENANT));
						setParam.put("tipStatus", cache.intConfigVal(Constants.TIP_STATUS, Constants.GLOBAL_TENANT));
						break;
					default:
						break;
					}
				}
				if (FileUtil.getBooleanValue(request.getTenantID())) {
					whereClause.append(" AND pr.tenant_id  = :tenantID ");
					setParam.put("tenantID", request.getTenantID());
				}

				if (FileUtil.getBooleanValue(request.getPartnerID())) {
					whereClause.append(" AND pr.partner_id = :partnerID ");
					setParam.put("partnerID", request.getPartnerID());
				}

				if (FileUtil.checkEmptyNull(request.getAccountID())) {
					whereClause.append(" and pr.customer_account_id in ("+request.getAccountID()+") ");
					//setParam.put("accountID", Arrays.asList(request.getAccountID().split(",")));

				}

				if (FileUtil.checkEmptyNull(request.getCustomerID())) {
					whereClause.append(" and pr.customer_id IN("+request.getCustomerID()+")");
					//setParam.put("customerID",  Arrays.asList(request.getCustomerID().split(",")));
				}

				if (FileUtil.checkEmptyNull(request.getWorkOrderID())) {
					whereClause.append(" and pr.work_order_id = :workOrderID ");
					setParam.put("workOrderID", request.getWorkOrderID());

				}
				if (FileUtil.checkEmptyNull(request.getActivityID())) {
					whereClause.append(" and pr.ACTIVITY_ID = :activityID");
					setParam.put("activityID", request.getActivityID());

				}

				/**
				 * if(FileUtil.getBooleanValue(request.getCustomerName())) { whereClause.
				 * append(" AND (cm.FIRST_NAME LIKE concat('%',request.getCustomerName(),'%') OR
				 * cm.MIDDLE_NAME LIKE concat('%',request.getCustomerName(),'%') " ) .append("OR
				 * cm.LAST_NAME LIKE concat('%',request.getCustomerName(),'%') OR
				 * cm.organization_NAME LIKE concat('%',+request.getCustomerName(),'%') " ); }
				 * 
				 * if(FileUtil.getBooleanValue(request.getAccountName())) { whereClause.
				 * append(" AND customer_account_name LIKE concat
				 * ('%',request.getAccountName(),'%' " ); }
				 */

				boolean flag = getFromDateAndToDateQuery(request, builder, whereClause, setParam, descOrderBY);
				if (flag == true) {
					builder.append(
							"select WORK_ORDER_ID,REQUEST_ID,pr.CUSTOMER_ID,pr.TENANT_ID,pr.CUSTOMER_ACCOUNT_ID,date_format(REQUEST_CREATION_DATE, '%Y-%m-%d %H:%i:%s') REQUEST_CREATION_DATE, ")
							.append(" '' CUSTOMER_NAME, '' CUSTOMER_ACCOUNT_NAME, ").append("PR.NODE_ID,")
							.append("pr.activity_id,").append("pr.STATUS FROM provisioning_requests pr WHERE 1=1 ")
							.append(" " + whereClause.toString() + " ");
				}
				logger.info("String Builder Query builder: {}", builder.toString());
				query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestsBO.class);
			} else {
				String requestAge = request.getFlag().replace("Days", "");
				if (requestAge.contains("-")) {
					String[] va = requestAge.split("-");
					whereClause.append(" >= ").append(va[0])
							.append(" AND TIMESTAMPDIFF(DAY, REQUEST_CREATION_DATE, CURRENT_DATE()) <= ").append(va[1]);
				} else {
					whereClause.append(requestAge);
				}
				boolean flag = getFromDateAndToDateQuery(request, builder, whereClause, setParam, descOrderBY);
				if (flag == true) {
					builder.append(
							"SELECT WORK_ORDER_ID,REQUEST_ID,pr.CUSTOMER_ID,pr.TENANT_ID,pr.CUSTOMER_ACCOUNT_ID,'' CUSTOMER_NAME, '' CUSTOMER_ACCOUNT_NAME, REQUEST_CREATION_DATE,")
							.append("PR.NODE_ID,pr.activity_id, STATUS FROM provisioning_requests pr ")
							.append("WHERE 1=1  AND TIMESTAMPDIFF(DAY, REQUEST_CREATION_DATE, CURRENT_DATE()) ")
							.append(whereClause);
					logger.info("String Builder Query builder: {}", builder.toString());
					query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestsBO.class);
//					query.setParameter("tenantID", request.getTenantID());
//					query.setParameter("activityID", request.getActivityID());
				}
			}
			for (Map.Entry<String, Object> entry : setParam.entrySet()) {
				String key = entry.getKey();
				String val = entry.getValue().toString().trim();
				query.setParameter(key, val);

			}
			PaginationDTO paginationObj = request.getPaginationObj();

			if (Objects.nonNull(paginationObj)) {
				paginationObj.setMinSize(
						(Integer.parseInt(paginationObj.getPageNumber()) - 1) * (paginationObj.getItemsPerPage()));
				paginationObj.setMaxSize(Integer.parseInt(paginationObj.getPageCount()));
				paginationObj.setTotalDisplayCount("1");
				request.setPaginationObj(paginationObj);
				query.setFirstResult(paginationObj.getMinSize());
				query.setMaxResults(paginationObj.getMaxSize());
				ObjectMapper maper = new ObjectMapper();
				logger.info("paginationObject :: {}", maper.writeValueAsString(paginationObj));
			} else {
				paginationObj = new PaginationDTO();
				paginationObj.setTotalDisplayCount("0");
				request.setPaginationObj(paginationObj);

			}
			requestList = query.getResultList();
			logger.debug("Response Result From ProvisioningrequestList Size {}", requestList.size());
			logger.info(" Ended ProvisioningRequestDaoImpl :: searchProvisioningRequest() ");
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: seachProvisioningRequest() ", e);
			throw e;
		}
		return requestList;
	}

	private boolean getFromDateAndToDateQuery(ProvisioningReqSearchDTO request, StringBuilder builder,
			StringBuilder whereClause, Map<String, Object> setParam, boolean descOrderBY) throws ProvisionException {
		logger.debug("Started getFromDateAndToDateQuery ");
		StringBuilder orderBycause = new StringBuilder();
		if (FileUtil.getBooleanValue(request.getFromDate()) && FileUtil.getBooleanValue(request.getToDate())) {
			LocalDate startDate = LocalDate.parse(DateUtill.getDateFromString(request.getFromDate()),
					DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate endDate = LocalDate.parse(DateUtill.getDateFromString(request.getToDate()),
					DateTimeFormatter.ISO_LOCAL_DATE);
			long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
			if (numberOfDays <= cache.longConfigVal(Constants.PLD, Constants.GLB_1)
					|| numberOfDays <= cache.longConfigVal(Constants.RPD, Constants.GLB_1)) {
				if(descOrderBY) {
					orderBycause.append("ORDER BY pr.REQUEST_CREATION_DATE DESC ");
				}
				whereClause.append(" AND date_format(pr.REQUEST_CREATION_DATE, '%Y-%m-%d %H:%i:%s') between ")
						.append("CONCAT(date_format(:fromDate , '%Y-%m-%d'),' 00:00:00') and CONCAT(date_format(:toDate, '%Y-%m-%d'),' 23:59:59')"+orderBycause);
				setParam.put("fromDate", DateUtill.getDateFromString(request.getFromDate()));
				setParam.put("toDate", DateUtill.getDateFromString(request.getToDate()));

				return true;
			}
			logger.debug("getFromDateAndToDateQuery Query :: {}", whereClause.toString());
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningReqDetailsBO> getProvisioningReqDetails(String requestID) {
		logger.info("Started ProvisioningRequestDaoImpl :: getProvisioningReqDetails() ");
		logger.debug(" getProvisioningReqDetails() is Used To fetch Data by passing argument requestID {}", requestID);
		List<ProvisioningReqDetailsBO> requestList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {

			builder.append("SELECT prd.*, pr.tenant_id, pr.partner_id FROM provisioning_requests pr ").append(
					"INNER JOIN provisioning_request_details prd ON pr.request_id = prd.request_id WHERE pr.REQUEST_ID = :requestID ");

//			builder.append(
//					"SELECT prd.*,pr.tenant_id,pr.partner_id FROM provisioning_request_details prd,provisioning_requests pr ");
//			builder.append("WHERE pr.request_id = prd.request_id AND prd.request_id = :requestID ");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningReqDetailsBO.class);
			query.setParameter("requestID", requestID);
			requestList = query.getResultList();
			logger.debug(" Response Result from getProvisioningReqDetails() returns list Of ProvRequest {}",
					requestList.size());
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: getProvisioningReqDetails() ", e);
			throw e;
		}
		return requestList;
	}

	@SuppressWarnings("unchecked")
	public ProvisioningReqDetailsModel getActiveProvisioningReqDetails(ProvisioningReqDTO request) {
		logger.info("Started ProvisioningRequestDaoImpl :: getActiveProvisioningReqDetails() ");
		logger.debug(
				" getActiveProvisioningReqDetails() is Used To fetch ProvisioningReqDetails by passing RequstDto {}",
				gson.toJson(request));
		List<ProvisioningReqDetailsModel> requestList = new ArrayList<>();
		ProvisioningReqDetailsModel detailsModel = new ProvisioningReqDetailsModel();
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			builder.append("SELECT * FROM provisioning_request_details WHERE REQUEST_DETAIL_ID =:detailID ");
			builder.append("AND VERSION_NO =:versionNo");
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningReqDetailsModel.class);
			query.setParameter("detailID", request.getRequestDetailID());
			query.setParameter("versionNo", request.getVersion());
			requestList = query.getResultList();
			if (!CollectionUtils.isEmpty(requestList)) {
				logger.debug(" Response Result From getActiveProvisioningReqDetails() ",
						gson.toJson(requestList.get(0)));
				 getEntityManager().detach(requestList.get(0));
				return requestList.get(0);
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: getActiveProvisioningReqDetails() ", e);
		}
		logger.info("Ended ProvisioningRequestDaoImpl :: getActiveProvisioningReqDetails() ");
		return detailsModel;
	}

	@SuppressWarnings("null")
	public boolean updateProvisioningRequest(ProvisioningReqDTO request) throws Exception {
		logger.info("Started ProvisioningRequestDaoImpl :: updateProvisioningRequest() ");
		logger.debug(" updateProvisioningRequest() is Used To update ProvisioningReqDTO RequstDto {}",
				gson.toJson(request));
		boolean result = false;
		StringBuilder builder = new StringBuilder();
		Query query = null;
		try {
			String modifiedOn = DateUtill.dateToString(Calendar.getInstance().getTime());

			builder.append("UPDATE provisioning_requests SET STATUS =:status, MODIFIED_BY =:modifiedBy, REMARKS =:remarks").append(
					", MODIFIED_IPADDR =:modifiedIpaddr, MODIFIED_ON = :modifiedOn where REQUEST_ID =:requestId");

			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("status", 11);
			query.setParameter("modifiedBy", request.getModifiedBy());
			query.setParameter("modifiedIpaddr", request.getModifiedIPAddr());
			query.setParameter("modifiedOn", modifiedOn);
			query.setParameter("requestId", request.getRequestID());
			query.setParameter("remarks", Constants.NEW_VERSION);
			int i = query.executeUpdate();
			if (i > 0) {
				result = true;
				logger.debug("Response Result From :: updateProvisioningRequest() ", result);
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: updateProvisioningRequest() ", e);
		}
		logger.info("Ended ProvisioningRequestDaoImpl :: updateProvisioningRequest() ");
		return result;
	}

	public boolean updateProvisioningReqDetails(ProvisioningReqDTO request) throws Exception {
		logger.info("Started ProvisioningRequestDaoImpl :: updateProvisioningReqDetails() ");
		logger.debug(" updateProvisioningReqDetails :: requestDtoJson {}", gson.toJson(request));
		boolean result = false;
		Query query = null;
		StringBuilder builder = new StringBuilder();
		try {
			builder.append("UPDATE provisioning_request_details SET STATUS =:status")
					.append(" WHERE REQUEST_DETAIL_ID = :requestId AND VERSION_NO =:versionNo ");

			query = getEntityManager().createNativeQuery(builder.toString());
			query.setParameter("status",
					cache.strConfigVal(Constants.NEW_VERSION_AND_NOT_VALIDATED, Constants.GLOBAL_TENANT));
			query.setParameter("versionNo", request.getVersion()).setParameter("requestId", request.getRequestDetailID());
			int rowsUpdated = query.executeUpdate();
			result = rowsUpdated > 0;
			logger.debug("Response Result From updateProvisioningReqDetails :: {}",result);
			logger.info("Ended ProvisioningRequestDaoImpl :: updateProvisioningReqDetails() ");
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: updateProvisioningReqDetails() ", e);
			throw e;
		}
		return result;
	}

	public boolean saveProvisioningReqDetails(ProvisioningReqDetailsModel requestDetails) {
		boolean status = false;
		try {
			getEntityManager().merge(requestDetails);
			status = true;
		} catch (Exception e) {
			logger.error("The Exception is ProvisioningRequestDaoImpl ::saveProvisioningReqDetails()", e);
			throw e;
		}
		return status;
	}

	@SuppressWarnings("unchecked")
	public List<ProvisioningDashBoadBO> getDashBoardDetails(ProvisioningReqSearchDTO request) throws Exception {
		List<ProvisioningDashBoadBO> requestList = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		boolean descOrderBY = false;
		Query query = null;
		try {
			logger.info(" Started ProvisioningRequestDaoImpl ::getDashBoardDetails() ");
			logger.debug(" ProvisioningRequestDaoImpl ::getDashBoardDetails() :: ProvisioningReqSearchDTO {}", gson.toJson(request));
			if (FileUtil.getBooleanValue(request.getTenantID())) {
				whereClause.append(" AND pr.tenant_id  = :tenantID ");
				setParam.put("tenantID", request.getTenantID());
			}

			if (FileUtil.getBooleanValue(request.getPartnerID())) {
				whereClause.append(" AND pr.partner_id = :partnerID ");
				setParam.put("partnerID", request.getPartnerID());
			}
			/**
			 * if(FileUtil.getBooleanValue(request.getCustomerName())) {
			 * whereClause.append(" AND (cm.FIRST_NAME LIKE '%"+request.getCustomerName()
			 * +"%' OR cm.MIDDLE_NAME LIKE '%"+request.getCustomerName()+"%' ") .append("OR
			 * cm.LAST_NAME LIKE '%"+request.getCustomerName() +"%' OR cm.organization_NAME
			 * LIKE '%"+request.getCustomerName()+"%') "); }
			 * 
			 * if(FileUtil.getBooleanValue(request.getAccountName())) { whereClause.append("
			 * AND customer_account_name LIKE '%"+request. getAccountName()+"%' "); }
			 */

			if (FileUtil.checkEmptyNull(request.getAccountID())) {
				whereClause.append(" and pr.customer_account_id in (:accountID) ");
				setParam.put("accountID", request.getAccountID());
			}

			if (FileUtil.checkEmptyNull(request.getCustomerID())) {
				whereClause.append(" and pr.customer_id in (:customerID) ");
				setParam.put("customerID", request.getCustomerID());
			}
			if (FileUtil.checkEmptyNull(request.getActivityID())) {
				whereClause.append(" and pr.ACTIVITY_ID in (:activityID) ");
				setParam.put("activityID", request.getActivityID());
			}

			boolean flag = getFromDateAndToDateQuery(request, builder, whereClause, setParam, descOrderBY);
			if (flag) {
				builder.append(
						"SELECT IFNULL(SUM(1),0) total,IFNULL(SUM(CASE WHEN (pr.status= :postStatus) THEN 1 ELSE 0 END),0) AS success ")
						.append(",IFNULL(SUM(CASE WHEN(pr.status=25 OR pr.status=21 OR pr.status= :activeStatus OR pr.status= :genStatus OR pr.status=23) THEN 1 ELSE 0 END),0) AS Inprogress ")
						.append(",IFNULL(SUM(CASE WHEN(pr.status= :postFailStatus or pr.status= :errorStatus or pr.status= :refStatus) THEN 1 ELSE 0 END),0) AS Failed")
						.append(",IFNULL(SUM((SELECT COUNT(1) FROM provisioning_request_details prd WHERE prd.request_id =pr.request_id GROUP BY prd.request_id)),0) AS sub_request ")
						.append("FROM provisioning_requests pr WHERE ").append(" 1=1 " + whereClause.toString() + " ");

				query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningDashBoadBO.class);

				for (Map.Entry<String, Object> entry : setParam.entrySet()) {
					String key = entry.getKey();
					String val = entry.getValue().toString();
					query.setParameter(key, val);

				}

				query.setParameter("postStatus",
						cache.intConfigVal(Constants.POST_PROV_SUCCE, Constants.GLOBAL_TENANT));
				query.setParameter("activeStatus", cache.intConfigVal(Constants.ACT_STATUS, Constants.GLOBAL_TENANT));
				query.setParameter("genStatus",
						cache.intConfigVal(Constants.PROV_REQ_GENERATED, Constants.GLOBAL_TENANT));
				query.setParameter("postFailStatus",
						cache.intConfigVal(Constants.POST_PROV_FAIL, Constants.GLOBAL_TENANT));
				query.setParameter("errorStatus", cache.intConfigVal(Constants.ERROR_IN_EXEC, Constants.GLOBAL_TENANT));
				query.setParameter("refStatus", cache.intConfigVal(Constants.RF_STATUS, Constants.GLOBAL_TENANT));

				requestList = query.getResultList();
				logger.debug(" Response From  ::getDashBoardDetails() :: ProvRequestList Size {}", requestList.size());
				logger.info("Ended ProvisioningRequestDaoImpl ::getDashBoardDetails() ");
			}
		} catch (Exception e) {
			logger.error("ProvisioningRequestDaoImpl :: getDashBoardDetails() ", e);
			throw e;
		}
		return requestList;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> requestSource(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestDaoImpl :: requestSource() ");
		logger.debug(" ProvisioningRequestDaoImpl :: requestSource() :: ProvisioningReqSearchDTO {}", request);
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		List<Object[]> list = new ArrayList<>();
		Map<String, Object> setParam = new HashMap<>();
		boolean descOrderBY = false;
		Query query = null;
		try {
			if (StringUtils.hasText(request.getActivityID())) {
				whereCause.append(" AND pr.activity_id =:activityID ");
			}
			boolean flag = getFromDateAndToDateQuery(request, builder, whereCause, setParam, descOrderBY);
			if (flag == true) {
				builder.append(
						"SELECT activity_id,COUNT(*) FROM provisioning_requests pr WHERE pr.tenant_id = :tenantID")
						.append(whereCause).append(" GROUP BY activity_id");
				query = getEntityManager().createNativeQuery(builder.toString());
				if (StringUtils.hasText(request.getTenantID())) {
					query.setParameter("tenantID", request.getTenantID());
				}
				if (StringUtils.hasText(request.getActivityID())) {
					query.setParameter("activityID", request.getActivityID());
				}
				for (Entry<String, Object> entry : setParam.entrySet()) {
					String key = entry.getKey();
					String val = entry.getValue().toString();
					query.setParameter(key, val);

				}
				list = query.getResultList();
				logger.debug("Response Result From :: requestSource() RequestsSize {}", list.size());
			}
			logger.info("Ended ProvisioningRequestDaoImpl :: requestSource() ");
		} catch (Exception e) {
			logger.error("Exception Occured ProvisioningRequestDaoImpl :: requestSource() ");
		}
		return list;

	}

	@SuppressWarnings({ "unchecked", "null" })
	@Override
	public List<Object[]> getHowLOngRequests(ProvisioningReqSearchDTO request) {

		logger.info("Started ProvisioningRequestDaoImpl :: getHowLOngRequests() ");
		logger.debug(" ProvisioningRequestDaoImpl :: getHowLOngRequests() :: ProvisioningReqSearchDTO dto {}", gson.toJson(request));
		String labelQuery = cache.strConfigVal(Constants.LABEL_REQ, Constants.GLB_1);
		String totalRequsts = cache.strConfigVal(Constants.TOTAL_REQ, Constants.GLB_1);
		String noOfDays = cache.strConfigVal(Constants.DAYS, Constants.GLB_1);
		String[] split = noOfDays.split(",");
		List<Object[]> list = null;
		Query query = null;
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> queryMap = new HashMap<>();
		boolean descOrderBY = false;
		try {
			if (StringUtils.hasText(request.getTenantID())) {
				whereCause.append(" WHERE pr.TENANT_ID = :tenantID");
				queryMap.put("tenantID", request.getTenantID());
			}
			if (StringUtils.hasText(request.getActivityID())) {
				whereCause.append(" AND pr.activity_id = :activityID");
				queryMap.put("activityID", request.getActivityID());
			}
			boolean flag = getFromDateAndToDateQuery(request, whereCause, whereCause, queryMap, descOrderBY);
			if (flag == true) {

				StringBuilder sqlQuery = new StringBuilder();
				sqlQuery.append("SELECT ").append(labelQuery);
				sqlQuery.append(" UNION ");
				sqlQuery.append("SELECT ").append(totalRequsts);
				String s ="";

				for (int i = 0; i < split.length; i++) {
					String sp = split[i].replace("Days", "");
					if (sp.contains("-")) {
						String[] days = sp.split("-");
						Integer valueOf = Integer.valueOf(days[0].trim());
						
						if(valueOf > 0) {
							 s ="day_age > "+days[0]+" AND";
						}
						sqlQuery.append("SUM(CASE WHEN ").append(s).append(" day_age <= ")
								.append(days[1]).append(" THEN 1 ELSE 0 END) AS '").append(split[i]).append(" ', ");
					} else {
						sqlQuery.append("SUM(CASE WHEN day_age ").append(sp).append(" THEN 1 ELSE 0 END) AS '")
								.append(split[i]);
						if (i == split.length - 1) {
							sqlQuery.append("' ");
						} else {
							sqlQuery.append("', ");
						}
					}
				}
				sqlQuery.append(
						"FROM (SELECT pr.request_id, TIMESTAMPDIFF(DAY, pr.REQUEST_CREATION_DATE, CURRENT_DATE()) AS day_age ");
				sqlQuery.append("FROM ").append("provisioning_requests pr");
				sqlQuery.append(whereCause).append(") request_age");
				query = getEntityManager().createNativeQuery(sqlQuery.toString());
				for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
				list = query.getResultList();
				logger.debug(" Response Result From :: getHowLOngRequests() ::  listSize {}", list.size());
			}
			logger.info("Ended ProvisioningRequestDaoImpl :: getHowLOngRequests() ");

		} catch (Exception e) {
			logger.error("An exception ocured in ProvisioningRequestDaoImpl :: getHowLOngRequests() ", e);
		}

		return list;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProvisioningRequestsBO> getViewHowLongRequest(ProvisioningReqSearchDTO dto) {
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		List<ProvisioningRequestsBO> list = null;
		Map<String, String> setParam = new HashMap<>();
		Query query = null;
		logger.info("Enterd into ProvisioningRequestDaoImpl :: getViewHowLongRequest() ");
		logger.debug(" ProvisioningRequestDaoImpl :: getViewHowLongRequest() ProvisioningReqSearchDTO {}", dto);
		try {
			String viewRequest = dto.getRequestAge();
			String replace = viewRequest.replace("Days", "");
			if (replace.contains("-")) {
				String[] va = replace.split("-");
				whereCause.append(" > ").append(va[0])
						.append(" AND TIMESTAMPDIFF(DAY, REQUEST_CREATION_DATE, CURRENT_DATE()) <= ").append(va[1]);
			} else {
				whereCause.append(replace);
			}
			if (FileUtil.getBooleanValue(dto.getFromDate()) && FileUtil.getBooleanValue(dto.getToDate())) {
				whereCause.append(" AND DATE_FORMAT(REQUEST_CREATION_DATE, '%Y-%m-%d') BETWEEN  ")
						.append("DATE_FORMAT(:fromDate, '%Y-%m-%d') AND DATE_FORMAT(:toDate, '%Y-%m-%d') ");
				setParam.put("fromDate", DateUtill.getDateFromString(dto.getFromDate()));
				setParam.put("toDate", DateUtill.getDateFromString(dto.getToDate()));
			}
			builder.append(
					"SELECT REQUEST_ID,pr.CUSTOMER_ID,pr.CUSTOMER_ACCOUNT_ID,'' CUSTOMER_NAME, '' CUSTOMER_ACCOUNT_NAME, REQUEST_CREATION_DATE,")
					.append("PR.NODE_ID,pr.activity_id, STATUS FROM provisioning_requests pr ")
					.append("WHERE 1=1 AND TENANT_ID = :tenantID AND TIMESTAMPDIFF(DAY, REQUEST_CREATION_DATE, CURRENT_DATE()) ")
					.append(whereCause);
			query = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestsBO.class);
			if (StringUtils.hasText(dto.getTenantID())) {
				query.setParameter("tenantID", dto.getTenantID());
			}
			list = query.getResultList();
			logger.debug(" Response Result :: getViewHowLongRequest() ProvisioningRequestsDto size {}", list.size());
			logger.info("Ended ProvisioningRequestDaoImpl :: getViewHowLongRequest() ");
		} catch (Exception e) {
			logger.error("Exception Occured in ProvisioningRequestDaoImpl :: getViewHowLongRequest() ", e);

		}
		return list;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getRequestByLobs(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestDaoImpl :: getRequestByLobs() ");
		logger.debug(" ProvisioningRequestDaoImpl :: getRequestByLobs() :: ProvisioningReqSearchDTO {}", gson.toJson(request));
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		List<Object[]> resultList = null;
		Query qurey = null;
		boolean descOrderBY = false;
		try {
			if (StringUtils.hasText(request.getActivityID())) {
				whereCause.append(" AND pr.activity_id =:activityID ");
				setParam.put("activityID", request.getActivityID());
			}
			boolean flag = getFromDateAndToDateQuery(request, builder, whereCause, setParam, descOrderBY);
			if (flag == true) {
				builder.append("SELECT prd.LOB_ID,COUNT(DISTINCT pr.REQUEST_ID) FROM provisioning_requests pr ")
						.append("LEFT JOIN provisioning_request_details prd ON pr.REQUEST_ID=prd.REQUEST_ID ")
						.append("WHERE pr.tenant_id = :tenantID AND prd.LOB_ID <> ").append(Constants.GLB_1)
						.append(whereCause).append(" GROUP BY prd.LOB_ID ");
				qurey = getEntityManager().createNativeQuery(builder.toString());
				if (StringUtils.hasLength(request.getTenantID())) {
					qurey.setParameter("tenantID", request.getTenantID());
				}
				for (Map.Entry<String, Object> entry : setParam.entrySet()) {
					qurey.setParameter(entry.getKey(), entry.getValue());
				}
				resultList = qurey.getResultList();
			}
			logger.debug(" Response Result From :: getRequestByLobs() :: Size {}", resultList.size());
			logger.info("Ended ProvisioningRequestDaoImpl :: getRequestByLobs() ");
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: getRequestByLobs() ", e);
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getRequestByServices(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestDaoImpl :: getRequestByServices() ");
		logger.debug(" ProvisioningRequestDaoImpl :: getRequestByServices() :: ProvisioningReqSearchDTO {}", gson.toJson(request));
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		List<Object[]> resultList = new ArrayList<>();
		Query qurey = null;
		boolean descOrderBY = false;

		try {

			if (StringUtils.hasText(request.getActivityID())) {
				whereCause.append(" AND pr.activity_id =:activityID ");
				setParam.put("activityID", request.getActivityID());
			}
			boolean falg = getFromDateAndToDateQuery(request, builder, whereCause, setParam, descOrderBY);
			if (falg == true) {
				builder.append("SELECT prd.SERVICE_ID,COUNT(DISTINCT pr.REQUEST_ID) FROM provisioning_requests pr ")
						.append("LEFT JOIN provisioning_request_details prd ON pr.REQUEST_ID=prd.REQUEST_ID ")
						.append("WHERE pr.tenant_id = :tenantID AND prd.SERVICE_ID <> ").append(Constants.GLB_1)
						.append(whereCause).append(" GROUP BY prd.SERVICE_ID");
				qurey = getEntityManager().createNativeQuery(builder.toString());
				qurey.setParameter("tenantID", request.getTenantID());
				for (Map.Entry<String, Object> entry : setParam.entrySet()) {
					qurey.setParameter(entry.getKey(), entry.getValue());
				}
				resultList = qurey.getResultList();
			}
			logger.debug(" Response From :: getRequestByServices() :: resultListSize {}", resultList.size());
			logger.info("Ended ProvisioningRequestDaoImpl :: getRequestByServices() ");
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: provisioningPriority() ", e);
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getRequestsByServers(ProvisioningReqSearchDTO request) {
		logger.info("Started ProvisioningRequestDaoImpl :: getRequestsByServers() ");
		logger.debug(" ProvisioningRequestDaoImpl :: getRequestsByServers() :: ProvisioningReqSearchDTO {}", gson.toJson(request));
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		Query qurey = null;
		List<Object[]> list = new ArrayList<>();
		boolean descOrderBY = false;
		try {
			if (StringUtils.hasText(request.getActivityID())) {
				whereCause.append("AND pr.activity_id =:activityID ");
				setParam.put("activityID", request.getActivityID());
			}
			boolean flag = getFromDateAndToDateQuery(request, builder, whereCause, setParam, descOrderBY);
			if (flag == true) {
				builder.append("SELECT prd.SERVER_NAME,COUNT(DISTINCT pr.REQUEST_ID) FROM provisioning_requests pr ")
						.append("LEFT JOIN provisioning_request_details prd ON pr.REQUEST_ID=prd.REQUEST_ID ")
						.append("WHERE pr.tenant_id = :tenantID ").append(whereCause)
						.append(" GROUP BY prd.SERVER_NAME");
				qurey = getEntityManager().createNativeQuery(builder.toString());
				qurey.setParameter("tenantID", request.getTenantID());
			} else {
				return list;
			}
			for (Map.Entry<String, Object> entry : setParam.entrySet()) {
				qurey.setParameter(entry.getKey(), entry.getValue());
			}
			list = qurey.getResultList();
			logger.debug(" Response From :: getRequestsByServers() list Size {}",list.size());
			logger.info("Ended ProvisioningRequestDaoImpl :: getRequestsByServers() ");
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: provisioningPriority()", e);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getRatePlanIds(ProvisioningReqSearchDTO dto) {
		logger.info("Started ProvisioningRequestDaoImpl :: getRatePlanIds ");
		logger.debug(" getRatePlanIds ProvisioningReqSearchDTO :: dto {} ",gson.toJson(dto));
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		Map<String, Object> setParam = new HashMap<>();
		List<Long> list = new ArrayList<>();
		Query qurey = null;
		boolean descOrderBY = false;
		try {
			if (StringUtils.hasText(dto.getTenantID())) {
				whereCause.append(" WHERE pr.tenant_id =:tenantID ");
				setParam.put("tenantID", dto.getTenantID());
			}
			if (StringUtils.hasText(dto.getActivityID())) {
				whereCause.append(" AND pr.activity_id =:activityID");
				setParam.put("activityID", dto.getActivityID());
			}
			boolean flag = getFromDateAndToDateQuery(dto, builder, whereCause, setParam, descOrderBY);
			if (flag == true) {
				builder.append("SELECT prd.RATE_PLAN_ID FROM provisioning_requests pr ")
						.append("LEFT JOIN provisioning_request_details prd ON pr.REQUEST_ID=prd.REQUEST_ID ")
						.append(whereCause).append("GROUP BY prd.RATE_PLAN_ID,prd.request_id ");
				qurey = getEntityManager().createNativeQuery(builder.toString());
				for (Map.Entry<String, Object> entry : setParam.entrySet()) {
					qurey.setParameter(entry.getKey(), entry.getValue());
				}
				list = qurey.getResultList();
				logger.debug(" Response Result From getRatePlanIds() ratePlanIdSize {}", list.size());
			}
			logger.info("Ended ProvisioningRequestDaoImpl :: getRatePlanIds ");
		} catch (Exception e) {
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: getRatePlanIds()", e);
		}
		return list;
	}
	
	@Override
	public ProvisioningRequestBO getProvisioningRequest(String workOrderId) {
		logger.info("Started ProvisioningRequestDaoImpl :: getProvisioningRequest :: workOrderId {}", workOrderId);
		StringBuilder builder = new StringBuilder();
		ProvisioningRequestBO requestObj = null;
		Query qurey = null;
		try {
			builder.append("SELECT pr.* FROM provisioning_request_details prd JOIN provisioning_requests pr ON pr.REQUEST_ID = prd.REQUEST_ID WHERE prd.REQ_SER_RESP_ID IN (:workOrderId)");
			qurey = getEntityManager().createNativeQuery(builder.toString(), ProvisioningRequestBO.class);
			qurey.setParameter("workOrderId",workOrderId);
			requestObj = (ProvisioningRequestBO) qurey.getResultList().get(0);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: getProvisioningRequest()", e);

		}
		logger.info("Ended ProvisioningRequestDaoImpl :: getProvisioningRequest :: Response From dao requestId", requestObj.getRequestID());
		return requestObj;
	}


	public Long getProvRequDtlById(String reqDtlId) {
	    logger.info("Started ProvisioningRequestDaoImpl :: getProvRequDtlById :: reqDtlId {}", reqDtlId);
	    StringBuilder builder = new StringBuilder();
	    Query qurey = null;
	    try {
	        builder.append("SELECT pr.work_order_id FROM provisioning_request_details prd, provisioning_requests pr ")
	            .append("WHERE prd.request_id=pr.request_id AND request_detail_id = :reqDtlId AND prd.status = :status ");
	        qurey = getEntityManager().createNativeQuery(builder.toString());
	        qurey.setParameter("reqDtlId", reqDtlId);
	        qurey.setParameter("status", 73);
	        Object result = qurey.getSingleResult();
	        if (result instanceof BigInteger) {
	            return ((BigInteger) result).longValue();
	        } else if (result instanceof Long) {
	            return (Long) result;
	        } else if (result instanceof Number) {
	            return ((Number) result).longValue();
	        } else {
	            throw new IllegalArgumentException("Unexpected result type: " + result.getClass());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: getProvisioningRequest()", e);
	    }
	    logger.info("Ended ProvisioningRequestDaoImpl :: getProvisioningRequest ");
	    return null;
	}
	
	@Override
	public ProvisioningRequestBO getWorkOrderByPassingRequDtlId(String reqDtlID,String externalID) throws ProvisionException {
		logger.info("Started ProvisioningRequestDaoImpl :: getWorkOrderByPassingReqexternalID :: ");
		StringBuilder builder = new StringBuilder();
		StringBuilder whereCause = new StringBuilder();
		ProvisioningRequestBO requestObj = new ProvisioningRequestBO();
		Map<String, Object> setParam = new HashMap<>();
		Query qurey = null;
		
		try {
			if(StringUtils.hasText(reqDtlID)) {
				whereCause.append("AND REQUEST_DETAIL_ID = :reqDtlID ");
				setParam.put("reqDtlID", reqDtlID);
			}if(StringUtils.hasText(externalID)) {
				whereCause.append("AND REQ_SER_RESP_ID = :externalID ");
				setParam.put("externalID", externalID);
			}
			  builder.append("SELECT pr.*  FROM provisioning_requests pr, provisioning_request_details prd ")
			  .append("WHERE prd.request_id=pr.request_id ").append(whereCause);
			  qurey = getEntityManager().createNativeQuery(builder.toString(),ProvisioningRequestBO.class);
			  for (Map.Entry<String, Object> entry : setParam.entrySet()) {
					qurey.setParameter(entry.getKey(), entry.getValue());
				}
			  requestObj  =  (ProvisioningRequestBO) qurey.getSingleResult();
			  logger.info("Ended ProvisioningRequestDaoImpl :: getProvisioningRequest ");		
		      return requestObj;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("An Exception Occured in ProvisioningRequestDaoImpl :: getWorkOrderByPassingReqexternalID()", e);
			throw new ProvisionException("Unable to find requests for provided Id ", Constants.ORDER_VALIDATION_FAILED);
		}
	}

}
