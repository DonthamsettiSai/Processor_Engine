package com.knot.provisioning.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.web.client.RestTemplate;

import com.knot.provioning.model.ProvisioningRequest;
import com.knot.provisioning.dao.ProvisioningDao;
import com.knot.provisioning.network.OMSNetwork;
import com.knot.provisioning.network.RefMasterNetwork;
import com.knot.provisioning.network.UmsNetwork;
import com.knot.provisioning.soapclient.SoapClient;
import com.knot.provisioning.soapclient.SoapConnector;

/**
 * @author Sameena
 *
 */

public interface ProvisionService {

	public Future processProvisioningRequest(ProvisioningRequest provisioningRequest,ProvisioningDao dao,ExecutorService executor, CacheService cache, OMSNetwork omsNet, RestTemplate restTemplate,SoapConnector connector,SoapClient clientConnector,UmsNetwork umsNet, RefMasterNetwork defNet);

}
