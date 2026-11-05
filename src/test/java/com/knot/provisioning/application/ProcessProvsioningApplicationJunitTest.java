package com.knot.provisioning.application;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knot.provioning.model.ProvRespDetails;
import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provioning.model.ProvisioningRequestDetails;
import com.knot.provisioning.bo.ConfigBo;
import com.knot.provisioning.bo.ProvisioningDashBoadBO;
import com.knot.provisioning.bo.ProvisioningReqDetailsBO;
import com.knot.provisioning.bo.ProvisioningRequestsBO;
import com.knot.provisioning.daoimpl.ConfigDaoImpl;
import com.knot.provisioning.daoimpl.ProvisioningDaoImpl;
import com.knot.provisioning.daoimpl.ProvisioningRequestDaoImpl;
import com.knot.provisioning.dto.ProvisioningReqSearchDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { ProcessProvsioningApplicationJunitTest.class })
@ActiveProfiles("test")
@ComponentScan("com.knot.*")
public class ProcessProvsioningApplicationJunitTest {

	private static final Logger log = LogManager.getLogger(ProcessProvsioningApplicationJunitTest.class);

	@Autowired
	private ProvisioningDaoImpl daoImpl;

	@Autowired
	private ProvisioningRequestDaoImpl requestDaoImpl;

	@Autowired
	private ConfigDaoImpl configDaoImpl;

	@Test
	public void updateRequestDetailObject() {

		log.info("Junit Test Case Started for ::  updateRequestDetailObject() ");
		BigInteger requestDetailId = BigInteger.valueOf(151);
		String requestObject = "sss";
		Boolean status = daoImpl.updateRequestObject(requestDetailId, requestObject);
		assertTrue(status);
		if (status == true) {
			log.info("requestDetailId and requestObject Updated Sucessfully  " + status);
			log.info("Junit Test Case Ended for ::  updateRequestDetailObject() " + status);
		} else {
			log.info("requestDetailId and requestObject Failed to update" + status);
		}

	}

	@Test
	public void updateProvisioningRequestDetails() {

		log.info("Junit Test Case Started for ::  updateProvisioningRequestDetails() ");
		
		BigInteger requestDetailId = BigInteger.valueOf(151);
		BigInteger requestId = BigInteger.valueOf(42);
		Boolean stats = daoImpl.updateProvisioningRequestDetails(requestDetailId, requestId);
		assertTrue(stats);
		if (stats == true) {
			log.info("requestDetailId and requestID Updated Sucessfully with requestPicketDate = NOW()  " + stats);
			log.info("Junit Test Case Ended for ::  updateRequestDetailObject() " + stats);
		} else {
			log.info("requestDetailId and requestID Failed to update" + stats);
		}

	}

	@Test
	public void updateProvisioningRequestDetailCompletions() {

		log.info("Junit Test Case Started for ::  updateProvisioningRequestDetailCompletion() ");
		
		BigInteger requestDetailId = BigInteger.valueOf(151);
		Integer status = 12;
		Integer retryCount = 4;
		String errorCode = "53";
		String errorDesc = "sucess";
		Boolean stat = daoImpl.updateProvisioningRequestDetailCompletion(requestDetailId, status, retryCount, errorCode,
				errorDesc);
		assertTrue(stat);
		if (stat == true) {
			log.info("updateProvisioningRequestDetailCompletions() Updated Sucessfully  " + status);
			log.info("Junit Test Case Ended for ::  updateProvisioningRequestDetailCompletions() " + status);
		} else {
			log.info("updateProvisioningRequestDetailCompletions() Failed to update" + status);
		}

	}

	@Test
	public void updateProvisioningRequest() {

		log.info("Junit Test Case Started for ::  updateProvisioningRequest() ");
		BigInteger requestId = BigInteger.valueOf(151);
		Integer status = 14;
		String remarks = "Sucess";
		Boolean stat = daoImpl.updateProvisioningRequest(requestId, status, remarks, null);
		assertTrue(stat);
		if (stat == false) {
			log.info("updateProvisioningRequest() Updated Sucessfully with requestId : " + requestId + " Status : "
					+ status + " remarks : " + remarks);
			log.info("Junit Test Case Ended for ::  updateProvisioningRequest() " + status);
		} else {
			log.info("updateProvisioningRequest() Failed to update" + status);
		}

	}

	@Test
	public void getByServiceSequence() {
		int requestId = 151;
		List<ProvisioningRequestDetails> byServiceSequence = daoImpl.getByServiceSequence(requestId);
		assertNotNull(byServiceSequence);
		log.info("Sucess Case  : " + byServiceSequence.toString());

	}

