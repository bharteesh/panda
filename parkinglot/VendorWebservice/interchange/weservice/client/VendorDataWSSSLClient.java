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
import org.apache.http.client.methods.HttpPost;
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
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VendorDataWSSSLClient {
	private static Log logger = LogFactory.getLog(VendorDataWSSSLClient.class);

	public synchronized String getResponse(String vendor) throws Exception {

		DefaultHttpClient defaultClient = new DefaultHttpClient();
		defaultClient = (DefaultHttpClient) wrapClientTLS(defaultClient);

		String prop = vendor + "."
				+ VendorDataInterChangeConstants.VENDOR_HOST_URL;
		String url = PropertyLoader.getProperty(vendor + "."
				+ VendorDataInterChangeConstants.VENDOR_HOST_URL);

		VendorNotiFicationJAXBProvider pro = new VendorNotiFicationJAXBProvider();
		String xmlString = pro.Marshall();

		ApacheHttpClient4Executor clientExecutor = new ApacheHttpClient4Executor(
				defaultClient);
		ClientRequest request = new ClientRequest(url, clientExecutor);

		request.accept(MediaType.APPLICATION_XML).body(
				MediaType.APPLICATION_XML, xmlString);
		String response1 = null;
		try {
			response1 = request.postTarget(String.class);
		} catch (Exception e) {
			logger.error("Exception while posting the request to webservice"
					+ e);
			throw new Exception();
		}
		return response1;
	}

	public static HttpClient wrapClientTLS(HttpClient base) throws Exception {

		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			logger.error("No such Algorithm Exception while getting the instance from ssl context "
					+ e);
			throw new Exception();
		}
		X509TrustManager tm = new X509TrustManager() {

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {

			}

			@Override
			public void x509(X509Certificate[] chain,
					String authType) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};

		try {
			ctx.init(null, new TrustManager[] { tm }, null);

		} catch (KeyManagementException e) {
			logger.error("Key management exception while initialzing context "
					+ e);
			throw new Exception();
		}
		SSLSocketFactory sslf = new SSLSocketFactory(ctx);

		ClientConnectionManager ccm = base.getConnectionManager();
		SchemeRegistry sr = ccm.getSchemeRegistry();

		sr.register(new Scheme("https", 443, sslf));

		return new DefaultHttpClient(ccm, base.getParams());
	}

}
