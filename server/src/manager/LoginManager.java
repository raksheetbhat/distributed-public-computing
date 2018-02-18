package manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import db.DBConnector;
import pojo.Device;

public class LoginManager {
	
	public static void register(Device device) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
	    System.out.println("New Device");
	    String sql = "INSERT INTO devicemanager (ram,doj) " +
	                  "VALUES ('"+device.getRam() + "','" + device.getDoj()+ "')";
	    
	    System.out.println(sql);
	    stmt.executeUpdate(sql);
	    	
	    conn.close();    
	}
	
	/*
	public static String getUsername(int userid) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
	    String sql = "SELECT username,emailid "
	    				+ "FROM userdetails WHERE userid="+userid;
	    
	    System.out.println("Final sql statement : "+ sql);
	    ResultSet rs = stmt.executeQuery(sql);
	   
	    if(!rs.next())
	    {
	        return "";
	    }
	    else
	    {
	    	System.out.println("Existing User");
	    	String username=rs.getString("username");
		    
		    rs.close();  
			conn.close();   
			return username;
	    }
	}*/
}