package org.publisher.elsevier;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ElsevierDateFix
{
  String HOME_DIRECTORY = "/export/home/padmin/elsevier_content"; // "/export/home/srn/publisher/elsevier/data";
  String[] SDOSCellPress = {};
  String[] SDOSAcademicPress = {}; // {"OXA05490","OXA05670"};
  String[] SDOSHarcourt = {}; // {"OXH05110","OXH05380","OXH05630"};
  String[] SDOSElsevier = {"OXM06340","OXM06350"}; // {"OXM05690","OXM05710","OXM05760","OXM05770","OXM05780","OXM05860","OXM05920","OXM05940","OXM06100","OXM06110","OXM06190"}; 
  int fixedYear = 2006;

  public static void main(String[] arg)
  {
      ElsevierDateFix elsevierDateFix = new ElsevierDateFix();
      elsevierDateFix.fixDate();
  }

  public ElsevierDateFix()
  {
  }

  public void fixDate()
  {
      if(SDOSCellPress != null && SDOSCellPress.length > 0)
      {
          for(int indx=0; indx < SDOSCellPress.length; indx++)
          {
              String filePath = HOME_DIRECTORY+"/"+"SDOSCellPress"+"/"+SDOSCellPress[indx];
              // fixDatasetYear(filePath);
              fixDatasetAMPM(filePath);
          }
      }
      else
      {
          System.out.println("No SDOSCellPress Dataset Directory to be fixed");
      }

      if(SDOSAcademicPress != null && SDOSAcademicPress.length > 0)
      {
          for(int indx=0; indx < SDOSAcademicPress.length; indx++)
          {
              String filePath = HOME_DIRECTORY+"/"+"SDOSAcademicPress"+"/"+SDOSAcademicPress[indx];
              // fixDatasetYear(filePath);
              fixDatasetAMPM(filePath);
          }
      }
      else
      {
          System.out.println("No SDOSAcademicPress Dataset Directory to be fixed");
      }

      if(SDOSHarcourt != null && SDOSHarcourt.length > 0)
      {
          for(int indx=0; indx < SDOSHarcourt.length; indx++)
          {
              String filePath = HOME_DIRECTORY+"/"+"SDOSHarcourt"+"/"+SDOSHarcourt[indx];
              // fixDatasetYear(filePath);
              fixDatasetAMPM(filePath);
          }
      }
      else
      {
          System.out.println("No SDOSHarcourt Dataset Directory to be fixed");
      }

      if(SDOSElsevier != null && SDOSElsevier.length > 0)
      {
          for(int indx=0; indx < SDOSElsevier.length; indx++)
          {
              String filePath = HOME_DIRECTORY+"/"+"SDOSElsevier"+"/"+SDOSElsevier[indx];
              // fixDatasetYear(filePath);
              fixDatasetAMPM(filePath);
          }
      }
      else
      {
          System.out.println("No SDOSElsevier Dataset Directory to be fixed");
      }
  }

  public void fixDatasetYear(String filePath)
  {
      System.out.println("Fixing(Year) Dataset Directory="+filePath);
      File file = new File(filePath);
      if(file != null)
      {
          setNewFileDateYear(file);
          if(file.isDirectory())
          {
              File[] listfiles = file.listFiles();
              for(int findx=0; findx < listfiles.length; findx++)
              {
                  setNewFileDateYear(listfiles[findx]);
              }
          }
      }
  }

  public void setNewFileDateYear(File file)
  {
      long lastModified = file.lastModified();
      GregorianCalendar fixedCalendar = new GregorianCalendar();
      fixedCalendar.setTimeInMillis(lastModified);
      System.out.print("Old date="+fixedCalendar.getTime()+"  ,  ");
      fixedCalendar.set(Calendar.YEAR, fixedYear);
      System.out.println("New date="+fixedCalendar.getTime());
      file.setLastModified(fixedCalendar.getTimeInMillis());
  }

  public void fixDatasetAMPM(String filePath)
  {
      System.out.println("Fixing(AMPM) Dataset Directory="+filePath);
      File file = new File(filePath);
      if(file != null)
      {
          setNewFileDateAMPM(file);
          if(file.isDirectory())
          {
              File[] listfiles = file.listFiles();
              for(int findx=0; findx < listfiles.length; findx++)
              {
                  setNewFileDateAMPM(listfiles[findx]);
              }
          }
      }
  }

  public void setNewFileDateAMPM(File file)
  {
      long lastModified = file.lastModified();
      GregorianCalendar fixedCalendar = new GregorianCalendar();
      fixedCalendar.setTimeInMillis(lastModified);
      System.out.print("Old date="+fixedCalendar.getTime()+"  ,  ");
// For production change -12 to +12
      fixedCalendar.set(Calendar.HOUR_OF_DAY, fixedCalendar.get(Calendar.HOUR_OF_DAY)+12);
      System.out.println("New date="+fixedCalendar.getTime());
      file.setLastModified(fixedCalendar.getTimeInMillis());
  }
}
