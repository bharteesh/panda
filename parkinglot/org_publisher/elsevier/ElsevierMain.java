package org.publisher.elsevier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.portico.conprep.util.database.DBClass;

import com.jscape.inet.ftp.Ftp;
import com.jscape.inet.ftp.FtpFile;
import com.jscape.inet.ftp.FtpFileParser;

public class ElsevierMain
{
// RANGA CHECK FOLLOW,
// If any static variable touched, compile ElsevierMain.java, ElevierValidate.java
// Change LOCAL_HOME - done(not an issue anymore passed as an argument)
// confirm dir in local machine - done
// Check if testAddDataSets(); is commented out - done
// Check if downLoadData(); is uncommented - done
// REMOVE 'break' (3 places) - done
// Check 4 feed dir on local machine - done
// Check log file directory - done
// For regular feeds comment out this 'datasetList.add(datasetDirectoryLocalPath);'(needs change) if directory already exists - done
// Commented 4 feeds - is uncommented - done
// Check if testValidate - is commented - done
// Check if others below testValidate - is uncommented - done
// Check mail for paul.Mostert (ElsevierValidate.java) - pending

  private static final String server = "support.sciencedirect.com";
  public static String LOCAL_HOME = ""; // "/export/home/padmin/elsevier_content"; // "/export/home/srn/publisher/elsevier/data"; // "/export/home/padmin/elsevier_content";
  private static String logFilePath = ""; // LOCAL_HOME + "/" + "logs";
  public static String confirmFilePath = ""; // LOCAL_HOME + "/" + "confirmation";
  public static String configDirectory = "config";
  public static String configFile = "elsevier.config";
  public static final String SEMAPHORE_FILE = "datasetinfo.xml";
  public static final String TOC_FILE = "dataset.toc";
  public static final String DATASETDIRPATH = "datasetdirpath";
  public static final String STARTTIME = "starttime";
  public static final String ENDTIME = "endtime";
  public static String LOCAL_LOOKUP = "0";
  public static String operationsDirectory = "operations";
  public static String operationsFile = "operations.out";
  public static String operationsFilePath = "";
  public static int RETRY_FILE_PULL_COUNT = 0;
  public static boolean DO_VALIDATE = true;

  public static PrintWriter logPrintWriter = null;

  public Hashtable downLoadedDataset = new Hashtable();

  private ElsevierMail elsevierMail = null;

  public static void main(String[] arg)
  {
      ElsevierMain elsevierMain = new ElsevierMain();
      if(arg.length > 0)
      {
          String tempLocalHome = arg[0];
          if(tempLocalHome != null && !tempLocalHome.equals(""))
          {
              LOCAL_HOME = tempLocalHome;
          }
          if(arg.length == 2)
          {
			  try
			  {
			      RETRY_FILE_PULL_COUNT = Integer.parseInt(arg[1]);
		      }
		      catch(Exception e)
		      {
				  System.out.println("Warning - Second argument RETRY_FILE_PULL_COUNT has to be a number greater that 0 - using default RETRY_FILE_PULL_COUNT="+RETRY_FILE_PULL_COUNT);
		      }
		  }
          else if(arg.length == 3)
          {
			  String doValidateString = arg[2];
			  DO_VALIDATE = Boolean.valueOf(doValidateString).booleanValue();
              System.out.println("DO_VALIDATE="+DO_VALIDATE);
          }
          else if(arg.length == 4)
          {
              System.out.println("LOCAL_LOOKUP");
              LOCAL_LOOKUP = arg[3];
              System.out.println("LOCAL_LOOKUP="+LOCAL_LOOKUP);
          }
          else
          {
              System.out.println("NO LOCAL_LOOKUP="+LOCAL_LOOKUP);
          }
      }
      if(LOCAL_HOME != null && !LOCAL_HOME.equals(""))
      {
          System.out.println("LOCAL_HOME directory is="+LOCAL_HOME);
          logFilePath = LOCAL_HOME + "/" + "logs";
          confirmFilePath = LOCAL_HOME + "/" + "confirmation";
          operationsFilePath = LOCAL_HOME + "/" + operationsDirectory;
          elsevierMain.initialize();
      }
      else
      {
          System.out.println("LOCAL_HOME directory not passed as the First argument parameter");
      }
  }

  public ElsevierMain()
  {
  }

  public static String getThisDateTime()
  {
       return ElsevierMain.getDateObject().toString();
  }

  public static Date getDateObject()
  {
      Calendar cal = Calendar.getInstance();
      Date dt = cal.getTime();
      return dt;
  }

