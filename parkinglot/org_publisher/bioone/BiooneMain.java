package org.publisher.bioone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.net.ftp.FTPClient;
import org.portico.conprep.util.DbConnectionPool;

public class BiooneMain
{
	private static final String LOC_PROPS = "Bioone.properties";
	private static Properties props = new Properties();
	static{
		try{
			InputStream in = ClassLoader.getSystemResourceAsStream(LOC_PROPS);
			props.load(in);
			in.close();
		}catch(Throwable t){
			System.out.println("Problem accessing Bioone.properties file.");
			System.exit(1);
		}
	}
	
	private static String smtpServer = props.getProperty("smtpServer");
	private static String from = props.getProperty("from");
	private static String to = props.getProperty("to");
	private static String replyTo = props.getProperty("replyTo");
	private static String subject = props.getProperty("subject");
	
	private static final String server = "archive.bioone.org";
	private static String srvUid = "portico-bioone";
	private static String srvPwd = "creTH3pR";
	private static String UrlAddress = "http://www.bioone.org/filelist.csv";
	//private static String dlFilename = "filelist.csv";
	private static String dlFilename = "newFileList.csv";
	private static String compPhrase1 = "collection";
	private static String compPhrase2 = "bioone/archive/";
	//private static String localBase = "/export/home/padmin/journal_content/INGEST_MASTER/BIO";
	private static String localBase = "/export/home/padmin/journal_content/INGEST_PROD/bill";
	private static String currentBatched = localBase+"/current/batched";
	private static String backBatched = localBase+"/electronic_back/batched";
	private static String currentUnbatched = localBase+"/current/unbatched";
	private static String updates = localBase+"/updates";
	private static DbConnectionPool aconPool = null;
	private static Connection acon = null;
	private static int rdrowcnt = 0;
	private static int wrrowcnt = 0;
	private static int notStoredCase1 = 0;
	private static int notStoredCase2 = 0;
	private static int notStoredOther = 0;
	private static int breakcnt = 5;

	static {
		String driverName = "oracle.jdbc.driver.OracleDriver";
		String aurl = "jdbc:oracle:thin:@pr2ptcora02.ithaka.org:2528:ARPDB1";
		String auser = "xmd_prod";
		String apassword = "xmd_pr0d1";
		int maxSize = 20;
		int maxAge = 20;
		System.out.println("getting Archive connection pool");
		aconPool = new DbConnectionPool(driverName, aurl, auser, apassword, maxSize, maxAge);
	}
    private static FTPClient ftp = null;
	private static PreparedStatement pstmtget = null;
	private static PreparedStatement pstmtseq = null;
	private static PreparedStatement pstmtins = null;
	private static String notStoredReason = "";


  public static void main(String[] arg) throws Exception
  {
	  DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	  String fetchStartTime = df.format(new Date());
	  if (getManifest()) {
		  getDataFiles();
		  System.out.println("read "+rdrowcnt+" manifest rows");
		  System.out.println("created "+wrrowcnt+" p_bioone_fetcher rows");
		  System.out.println(" notStoredDueToVersion count="+notStoredCase1+" notStoredDueToNestedFolders count="+notStoredCase2);
		  System.out.println(" notStoredOther count="+notStoredOther);
		  String fetchEndTime = df.format(new Date());

		  String msg = "\n\nBioone fetch Succeeded \n\n" 
			+ "Fetch began: " + fetchStartTime + " Fetch ended: " + fetchEndTime + " \n"
			+ " read "+rdrowcnt+" manifest rows \n"
			+ " created "+wrrowcnt+" p_bioone_fetcher rows \n"
			+ " notStoredDueToVersion count="+notStoredCase1+" notStoredDueToNestedFolders count="+notStoredCase2+" \n"
		    + " notStoredOther count="+notStoredOther+" \n";
		  send(msg);
	  }
  }

  public static boolean getManifest() {
	  boolean success = false;
	    /*
		try {
		  URL page = new URL(UrlAddress);
		  HttpURLConnection conn = (HttpURLConnection) page.openConnection();
		  conn.connect();
		  InputStreamReader in = new InputStreamReader((InputStream) conn.getContent());
		  BufferedReader buff = new BufferedReader(in);
		  File temp = new File(dlFilename);
		  FileWriter fw = new FileWriter(temp);
		  System.out.println("Getting manifest ...");
		  String line;
		  int linecnt = 0;
		  do {
		    line = buff.readLine();
		    if (line != null) fw.write(line+"\n");
		    fw.flush();
		    linecnt++;
		    if (linecnt % 10000 == 0) System.out.println("linecnt = "+linecnt);
		  } while (line != null);
		  success = true;
		} catch (Exception e) {
		  System.out.println("IO Error:" + e.getMessage());
		}
		*/
		success = true;
		return success;
	 }

