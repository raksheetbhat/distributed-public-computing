package servlets;

import java.io.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;  
import javax.servlet.http.*;
  
public class FileDownloader extends HttpServlet {  

private static final long serialVersionUID = -5624178871256567204L;
public static String file_location="";

public void doGet(HttpServletRequest request, HttpServletResponse response)  
    throws ServletException, IOException {  
  
	response.setContentType("image/png");  
	
	file_location=request.getParameter("path");
	System.out.println("Inside file downloader"+file_location);
	
    // Get the absolute path of the image
    ServletContext sc = getServletContext();
    
    // Get the MIME type of the image
    String mimeType = sc.getMimeType(file_location);
    if (mimeType == null) {
        sc.log("Could not get MIME type of "+file_location);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }
 
    // Set content type
    response.setContentType(mimeType);
 
    // Set content size
    File file = new File(file_location);
    response.setContentLength((int)file.length());
 
    // Open the file and output streams
    FileInputStream in = new FileInputStream(file);
    OutputStream out = response.getOutputStream();
 
    // Copy the contents of the file to the output stream
    byte[] buf = new byte[1024];
    int count = 0;
    while ((count = in.read(buf)) >= 0) {
        out.write(buf, 0, count);
    }
    in.close();
    out.close();
	}   
}  