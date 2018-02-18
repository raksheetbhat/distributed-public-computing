package servlets;

import java.io.*;
import java.sql.SQLException;

import javax.servlet.ServletException;  
import javax.servlet.http.*;

import manager.StorageManager;
import pojo.Storage;  

  
public class FileFetch extends HttpServlet {  

private static final long serialVersionUID = 7630300784693118565L;

public static String path="";
public static String script_location="G:/eclipse_workspace/DisCo/PicCrypt.py";
public static String image_location="";
public static int n=7;
public static int k=4;
public static int storageID;
public static int userID;

public void doPost(HttpServletRequest request, HttpServletResponse response)  
    throws ServletException, IOException {  
  
	response.setContentType("text/html");  
	PrintWriter out = response.getWriter();
	Storage storage = null;
	
	storageID=Integer.parseInt(request.getParameter("storageID"));
	
    try {
			storage=StorageManager.fetchInfo(storageID);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    out.println("File Name : " + storage.getFilename());
    out.println("Fragment Count : " + storage.getFragmentCount());
    out.println("Min Fragment : " + storage.getMinFragment());
    out.println("UserID : " + storage.getUserID());
    
    
    
	}   
}  