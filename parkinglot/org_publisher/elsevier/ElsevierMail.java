package org.publisher.elsevier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ElsevierMail
{
  // Mail
  public static final String SMTP_HOST = "smtp.ithaka.org"; // or 127.0.0.1
  public static final String MAIL_SUBJECT_PREFIX = "SDOS Dataset Confirmation:";

  public static final String MAILTO = "MAILTO=";
  public static final String MAILCC = "MAILCC=";
  public static final String MAILFROM = "MAILFROM=";
  public static final String MAILREPLYBACK = "MAILREPLYBACK=";
  public static final String MAILCCEXTERNAL = "MAILCCEXTERNAL=";
  public static final String MAILSEPARATOR = "|";

  // DB
  public static final String DBURL = "DBURL=";
  public static final String DBUSER = "DBUSER=";
  public static final String DBPASSWORD = "DBPASSWORD=";

  public static final String SUCCESS = "0";
  public static final String FAILURE = "1";
  public static final String NODATA = "2";

  public ArrayList mailFromList = new ArrayList();
  public ArrayList mailToList = new ArrayList();
  public ArrayList mailCcList = new ArrayList();
  public ArrayList mailReplyBackList = new ArrayList();
  public ArrayList mailCcExternalList = new ArrayList();

  private String dbUrl = "";
  private String dbUser = "";
  private String dbPassword = "";

  public ElsevierMail()
  {
/*
      try
      {
          readConfig();
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierMail()constructor="+e.getMessage());
          System.out.println("Exception in ElsevierMail()constructor="+e.getMessage());
      }
      finally
      {
      }
*/
  }

  public void readConfig()
  {
      try
      {
          // Just to make sure it is clean before
          mailFromList.clear();
          mailToList.clear();
          mailCcList.clear();
          mailReplyBackList.clear();
          mailCcExternalList.clear();

          ElsevierMain.printMe("ElsevierMail.readConfig() Start");
          System.out.println("ElsevierMail.readConfig() Start");

          File configfile = new File(ElsevierMain.LOCAL_HOME + File.separator + ElsevierMain.configDirectory + File.separator + ElsevierMain.configFile);
          if(configfile == null)
          {
              ElsevierMain.printMe("ERROR: ElsevierMail-readConfig-elsevier config file missing");
              System.out.println("ERROR: ElsevierMail-readConfig-elsevier config file missing");
          }
          else
          {
              BufferedReader in = new BufferedReader(new FileReader(configfile));
              String str = "";
              while((str = in.readLine()) != null) 
              {
                  ElsevierMain.printMe("ElsevierMail.readConfig() line ="+str);
                  System.out.println("ElsevierMail.readConfig() line ="+str);

                  if(str.startsWith(ElsevierMail.MAILTO))
                  {
                      String mailString = str.substring(ElsevierMail.MAILTO.length());
                      StringTokenizer st = new StringTokenizer(mailString, ElsevierMail.MAILSEPARATOR);
                      while (st.hasMoreTokens()) 
                      {
                          String mailId = st.nextToken();
                          mailToList.add(mailId);
                          ElsevierMain.printMe("ElsevierMail.readConfig()-mailId(TO)="+mailId);
                          System.out.println("ElsevierMail.readConfig()-mailId(TO)="+mailId);
                      }
                  }
                  else if(str.startsWith(ElsevierMail.MAILCC))
                  {
                      String mailString = str.substring(ElsevierMail.MAILCC.length());
                      StringTokenizer st = new StringTokenizer(mailString, ElsevierMail.MAILSEPARATOR);
                      while (st.hasMoreTokens()) 
                      {
                          String mailId = st.nextToken();
                          mailCcList.add(mailId);
                          ElsevierMain.printMe("ElsevierMail.readConfig()-mailId(CC)="+mailId);
                          System.out.println("ElsevierMail.readConfig()-mailId(CC)="+mailId);
                      }
                  }
                  else if(str.startsWith(ElsevierMail.MAILFROM))
                  {
                      String mailString = str.substring(ElsevierMail.MAILFROM.length());
                      StringTokenizer st = new StringTokenizer(mailString, ElsevierMail.MAILSEPARATOR);
                      while (st.hasMoreTokens()) 
                      {
                          String mailId = st.nextToken();
                          mailFromList.add(mailId);
                          ElsevierMain.printMe("ElsevierMail.readConfig()-mailId(FROM)="+mailId);
                          System.out.println("ElsevierMail.readConfig()-mailId(FROM)="+mailId);
                      }
                  }
                  else if(str.startsWith(ElsevierMail.MAILREPLYBACK))
                  {
                      String mailString = str.substring(ElsevierMail.MAILREPLYBACK.length());
                      StringTokenizer st = new StringTokenizer(mailString, ElsevierMail.MAILSEPARATOR);
                      while (st.hasMoreTokens()) 
                      {
                          String mailId = st.nextToken();
                          mailReplyBackList.add(mailId);
                          ElsevierMain.printMe("ElsevierMail.readConfig()-mailId(REPLYBACK)="+mailId);
                          System.out.println("ElsevierMail.readConfig()-mailId(REPLYBACK)="+mailId);
                      }
                  }
                  else if(str.startsWith(ElsevierMail.MAILCCEXTERNAL))
                  {
                      String mailString = str.substring(ElsevierMail.MAILCCEXTERNAL.length());
                      StringTokenizer st = new StringTokenizer(mailString, ElsevierMail.MAILSEPARATOR);
                      while (st.hasMoreTokens()) 
                      {
                          String mailId = st.nextToken();
                          mailCcExternalList.add(mailId);
                          ElsevierMain.printMe("ElsevierMail.readConfig()-mailId(CCEXTERNAL)="+mailId);
                          System.out.println("ElsevierMail.readConfig()-mailId(CCEXTERNAL)="+mailId);
                      }
                  }
                  else if(str.startsWith(ElsevierMail.DBURL))
                  {
                      dbUrl = str.substring(ElsevierMail.DBURL.length());
                  }
                  else if(str.startsWith(ElsevierMail.DBUSER))
                  {
                      dbUser = str.substring(ElsevierMail.DBUSER.length());
                  }
                  else if(str.startsWith(ElsevierMail.DBPASSWORD))
                  {
                      dbPassword = str.substring(ElsevierMail.DBPASSWORD.length());
                  }
              }

              if(mailToList != null && mailToList.size() > 0)
              {
                  for(int indx=0; indx < mailToList.size(); indx++)
                  {
                      ElsevierMain.printMe("ElsevierMail.readConfig()-mailToList array="+(String)mailToList.get(indx));
                      System.out.println("ElsevierMail.readConfig()-mailToList array="+(String)mailToList.get(indx));
                  }
              }
              if(mailCcList != null && mailCcList.size() > 0)
              {
                  for(int indx=0; indx < mailCcList.size(); indx++)
                  {
                      ElsevierMain.printMe("ElsevierMail.readConfig()-mailCcList array="+(String)mailCcList.get(indx));
                      System.out.println("ElsevierMail.readConfig()-mailCcList array="+(String)mailCcList.get(indx));
                  }
              }
              if(mailFromList != null && mailFromList.size() > 0)
              {
                  for(int indx=0; indx < mailFromList.size(); indx++)
                  {
                      ElsevierMain.printMe("ElsevierMail.readConfig()-mailFromList array="+(String)mailFromList.get(indx));
                      System.out.println("ElsevierMail.readConfig()-mailFromList array="+(String)mailFromList.get(indx));
                  }
              }
              if(mailReplyBackList != null && mailReplyBackList.size() > 0)
              {
                  for(int indx=0; indx < mailReplyBackList.size(); indx++)
                  {
                      ElsevierMain.printMe("ElsevierMail.readConfig()-mailReplyBackList array="+(String)mailReplyBackList.get(indx));
                      System.out.println("ElsevierMail.readConfig()-mailReplyBackList array="+(String)mailReplyBackList.get(indx));
                  }
              }
              if(mailCcExternalList != null && mailCcExternalList.size() > 0)
              {
                  for(int indx=0; indx < mailCcExternalList.size(); indx++)
                  {
                      ElsevierMain.printMe("ElsevierMail.readConfig()-mailCcExternalList array="+(String)mailCcExternalList.get(indx));
                      System.out.println("ElsevierMail.readConfig()-mailCcExternalList array="+(String)mailCcExternalList.get(indx));
                  }
              }
          }
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierMail.readConfig()="+e.getMessage());
          System.out.println("Exception in ElsevierMail.readConfig()="+e.getMessage());
      }
      finally
      {
      }

      ElsevierMain.printMe("ElsevierMail.readConfig() Done");
      System.out.println("ElsevierMail.readConfig() Done");
  }
  
  public ArrayList getMailToList()
  {
      return mailToList;
  }

  public ArrayList getMailCcList()
  {
      return mailCcList;
  }

  public ArrayList getMailFromList()
  {
      return mailFromList;
  }

  public ArrayList getMailReplyBackList()
  {
      return mailReplyBackList;
  }

  public ArrayList getMailCcExternalList()
  {
      return mailCcExternalList;
  }

  public String getDbUrl()
  {
      return dbUrl;
  }

  public String getDbUser()
  {
      return dbUser;
  }

  public String getDbPassword()
  {
      return dbPassword;
  }

  // public boolean mailConfirmation(String datasetDirName, String msgTextXML, String startTime, String endTime, boolean status)
  // status=0(Successful), 1(Failure), 2(No data)
  public boolean mailConfirmationExternal(String statusString, String subject, String body)
  {
      boolean isSuccessful = true;
      
      ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()-Status="+statusString);
      System.out.println("ElsevierMail.mailConfirmationExternal()-Status="+statusString);

      ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()-Subject="+subject);
      System.out.println("ElsevierMail.mailConfirmationExternal()-Subject="+subject);

      ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()-Body="+body);
      System.out.println("ElsevierMail.mailConfirmationExternal()-Body="+body);

      try
      {
          // Get system properties
          Properties props = System.getProperties();
          // Setup mail server
          props.put("mail.smtp.host", ElsevierMail.SMTP_HOST);
          // Get session
          Session session = Session.getDefaultInstance(props, null);
          // Define message
          MimeMessage message = new MimeMessage(session);
          if(statusString.equals(ElsevierMail.SUCCESS))
          {
              if(mailToList != null && mailToList.size() > 0 &&
                    mailCcExternalList != null && mailCcExternalList.size() > 0 &&
                    mailFromList != null && mailFromList.size() > 0 &&
                    mailReplyBackList != null && mailReplyBackList.size() > 0)
              {
                  message.setFrom(new InternetAddress((String)mailFromList.get(0)));
                  InternetAddress[] replyAddresses = new InternetAddress[1];
                  replyAddresses[0] = new InternetAddress((String)mailReplyBackList.get(0));
                  message.setReplyTo(replyAddresses);

                  // Success, mail TO Publisher, other TO(s) list
                  for(int indxto=0; indxto < mailToList.size(); indxto++)
                  {
                      message.addRecipient(Message.RecipientType.TO, new InternetAddress((String)mailToList.get(indxto))); // elsevier and others in TO list
                      ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()-TO="+(String)mailToList.get(indxto));
                      System.out.println("ElsevierMail.mailConfirmationExternal()-TO="+(String)mailToList.get(indxto));
                  }
                  // Success, mail CC External list
                  for(int indxccext=0; indxccext < mailCcExternalList.size(); indxccext++)
                  {
                      message.addRecipient(Message.RecipientType.CC, new InternetAddress((String)mailCcExternalList.get(indxccext))); // ranga/CCExt list
                      ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()-CC External="+(String)mailCcExternalList.get(indxccext));
                      System.out.println("ElsevierMail.mailConfirmationExternal()-CC External="+(String)mailCcExternalList.get(indxccext));
                  }
                  message.setSubject(subject);
                  message.setText(body);
                  // Send message
                  Transport.send(message);
              }
              else
              {
                  isSuccessful = false;            
                  // Error mailTo OR mailCcExternalList OR mailFrom OR mailReplyBack not defined in the config file
                  ElsevierMain.printMe("ElsevierMail.mailConfirmationExternal()- MAILTO or MAILCCEXT or MAILFROM or MAILREPLYBACK is not defined in the config file");
                  System.out.println("ElsevierMail.mailConfirmationExternal()- MAILTO or MAILCCEXT or MAILFROM or MAILREPLYBACK is not defined in the config file");
              }
          }
          else
          {
              // Currently no external mail is sent for Failures
          }
      }
      catch(Exception e)
      {
          isSuccessful = false;
          ElsevierMain.printMe("Exception in ElsevierMail.mailConfirmationExternal()="+e.getMessage());
          System.out.println("Exception in ElsevierMail.mailConfirmationExternal="+e.getMessage());
      }
      finally
      {
      }
      return isSuccessful;
  }

  public boolean mailConfirmationInternal(String statusString, String subject, String body)
  {
      boolean isSuccessful = true;
      
      ElsevierMain.printMe("ElsevierMail.mailConfirmationInternal()-Status="+statusString);
      System.out.println("ElsevierMail.mailConfirmationInternal()-Status="+statusString);

      ElsevierMain.printMe("ElsevierMail.mailConfirmationInternal()-Subject="+subject);
      System.out.println("ElsevierMail.mailConfirmationInternal()-Subject="+subject);

      ElsevierMain.printMe("ElsevierMail.mailConfirmationInternal()-Body="+body);
      System.out.println("ElsevierMail.mailConfirmationInternal()-Body="+body);

      try
      {
          // Get system properties
          Properties props = System.getProperties();
          // Setup mail server
          props.put("mail.smtp.host", ElsevierMail.SMTP_HOST);
          // Get session
          Session session = Session.getDefaultInstance(props, null);
          // Define message
          MimeMessage message = new MimeMessage(session);
          if(mailCcList != null && mailCcList.size() > 0 &&
                mailFromList != null && mailFromList.size() > 0)
          {
              message.setFrom(new InternetAddress((String)mailFromList.get(0)));

              // Success, mail CC list
              for(int indxcc=0; indxcc < mailCcList.size(); indxcc++)
              {
                  message.addRecipient(Message.RecipientType.TO, new InternetAddress((String)mailCcList.get(indxcc))); // ranga or CC list entry
                  ElsevierMain.printMe("ElsevierMail.mailConfirmationInternal()-CC="+(String)mailCcList.get(indxcc));
                  System.out.println("ElsevierMail.mailConfirmationInternal()-CC="+(String)mailCcList.get(indxcc));
              }
              message.setSubject(subject);
              message.setText(body);
              // Send message
              Transport.send(message);
          }
          else
          {
              isSuccessful = false;            
              // Error mailCc OR mailFrom not defined in the config file
              ElsevierMain.printMe("ElsevierMail.mailConfirmationInternal()- MAILCC or MAILFROM is not defined in the config file");
              System.out.println("ElsevierMail.mailConfirmationInternal()- MAILCC or MAILFROM is not defined in the config file");
          }
      }
      catch(Exception e)
      {
          isSuccessful = false;
          ElsevierMain.printMe("Exception in ElsevierMail.mailConfirmationInternal()="+e.getMessage());
          System.out.println("Exception in ElsevierMail.mailConfirmationInternal="+e.getMessage());
      }
      finally
      {
      }
      return isSuccessful;
  }

  public static String createSubjectExternal(String statusString, String feedName, String datasetDirName, String startTime, String endTime)
  {
      String subject = "";
      if(statusString.equals(ElsevierMail.SUCCESS))
      {
          subject = ElsevierMail.MAIL_SUBJECT_PREFIX + " " + datasetDirName + " " + "(" + startTime + "-" + endTime + ")";
      }
      else
      {
          // Currently no external mail/subject is sent for Failures
      }

      return subject;
  }

  public static String createSubjectInternal(String statusString, String feedName, String datasetDirName, String startTime, String endTime)
  {
      String subject = "";
      if(statusString.equals(ElsevierMail.SUCCESS))
      {
          subject = "Successful" + ":" + "Elsevier" + " " + feedName + " " + datasetDirName;
      }
      else if(statusString.equals(ElsevierMail.FAILURE))
      {
          subject = "Failure" + ":" + "Elsevier" + " " + feedName + " " + datasetDirName;
      }
      else
      {
          // No data
          subject = "No Data" + ":" + "Elsevier" + " " + feedName;
      }

      return subject;
  }
}