  public static void getDataFiles()
  {
    String destinationFolder = "";
    String feedName = "";
	String chgDir = "";
    String homeDirectoryPublisher = "";
    String filelistitem = "";

    try
    {
      // Connect and logon to FTP Server
      ftp = new FTPClient();
      ftp.connect( server );
      ftp.login( srvUid, srvPwd );
      System.out.println("Connected to " + server + ".");
      System.out.println(ftp.getReplyString());

      homeDirectoryPublisher = ftp.printWorkingDirectory();
      System.out.println("homeDirectoryPublisher="+homeDirectoryPublisher);

	  boolean done = false;
      FileReader file = new FileReader(dlFilename);			
      BufferedReader freader = new BufferedReader(file);
      String line = "";
      String date = "";
      String fpath = "";
	  acon = aconPool.getConnection();
	  createPstmt();
	  
	  // we are parsing lines like these:
	  //"collection","datestamp","file path"
	  //"BioOne.1","14-FEB-08","/net/SiteData/bioone/archive/0002-7685/62/1/sgml/i0002-7685-62-1-2.sgm"

	  System.out.println("Getting data ...");
      while (!done) {
        	line = freader.readLine();
			rdrowcnt++;
			if (rdrowcnt % 1000 == 0) {
				System.out.println("rd-manifestRows="+rdrowcnt);
			}
        	if (line == null) {
        		done = true;
        	} else if (line.substring(1).startsWith(compPhrase1)) {}
        	else {
        		int i = line.indexOf(',');
        		String temp = line.substring(i+2);
        		i = temp.indexOf('"');
        		date = temp.substring(0,i);
        		i = temp.indexOf(compPhrase2);
        	  if (i > 0) {
        		String temp1 = temp.substring(i+15);
        		i = temp1.indexOf('"');
        		line = temp1.substring(0,i);
        		if (!getFetchRow(date,line)) {
        		  i=line.lastIndexOf('/');
        		  chgDir = line.substring(0,i);
        		  filelistitem = line.substring(i+1);
        		  System.out.println("calling process, date="+date+" chgDir="+chgDir);
        		  process(date, chgDir);
        		}
        	  } else System.out.println("unexpected filepath="+temp);
        	}
        	//if (wrrowcnt == breakcnt) break;
      }

      closePstmt();
	  acon.close();
	  aconPool.returnConnection(acon);
	  acon = null;
      ftp.changeWorkingDirectory(homeDirectoryPublisher);
      System.out.println("Reset ftp.changeWorkingDirectory="+homeDirectoryPublisher);

      // Logout from the FTP Server and disconnect
      ftp.logout();
      ftp.disconnect();
      ftp = null;
      System.out.println("ftp session logged out and disconnected");
    } catch( Exception e ) {
      System.out.println("Exception message is="+e.getMessage());
      System.out.println("Error exception in BiooneMain-getDataFiles="+e.getMessage());
      e.printStackTrace();
    } finally {
        System.out.println("Entered finally");
        if (acon != null) {
        	try {if (pstmtget != null) pstmtget.close();}
        	catch (Exception e) {e.printStackTrace();}
        	try {if (pstmtseq != null) pstmtseq.close();}
        	catch (Exception e) {e.printStackTrace();}
        	try {if (pstmtins != null) pstmtins.close();}
        	catch (Exception e) {e.printStackTrace();}
        	try {acon.close();}
        	catch (Exception e) {e.printStackTrace();}
			aconPool.returnConnection(acon);
        }
        if(ftp != null) {
            try {
                ftp.logout();
                ftp.disconnect();
                System.out.println("ftp session logged out and disconnected");
            } catch(Exception elogout) {
                System.out.println("Error exception in BiooneMain-getDataFiles-logout");
                elogout.printStackTrace();
            }
        }
    }
  }

  	public static void createPstmt() throws Exception {
		String query = "select p_bioone_fetch_key from p_bioone_fetch";
		query += " where p_filelist_date = ? and p_filelist_fpath = ?";
		pstmtget = acon.prepareStatement(query);
		pstmtseq = acon.prepareStatement("select p_bioone_fetch_key.NextVal from dual");
		pstmtins = acon.prepareStatement("insert into p_bioone_fetch (p_bioone_fetch_key, p_filelist_date, p_filelist_fpath, p_stored_date, p_comment) values(?,?,?,?,?)");
  	}
  	