  public void initialize()
  {
      FileOutputStream logFos = null;

      try
      {

          // Initialize log service
          String currentDateTime = ElsevierMain.getThisDateTime();
          String logFileAbsoluteName = logFilePath + "/" + "elsevier_" + currentDateTime + ".logs";

          logFos = new FileOutputStream(logFileAbsoluteName);
          logPrintWriter = new PrintWriter(logFos,true); // AutoFlush is true

          // Initialize mail service
          elsevierMail = new ElsevierMail();
          elsevierMail.readConfig();
          initializeDB(elsevierMail.getDbUrl(), elsevierMail.getDbUser(), elsevierMail.getDbPassword());

  // testValidate();

          if(LOCAL_LOOKUP.equals("0"))
          {
              downLoadData();
              // testAddDataSets();
		  }
		  else if(LOCAL_LOOKUP.equals("1"))
		  {
             populateLocalData(LOCAL_HOME + "/" + "SDOSCellPress");
             populateLocalData(LOCAL_HOME + "/" + "SDOSAcademicPress");
             populateLocalData(LOCAL_HOME + "/" + "SDOSHarcourt");
             populateLocalData(LOCAL_HOME + "/" + "SDOSElsevier");
          }
          else
          {
			  printMe("initialize-Start populateDataFromLookupFile-LOCAL_LOOKUP_FILE="+LOCAL_LOOKUP);
			  populateDataFromLookupFile(LOCAL_LOOKUP);
			  printMe("initialize-End populateDataFromLookupFile-LOCAL_LOOKUP_FILE="+LOCAL_LOOKUP);
		  }
// RANGA, uncomment this for production
          validateData();
      }
      catch(Exception e)
      {
          System.out.println("Exception in ElsevierMain-initialize="+e.getMessage());
      }
      finally
      {
          printMe("initialize-Closing log stream and writer");
          if(logFos != null)
          {
              try
              {
                  logFos.flush();
                  logPrintWriter.flush();

                  logPrintWriter.close();
                  logFos.close();
              }
              catch(Exception elog)
              {
                  System.out.println("Exception in log file closing="+elog.getMessage());
              }
          }
          releaseDBConnection();
      }
  }

  public void downLoadData()
  {
      System.out.println("Start-downLoadData-"+ElsevierMain.getThisDateTime());

      ArrayList downLoadedDatasetPerFeed = null;

      try
      {
          printMe("Start-downLoadData-"+ElsevierMain.getThisDateTime());

          // SDOSCellPress
          printMe("Start-SDOSCellPress-"+ElsevierMain.getThisDateTime());
          downLoadedDatasetPerFeed = getDataFiles("sdosftp71", "cel342", LOCAL_HOME + "/" + "SDOSCellPress", "SDOSCellPress");
          printMe("End-SDOSCellPress-"+ElsevierMain.getThisDateTime());
          downLoadedDataset.put("SDOSCellPress", downLoadedDatasetPerFeed);

          printMe("Start-SDOSAcademicPress-"+ElsevierMain.getThisDateTime());
          // SDOSAcademicPress
          downLoadedDatasetPerFeed = getDataFiles("sdosftp38", "oha920", LOCAL_HOME + "/" + "SDOSAcademicPress", "SDOSAcademicPress");
          printMe("End-SDOSAcademicPress-"+ElsevierMain.getThisDateTime());
          downLoadedDataset.put("SDOSAcademicPress", downLoadedDatasetPerFeed);

          // SDOSHarcourt
          printMe("Start-SDOSHarcourt-"+ElsevierMain.getThisDateTime());
          downLoadedDatasetPerFeed = getDataFiles("sdosftp53", "ohh348", LOCAL_HOME + "/" + "SDOSHarcourt", "SDOSHarcourt");
          printMe("End-SDOSHarcourt-"+ElsevierMain.getThisDateTime());
          downLoadedDataset.put("SDOSHarcourt", downLoadedDatasetPerFeed);

          // SDOSElsevier
          printMe("Start-SDOSElsevier-"+ElsevierMain.getThisDateTime());
          downLoadedDatasetPerFeed = getDataFiles("sdosftp18", "ohm518", LOCAL_HOME + "/" + "SDOSElsevier", "SDOSElsevier");
          printMe("End-SDOSElsevier-"+ElsevierMain.getThisDateTime());
          downLoadedDataset.put("SDOSElsevier", downLoadedDatasetPerFeed);
          printMe("End-downLoadData-"+ElsevierMain.getThisDateTime());
      }
      catch(Exception e)
      {
          String statusString = ElsevierMail.FAILURE;
          elsevierMail.mailConfirmationInternal(statusString,
                                                   ElsevierMail.createSubjectInternal(statusString, "", "", "", ""),
                                                   "");
          printMe("Exception in main="+e.getMessage());
          System.out.println("Exception in downLoadData="+e.getMessage());
      }
      finally
      {
      }

      System.out.println("End-downLoadData-"+ElsevierMain.getThisDateTime());
  }

