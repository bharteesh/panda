package org.ithaka.cm.ebooks.vendor.interchange.webservice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.Endpoint;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;





public class IthakaHttpsServer {
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException{
		
		IthakaServiceInteface sinf= new IthakaService();
		Endpoint endpoint =Endpoint.create(sinf);
		
		SSLContext ssl=null;
		KeyManagerFactory kmf=null;
		KeyStore store =null;
		KeyManager [] keyManager=null;
		TrustManagerFactory tmf= null;
		try {
			 ssl =SSLContext.getInstance("SSLV3");
			//ssl =SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			 kmf =KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//X509TrustManager tm =new X509TrustManager(){
//
//	@Override
//	public void checkClientTrusted(X509Certificate[] chain, String authType)
//			throws CertificateException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void checkServerTrusted(X509Certificate[] chain, String authType)
//			throws CertificateException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public X509Certificate[] getAcceptedIssuers() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//};
		store=KeyStore.getInstance(KeyStore.getDefaultType());
		store.load(new FileInputStream("C:\\JSON-workspace\\JsonUtility\\keystorenew2.jks"),"cmithaka".toCharArray());
		kmf.init(store, "cmithaka".toCharArray());
		keyManager= new KeyManager[1];
		keyManager=kmf.getKeyManagers();
		tmf=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(store);
		TrustManager[] trustManagers=tmf.getTrustManagers();
		try {
			ssl.init(keyManager, trustManagers, new SecureRandom());
			//ssl.init(null, null, null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SSLSocketFactory ssf =new SSLSocketFactory(ssl);
		//ClientConnectionManager scm =
		HttpsConfigurator configurator =new HttpsConfigurator(ssl);
	    HttpsServer httpsServer =HttpsServer.create(new InetSocketAddress("localhost",443),443);
	    httpsServer.setHttpsConfigurator(configurator);
	    HttpContext context =httpsServer.createContext("/sslTest");
	    httpsServer.start();
	  endpoint.publish(context);
	  endpoint.getProperties();
	  System.out.print("Server Started");
	  //   Endpoint.publish("https://localhost:443/sslTest", new SOAPService());
	}

}
