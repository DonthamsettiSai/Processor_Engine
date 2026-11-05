package com.knot.provisioning.service;

import java.util.List;

import com.knot.provioning.model.ActivityConfig;
import com.knot.provisioning.dto.ActivityConfigDto;
import com.knot.provisioning.dto.LifeCycleDto;
import com.knot.provisioning.dto.MsisdnDTO;

/**
 * @author Sai D
 *
 */

public interface ProvisioningService {

	public void processProvisioningRequest() throws Exception;

	public boolean processProvisioningErrorRequests() throws Exception;

	public Boolean getProvisioningRequestErrors(Integer reqDetailId);

	public boolean activeFirstResource(MsisdnDTO dto) throws Exception;

	public boolean updateLifeCycleStatus(LifeCycleDto dto) throws Exception;

	public boolean saveProvActivityConfig(List<ActivityConfigDto> activityConfig) throws Exception;

	public List<ActivityConfig> getAllActvityConfigs(ActivityConfigDto dto) throws Exception;
	
}
