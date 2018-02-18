package servlets;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import javax.servlet.ServletException;  
import javax.servlet.http.*;

import com.google.android.gcm.server.Message;
import com.google.gson.Gson;
import com.oreilly.servlet.MultipartRequest;

import manager.DeviceManager;
import manager.GCMManager;
import manager.StorageManager;
import pojo.Notification;
import pojo.Storage;
  
public class FileUpload extends HttpServlet {  

private static final long serialVersionUID = 7630300784693118565L;

public static String path="";
public static String script_location="G:/eclipse_workspace/DisCo/PicCrypt.py";
public static String image_location="";
public static String key="asd";
public static int n=7;
public static int k=4;

public void doPost(HttpServletRequest request, HttpServletResponse response)  
    throws ServletException, IOException {  
  
	response.setContentType("text/html");  
	PrintWriter out = response.getWriter();

	Storage storage= new Storage();
	Random rand=new Random();
	ArrayList<String> devices = null;
	int r=rand.nextInt()%5;

	path="I:/files/"+Integer.toString(r);
	File theDir = new File(path);

	//if the directory does not exist, create it
	if (!theDir.exists()) {
		System.out.println("creating directory: " + Integer.toString(r));
		boolean result = false;

		try{
			theDir.mkdir();
			result = true;
		} 
		catch(SecurityException se){
			//handle it
		}        
		if(result) {    
			System.out.println("DIR created");  
		}
	}

	MultipartRequest m=new MultipartRequest(request,path,2393494); 
	Enumeration<?> files=m.getFileNames();

	String filename="";
	while(files.hasMoreElements())
	{
		String name=(String)files.nextElement();
		filename=m.getFilesystemName(name);
	}
	out.print("successfully uploaded " + filename);
	
	storage.setFilename(filename);
	storage.setUserID(1);
	storage.setFragmentCount(n);
	storage.setMinFragment(k);
	
	String s = null;
	image_location=path+"/"+filename;
	
    try {
           
        //Process p = Runtime.getRuntime().exec("python G:/eclipse_workspace/DisCo/PicCrypt.py -e I:/a.jpg I:/new asd 7 4");
        out.println("python "+ script_location +" -e "+ image_location + " " + path +" "+ key+" "+ n +" "+ k);
    	Process p = Runtime.getRuntime().exec("python "+ script_location +" -e "+ image_location + " " + path +" "+ key+" "+ n +" "+ k);
        
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
		StorageManager.uploadFile(storage);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    

    try {
		GCMManager.ping();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    try {
		Thread.sleep(10000);
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
    	notification.setUrl("http://10.50.46.126:8080/DisCo/FileDownloader?path=I:/files/"+r+"/"+i+".png");
    	
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
    	
        System.out.println(devices.get(i));
        
    	GCMManager.notify(devices.get(i), message);	
    	break;
    }
        
    out.print("success");  
	}  
}    