  	public static void closePstmt() throws Exception {
  		pstmtget.close();
  		pstmtseq.close();
  		pstmtins.close();
  	}
  
    public static void process(String date, String chgDir) throws Exception {
    	//    	 Rules-3/4/5/6
    	String issueDir = chgDir.substring(0,chgDir.lastIndexOf("/"));
    	String volDir = issueDir.substring(0,issueDir.lastIndexOf("/"));
    	String csDir = volDir.substring(0,volDir.lastIndexOf("/"));
    	File checkDir1 = new File(currentBatched+"/"+issueDir);
    	File checkDir2 = new File(backBatched+"/"+issueDir);
    	File checkDir3 = new File(currentUnbatched+"/"+issueDir);
    	File checkDir4 = new File(currentBatched+"/"+csDir);
    	File checkDir5 = new File(backBatched+"/"+csDir);
    	boolean success = false;
    	if (checkDir4 != null) {
    		if (!checkDir4.exists()) {
    			if (checkDir5 != null) {
    				if (!checkDir5.exists()) {
    					if (checkDir1 != null) {
    						if (!checkDir1.exists()) {
    							if (checkDir2 != null) {
    								if (!checkDir2.exists()) {
    									if (checkDir3 != null) {
    										if (!checkDir3.exists()) {
    											File checkDir3a = new File(currentUnbatched+"/"+csDir);
    											if (!checkDir3a.exists()) checkDir3a.mkdir();
    											File checkDir3b = new File(currentUnbatched+"/"+volDir);
    											if (!checkDir3b.exists()) checkDir3b.mkdir();
    											checkDir3.mkdir();
    											//procRule3(issueDir,date);
    											procIssue(issueDir,date,currentUnbatched);
    											success = true;
    										} else {
    											//procRule3(issueDir,date);
    											procIssue(issueDir,date,currentUnbatched);
    											success = true;
    										}
    									} else {
    										System.out.println("currentUnbatched issueDir folder is null");
    									}
    								}
    							} else {
    								System.out.println("backBatched issueDir folder is null");
    							}
    						}
    					} else {
    						System.out.println("currentBatched issueDir folder is null");	
    					}
    				} else {
    					// Rule-5
    					File checkDir5c = new File(updates+"/"+issueDir);
    					if (!checkDir5c.exists()) {
							File checkDir5a = new File(updates+"/"+csDir);
							if (!checkDir5a.exists()) checkDir5a.mkdir();
							File checkDir5b = new File(updates+"/"+volDir);
							if (!checkDir5b.exists()) checkDir5b.mkdir();
							checkDir5c.mkdir();
    						//procRule5(issueDir,date);
							procIssue(issueDir,date,updates);
    						success = true;
    					} else {
    						// Rule-6
    						//procRule5(issueDir,date);
    						procIssue(issueDir,date,updates);
    						success = true;
    					}
    				}
    			} else {
    				System.out.println("backBatched csDir folder is null");
    			}
    		} else {
				// Rule-5
				File checkDir4c = new File(updates+"/"+issueDir);
				if (!checkDir4c.exists()) {
					File checkDir4a = new File(updates+"/"+csDir);
					if (!checkDir4a.exists()) checkDir4a.mkdir();
					File checkDir4b = new File(updates+"/"+volDir);
					if (!checkDir4b.exists()) checkDir4b.mkdir();
					checkDir4c.mkdir();
					//procRule5(issueDir,date);
					procIssue(issueDir,date,updates);
					success = true;
				} else {
					// Rule-6
					//procRule5(issueDir,date);
					procIssue(issueDir,date,updates);
					success = true;
				}
    		}
    	} else {
    		System.out.println("currentBatched csDir folder is null");
    	}
    }

