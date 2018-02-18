package servlets;

import java.io.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Date;

import javax.servlet.ServletException;  
import javax.servlet.http.*;  
import com.oreilly.servlet.MultipartRequest;

import manager.DatasetManager;
import pojo.Dataset;  
  
public class DatasetUpload extends HttpServlet {  

private static final long serialVersionUID = 7630300784693118565L;


public void doPost(HttpServletRequest request, HttpServletResponse response)  
    throws ServletException, IOException {  
  
response.setContentType("text/html");  
PrintWriter out = response.getWriter();


Dataset dataset=new Dataset();


MultipartRequest m=new MultipartRequest(request,"I:/new",2393494); 
Enumeration<?> files=m.getFileNames();

String filename="";
while(files.hasMoreElements())
{
	String name=(String)files.nextElement();
	filename=m.getFilesystemName(name);
}

SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

Date d=null;
try {
	d = format.parse(m.getParameter("duedate"));
} catch (ParseException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}

dataset.setUploaddate(new Date());
dataset.setDuedate(d);
dataset.setTask(m.getParameter("task").toString());
dataset.setUserID(1);
dataset.setFilename(filename);

System.out.println("Upload Date " + dataset.getUploaddate());
System.out.println("Due Date " + dataset.getDuedate());
System.out.println("Task " + dataset.getTask());
System.out.println("UserID " + dataset.getUserID());
System.out.println("File Name " + dataset.getFilename());

try {
	DatasetManager.uploadData(dataset);
} catch (SQLException e) {
	e.printStackTrace();
}


out.print("successfully uploaded " + filename);  
}  
}  