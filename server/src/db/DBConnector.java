package db;

import java.sql.*;

public class DBConnector {
   
	static Boolean server=false;
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   
	static String DB_URL;
    static String USER; 
    static String PASS; 
   
    public static Connection getConnection() {
   
    Connection conn = null;
    Statement stmt = null;
    //String host,port;
    //String url;
    /*
    if(server)
    {
    	host=System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    	port=System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    	//url=System.getenv("OPENSHIFT_MYSQL_DB_URL");
    	DB_URL=String.format("jdbc:mysql://%s:%s/majorproject", host,port);
    	USER= "adminSeYWiA4";
    	PASS= "BH2CxfNVyKTd";
    }*/
    
    //else
    //{
    	DB_URL = "jdbc:mysql://localhost/majorproject";
    	USER= "root";
    	PASS= "mysql911";
    //}
    
    try{
    	//Register JDBC driver
    	Class.forName("com.mysql.jdbc.Driver");
    	//Open a connection
    	System.out.println("Connecting to database...");
      
    	
    	System.out.println("DB URL : "+DB_URL);
    	conn = DriverManager.getConnection(DB_URL, USER, PASS);
    	return conn;
    	}catch(SQLException se){
    		//Handle errors for JDBC
    		se.printStackTrace();
    	}catch(Exception e){
    		//Handle errors for Class.forName
    		e.printStackTrace();
    	}finally{
    		//finally block used to close resources
    	try{
         if(stmt!=null)
            stmt.close();
    	}catch(SQLException se2){
      }
    }
      return conn;
   }
}