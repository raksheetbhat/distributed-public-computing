package manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import db.DBConnector;

public class DeviceManager {
	
	public static ArrayList<String> getOnlineDevices() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
		ArrayList<String> ids = new ArrayList<String>();
	    String sql = "SELECT GCMID "
	    				+ "FROM devicemaster where online="+1;
	    System.out.println("Final sql statement : "+ sql);
	    
	    ResultSet rs = stmt.executeQuery(sql);
	   
	    while(rs.next())
	    {	    
			ids.add(rs.getString("GCMID"));
	    }
	    rs.close();  
		conn.close(); 		
		return ids;
	}
	
	public static ArrayList<String> getGCMID() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
		ArrayList<String> ids = new ArrayList<String>();
	    String sql = "SELECT GCMID "
	    				+ "FROM devicemaster";
	    System.out.println("Final sql statement : "+ sql);
	    
	    ResultSet rs = stmt.executeQuery(sql);
	   
	    while(rs.next())
	    {	    
			ids.add(rs.getString("GCMID"));
	    }
	    rs.close();  
		conn.close(); 		
		return ids;
	}
	
	public static void setOnlineStatus(int deviceID) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
		String sql = "UPDATE devicemaster SET online="+ 1 
	    				+ " WHERE deviceID="+deviceID;
	    
		System.out.println("Final sql statement : "+ sql);
	    
	    stmt.executeUpdate(sql);  
		conn.close(); 		
	}
}