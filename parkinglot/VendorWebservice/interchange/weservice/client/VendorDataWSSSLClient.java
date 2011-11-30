package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.ws.rs.core.MediaType;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VendorDataWSSSLClient {
	private static Log logger =LogFactory.getLog(VendorDataWSSSLClient.class);
	public synchronized String getResponse(String vendor) throws Exception {
		HttpResponse response = null;
		KeyStore trust = null;
		FileInputStream fstream = null;
		SSLSocketFactory sslf = null;
		try {
			trust = KeyStore.getInstance(KeyStore.getDefaultType());
			fstream = new FileInputStream(
					new File(
							PropertyLoader
									.getProperty(vendor
											+ "."
											+ VendorDataInterChangeConstants.VENDOR_KEY_STORE_LOC)
											+ PropertyLoader
											.getProperty(vendor
													+ "."
											
											+ VendorDataInterChangeConstants.VENDOR_KEY_STORE_FILE)));
			trust.load(fstream,PropertyLoader.getProperty((vendor+"."+VendorDataInterChangeConstants.VENDOR_KEY_STORE_PASSWORD)).toCharArray());
			fstream.close();
			sslf = new SSLSocketFactory(trust);
			
		} catch (KeyStoreException e) {
			logger.error("Exception occured while creating instance of keystore"+e);
			throw new Exception("Exception occured while creating instance of keystore",e);
		} catch (FileNotFoundException e) {
			logger.error("Unable to find the keystore file",e);
			throw new Exception("Exception occured while creating instance of keystore",e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Scheme scheme = new Scheme("https", 443,sslf);

		// sslsocket.
		// ssl.

		// request
		// post
//		HttpClientParams params =new HttpConnectionParams();
//		HttpConnectionParams httpParams=new HttpConnectionParams();
//		httpParams.setConnectionTimeout(10000);
		//DefaultHttpClient defaultClient = new DefaultHttpClient();
		DefaultHttpClient defaultClient = new DefaultHttpClient();
		          defaultClient =(DefaultHttpClient) wrapClientTLS(defaultClient,trust);
		          
//		try {
//			//SSLContext ctx =SSLContext.getInstance("TLS");
//			//ctx.i
//		} catch (NoSuchAlgorithmException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	//	HttpClient defaultClient =new HttpClient();
//		defaultClient.getConnectionManager().getSchemeRegistry()
//				.register(scheme);
	String url = "https://localhost/sslTest?wsdl";
		//           String url ="https://localhost/sslTest/sayHello?bala";
		String xmlString = "bala";
		 HttpGet get =new HttpGet(url);
		// HttpParams params  =new HttpParams();
		// get.s
		 //get.setParams("bala");
		//get.
//		  try {
//			response=defaultClient.execute(get);
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		ApacheHttpClient4Executor clientExecutor=new ApacheHttpClient4Executor(defaultClient);
		ClientRequest request = new ClientRequest(url,clientExecutor);
		//request.queryParameter("name", "bala");
		
//		request.accept(MediaType.APPLICATION_XML).body(
//				MediaType.APPLICATION_XML, xmlString);
//		request.accept(MediaType.TEXT_PLAIN).body(
//			MediaType.TEXT_PLAIN, xmlString);
		String response1=null;
		try {
			//String response1 = request.postTarget(String.class);
			response1 = request.getTarget(String.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      // String responseString=response.toString();
		return response1;

	}
	public static HttpClient wrapClientTLS(HttpClient base,KeyStore trust){
		//DefaultHttpClient client=(DefaultHttpClient) base;
		SSLContext ctx=null;
		try {
			 ctx =SSLContext.getInstance("TLS");
			// ctx =SSLContext.getInstance("jks");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		X509TrustManager tm =new X509TrustManager(){

			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		
			
		};
		//KeyManager [] 
		try {
			ctx.init(null, new TrustManager[]{tm}, null);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SSLSocketFactory sslf =new SSLSocketFactory(ctx);
		
		//sslf.setHostnameVerifier(hostnameVerifier);
		ClientConnectionManager ccm=base.getConnectionManager();
		SchemeRegistry sr =ccm.getSchemeRegistry();
		//sr.register(new Scheme("https",sslf,443));
		sr.register(new Scheme("https",443,sslf));
		
		return new DefaultHttpClient(ccm,base.getParams());
	}
	
	
	public static HttpClient wrapClientJKS(HttpClient base,KeyStore trust,String vendor) throws Exception{
		ClientConnectionManager ccm=null;
		KeyStore keyStore = null;
		FileInputStream fstream = null;
		SSLSocketFactory sslf = null;
		SSLContext sslCtx=SSLContext.getInstance("https");
		try {
			trust = KeyStore.getInstance(KeyStore.getDefaultType());
			fstream = new FileInputStream(
					new File(
							PropertyLoader
									.getProperty(vendor
											+ "."
											+ VendorDataInterChangeConstants.VENDOR_KEY_STORE_LOC)
											+ PropertyLoader
											.getProperty(vendor
													+ "."
											
											+ VendorDataInterChangeConstants.VENDOR_KEY_STORE_FILE)));
			trust.load(fstream,PropertyLoader.getProperty((vendor+"."+VendorDataInterChangeConstants.VENDOR_KEY_STORE_PASSWORD)).toCharArray());
			PrivateKey key =(PrivateKey) keyStore.getKey("mykey","testkey".toCharArray());
			Certificate []certChain=keyStore.getCertificateChain("mykey");
			//sslCtx.
		
		
	}
		catch (KeyStoreException e) {
			logger.error("Exception occured while creating instance of keystore"+e);
			throw new Exception("Exception occured while creating instance of keystore",e);
		} catch (FileNotFoundException e) {
			logger.error("Unable to find the keystore file",e);
			throw new Exception("Exception occured while creating instance of keystore",e);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DefaultHttpClient(ccm,base.getParams());
}
}
	
	
