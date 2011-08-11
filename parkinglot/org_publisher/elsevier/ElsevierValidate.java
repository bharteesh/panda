package org.publisher.elsevier;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.publisher.elsevier.xml.Dataset;

public class ElsevierValidate
{
  private final static String VALIDATE_PKG = "org.publisher.elsevier.xml";
  private JAXBContext jAXBContext = null;
  private Unmarshaller unmarshaller = null;
  private Marshaller marshaller = null;
  private static final int BUFFER_SIZE = 1024;
  public static final String FAILED = "FAILED";
  public static final String PASSED = "PASSED";

  // Confirmation Text XML
  public static final String CONFIRMATION_CUSTOMER_TAG = "customer=";
  public static final String CONFIRMATION_STATUS_TAG = "status=";
  public static final String CONFIRMATION_CUSTOMER_VALUE = "POR";
  public static final String CONFIRMATION_STATUS_VALUE = "Confirmation";

  // public static final String MAIL_FROM = "seshadri.ranganathan@portico.org";
  // public static final String MAIL_TO = "sranganathan@ncstech.com"; // "P.Mostert@elsevier.com"; // "sranganathan@ncstech.com";
  // public static final String MAIL_CC1 = "seshadri.ranganathan@portico.org";
  // public static final String MAIL_CC2 = "seshadri.ranganathan@portico.org";

  public ElsevierMail elsevierMail = null;

  public ElsevierValidate(ElsevierMail tElsevierMail)
  {
      try
      {
          elsevierMail = tElsevierMail;
          jAXBContext = JAXBContext.newInstance(VALIDATE_PKG);
          unmarshaller = jAXBContext.createUnmarshaller();
          marshaller = jAXBContext.createMarshaller();
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierValidate.ElsevierValidate()="+e.getMessage());
          System.out.println("Exception in ElsevierValidate.ElsevierValidate()="+e.getMessage());
      }
      finally
      {
      }
  }

  public ElsevierValidate()
  {
  }

