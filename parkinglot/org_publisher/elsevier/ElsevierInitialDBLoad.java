package org.publisher.elsevier;

import java.io.File;
import java.util.ArrayList;

import org.portico.conprep.util.database.DBClass;

public class ElsevierInitialDBLoad
{

  private String SEMAPHORE_FILE = "datasetinfo.xml";
  private String TOC_FILE = "dataset.toc";
  private String DBURL="pr2ptcprod03.ithaka.org:2521:arpdb1"; // "pr2ptcdev03.ithaka.org:2521:poddb1";
  private String DBUSER="xmd_prod"; // "xmd_dev";
  private String DBPASSWORD="xmd_prod"; // "xmd_dev";


  public static void main(String[] args)
  {
      if(args != null && args.length == 1)
      {
          ElsevierInitialDBLoad elsevierInitialDBLoad = new ElsevierInitialDBLoad();
          elsevierInitialDBLoad.doProcess(args[0]);
      }
      else
      {
          System.out.println("ElsevierInitialDBLoad-usage= ElsevierInitialDBLoad <absolute file path of top level data directory>");
      }
  }

  public ElsevierInitialDBLoad()
  {
  }

  public boolean doProcess(String topLevelDataDirAbsolutePath)
  {
      boolean isSuccessful = true;
      ElsevierDB elsevierDB = null;

      try
      {
          isSuccessful = initialize();
          if(isSuccessful == true)
          {
              ArrayList lookUpFeedList = new ArrayList();
              lookUpFeedList.add("SDOSAcademicPress");
              lookUpFeedList.add("SDOSCellPress");
              lookUpFeedList.add("SDOSElsevier");
              lookUpFeedList.add("SDOSHarcourt");

              File topLevelDataDirFile = new File(topLevelDataDirAbsolutePath);
              if(topLevelDataDirFile != null && topLevelDataDirFile.exists())
              {
                  File[] feedDirList = topLevelDataDirFile.listFiles();
                  for(int feedindx=0; feedindx < feedDirList.length; feedindx++)
                  {
                      String feedName = feedDirList[feedindx].getName();
                      if(lookUpFeedList.contains(feedName) == true)
                      {
                          System.out.println("ElsevierInitialDBLoad-------Start--------------feedName="+feedName);

                          File[] datasetDirList = feedDirList[feedindx].listFiles();
                          for(int datasetIndx=0; datasetIndx < datasetDirList.length; datasetIndx++)
                          {
                              System.out.println("ElsevierInitialDBLoad-------Start--------------Dataset="+datasetDirList[datasetIndx].getName());

                              elsevierDB = new ElsevierDB(datasetDirList[datasetIndx].getName(), feedName, datasetDirList[datasetIndx].lastModified()+"");
                              if(elsevierDB != null)
                              {
                                  File[] datasetFiles = datasetDirList[datasetIndx].listFiles();
                                  for(int fileindx=0; fileindx < datasetFiles.length; fileindx++)
                                  {
                                      String fileName = datasetFiles[fileindx].getName();
                                      if(fileName.equals(SEMAPHORE_FILE) || fileName.equals(TOC_FILE))
                                      {
                                          continue; 
                                      }
                                      else
                                      {
                                          elsevierDB.addDataunitInfo(fileName, getFileSize(datasetFiles[fileindx]), datasetFiles[fileindx].lastModified()+"");             
                                      }
	                          }
                                  isSuccessful = elsevierDB.doProcess(); 
                                  if(isSuccessful == false)
                                  {
                                      System.out.println("ElsevierInitialDBLoad - Failed in elsevierDB.doProcess()-datasetName="+datasetDirList[datasetIndx].getName());
                                  }
                              }
                              else
                              {
                                  isSuccessful = false;
                                  System.out.println("ElsevierInitialDBLoad - Failed in - new ElsevierDB constructor elsevierDB is NULL");
                              }

                              System.out.println("ElsevierInitialDBLoad:------End--------------Dataset,isSuccessful="+
                                                                                               datasetDirList[datasetIndx].getName()+","+
                                                                                               isSuccessful);
                          }

                          System.out.println("ElsevierInitialDBLoad-------End--------------feedName="+feedName);
                      }
                  }
              }
          }
          else
          {
              System.out.println("ElsevierInitialDBLoad - Failed in initialize");
          }
      }
      catch(Exception e)
      {
          isSuccessful = false;
          System.out.println("Exception in ElsevierInitialDBLoad.doProcess()-topLevelDataDirAbsolutePath="+topLevelDataDirAbsolutePath+":"+e.getMessage());
      }
      finally
      {
          releaseDBConnection();
      }

     
      System.out.println("ElsevierInitialDBLoad.doProcess()-topLevelDataDirAbsolutePath,isSuccessful="+topLevelDataDirAbsolutePath+","+isSuccessful);

      return isSuccessful;
  }

  public String getFileSize(File file)
  {
      long fileSize = file.length();
      return fileSize+"";
  }

  private boolean initialize()
  {
      boolean isSuccessful = true;

      DBClass.initializeDB(DBURL, DBUSER, DBPASSWORD);

      return isSuccessful;
  }

  private void releaseDBConnection()
  {
      DBClass.releaseDBConnection();
  }
}
