package org.portico.conprep.ui.helper;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.portico.common.config.LdapUtil;

public class RenameUtil {

	public static int deleteSuRename(String batchId) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
		int returnVal = 0;
		try {
	        String driverName = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbdriver");
	        String url = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburlprefix") +
	          LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburl");
	        String user = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbuser");
	        String password = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbpassword");
	        Driver driver = (Driver) Class.forName(driverName).newInstance();
	        DriverManager.registerDriver(driver);
	        con = DriverManager.getConnection(url, user, password);

	        String query = "delete from p_su_rename_action where p_batch_accession_id = ?";
	        pstmt = con.prepareStatement(query);
			pstmt.setString(1, batchId);
			returnVal = pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} finally {
			try {if (pstmt != null) pstmt.close();}
			catch (Exception e){e.printStackTrace();}
			try {if (con != null) con.close();}
			catch (Exception e){e.printStackTrace();}
		}
		return returnVal;
	}

	public static boolean getSuRename(String batchId, String suStateId) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean returnVal = false;
		try {
	        String driverName = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbdriver");
	        String url = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburlprefix") +
	          LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburl");
	        String user = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbuser");
	        String password = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbpassword");
	        Driver driver = (Driver) Class.forName(driverName).newInstance();
	        DriverManager.registerDriver(driver);
	        con = DriverManager.getConnection(url, user, password);

	        String query = "select * from p_su_rename_action where p_batch_accession_id = ? and p_orig_filename in"+
	        	" (select p_orig_filename from p_su where p_accession_id = ?)";
	        pstmt = con.prepareStatement(query);
			pstmt.setString(1, batchId);
            pstmt.setString(2, suStateId);
			rs = pstmt.executeQuery();
			if (rs.next()) returnVal = true;
			rs.close();
			pstmt.close();
			con.close();
		} finally {
			try {if (rs != null) rs.close();}
			catch (Exception e){e.printStackTrace();}
			try {if (pstmt != null) pstmt.close();}
			catch (Exception e){e.printStackTrace();}
			try {if (con != null) con.close();}
			catch (Exception e){e.printStackTrace();}
		}
		return returnVal;
	}

	public static void updateSuRename(String batchId, String suStateId, String newFname, String modifier) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;

	  try {
	        String driverName = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbdriver");
	        String url = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburlprefix") +
	          LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburl");
	        String user = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbuser");
	        String password = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbpassword");
	        Driver driver = (Driver) Class.forName(driverName).newInstance();
	        DriverManager.registerDriver(driver);
	        con = DriverManager.getConnection(url, user, password);
		    sql = "update p_su_rename_action set p_new_work_filename =?, p_modified_by=?, p_modify_timestamp=?"+ 
					" where p_batch_accession_id = ? and p_orig_filename in"+
		        	" (select p_orig_filename from p_su where p_accession_id = ?)";
		    pstmt = con.prepareStatement(sql);
		    pstmt.setString(1, newFname);
		    pstmt.setString(2, modifier);
		    pstmt.setTimestamp(3, new Timestamp(new Date().getTime()));
		    pstmt.setString(4, batchId);
		    pstmt.setString(5, suStateId);
		    pstmt.executeUpdate();
		    pstmt.close();
		    con.close();
	  } finally {
		  try {if (pstmt != null) pstmt.close();}
		  catch (Exception e) {e.printStackTrace();}
		  try {if (con != null) con.close();}
		  catch (Exception e){e.printStackTrace();}
	  }
	}
	
	public static void createSuRename(String batchId, String suStateId, String newFname, String creator) throws Exception {
		PreparedStatement pstmt = null;
		Connection con = null;
	  try {
        String driverName = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbdriver");
        String url = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburlprefix") +
          LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dburl");
        String user = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbuser");
        String password = LdapUtil.getAttribute("dc=database", "cn=conprepdb", "dbpassword");
        Driver driver = (Driver) Class.forName(driverName).newInstance();
        DriverManager.registerDriver(driver);
        con = DriverManager.getConnection(url, user, password);

        String insert = "insert into p_su_rename_action (" +
                  "p_id,p_orig_filename, p_batch_accession_id, p_new_work_filename, p_created_by, p_create_timestamp, p_modify_timestamp)" +
    			  " select P_RENAME_ENTRY_SEQ.nextval,p_orig_filename,?,?,?,?,? from p_su where p_accession_id = ?";

        pstmt = con.prepareStatement(insert);
	    pstmt.setString(1, batchId);
	    pstmt.setString(6, suStateId);
	    pstmt.setString(2, newFname);
	    pstmt.setString(3, creator);
	    pstmt.setTimestamp(4, new Timestamp(new Date().getTime()));
	    pstmt.setTimestamp(5, null);
	    pstmt.executeUpdate();
	    pstmt.close();
	    con.close();
	  } finally {
		  try {if (pstmt != null) pstmt.close();}
		  catch (Exception e){e.printStackTrace();}
		  try {if (con != null) con.close();}
		  catch (Exception e){e.printStackTrace();}
	  }
	}
}