  public boolean isValidDataset(ArrayList singleDataSetInfo, String feedName)
  {
      boolean gIsValid = true;
      File semaphoreFile = null;
      Hashtable fileListHash = new Hashtable();
      ArrayList alist = new ArrayList();
      boolean isValid = true;
      String datasetDirName = "";
      File localDatasetDir = null;
      ValuePair valuePair = null;
      String startTime = "";
      String endTime = "";
      Dataset dataset = null;
      ElsevierDB elsevierDB = null;

      // Walk thro' the file(s) underneath localDatasetDir
      // Ignore semaphore file
      // get the actual file size, checksum on the local file system
      // Pick the file size, checksum as given by the publisher for this file

      try
      {
          // get the File localDatasetDir
          if(singleDataSetInfo != null && singleDataSetInfo.size() > 0)
          {
              for(int sdindx=0; sdindx < singleDataSetInfo.size(); sdindx++)
              {
                  valuePair = (ValuePair)singleDataSetInfo.get(sdindx);
                  String key = valuePair.getKey();
                  String value = valuePair.getValue();
                  if(key.equals(ElsevierMain.DATASETDIRPATH))
                  {
                      localDatasetDir = new File(value);
                  }
                  else if(key.equals(ElsevierMain.STARTTIME))
                  {
                      startTime = value;
                  }
                  else if(key.equals(ElsevierMain.ENDTIME))
                  {
                      endTime = value;
                  }
              }
          }
          datasetDirName = localDatasetDir.getName();

          elsevierDB = new ElsevierDB(datasetDirName, feedName, localDatasetDir.lastModified()+"");

          ElsevierMain.printMe("Validating:------Start--------------Dataset="+datasetDirName);
          System.out.println("Validating:------Start--------------Dataset="+datasetDirName);

          File[] localDatasetFiles = localDatasetDir.listFiles();
          for(int sindx=0; sindx < localDatasetFiles.length; sindx++)
          {
               String fileName = localDatasetFiles[sindx].getName();
               if(fileName.equals(ElsevierMain.SEMAPHORE_FILE))
               {
                   semaphoreFile = localDatasetFiles[sindx];
                   break;
               }
	      }

	      if(semaphoreFile != null)
	      {
              ElsevierMain.printMe("ElsevierValidate:isValidDataset-ElsevierMain.DO_VALIDATE="+ElsevierMain.DO_VALIDATE);

			  if(ElsevierMain.DO_VALIDATE == true)
			  {
	              ElsevierMain.printMe("ElsevierValidate:isValidDataset-VALIDATING");
	              System.out.println("ElsevierValidate:isValidDataset-VALIDATING");

                  dataset = getDatasetInfo(semaphoreFile);
                  List expectedDatasetFileList = dataset.getFile();
                  if(expectedDatasetFileList.size() != localDatasetFiles.length-1) // ignore semaphore file in localDatasetFiles
                  {
	                  ElsevierMain.printMe("Validating:"+"WARNING:Local file(s),Expected file(s) do not match="+(localDatasetFiles.length-1)+","+expectedDatasetFileList.size());
	                  System.out.println("Validating:"+"WARNING:Local file(s),Expected file(s) do not match="+(localDatasetFiles.length-1)+","+expectedDatasetFileList.size());
	                  gIsValid = false;
	              }

                  for(int dindx=0; dindx < localDatasetFiles.length; dindx++)
                  {
	                  String localFileSize = "";
		              String localFileChecksum = "";
		              String expectedFileSize = "";
		              String expectedFileChecksum = "";

                      String localFileName = localDatasetFiles[dindx].getName();
                      if(localFileName.equals(ElsevierMain.SEMAPHORE_FILE))
                      {
		                  continue;
                      }
                      else
                      {
		                  ElsevierMain.printMe("Validating:--------------------FileName="+localFileName);
		                  System.out.println("Validating:--------------------FileName="+localFileName);

		                  localFileSize = getFileSize(localDatasetFiles[dindx]);
                          localFileChecksum = getFileCheckSum(localDatasetFiles[dindx]);
                          if(expectedDatasetFileList != null && expectedDatasetFileList.size() > 0)
                          {
                              for(int gindx=0; gindx < expectedDatasetFileList.size(); gindx++)
                              {
		                          org.publisher.elsevier.xml.File expectedFile = (org.publisher.elsevier.xml.File)expectedDatasetFileList.get(gindx);
		                          if(expectedFile.getName().equals(localFileName))
			                      {
			                          expectedFileSize = expectedFile.getSize();
				                      if(expectedFileSize == null)
				                      {
				                          expectedFileSize = "";
				                      }
			                          expectedFileChecksum = expectedFile.getMd5();
				                      if(expectedFileChecksum == null)
				                      {
				                          expectedFileChecksum = "";
				                      }
				                      break;
			                      }
			                  }
		                  }
                          if(!localFileName.equals(ElsevierMain.TOC_FILE))
                          {
                              elsevierDB.addDataunitInfo(localFileName, localFileSize, localDatasetFiles[dindx].lastModified()+"");
                          }
		              }
	                  boolean isFileSizeMatched = false;
		              boolean isFileChecksumMatched = false;

		              if(localFileSize != null && !localFileSize.equals("") &&
		                     expectedFileSize != null && !expectedFileSize.equals("") &&
		                     localFileSize.equals(expectedFileSize))
		              {
		                  isFileSizeMatched = true;
		              }

		              if(localFileChecksum != null && !localFileChecksum.equals("") &&
		                     expectedFileChecksum != null && !expectedFileChecksum.equals("") &&
		                     localFileChecksum.equals(expectedFileChecksum))
		              {
		                  isFileChecksumMatched = true;
		              }

		              ElsevierMain.printMe("Validating:actualsize,expectedsize,status="+localFileSize+","+expectedFileSize+","+isFileSizeMatched);
		              System.out.println("Validating:actualsize,expectedsize,status="+localFileSize+","+expectedFileSize+","+isFileSizeMatched);
		              ElsevierMain.printMe("Validating:actualfilechecksum,expectedfilechecksum,status="+localFileChecksum+","+expectedFileChecksum+","+isFileChecksumMatched);
		              System.out.println("Validating:actualfilechecksum,expectedfilechecksum,status="+localFileChecksum+","+expectedFileChecksum+","+isFileChecksumMatched);

		              isValid = isFileSizeMatched && isFileChecksumMatched;

		              if(isValid == false)
		              {
		                  gIsValid = false;
		              }
	              }
		      }
		      else
		      {
				  // Workaround no validation
	              ElsevierMain.printMe("ElsevierValidate:isValidDataset-NO VALIDATION");
	              System.out.println("ElsevierValidate:isValidDataset-NO VALIDATION");

                  for(int dindx=0; dindx < localDatasetFiles.length; dindx++)
                  {
	                  String localFileSize = "";
		              String localFileChecksum = "";

                      String localFileName = localDatasetFiles[dindx].getName();
                      if(localFileName.equals(ElsevierMain.SEMAPHORE_FILE))
                      {
		                  continue;
                      }
                      else
                      {
		                  ElsevierMain.printMe("Validating:--------------------FileName="+localFileName);
		                  System.out.println("Validating:--------------------FileName="+localFileName);

		                  localFileSize = getFileSize(localDatasetFiles[dindx]);
                          localFileChecksum = getFileCheckSum(localDatasetFiles[dindx]);
                          if(!localFileName.equals(ElsevierMain.TOC_FILE))
                          {
                              elsevierDB.addDataunitInfo(localFileName, localFileSize, localDatasetFiles[dindx].lastModified()+"");
                          }
		              }
	              }
			  }
          }
      }
      catch(Exception e)
      {
          gIsValid = false;
          System.out.println("Exception in ElsevierValidate.isValidDataset(dataset)="+datasetDirName+":"+e.getMessage());
      }
      finally
      {
      }

      // If file name, file size, file checksum does not match
      // Log error info
      // return false/true

      String outputStatus = ElsevierValidate.FAILED;
      if(gIsValid == true)
      {
	      outputStatus = ElsevierValidate.PASSED;
      }

      ElsevierMain.printMe("Validating:--------End-------------------Dataset,status="+datasetDirName+","+outputStatus);
      System.out.println("Validating:--------End-------------------Dataset,status="+datasetDirName+","+outputStatus);

      // createSendConfirmation
      createSendConfirmation(semaphoreFile, datasetDirName, startTime, endTime, gIsValid, feedName);

      if(gIsValid == true)
      {
         ElsevierMain.printMe("DB Update:--------Start-------------------Dataset="+datasetDirName);
         System.out.println("DB Update:--------Start-------------------Dataset="+datasetDirName);

         gIsValid = elsevierDB.doProcess();

         ElsevierMain.printMe("DB Update:--------End-------------------Dataset,status="+datasetDirName+","+gIsValid);
         System.out.println("DB Update:--------End-------------------Dataset,status="+datasetDirName+","+gIsValid);
      }

      return gIsValid;
  }

