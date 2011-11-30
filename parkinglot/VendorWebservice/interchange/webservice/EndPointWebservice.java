package org.ithaka.cm.ebooks.vendor.interchange.webservice;

import javax.jws.WebService;

@WebService(endpointInterface="org.ithaka.cm.ebooks.vendor.interchange.webservice.Greeting")
public class EndPointWebservice implements Greeting{
	@Override
	public String sayHello(String name){
		return "Hello Welcome"+name;
	}

}
