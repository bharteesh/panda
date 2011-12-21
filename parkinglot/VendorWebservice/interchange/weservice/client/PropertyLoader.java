package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ithaka.cm.event.client.util.EventClientConstants;

/**
 * 
 * @author BKumaresan
 *
 */

public class PropertyLoader {
	private static Properties props;
	private static Log logger = LogFactory.getLog(PropertyLoader.class);
	

	
	/**
	 * 
	 */
	
	private  void  loadProperties() {
		props = new Properties();

		InputStream ins =this.getClass().getClassLoader().getResourceAsStream(VendorDataInterChangeConstants.VENDOR_DATA_PROPERTIES_FILE_NAME);
		try {
			props.load(ins);
		} catch (IOException e) {
			logger.error("Unable to Load the Vendor Data Properties file " + e);
		}		
	}
	
	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	
	public static String getProperty(String propertyName) {
		if(props==null){
			PropertyLoader pl =new PropertyLoader();
			pl.loadProperties();
		}		
		String property = props.getProperty(propertyName);
		return property;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public static Properties getVendorDataProperties() {
		if(props==null){
			
			PropertyLoader pl =new PropertyLoader();
			pl.loadProperties();
		}		
		return props;
	}
}
