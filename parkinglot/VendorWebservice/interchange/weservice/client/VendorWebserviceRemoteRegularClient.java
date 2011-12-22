package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import javax.ws.rs.core.MediaType;

import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientRequest;

public class VendorWebserviceRemoteRegularClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://192.168.44.137:8080/VendorService/seam/resource/rest/notifications/postnotifications";
		 
		
		VendorNotiFicationJAXBProvider pro =new VendorNotiFicationJAXBProvider();
		String xmlString=null;
		try {
			xmlString = pro.Marshall("cts8r733z_2_6");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ClientRequest request = new ClientRequest(url);
		 request.accept(MediaType.APPLICATION_XML).body(
					MediaType.APPLICATION_XML, xmlString);
		try {
			String response = request.postTarget( String.class);
			System.out.print(response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
