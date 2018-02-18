package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;

import manager.DeviceManager;
import manager.GCMManager;
import pojo.Notification;

public class FileDecrypt extends HttpServlet {  

private static final long serialVersionUID = 7630300784693118565L;

public static String path="";
public static String script_location="G:/eclipse_workspace/DisCo/PicCrypt.py";
public static String image_location="";
public static String key="asd";
public static int n=7;
public static int k=4;

public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException  {  
  
	
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	int userid= Integer.parseInt(request.getParameter("userID"));
	ArrayList<String> devices = null;
	
	File folder = new File("I:/files/"+userid);
	File[] listOfFiles = folder.listFiles();

	Arrays.sort(listOfFiles);
	
	String fileParam="";

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
    	
    	String f= listOfFiles[i].getName().substring(0,listOfFiles[i].getName().lastIndexOf("."));
    	
        System.out.println("File " + f);
        fileParam+=f+" ";
      } else if (listOfFiles[i].isDirectory()) {
        System.out.println("Directory " + listOfFiles[i].getName());
      }
    }
    
    image_location="I:/files/"+userid;
  
    try {
    	String s = null;
        //Process p = Runtime.getRuntime().exec("python G:/eclipse_workspace/DisCo/PicCrypt.py -e I:/a.jpg I:/new asd 7 4");
        out.println("python "+ script_location +" -d "+ image_location + " " + k +" "+ fileParam);
    	
        Process p = Runtime.getRuntime().exec("python "+ script_location +" -d "+ image_location + " " + k +" "+ fileParam);
        
        
        BufferedReader stdInput = new BufferedReader(new 
             InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
             InputStreamReader(p.getErrorStream()));

        System.out.println("Standard output :\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        
        System.out.println("Error (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
    catch (IOException e) {
        System.out.println("IO exception");
        e.printStackTrace();
    }
    
    try {
		GCMManager.ping();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
		devices =  DeviceManager.getOnlineDevices();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    for(int i=0;i<n;i++)
    {
    	Notification notification = new Notification();
    	
    	notification.setType("store");
    	notification.setFileName(i+".png");
    	notification.setStorageID(1);
    	notification.setFragmentID(1);
    	notification.setSubType("receive");
    	notification.setUrl("http://10.50.46.126:8080/DisCo/FileDownloader?path=I:/files/"+userid+"/"+i+".png");
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(notification);

        // use this to send message with payload data
        Message message = new Message.Builder()
        .collapseKey("message")
        .timeToLive(3)
        .delayWhileIdle(true)
        .addData("message", "Sending data")
        .addData("title", "Major Project Notification")
        .addData("data",json)//you can get this message on client side app
        .build();  
    	
    	GCMManager.notify(devices.get(i), message);	
    }
    
    
    
    out.print("success");  
	}  
}