    public static void procIssue(String issueDir, String date, String folder) throws Exception {
		// store retrieved file & rest of issue into unbatched or updates after creating corresponding folder
		String baseDir = "/"+issueDir;
		ftp.changeWorkingDirectory(baseDir);
		//System.out.println("changed WorkingDirectoryPublisher="+issueDir);
		String[] basefnames = ftp.listNames();
		String subDir = "";
		for ( int j1=0; j1<basefnames.length; j1++) {
			//System.out.println("basefname="+basefnames[j1]);
                ftp.changeWorkingDirectory(baseDir);
                subDir = baseDir+"/"+basefnames[j1];
				ftp.changeWorkingDirectory(subDir);
				// is this a folder or not?
				if (ftp.printWorkingDirectory().equals(baseDir)) {
					// retrieve this one file
	        		if (!getFetchRow(date,issueDir+"/"+basefnames[j1])) {
	        			boolean stored = false;
	        			boolean skip = false;
	        			try {
	        				stored=ftpRetrieveFile(basefnames[j1],folder+"/"+issueDir);
	        			} catch (Exception e) {
	        				e.printStackTrace();
	        				System.out.println("exception while processing "+basefnames[j1]+":"+e.getMessage());
		        			setFetchRow(date,issueDir+"/"+basefnames[j1],stored,e.getMessage());
		        			notStoredOther++;
		        			skip=true;
	        			}
    					// if stored = false, then set p_comment to this setting; else ignore this setting
	        			if (!skip) setFetchRow(date,issueDir+"/"+basefnames[j1],stored,notStoredReason);
	        		}
				} else {
					// retrieve contents of this folder
					String tempDirString = folder+"/"+issueDir+"/"+basefnames[j1];
					File tempDir = new File(tempDirString);
					tempDir.mkdir();
	      			  String[] fnames = ftp.listNames();
	      			  for ( int j=0; j<fnames.length; j++) {
	  	        		if (!getFetchRow(date,issueDir+"/"+basefnames[j1]+"/"+fnames[j])) {
	      				  boolean stored = false;
	      				  boolean skip = false;
	      				  // when contentType folders contain folders, ignore them 
	      				  if (fnames[j].indexOf('.') > 0) {
	      					  try {
	      					  stored=ftpRetrieveFile(fnames[j],tempDirString);
	      					  } catch (Exception e) {
	      						  e.printStackTrace();
	  	        				  System.out.println("exception while processing "+basefnames[j1]+"/"+fnames[j]+":"+e.getMessage());
			        			  setFetchRow(date,issueDir+"/"+basefnames[j1]+"/"+fnames[j],stored,e.getMessage());
			        			  notStoredOther++;
			        			  skip=true;
	      					  }
	      					  // if stored = false, then set p_comment to this setting; else ignore this setting
	      					  if (!skip) setFetchRow(date,issueDir+"/"+basefnames[j1]+"/"+fnames[j],stored,notStoredReason);
	      				  } else {
	      					  stored=false;
	      					  setFetchRow(date,issueDir+"/"+basefnames[j1]+"/"+fnames[j],stored,"Ignore folders within folders");
	      					  notStoredCase2++;
	      				  }
	  	        		}
	      			  }
				}
		}
    }
 
    public static boolean ftpRetrieveFile(String fname, String destinationDir) throws Exception {
    	System.out.println("retrieving fname="+fname);
		  String[] mons = {"Jan ","Feb ","Mar ","Apr ","May ","Jun ","Jul ","Aug ","Sep ","Oct ","Nov ","Dec "};
		  SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd  yyyy");
		  SimpleDateFormat sdf2 = new SimpleDateFormat("MMM  d  yyyy");
		  String time = "";
		  Date dt = null;
		  Date dt1 = new Date();
		  long xmils = 0;
		  String temp2=ftp.getStatus(fname);
		  System.out.println("status="+temp2);
		  boolean stored = false;
		  notStoredReason = "";
			  for (int k=0; k<mons.length; k++) {
				  int l1 = temp2.indexOf(mons[k]);
				  if (l1 > 0) {
					  xmils = 0;
					  int l2=temp2.indexOf(fname);
					  System.out.println("l1="+l1+" l2="+l2);
					  time = temp2.substring(l1,l2-1);
					  if (time.substring(9,10).equals(":")) {
						  if (time.substring(4,5).equals(" ")) {
							  dt = sdf2.parse(time.substring(0,6)+"  2008");
							  if (dt.after(dt1)) dt = sdf2.parse(time.substring(0,6)+"  2007");
						  }
						  else {
							  dt = sdf1.parse(time.substring(0,6)+"  2008");
  						  if (dt.after(dt1)) dt = sdf1.parse(time.substring(0,6)+"  2007");
						  }
						  int secs = Integer.parseInt(time.substring(7,9)) * 3600;
						  secs += Integer.parseInt(time.substring(10,12)) * 60;
						  xmils = (long)secs*1000;
					  } else {
						  if (time.substring(4,5).equals(" ")) dt = sdf2.parse(time);
						  else dt = sdf1.parse(time);
					  }
					  //System.out.println(time+" "+fname);
	                  File dataFileLocal = new File(destinationDir+"/"+fname);
	                  if (dataFileLocal.exists()) {
	                	  if (dataFileLocal.lastModified() < dt.getTime()+xmils) {
		                	  FileOutputStream fos = new FileOutputStream(dataFileLocal);
		                	  ftp.retrieveFile(fname, fos );
		                	  dataFileLocal.setLastModified(dt.getTime()+xmils);
		                	  stored=true;
		                	  fos.close();
	                	  } else {
	                		  notStoredCase1++;
	                		  notStoredReason="LocalDir has newer version";
	                	  }
	                  } else {
	                	  FileOutputStream fos = new FileOutputStream(dataFileLocal);
	                	  ftp.retrieveFile(fname, fos );
	                	  dataFileLocal.setLastModified(dt.getTime()+xmils);
	                	  stored=true;
	                	  fos.close();
	                  }
					  break;
				  }
				  if (k == 11) {
					  System.out.println("fname="+fname+" unexp ftpsrv tstamp="+time);
					  System.out.println("getStatus(fname)="+temp2);
					  notStoredOther++;
					  notStoredReason="Unexp ftpsrv tstamp";
				  }
			  }
			  return stored;
    }
    