  public Dataset getDatasetInfo(File semaphoreFile)
  {
      ElsevierMain.printMe("ElsevierValidate-getDatasetInfo-Start");
      System.out.println("Start-getDatasetInfo");

      Dataset dataset = null;

      try
      {
          dataset = (Dataset)unmarshaller.unmarshal(semaphoreFile);
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierValidate.getDatasetInfo()="+e.getMessage());
          System.out.println("Exception in ElsevierValidate.getDatasetInfo="+e.getMessage());
      }
      finally
      {
      }

      ElsevierMain.printMe("ElsevierValidate-getDatasetInfo-End");
      System.out.println("End-getDatasetInfo");

      return dataset;
  }

  public String getFileSize(File file)
  {
      long fileSize = file.length();
      return fileSize+"";
  }

  public String getFileCheckSum(File file)
  {
      // MD5 checksum
      String algorithm = "MD5";
      String checksumValue = "";
      try
      {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[BUFFER_SIZE];
        int readsize = 0;
        while((readsize = in.read(bytes)) != -1)
            digest.update(bytes, 0, readsize);
        checksumValue = translateArray(digest.digest());
        if(checksumValue != null && !checksumValue.equals(""))
        {
            checksumValue = checksumValue.toLowerCase();
        }
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierValidate.getFileCheckSum()="+e.getMessage());
          System.out.println("Exception in ElsevierValidate.getFileCheckSum="+e.getMessage());
      }
      finally
      {
      }

      return checksumValue;
  }

