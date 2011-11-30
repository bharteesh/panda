package org.ithaka.cm.ebooks.vendor.interchange.webservice;

import javax.jws.WebService;



@WebService(endpointInterface="org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaServiceInteface")
public class IthakaService implements IthakaServiceInteface {
	

	@Override
	public String sayHello() {
		
		return "Successfully Authunticated the Certificate Mr.";
	}

}
