package org.ithaka.cm.ebooks.vendor.interchange.weservice.client;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ithaka.cm.ws.vendor.notification.xml.NewSubmission;
import org.ithaka.cm.ws.vendor.notification.xml.Notification;
import org.ithaka.cm.ws.vendor.notification.xml.ObjectFactory;

public class VendorNotiFicationJAXBProvider {
	private static  Log logger =LogFactory.getLog(VendorNotiFicationJAXBProvider.class);
	private static JAXBContext jaxbContext;
	private ObjectFactory objFactory;
	
	public String  Marshall(){
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
		NewSubmission newSubmission=objFactory.createNewSubmission();
		newSubmission.setSubmissionId("ID");
		newSubmission.setSubmissionId("SubmissionID");
		newSubmission.setVersionId("VersionID");
		newSubmission.setFilename("FileName");
		newSubmission.setMd5("MD5");
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