  private String translateArray(byte[] bytes)
  {
      char[] hexValues ={'0','1','2','3','4','5','6','7','8','9',
            'A','B','C','D','E','F'};
      StringBuffer buffer = new StringBuffer();
      for (int counter = 0; counter < bytes.length; counter++)
            buffer.append(hexValues[((bytes[counter] & 0xf0) >> 4)])
                .append(hexValues[(bytes[counter] & 0x0f)]);
     return buffer.toString();
  }

  public boolean createSendConfirmation(File semaphoreFile, String datasetDirName, String startTime, String endTime, boolean status, String feedName)
  {
      boolean isSuccessful = true;
      try
      {
          String confirmFileName = datasetDirName + "_" + ElsevierMain.getThisDateTime() + ".txt";
          File confirmFile = new File(ElsevierMain.confirmFilePath + File.separator + confirmFileName);
          FileOutputStream fos = new FileOutputStream(confirmFile);
          // marshaller.marshal(dataset, fos );
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          FileInputStream fis = new FileInputStream(semaphoreFile);
          int tempChar;
          while ( (tempChar = fis.read()) != -1)
          {
              baos.write(tempChar);
          }
          fis.close();
          String inputFileAsString = baos.toString("utf-8");

          // First substitution
          // customer="OHL"
          int customerIndex = inputFileAsString.indexOf(ElsevierValidate.CONFIRMATION_CUSTOMER_TAG);
          int customerValueStartIndex = inputFileAsString.indexOf("\"", customerIndex);
          String firstString = inputFileAsString.substring(0, customerValueStartIndex);
          int customerValueEndIndex = inputFileAsString.indexOf("\"", customerValueStartIndex+1);
          String substitute = "\"" + ElsevierValidate.CONFIRMATION_CUSTOMER_VALUE + "\"";
          String remainingString = inputFileAsString.substring(customerValueEndIndex+1);
          String finalString = firstString + substitute + remainingString;

          // Second substitution
          // status="Announcement"
          customerIndex = finalString.indexOf(ElsevierValidate.CONFIRMATION_STATUS_TAG);
          customerValueStartIndex = finalString.indexOf("\"", customerIndex);
          firstString = finalString.substring(0, customerValueStartIndex);
          customerValueEndIndex = finalString.indexOf("\"", customerValueStartIndex+1);
          substitute = "\"" + ElsevierValidate.CONFIRMATION_STATUS_VALUE + "\"";
          remainingString = finalString.substring(customerValueEndIndex+1);
          finalString = firstString + substitute + remainingString;

          fos.write(finalString.getBytes("utf-8"));
          fos.flush();
          fos.getFD().sync();
          fos.close();

          if(status == true)
          {
               String statusString = ElsevierMail.SUCCESS;
               elsevierMail.mailConfirmationExternal(statusString,
                                                       ElsevierMail.createSubjectExternal(statusString, feedName, datasetDirName, startTime, endTime),
                                                       finalString);
               elsevierMail.mailConfirmationInternal(statusString,
                                                       ElsevierMail.createSubjectInternal(statusString, feedName, datasetDirName, startTime, endTime),
                                                       "");
          }
          else
          {
               String statusString = ElsevierMail.FAILURE;
               elsevierMail.mailConfirmationInternal(statusString,
                                                       ElsevierMail.createSubjectInternal(statusString, feedName, datasetDirName, startTime, endTime),
                                                       "");
          }
      }
      catch(Exception e)
      {
          isSuccessful = false;
          ElsevierMain.printMe("Exception in ElsevierValidate.createSendConfirmation()="+e.getMessage());
          System.out.println("Exception in ElsevierValidate.createSendConfirmation="+e.getMessage());
          String statusString = ElsevierMail.FAILURE;
          elsevierMail.mailConfirmationInternal(statusString,
                                                   ElsevierMail.createSubjectInternal(statusString, feedName, datasetDirName, startTime, endTime),
                                                   "");
      }
      finally
      {
      }

      return isSuccessful;
  }
}
