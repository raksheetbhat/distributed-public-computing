package manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import db.DBConnector;
import pojo.Dataset;

public class DatasetManager {
	
	public static void uploadData(Dataset dataset) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
	    System.out.println("New Dataset");
	    String sql = "INSERT INTO dataset ("
	    		+ "userid,"
	    		+ "filename,"
	    		+ "uploaddate,"
	    		+ "duedate,task)" 
	    		+ "VALUES ('"
	    		+ dataset.getUserID() + "','" 
	            + dataset.getFilename()+ "','" 
	            + new java.sql.Date(dataset.getUploaddate().getTime())+ "','" 
	            + new java.sql.Date(dataset.getDuedate().getTime())+ "','" 
	            + dataset.getTask() + "')";
	    
	    System.out.println(sql);
	    stmt.executeUpdate(sql);	    	
	    conn.close();    
	}
}
