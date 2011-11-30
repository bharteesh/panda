package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaService;
import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaServiceInteface;
import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaServiceService;

public class VendorDataClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VendorDataWSSSLClient client =new VendorDataWSSSLClient();
		String response=null;
		try {
			response = client.getResponse("vendor1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(response);
//		IthakaServiceService se = new IthakaServiceService();
//		IthakaServiceInteface in=se.getIthakaServicePort();
//		System.out.println(in.sayHello("Bala"));
		

	}

}