  public ArrayList getDataFiles(String username,
                   String password,
                   String destinationFolder,
                   String feedName)
  {
    ArrayList datasetList = new ArrayList();
    ValuePair valuePair = null;

    Ftp ftp = null;
    try
    {
      // Connect and logon to FTP Server
      ftp = new Ftp();
//      UnixParser unixParser = new UnixParser();
//      ftp.setFtpFileParser(unixParser);
      ftp.setHostname(server);
      ftp.setUsername(username);
      ftp.setPassword(password);
      ftp.connect();
      ftp.setBinary();

/*
      if(ftpsitetimestamp-6hrs(eg:offset) > currenttime){
		  year = year -1;
		  Calendar.setTime(ftpsitetimestamp)-1year;
	  }



*/



// Added to take care of Timezone start
//      FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
//      conf.setServerTimeZoneId("Europe/Amsterdam");
//      ftp.configure(conf);
// Added to take care of Timezone end
      printMe("Connected to " + server);
      printMe("server-getSystemType=" + ftp.getSystemType());

      // List the files in the directory
      String homeDirectoryPublisher = ftp.getDir();
      printMe("homeDirectoryPublisher="+homeDirectoryPublisher);


//      FTPFile[] files = ftp.listFiles();
//      printMe( "Number of files in dir: " + files.length );
      DateFormat df = DateFormat.getDateTimeInstance();// DateFormat.getDateInstance( DateFormat.SHORT );

      printMe(ftp.getDirListingAsString());

      int i = 0;
      for (Enumeration files = ftp.getDirListing(); files.hasMoreElements();)
      {
		i++;
        printMe("----------------Start Dataset Processing file="+i);
        FtpFile tmpFTPFile = (FtpFile)files.nextElement();
        FtpFileParser tmpFtpFileParser = tmpFTPFile.getFtpFileParser();
        if(tmpFTPFile == null)
        {
			// The apache JIRA tells that 29,FEB,2008 is possibly an invalid date, there are posting on this, people have
			// faced this problem.
			// Workaround from our side is ignore this file/directory, nothing can be done,
			// atleast rest of the data can be pulled.
			printMe("ERROR - Unable to pull the ftpfile, the file/directory index is="+i);
		}
		else
		{

            // printMe( "Publisher site(Timezone-Direct) filename= "+ tmpFTPFile.getFilename());
            // printMe( "Publisher site(Timezone-Direct) Date= "+ ftp.getFileTimestamp(homeDirectoryPublisher + File.separator + tmpFTPFile.getFilename()));

				// ftp.getFileTimestamp(remoteFileName); // jscape provides this, but may not work all the time depending on the FTP Server
			java.util.Date fileDate = getMassagedDate(tmpFtpFileParser.getDateTime(tmpFTPFile));
            printMe( "Publisher site File/Dir name()="+tmpFTPFile.getFilename()+",FtpDate(Massaged)="+ fileDate);

            Calendar displayftpSiteCalendar = Calendar.getInstance();
            displayftpSiteCalendar.setTimeInMillis(fileDate.getTime());
            GregorianCalendar gregorianCalendar = getGregorianCalendar(displayftpSiteCalendar);
            printMe( "GregorianCalendar time="+gregorianCalendar.getTime());
            printMe( "GregorianCalendar used for setting dir/file timestamp="+gregorianCalendar.getTimeInMillis());
            printMe( "\t" + tmpFTPFile.getFilename());
            printMe( "\t" + "isDirectory=" + tmpFTPFile.isDirectory());
            if(tmpFTPFile.isDirectory() == true)
            {
                String datasetDirectoryLocalPath = destinationFolder + File.separator + tmpFTPFile.getFilename();
                File datasetDirectoryLocal = new File(datasetDirectoryLocalPath);
                if(datasetDirectoryLocal != null)
                {
                    if(!datasetDirectoryLocal.exists())
                    {
                        Calendar ftpSiteCalendar = Calendar.getInstance();
                        ftpSiteCalendar.setTimeInMillis(fileDate.getTime());
                        GregorianCalendar gregorianCalendarForDir = getGregorianCalendar(ftpSiteCalendar);
/*
                        int ftpSiteCalendarYear = ftpSiteCalendar.get(Calendar.YEAR);
                        Calendar rightNowCalendar = Calendar.getInstance();
                        int rightNowCalendarYear = rightNowCalendar.get(Calendar.YEAR);
                        if(ftpSiteCalendarYear != rightNowCalendarYear)
                        {
                            try
                            {
                                printMe("WARNING Year mismatch API,NOW="+ftpSiteCalendarYear+","+rightNowCalendarYear+ " setting to="+rightNowCalendarYear);
                                printMe( "\n" );
                                ftpSiteCalendar.set(Calendar.YEAR, rightNowCalendarYear);
                            }
                            catch(Exception yearException)
                            {
                                printMe("WARNING Exception in setting year(directory) from="+ftpSiteCalendarYear+" to="+rightNowCalendarYear + " Exception="+ yearException.getMessage());
                            }
                        }
                        Date fileDate = ftpSiteCalendar.getTime();
*/
                        long gregorianCalendarForDirInMilli = gregorianCalendarForDir.getTimeInMillis();
                        String datasetDirectoryPublisher = homeDirectoryPublisher + File.separator + tmpFTPFile.getFilename();
                        ftp.setDir(datasetDirectoryPublisher);

                        boolean foundSemaphore = false;
                        for (Enumeration filesPerDataset = ftp.getDirListing(); filesPerDataset.hasMoreElements();)
                        {
							FtpFile tmpFTPFilePerDataset = (FtpFile)filesPerDataset.nextElement();
                            if(tmpFTPFilePerDataset.getFilename().equals(SEMAPHORE_FILE))
                            {
                                foundSemaphore = true;
                                break;
                            }
                        }
                        if(foundSemaphore == true)
                        {
                            printMe("Semaphore found for dataset="+tmpFTPFile.getFilename());
                            datasetDirectoryLocal.mkdir();
                            printMe("Local Dataset directory(created)=" + destinationFolder + File.separator + tmpFTPFile.getFilename());
                            datasetDirectoryPublisher = homeDirectoryPublisher + File.separator + tmpFTPFile.getFilename();
                            printMe("Publisher Dataset directory=" + datasetDirectoryPublisher);

                            ArrayList singleDatasetInfo = new ArrayList();

                            valuePair = new ValuePair();
                            valuePair.setKey(ElsevierMain.DATASETDIRPATH);
                            valuePair.setValue(datasetDirectoryLocalPath);
                            singleDatasetInfo.add(valuePair);

                            Calendar calStart = Calendar.getInstance();

                            printMe("Start time for dataset="+tmpFTPFile.getFilename() + "-" + calStart.getTime().toString());
                            System.out.println("Start time for dataset="+tmpFTPFile.getFilename() + "-" + calStart.getTime().toString());

                            String starthourMinute = calStart.get(Calendar.HOUR_OF_DAY)+":"+getFormattedMinute(calStart.get(Calendar.MINUTE));
                            int startDayOfTheYear = calStart.get(Calendar.DAY_OF_YEAR);
                            valuePair = new ValuePair();
                            valuePair.setKey(ElsevierMain.STARTTIME);
                            valuePair.setValue(starthourMinute);
                            singleDatasetInfo.add(valuePair);

                            // datasetList.add(datasetDirectoryLocalPath);

                            //for(int dfileindx=0; dfileindx < filesPerDataset.length; dfileindx++)
                            for (Enumeration pFilesPerDataset = ftp.getDirListing(); pFilesPerDataset.hasMoreElements();)
                            {
								FtpFile pTmpFTPFile = (FtpFile)pFilesPerDataset.nextElement();
								java.util.Date pFileDate = getMassagedDate(tmpFtpFileParser.getDateTime(pTmpFTPFile));
                                printMe( "Publisher site File name()="+pTmpFTPFile.getFilename()+",FtpDate(Massaged)="+ pFileDate);
                                Calendar fileFtpSiteCalendar = Calendar.getInstance();
                                fileFtpSiteCalendar.setTimeInMillis(pFileDate.getTime());
                                GregorianCalendar gregorianCalendarForFile = getGregorianCalendar(fileFtpSiteCalendar);
/*
                                int fileFtpSiteCalendarYear = fileFtpSiteCalendar.get(Calendar.YEAR);
                                if(fileFtpSiteCalendarYear != rightNowCalendarYear)
                                {
                                    try
                                    {
                                        fileFtpSiteCalendar.set(Calendar.YEAR, rightNowCalendarYear);
                                    }
                                    catch(Exception fileYearException)
                                    {
                                        printMe("WARNING Exception in setting year(file) from="+fileFtpSiteCalendarYear+" to="+rightNowCalendarYear + " Exception="+ fileYearException.getMessage());
                                    }
                                }
                                Date dataFileDate = fileFtpSiteCalendar.getTime();
*/
                                long gregorianCalendarForFileInMilli = gregorianCalendarForFile.getTimeInMillis();
                                printMe( "\t" + pTmpFTPFile.getFilename());
                                long fileSizeOnFtpSite = pTmpFTPFile.getFilesize();
                                printMe( "\t" + "fileSizeOnFtpSite="+fileSizeOnFtpSite);
                                long fileSizeOnLocalSystem = 0;
                                // Start While counter - RETRY_FILE_PULL_COUNT
                                int retryFilePullCounter = 0;
                                File dataFileLocal = null;

                                while(fileSizeOnLocalSystem != fileSizeOnFtpSite &&
                                        retryFilePullCounter >= 0 &&
                                        retryFilePullCounter <= RETRY_FILE_PULL_COUNT)
                                {
									printMe("RetryFilePullCount=" + retryFilePullCounter);
                                    dataFileLocal = new File(destinationFolder +
                                               File.separator +
                                               tmpFTPFile.getFilename() +
                                               File.separator +
                                               pTmpFTPFile.getFilename());
                                    if(dataFileLocal.exists())
                                    {
										dataFileLocal.delete();
									}
                                    FileOutputStream fos = null;
                                    try
                                    {
                                        fos = new FileOutputStream(dataFileLocal);
                                        ftp.download(fos, pTmpFTPFile.getFilename());

                                        // 24NOV2009, there are instances when the NFS stores the saved file(s) in the cache
                                        //            but ultimately(later while writing to the disk) fails to write it due to low disk memory space
                                        // Things would look good here when we check for the 'fileSizeOnLocalSystem'
                                        // but later it would turn out to be 0 byte file(s).
                                        // So, let us try to 'sync' - force the the NFS (file system) to write all its cache to the disk memory.
                                        fos.flush();
                                        printMe("Start calling fos.getFD().sync() for =" + pTmpFTPFile.getFilename());
                                        System.out.println("Start calling fos.getFD().sync() for =" + pTmpFTPFile.getFilename());
                                        fos.getFD().sync();
                                        printMe("End calling fos.getFD().sync() for =" + pTmpFTPFile.getFilename());
                                        System.out.println("End calling fos.getFD().sync() for =" + pTmpFTPFile.getFilename());
								    }
								    catch(Exception e)
								    {
                                        printMe("Exception while retrieving file ="+e.getMessage());
                                        printMe("Error exception while retrieving file in ElsevierMain-getDataFiles="+e.getMessage());
                                        e.printStackTrace();
                                        String statusString = ElsevierMail.FAILURE;
                                        elsevierMail.mailConfirmationInternal(statusString,
                                                   ElsevierMail.createSubjectInternal(statusString, feedName, "", "", ""),
                                                   "");
									}
									finally
									{
										try
										{
											if(null != fos)
											{
                                                fos.close();
									        }
									    }
									    catch(Exception e)
									    {
                                            printMe("Exception while closing the output stream ="+e.getMessage());
                                            e.printStackTrace();
										}
									}
                                    retryFilePullCounter++;
                                    fileSizeOnLocalSystem = dataFileLocal.length();
							    }
                                // End While Counter


                                // dataFileLocal.setLastModified(dataFileDate.getTime());
                                if(dataFileLocal != null)
                                {
                                    dataFileLocal.setLastModified(gregorianCalendarForFileInMilli);
							    }
                                // RANGA uncomment for testing to pull only 1 file in a dataset
                                // break;
                            }

                            Calendar calEnd = Calendar.getInstance();

                            printMe("End time for dataset="+tmpFTPFile.getFilename() + "-" + calEnd.getTime().toString());
                            System.out.println("End time for dataset="+tmpFTPFile.getFilename() + "-" + calEnd.getTime().toString());

                            String endhourMinute = calEnd.get(Calendar.HOUR_OF_DAY)+":"+getFormattedMinute(calEnd.get(Calendar.MINUTE));
                            int endDayOfTheYear = calEnd.get(Calendar.DAY_OF_YEAR);
                            int dayDifference = endDayOfTheYear - startDayOfTheYear;
                            if(dayDifference != 0)
                            {
                                endhourMinute = endhourMinute + "+" + dayDifference;
                            }
                            valuePair = new ValuePair();
                            valuePair.setKey(ElsevierMain.ENDTIME);
                            valuePair.setValue(endhourMinute);
                            singleDatasetInfo.add(valuePair);
                            datasetList.add(singleDatasetInfo);

                            printMe("Local Dataset set directory timestamp(dataset,timestamping)=" + tmpFTPFile.getFilename() + ","+gregorianCalendarForDir.getTime());
                            datasetDirectoryLocal.setLastModified(gregorianCalendarForDirInMilli);
                        }
                        else
                        {
                            printMe("WARNING:Semaphore not found Retry pulling Dataset again later="+tmpFTPFile.getFilename());
                        }
                        // RANGA, uncomment break for just testing 1 dataset pull
                        // break;
                    }
                    else
                    {
                        // Dataset already exists, ignore this
                        printMe("Ignored Dataset(Not pulled) Directory already exists="+destinationFolder + File.separator + tmpFTPFile.getFilename());
                        // RANGA coment this out for regular feed(s), this is only to validate the dataset(s) previously pulled without validation
                        // datasetList.add(datasetDirectoryLocalPath);
                        // RANGA, uncomment break for just testing 1 existing dataset validation
                        // break;
                    }
                }
                else
                {
                    printMe("Error datasetDirectoryLocal is null for="+destinationFolder + File.separator + tmpFTPFile.getFilename());
                }
            }
            else
            {
                printMe("Ignored - Not a directory(dataset)="+tmpFTPFile.getFilename());
            }
	    }

        printMe("----------------End Dataset Processing file="+i);
      }
      ftp.setDir(homeDirectoryPublisher);
      printMe("Reset ftp.changeWorkingDirectory="+homeDirectoryPublisher);

      // Logout from the FTP Server and disconnect
      ftp.disconnect();
      ftp = null;
      printMe("ftp session logged out and disconnected");
    }
    catch( Exception e )
    {
      printMe("Exception message is="+e.getMessage());
      printMe("Error exception in ElsevierMain-getDataFiles="+e.getMessage());
      e.printStackTrace();
      String statusString = ElsevierMail.FAILURE;
      elsevierMail.mailConfirmationInternal(statusString,
                                                   ElsevierMail.createSubjectInternal(statusString, feedName, "", "", ""),
                                                   "");
    }
    finally
    {
        printMe("Entered finally");
        if(ftp != null)
        {
            try
            {
                // Logout from the FTP Server and disconnect
                ftp.disconnect();
                printMe("ftp session logged out and disconnected");
            }
            catch(Exception elogout)
            {
                printMe("Error exception in ElsevierMain-getDataFiles-logout");
                elogout.printStackTrace();
            }
        }
    }

    return datasetList;
  }

