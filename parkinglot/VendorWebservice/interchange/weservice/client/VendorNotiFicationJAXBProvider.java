package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ithaka.cm.ws.vendor.notification.xml.NewSubmission;
import org.ithaka.cm.ws.vendor.notification.xml.Notification;
import org.ithaka.cm.ws.vendor.notification.xml.ObjectFactory;

public class VendorNotiFicationJAXBProvider {
	private static  Log logger =LogFactory.getLog(VendorNotiFicationJAXBProvider.class);
	private static JAXBContext jaxbContext;
	private ObjectFactory objFactory;
	
	public String  Marshall(String submission) throws Exception{
		Marshaller marshaller=null;
		if(jaxbContext==null){
			try {
				jaxbContext=JAXBContext.newInstance("org.ithaka.cm.ws.vendor.notification.xml");
				 marshaller =jaxbContext.createMarshaller();
				
			} catch (JAXBException e) {
			logger.info("JAXB Exception while creating the context");
			}
		}
		objFactory = new ObjectFactory();
		Notification notification=objFactory.createNotification();
		//notification.setVendor("Apex");
		notification.setVendor(PropertyLoader.getProperty(VendorDataInterChangeConstants.VENDOR_NAME));
		String [] submissionStringArray =submission.split("\\_");
		String bookid=submissionStringArray[0];
		String versionid=submissionStringArray[1];
		String submissionid=submissionStringArray[2];
		
		notification.setNotificationId("newId");
		GregorianCalendar c =new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar cal=null;
		try {
			cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e1) {
			throw new Exception(e1);
		}
		notification.setDate(cal);
		NewSubmission newSubmission=objFactory.createNewSubmission();
		
		
		
		newSubmission.setSubmissionId(new BigInteger(submissionid));
		newSubmission.setVersionId(new BigInteger(versionid));
		newSubmission.setFilename(submission);
		newSubmission.setMd5("MD5");
		newSubmission.setId(bookid);
		notification.setNewSubmission(newSubmission);
		StringWriter sw =new StringWriter();
		try {
			marshaller.marshal(notification, sw);
			//marshaller.m
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String note=sw.toString();
		return note;          
		
	}	

}
