package manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import db.DBConnector;
import pojo.Storage;


public class StorageManager {
	
	public static void uploadFile(Storage storage) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
	    System.out.println("New Storage");
	    String sql = "INSERT INTO storagemaster ("
	    		+ "userID,"
	    		+ "fileName,"
	    		+ "fragmentCount,"
	    		+ "minFragment)" 
	    		+ "VALUES ("
	    		+ storage.getUserID() + ",'" 
	            + storage.getFilename()+ "'," 
	            + storage.getFragmentCount()+ "," 
	            + storage.getMinFragment()+ ")";
	    
	    System.out.println(sql);
	    stmt.executeUpdate(sql);	    	
	    conn.close();    
	}
	
	public static Storage fetchInfo (int storageID) throws SQLException
	{
		Storage storage = new Storage();
		Connection conn = null;
		Statement stmt = null;
		conn=DBConnector.getConnection();
		stmt = conn.createStatement();
	    
	    String sql = "SELECT userID,filename,fragmentCount,minFragment "
	    				+ "FROM storagemaster where storageID="+storageID;
	    System.out.println("Final sql statement : "+ sql);
	    
	    ResultSet rs = stmt.executeQuery(sql);
	   
	    while(rs.next())
	    {	    
	    	storage.setStorageID(storageID);
			storage.setUserID(rs.getInt("userID"));
			storage.setFilename(rs.getString("fileName"));
			storage.setFragmentCount(rs.getInt("fragmentCount"));
			storage.setMinFragment(rs.getInt("minFragment"));	
	      }
	    rs.close();  
		conn.close(); 		
		return storage;
	}
}