  public void validateData()
  {
      printMe("Start-validateData-"+ElsevierMain.getThisDateTime());
      System.out.println("Start-validateData-"+ElsevierMain.getThisDateTime());

      try
      {
          ArrayList elsevierSuccessPullList = new ArrayList();
          if(downLoadedDataset != null && downLoadedDataset.size() > 0)
          {
              ElsevierValidate elsevierValidate = new ElsevierValidate(elsevierMail);
              Enumeration datasetListEnumerate = downLoadedDataset.keys();
              while(datasetListEnumerate.hasMoreElements())
              {
	          Object feedHashKey = datasetListEnumerate.nextElement();
                  String feedname = (String)feedHashKey;
                  Object datasetListObject = downLoadedDataset.get(feedHashKey);
    		  ArrayList datasetList = (ArrayList)datasetListObject;
                  if(datasetList != null && datasetList.size() > 0)
                  {
                      String extString = feedname;
                      for(int indx=0; indx < datasetList.size(); indx++)
                      {
                          ArrayList singleDatasetInfo = (ArrayList)datasetList.get(indx);
                          boolean isValid = elsevierValidate.isValidDataset(singleDatasetInfo, feedname);
                          if(isValid == true)
                          {
                              extString = extString + "," + getSuccessPullString(singleDatasetInfo);
                          }
                      }
                      elsevierSuccessPullList.add(extString);
                  }
                  else
                  {
                      String statusString = ElsevierMail.NODATA;
                      elsevierMail.mailConfirmationInternal(statusString,
                                                   ElsevierMail.createSubjectInternal(statusString, feedname, "", "", ""),
                                                   "");
                  }
              }
          }

          printMe("ElsevierMain-Download-ValidateData Completed for all downloaded Datasets");
          System.out.println("ElsevierMain-Download-ValidateData Completed for all downloaded Datasets");
          printMe("ElsevierMain-Start Create file of successful Datasets to run statistics");
          System.out.println("ElsevierMain-Start Create file of successful Datasets to run statistics");

          if(elsevierSuccessPullList != null && elsevierSuccessPullList.size() > 0)
          {
              String oprnFileString = operationsFilePath + "/" + operationsFile;
              File oprnFile = new File(oprnFileString);
              if(oprnFile != null)
              {
                 FileWriter fosOprn = new FileWriter(oprnFile, true); // append is true
                 for(int indx=0; indx < elsevierSuccessPullList.size(); indx++)
                 {
                     fosOprn.write((String)elsevierSuccessPullList.get(indx) + "\n");
                 }
                 fosOprn.flush();
                 fosOprn.close();
              }
              else
              {
                  printMe("ElsevierMain-Operations file could not be created="+oprnFileString);
                  System.out.println("ElsevierMain-Operations file could not be created="+oprnFileString);
              }
          }
          else
          {
              printMe("ElsevierMain-No successful Datasets downloaded");
              System.out.println("ElsevierMain-No successful Datasets downloaded");
          }

          printMe("ElsevierMain-End Create file of successful Datasets to run statistics");
          System.out.println("ElsevierMain-End Create file of successful Datasets to run statistics");
      }
      catch(Exception e)
      {
          printMe("Error exception in ElsevierMain-validateData="+e.getMessage());
          System.out.println("Error exception in ElsevierMain-validateData="+e.getMessage());
      }
      finally
      {
      }

      printMe("End-validateData-"+ElsevierMain.getThisDateTime());
      System.out.println("End-validateData-"+ElsevierMain.getThisDateTime());
  }

