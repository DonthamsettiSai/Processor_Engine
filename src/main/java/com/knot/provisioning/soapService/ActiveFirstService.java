package com.knot.provisioning.soapService;

import com.knot.tring.procesprov.schema.ActiveFirstRequest;

public interface ActiveFirstService {
	boolean processActiveFirst(ActiveFirstRequest request) throws Exception;

}
