package manager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;

import pojo.Notification;

public class GCMManager {

	public static String serverKey="AIzaSyAuf-bxJ0jx0KUoKLtMxOhnlmQ5UldFWFQ";
	public static String senderID="889397844500";
	
	public static void notify(ArrayList<String> id,Message message) throws IOException
	{
		Sender sender = new  Sender(serverKey);
		MulticastResult multicastResult = sender.send(message, id, 0);
		System.out.println("Multicast Message Result: " + multicastResult.toString());
	}
	
	public static void notify(String id,Message message) throws IOException
	{
		Sender sender = new  Sender(serverKey);
		Result result = sender.send(message,id,0);        
        System.out.println("Message Result: "+result.toString());
	}
	
	public static void ping() throws IOException, SQLException
	{
		Sender sender = new  Sender(serverKey);
	    
		Notification notification = new Notification();
    	
    	notification.setType("ping");
    	notification.setUrl("http://10.50.46.126:8080/DisCo/OnlineStatus");
    	
    	Gson gson = new Gson();
    	String json = gson.toJson(notification);
		
		Message message = new Message.Builder()
                 						.collapseKey("message")
                 						.timeToLive(3)
                 						.delayWhileIdle(true)
                 						.addData("message", "Sending data")
                 						.addData("title", "Major Project Notification")
                 						.addData("data",json)//you can get this message on client side app
                 						.build(); 
	     
	    ArrayList<String> id = DeviceManager.getGCMID();
		MulticastResult multicastResult = sender.send(message, id, 0);
		System.out.println("Multicast Message Result: " + multicastResult.toString());
	}
	
	
}