  public static String getFormattedMinute(int minuteValue)
  {
      String minuteFormattedString = ""+minuteValue;
      if(minuteValue < 10)
      {
          minuteFormattedString = "0"+minuteValue;
      }

      return minuteFormattedString;
  }

  public String getSuccessPullString(ArrayList singleDataSetInfo)
  {
      String extString = "";
      if(singleDataSetInfo != null && singleDataSetInfo.size() > 0)
      {
          for(int sdindx=0; sdindx < singleDataSetInfo.size(); sdindx++)
          {
             ValuePair valuePair = (ValuePair)singleDataSetInfo.get(sdindx);
             String key = valuePair.getKey();
             String value = valuePair.getValue();
             if(key.equals(ElsevierMain.DATASETDIRPATH))
             {
                 File localDatasetDir = new File(value);
                 if(localDatasetDir != null)
                 {
                     extString = localDatasetDir.getName();
                 }
                 break;
             }
          }
      }

      return extString;
  }

  public void initializeDB(String url, String user, String password)
  {
      DBClass.initializeDB(url, user, password);
  }

  public void releaseDBConnection()
  {
      DBClass.releaseDBConnection();
  }

  public static void printMe(String msg)
  {
      System.out.println(msg);
      logPrintWriter.println(msg);
      logPrintWriter.flush();
  }