	@Test
	public void getByServiceParallel() {
		int requestId = 151;
		List<ProvisioningRequestDetails> detailsList = daoImpl.getByServiceParallel(requestId);
		assertNotNull(detailsList);
		log.info("Sucess Case  : " + detailsList.toString());
	}

	@Test
	public void updateProvReqProgress() {

		log.info("Junit Test Case Started for ::  updateProvReqProgress() ");
BigInteger requestId = BigInteger.valueOf(151);
		int status = 16;
		Boolean stat = daoImpl.updateProvReqProgress(requestId, status);
		assertTrue(stat);
		if (stat == false) {
			log.info("updateProvReqProgress() Updated Sucessfully with requestId :" + requestId + " " + "Status :"
					+ status);
			log.info("Junit Test Case Ended for ::  updateRequestDetailObject() " + stat);
		} else {
			log.info("requestDetailId and requestObject Failed to update" + stat);
		}

	}

	@Test
	public void getDependentResponse() {

		log.info("Junit Test Case Started for ::  getDependentResponse() ");
		int requestId = 54;
		int requestDetailID = 190;
		String response = daoImpl.getDependentResponse(requestDetailID, requestId);
		assertNotNull(response);
		log.info("Sucess Case  : " + response);
		log.info("Junit Test Case Ended for ::  getDependentResponse() ");
	}

	@Test
	public void getRequestDetailsGroups() {

		log.info("Junit Test Case Started for ::  getRequestDetailsGroups() ");
		BigInteger requestId = BigInteger.valueOf(151);
		List<ProvisioningRequestDetails> requestDetailsGroups = daoImpl.getRequestDetailsGroups(requestId);
		assertNotNull(requestDetailsGroups);
		if (!requestDetailsGroups.isEmpty() && requestDetailsGroups !=null) {
			log.info("sucess" + "---" + requestDetailsGroups.size());
		} else {
			log.info("false");
		}
		log.info("Junit Test Case Ended for ::  getRequestDetailsGroups() ");
	}

	@Test
	public void getProvisioningRequests() {

		log.info("Junit Test Case Started for ::  getProvisioningRequests() ");

		List<ProvisioningRequest> provisioningRequests = daoImpl.getProvisioningRequests(null, null);
		assertNotNull(provisioningRequests);
		log.info("Sucess Case  : " + provisioningRequests.toString() + " provisioningRequests.Size()   "
				+ provisioningRequests.size());

		log.info("Junit Test Case Ended for ::  getProvisioningRequests() ");
	}

	@Test
	public void getProvisioningErrorRequests() {

		log.info("Junit Test Case Started for ::  getProvisioningErrorRequests() ");

		List<ProvisioningRequest> provisioningErrorRequests = daoImpl.getProvisioningErrorRequests();
		assertNotNull(provisioningErrorRequests);
		if (!provisioningErrorRequests.isEmpty() && provisioningErrorRequests !=null) {
			log.info("sucess" + provisioningErrorRequests.size());
		} else {
			log.info("false");
		}
		log.info("Junit Test Case Ended for ::  getProvisioningErrorRequests() ");

	}

	@Test

	public void updateProvisioningRequestProgress() {

		log.info("Junit Test Case Started for ::  updateProvisioningRequestProgress() ");

		BigInteger requestId = BigInteger.valueOf(151);
		Boolean status = daoImpl.updateProvisioningRequestProgress(requestId);
		assertTrue(status);
		if (status == false) {
			log.info("updateProvisioningRequestProgress()  Updated Sucessfully  ");
		} else {
			log.info("true");
		}
		log.info("Junit Test Case Ended for ::  updateProvisioningRequestProgress() ");
	}

	@Test
	public void updateProvisioningRequestDetailCompletion() {

		log.info("Junit Test Case Started for ::  updateProvisioningRequestDetailCompletion() ");

		BigInteger requestDetailId = BigInteger.valueOf(151);
		Integer status = 54;
		Integer retryCount = 4;
		Boolean stats = daoImpl.updateProvisioningRequestDetailCompletion(requestDetailId, status, retryCount);
		assertTrue(stats);
		if (stats == true) {
			log.info("updateProvisioningRequestDetailCompletion() Updated Sucessfully  ");
			log.info("Junit Test Case Ended for ::  updateProvisioningRequestDetailCompletion() ");
		} else {
			log.info("updateProvisioningRequestDetailCompletion() Failed to update ");
		}
	}

