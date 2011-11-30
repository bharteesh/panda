package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.ws.rs.core.MediaType;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientRequest;




public class DataInterchanges {
	
	
	public void postNotification(String notification){
		VendorNotiFicationJAXBProvider  jaxbfacade=new VendorNotiFicationJAXBProvider();
		jaxbfacade.Marshall();
		VendorDataWSSSLClient sslClient=new VendorDataWSSSLClient();
		try {
			String response=sslClient.getResponse("vendor");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	

}