  public void testAddDataSets()
  {
      ArrayList datasetDirList = new ArrayList();
      // String testdatasetDir = "/export/home/srn/publisher/elsevier/data/SDOSCellPress/OXC00330";
      ArrayList alist = new ArrayList();
      ValuePair valuePair = null;
      try
      {
          datasetDirList.add("/export/home/padmin/elsevier_content/SDOSAcademicPress/OXA05170");
          datasetDirList.add("/export/home/padmin/elsevier_content/SDOSHarcourt/OXH05040");
          datasetDirList.add("/export/home/padmin/elsevier_content/SDOSElsevier/OXM05530");

                  for(int dindx=0; dindx < datasetDirList.size(); dindx++)
                  {
                        ArrayList singleDatasetInfo = new ArrayList();
                        valuePair = new ValuePair();
                        valuePair.setKey(ElsevierMain.DATASETDIRPATH);
                        valuePair.setValue((String)datasetDirList.get(dindx));
                        singleDatasetInfo.add(valuePair);

                        Calendar calStart = Calendar.getInstance();
                        String starthourMinute = calStart.get(Calendar.HOUR_OF_DAY)+":"+calStart.get(Calendar.MINUTE);
                        int startDayOfTheYear = calStart.get(Calendar.DAY_OF_YEAR);
                        valuePair = new ValuePair();
                        valuePair.setKey(ElsevierMain.STARTTIME);
                        valuePair.setValue(starthourMinute);
                        singleDatasetInfo.add(valuePair);

                        Calendar calEnd = Calendar.getInstance();
                        String endhourMinute = calEnd.get(Calendar.HOUR_OF_DAY)+":"+calEnd.get(Calendar.MINUTE);
                        int endDayOfTheYear = calEnd.get(Calendar.DAY_OF_YEAR);
                        int dayDifference = endDayOfTheYear - startDayOfTheYear;
                        if(dayDifference != 0)
                        {
                            endhourMinute = endhourMinute + "+" + dayDifference;
                        }
                        valuePair = new ValuePair();
                        valuePair.setKey(ElsevierMain.ENDTIME);
                        valuePair.setValue(endhourMinute);
                        singleDatasetInfo.add(valuePair);
                        alist.add(singleDatasetInfo);
                  }

                  downLoadedDataset.put("SDOSCellPress", alist);

      }
      catch(Exception e)
      {
          printMe("Error exception in ElsevierMain-testAddDataSets="+e.getMessage());
          System.out.println("Error exception in ElsevierMain-testAddDataSets="+e.getMessage());
      }
      finally
      {
      }
  }

