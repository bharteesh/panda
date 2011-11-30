package org.ithaka.cm.ebooks.vendor.interchange.webservice;

import javax.xml.ws.Endpoint;

public class TrialHttpsServer {
	public static void main (String [] args){
		Endpoint.publish("http://localhost:8080/test/IthakaService", new IthakaService());
	}

}
