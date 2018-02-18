package servlets;

import java.io.*;
import java.sql.SQLException;

import javax.servlet.ServletException;  
import javax.servlet.http.*;

import manager.DeviceManager;  
  
public class OnlineStatus extends HttpServlet {  


	private static final long serialVersionUID = -2190691174869223479L;

public void doPost(HttpServletRequest request, HttpServletResponse response)  
    throws ServletException, IOException {  
  
	response.setContentType("text/html");  
	PrintWriter out = response.getWriter();

	int deviceID=Integer.parseInt(request.getParameter("deviceID"));
    
	try {
		DeviceManager.setOnlineStatus(deviceID);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}   
}