  public void populateLocalData(String localFeedDirectory)
  {
// LOCAL_HOME + "/" + "SDOSCellPress"

      ValuePair valuePair = null;
      try
      {
          File localFeedDirectoryFile = new File(localFeedDirectory);
          if(localFeedDirectoryFile != null)
          {
              ArrayList datasetList = new ArrayList();
              File[] datasetDirList = localFeedDirectoryFile.listFiles();

              printMe("populateLocalData-feedDirectory,no. of datasets="+localFeedDirectory+","+datasetDirList.length);
              System.out.println("populateLocalData-feedDirectory,no. of datasets="+localFeedDirectory+","+datasetDirList.length);

              for(int dindx=0; dindx < datasetDirList.length; dindx++)
              {
                  String datasetDirectoryLocalPath = localFeedDirectory + File.separator + datasetDirList[dindx].getName();

                  ArrayList singleDatasetInfo = new ArrayList();

                  valuePair = new ValuePair();
                  valuePair.setKey(ElsevierMain.DATASETDIRPATH);
                  valuePair.setValue(datasetDirectoryLocalPath);
                  singleDatasetInfo.add(valuePair);

                  Calendar calStart = Calendar.getInstance();

                  printMe("populateLocalData-Start time for dataset="+datasetDirectoryLocalPath + "-" + calStart.getTime().toString());
                  System.out.println("populateLocalData-Start time for dataset="+datasetDirectoryLocalPath + "-" + calStart.getTime().toString());

                  String starthourMinute = calStart.get(Calendar.HOUR_OF_DAY)+":"+getFormattedMinute(calStart.get(Calendar.MINUTE));
                  int startDayOfTheYear = calStart.get(Calendar.DAY_OF_YEAR);
                  valuePair = new ValuePair();
                  valuePair.setKey(ElsevierMain.STARTTIME);
                  valuePair.setValue(starthourMinute);
                  singleDatasetInfo.add(valuePair);

                  Calendar calEnd = Calendar.getInstance();

                  printMe("populateLocalData-End time for dataset="+datasetDirectoryLocalPath + "-" + calEnd.getTime().toString());
                  System.out.println("populateLocalData-End time for dataset="+datasetDirectoryLocalPath + "-" + calEnd.getTime().toString());

                  String endhourMinute = calEnd.get(Calendar.HOUR_OF_DAY)+":"+getFormattedMinute(calEnd.get(Calendar.MINUTE));
                  int endDayOfTheYear = calEnd.get(Calendar.DAY_OF_YEAR);
                  int dayDifference = endDayOfTheYear - startDayOfTheYear;
                  if(dayDifference != 0)
                  {
                      endhourMinute = endhourMinute + "+" + dayDifference;
                  }
                  valuePair = new ValuePair();
                  valuePair.setKey(ElsevierMain.ENDTIME);
                  valuePair.setValue(endhourMinute);
                  singleDatasetInfo.add(valuePair);
                  datasetList.add(singleDatasetInfo);
              }

              if(datasetList != null && datasetList.size() > 0)
              {
                 downLoadedDataset.put(localFeedDirectoryFile.getName(), datasetList);
              }
          }

      }
      catch(Exception e)
      {
          printMe("Error exception in ElsevierMain-populateLocalData="+e.getMessage());
          System.out.println("Error exception in ElsevierMain-populateLocalData="+e.getMessage());
      }
      finally
      {
      }
  }

  public void testValidate()
  {
      ElsevierValidate elsevierValidate = new ElsevierValidate();
  }

