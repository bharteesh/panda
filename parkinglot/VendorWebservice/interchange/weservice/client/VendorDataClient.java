package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaService;
import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaServiceInteface;
import org.ithaka.cm.ebooks.vendor.interchange.webservice.IthakaServiceService;

public class VendorDataClient {
	private static Log logger =LogFactory.getLog(VendorDataClient.class);
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		VendorDataWSSSLClient client =new VendorDataWSSSLClient();
		String response=null;
		String submission=PropertyLoader.getProperty(VendorDataInterChangeConstants.SUBMISSION);
		try {
			response = client.getResponse("vendor1",submission);
		} catch (Exception e) {
		logger.error("Exception while posting the request "+e);
		}
		logger.info((response));

		

	}

}