	public static boolean getFetchRow(String date, String fpath) throws Exception {
		int x = 0;
		int discardcnt = 0;
		ResultSet rs = null;
		boolean exists = false;
		int fetchKey = 0;
		try {
    		pstmtget.setString(1, date);
    		pstmtget.setString(2, fpath);
			rs = pstmtget.executeQuery();
			if (rs.next()) {
				fetchKey = rs.getInt("p_bioone_fetch_key");
				if (fetchKey > 0) {
					exists = true;
				} else {
					exists = false;
				}
			} else {
				exists = false;
			}
			if (rs != null) rs.close();
		} finally {
		  try{if (rs != null) rs.close();}
		  catch (Exception e) {e.printStackTrace();}
		}
		return exists;
	}

	public static void setFetchRow(String date, String fpath, boolean stored, String comment) throws Exception {
		String id = null;
		ResultSet rs = null;
		try {
			rs = pstmtseq.executeQuery();
			if (rs.next()) {
				id = rs.getString(1);
				pstmtins.setInt(1, Integer.parseInt(id));
				pstmtins.setString(2, date);
				pstmtins.setString(3, fpath);
				if (stored) {
					pstmtins.setTimestamp(4, new Timestamp(new Date().getTime()));
					pstmtins.setString(5, "");
				} else {
					pstmtins.setTimestamp(4, null);
					pstmtins.setString(5, comment);
				}
				pstmtins.executeUpdate();
				acon.commit();
				wrrowcnt++;
				if (wrrowcnt % 1000 == 0) System.out.println("wr-rowcnt="+wrrowcnt);
			} else {
				System.out.println("sequence returned null");
			}
			if (rs != null) rs.close();
		} finally {
			try{if (rs != null)	rs.close();}
			catch (Exception e) {e.printStackTrace();}
		}
	}

	private static void send(String tempBody) throws Exception {
		//String tempSubject = "FTP Push.";
		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtpServer);
		Session session = Session.getDefaultInstance(props, null);
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		Address[] replyAddress = new Address[1];
		replyAddress[0] = new InternetAddress(replyTo);
		msg.setReplyTo(replyAddress);
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
		msg.setSubject(subject);
		msg.setText(tempBody);
		msg.setSentDate(new Date());
		Transport.send(msg);
		System.out.println("Message sent OK.");
	}

	/*
	public static void otherStuff() throws Exception {
		  FTPFile[] files = ftp.listFiles();
		  System.out.println("number files in dir:"+files.length);
		  for ( int j=0; j<files.length; j++ ) {
			FTPFile tmpFTPFile = files[j];
			if (tmpFTPFile == null) {}
			//else if (tmpFTPFile.isDirectory() == true) {}
			else {
			  Calendar displayftpSiteCalendar = files[j].getTimestamp();
			  GregorianCalendar gregorianCalendar = getGregorianCalendar(displayftpSiteCalendar);
			  System.out.println("file:"+files[j].getName()+" GregorianCalendar time="+gregorianCalendar.getTime());
			}
		  }
	}	
	*/
}