  public GregorianCalendar getGregorianCalendar(Calendar cal)
  {
      return new GregorianCalendar(cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DATE),
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    cal.get(Calendar.SECOND));
  }

  public void populateDataFromLookupFile(String LOCAL_LOOKUP_FILE)
  {
	  try
	  {
          printMe("start populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);
          System.out.println("start populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);

          File lookupFile = new File(LOCAL_LOOKUP_FILE);
          if(lookupFile != null)
          {
              BufferedReader in = new BufferedReader(new FileReader(lookupFile));
              String str = "";
              while((str = in.readLine()) != null)
              {
                  printMe("start lineitem populateDataFromLookupFile-line ="+str);
                  System.out.println("start lineitem populateDataFromLookupFile-line ="+str);

                  ArrayList singleDatasetInfo = new ArrayList();
                  ValuePair valuePair = null;
				  String currentfeedname = "";
				  String currentdatasetdirpath = "";
				  String currentstarttime = "";
				  String currentendtime = "";
                  StringTokenizer st = new StringTokenizer(str, ElsevierMail.MAILSEPARATOR);
                  while (st.hasMoreTokens())
                  {
                      String currentToken = st.nextToken();
                      int indx = currentToken.indexOf("=");
                      if(indx != -1)
                      {
						  String key = currentToken.substring(0, indx);
						  String value = currentToken.substring(indx+1);
						  if(key.equalsIgnoreCase("FEEDNAME"))
						  {
							  currentfeedname = value;
						  }
						  else if(key.equalsIgnoreCase(ElsevierMain.DATASETDIRPATH))
						  {
							  currentdatasetdirpath = value;
                              valuePair = new ValuePair();
                              valuePair.setKey(key);
                              valuePair.setValue(value);
                              singleDatasetInfo.add(valuePair);
						  }
						  else if(key.equalsIgnoreCase(ElsevierMain.STARTTIME))
						  {
							  currentstarttime = value;
                              valuePair = new ValuePair();
                              valuePair.setKey(key);
                              valuePair.setValue(value);
                              singleDatasetInfo.add(valuePair);
						  }
						  else if(key.equalsIgnoreCase(ElsevierMain.ENDTIME))
						  {
							  currentendtime = value;
                              valuePair = new ValuePair();
                              valuePair.setKey(key);
                              valuePair.setValue(value);
                              singleDatasetInfo.add(valuePair);
						  }
					  }
                  }
				  if(currentfeedname.equals("") ||
				       currentdatasetdirpath.equals("") ||
				       currentstarttime.equals("") ||
				       currentendtime.equals(""))
				  {
					  printMe("ERROR - populateDataFromLookupFile-some of the entries are missing for ="+str);
					  System.out.println("ERROR - populateDataFromLookupFile-some of the entries are missing for ="+str);
				  }
				  else
				  {
					  printMe("populateDataFromLookupFile-parsed entries="+"currentfeedname="+currentfeedname+"|"+"currentdatasetdirpath="+currentdatasetdirpath+"|"+"currentstarttime="+currentstarttime+"|"+"currentendtime="+currentendtime);
					  System.out.println("populateDataFromLookupFile-parsed entries="+"currentfeedname="+currentfeedname+"|"+"currentdatasetdirpath="+currentdatasetdirpath+"|"+"currentstarttime="+currentstarttime+"|"+"currentendtime="+currentendtime);

					  ArrayList currentDatasetList = null;
                      if(downLoadedDataset.containsKey(currentfeedname))
                      {
						  currentDatasetList = (ArrayList)downLoadedDataset.get(currentfeedname);
					  }
					  else
					  {
						  currentDatasetList = new ArrayList();
					  }
					  currentDatasetList.add(singleDatasetInfo);
					  // Uncomment for final production
				      downLoadedDataset.put(currentfeedname, currentDatasetList);
				  }

                  printMe("end lineitem populateDataFromLookupFile-line ="+str);
                  System.out.println("end lineitem populateDataFromLookupFile-line ="+str);
			  }
		  }
	  }
	  catch(Exception e)
	  {
          printMe("Error exception in ElsevierMain-populateDataFromLookupFile="+e.toString());
          System.out.println("Error exception in ElsevierMain-populateDataFromLookupFile="+e.toString());
	  }
	  finally
	  {
          printMe("finally populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);
          System.out.println("finally populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);
	  }
      printMe("end populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);
      System.out.println("end populateDataFromLookupFile-for LOCAL_LOOKUP_FILE ="+LOCAL_LOOKUP_FILE);
  }

  public static java.util.Date getMassagedDate(java.util.Date incomingFtpDate){
      Calendar tomorrow = Calendar.getInstance();
      tomorrow.add(Calendar.DAY_OF_MONTH, 1);
      Calendar incoming = Calendar.getInstance();
      incoming.setTime(incomingFtpDate);
      // Now compare if the incomingdate is > than(ie. after) tomorrow
      if(incoming.after(tomorrow)){
		  // if the incoming date is a date after tomorrow, it does not make sense, it is a future date,
		  //    the FTP Parser could not parse it properly, so treat it as a previous year
		  incoming.add(Calendar.YEAR, -1);
	  }

      return incoming.getTime();
  }
}