	@Test
	public void getProvisioningResponse() {
		log.info("Junit Test Case Started for ::  getProvisioningResponse() ");
		ProvisioningRequestDetails details = new ProvisioningRequestDetails();
		ProvisioningRequest request = new ProvisioningRequest();
		details.setRequestDetailID(BigInteger.valueOf(189));
		details.setServiceID(BigInteger.valueOf(10280));
		request.setRequestID(BigInteger.valueOf(54));
		request.setActivityID("10017");
		request.setActionID(1);

		List<ProvRespDetails> provisioningResponse = daoImpl.getProvisioningResponse(details, request);
		assertNotNull(provisioningResponse);
		log.info("Junit Test Case Ended for ::  getProvisioningResponse() ");
	}

	@Test
	public void searchProvisioningRequest() {

		try {
			ObjectMapper mapper = new ObjectMapper();
			String requestDto = "{\r\n" + "    \"paginationObj\": {\r\n" + "        \"maxSize\": 0,\r\n"
					+ "        \"minSize\": 0,\r\n" + "        \"pageCount\": 11,\r\n"
					+ "        \"pageNumber\": 1,\r\n" + "        \"searchParameter\": \"\",\r\n"
					+ "        \"itemsPerPage\": 10,\r\n" + "        \"sortColumn\": \"\",\r\n"
					+ "        \"sortOrder\": \"\",\r\n" + "        \"totalDisplayCount\": \"\"\r\n" + "    },\r\n"
					+ "    \"fromDate\": \"08/14/2023\",\r\n" + "    \"toDate\": \"08/21/2023\",\r\n"
					+ "    \"partnerID\": \"-1\",\r\n" + "    \"tenantID\": \"10279\",\r\n" + "    \"flag\": \"S\"\r\n"
					+ "}";
			log.info("Junit Test Case Started for ::  searchProvisioningRequest() ");
			ProvisioningReqSearchDTO searchRequest = mapper.readValue(requestDto, ProvisioningReqSearchDTO.class);
			List<ProvisioningRequestsBO> request = requestDaoImpl.searchProvisioningRequest(searchRequest);
			assertNotNull(request);
			if (!request.isEmpty() && request != null) {
				log.info("requestSize " + request.size());

			} else {
				log.info("failueR ");
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		log.info("Junit Test Case ended for ::  searchProvisioningRequest() ");

	}

	@Test
	public void getDashBoardDetails() {

		try {
			ObjectMapper mapper = new ObjectMapper();
			String requestJson = "{\r\n" + "    \"paginationObj\": {\r\n" + "        \"maxSize\": 0,\r\n"
					+ "        \"minSize\": 0,\r\n" + "        \"pageCount\": 10,\r\n"
					+ "        \"pageNumber\": 1,\r\n" + "        \"searchParameter\": \"\",\r\n"
					+ "        \"sortColumn\": \"\",\r\n" + "        \"sortOrder\": \"\",\r\n"
					+ "        \"totalDisplayCount\": \"\"\r\n" + "    },\r\n" + "    \"fromDate\": \"08/14/2023\",\r\n"
					+ "    \"toDate\": \"08/21/2023\",\r\n" + "    \"partnerID\": \"-1\",\r\n"
					+ "    \"tenantID\": \"10279\"\r\n" + "}";
			ProvisioningReqSearchDTO request = mapper.readValue(requestJson, ProvisioningReqSearchDTO.class);
			log.info("Junit Test Case Started for ::  getDashBoardDetails() ");
			List<ProvisioningDashBoadBO> dashBoardDetails = requestDaoImpl.getDashBoardDetails(request);
			assertNotNull(dashBoardDetails);
			if (!dashBoardDetails.isEmpty()) {
				log.info("requestSize " + dashBoardDetails.size());

			} else {
				log.info("failueR ");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		log.info("Junit Test Case Started for ::  getDashBoardDetails() ");
	}

	@Test
	public void getProvisioningReqDetails() {

		log.info("Junit Test Case Started for ::  getProvisioningReqDetails() ");
		String requestId = "11294";

		List<ProvisioningReqDetailsBO> provisioningReqDetails = requestDaoImpl.getProvisioningReqDetails(requestId);
		assertNotNull(provisioningReqDetails);
		if (!provisioningReqDetails.isEmpty()) {
			log.info("requestSize " + provisioningReqDetails.size());

		} else {
			log.info("failueR ");
		}
		log.info("Junit Test Case Started for ::  getProvisioningReqDetails() ");
	}

	@Test
	public void getAllConfigs() {
		String tenantID = "10231";
		log.info("jUNIT TestCse started for getAllConfigs() ");
		List<ConfigBo> allConfigs = configDaoImpl.getAllConfigs(tenantID);
		assertNotNull(allConfigs);
		if (!allConfigs.isEmpty() && allConfigs != null) {
			log.info("getAllConfigsSize() " + allConfigs.size());
		} else {
			log.info("failueR ");
		}

	}

}
