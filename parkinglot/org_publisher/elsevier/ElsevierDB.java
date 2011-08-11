package org.publisher.elsevier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.portico.conprep.util.database.DBClass;

public class ElsevierDB
{

  private static final String FILE_SIZE = "file_size";
  private static final String FILE_RECEIPT_DATE = "file_receipt_date";
  private static final String RECEIPTMODE = "FTP";
  private static final String PROVIDERNAME = "Elsevier";
  private static final String FTPED_STATUS = "FTPED";

  private Hashtable dataunitDB = new Hashtable();
  private String datasetName = "";
  private String feedName = "";
  private String datasetReceiptDate = "";

  private ValuePair valuePair = null;

  public ElsevierDB()
  {
  }

  public ElsevierDB(String datasetNameIn, String feedNameIn, String datasetReceiptDateIn)
  {
      datasetName = datasetNameIn;
      feedName = feedNameIn;
      datasetReceiptDate = datasetReceiptDateIn;
  }

  public void setDatasetName(String datasetNameIn)
  {
      datasetName = datasetNameIn;
  }

  public void setFeedName(String feedNameIn)
  {
      feedName = feedNameIn;
  }

  public void setDatasetReceiptDate(String datasetReceiptDateIn)
  {
      datasetReceiptDate = datasetReceiptDateIn;
  }

  public void addDataunitInfo(String fileNameIn, String fileSizeIn, String fileReceiptDate)
  {
      ArrayList alist = new ArrayList(); 
      valuePair = new ValuePair();
      valuePair.setKey(ElsevierDB.FILE_SIZE);
      valuePair.setValue(fileSizeIn);
      alist.add(valuePair);

      valuePair = new ValuePair();
      valuePair.setKey(ElsevierDB.FILE_RECEIPT_DATE);
      valuePair.setValue(fileReceiptDate);
      alist.add(valuePair);

      dataunitDB.put(fileNameIn, alist);
  }

  public boolean doProcess()
  {
      boolean isSuccessful = true;

      try
      {
          isSuccessful = DBClass.insertSourceDatasetDB(datasetName,
                                                        ElsevierDB.getLongDateFromString(datasetReceiptDate),
                                                        ElsevierDB.RECEIPTMODE,
                                                        feedName,
                                                        DBClass.getProviderIdFromDB(ElsevierDB.PROVIDERNAME),
                                                        ElsevierDB.FTPED_STATUS);

          // This is the same datasetId(seq.nextval) that we internally created during the dataset insert(above call).
          int datasetIdDB = DBClass.getDatasetIdFromDB(feedName, datasetName);
          if(dataunitDB != null && dataunitDB.size() > 0)
          {
              Enumeration dataunitEnumerate = dataunitDB.keys();
              while(dataunitEnumerate.hasMoreElements())
              {
                  String filesize = "0";
                  String dataunitReceiptDate = "0";
                  String fileName = (String)dataunitEnumerate.nextElement();
                  ArrayList dlist = (ArrayList)dataunitDB.get(fileName);
                  if(dlist != null && dlist.size() > 0)
                  {
                      ValuePair valuePair = null;
                      for(int dindx=0; dindx < dlist.size(); dindx++)
                      {
                          valuePair = (ValuePair)dlist.get(dindx);
                          if(valuePair.getKey().equals(ElsevierDB.FILE_SIZE))
                          {
                              filesize = valuePair.getValue();
                          }
                          else if(valuePair.getKey().equals(ElsevierDB.FILE_RECEIPT_DATE))
                          {
                              dataunitReceiptDate = valuePair.getValue();
                          }
                      }
                  }

                  isSuccessful = DBClass.insertIntoSourceDataunitDB(datasetIdDB,
                                                                    fileName,
                                                                    ElsevierDB.getIntFileSize(filesize),
                                                                    ElsevierDB.getLongDateFromString(dataunitReceiptDate),
                                                                    ElsevierDB.FTPED_STATUS);
                  if(isSuccessful == false)
                  {
                      ElsevierMain.printMe("Failure in ElsevierDB.doProcess()-fileName="+fileName);
                      System.out.println("Failure in ElsevierDB.doProcess()-fileName="+fileName);
                      break;
                  }
              }
          }
      }
      catch(Exception e)
      {
          isSuccessful = false;
          ElsevierMain.printMe("Exception in ElsevierDB.doProcess()="+e.toString());
          System.out.println("Exception in ElsevierDB.doProcess()="+e.toString());
      }
      finally
      {
      }

      return isSuccessful;
  }
  
  public static long getLongDateFromString(String dateIn)
  {
      long longDate = 0;

      try
      {
          longDate = Long.parseLong(dateIn);
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierDB.getLongDateFromString()-dateIn="+dateIn+","+e.toString());
          System.out.println("Exception in ElsevierDB.getLongDateFromString()-dateIn="+dateIn+","+e.toString());
      }
      finally
      {
      }
      
      return longDate;
  }

  public static int getIntFileSize(String fileSizeIn)
  {
      int intFileSize = 0;

      try
      {
          intFileSize = Integer.parseInt(fileSizeIn);
      }
      catch(Exception e)
      {
          ElsevierMain.printMe("Exception in ElsevierDB.getIntFileSize()-fileSizeIn="+fileSizeIn+","+e.toString());
          System.out.println("Exception in ElsevierDB.getIntFileSize()-fileSizeIn="+fileSizeIn+","+e.toString());
      }
      finally
      {
      }
      
      return intFileSize;
  }
}
