package test;

import java.io.IOException;
import java.util.ArrayList;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;

import pojo.Notification;

public class Test {

    public static void main(String args[]) throws IOException {

    	{
            
                	String ServerKey="AIzaSyAuf-bxJ0jx0KUoKLtMxOhnlmQ5UldFWFQ";    	
                	String SenderID="889397844500";
                	String receid1="cZgoqsv_RHg:APA91bGCkknlqhol5W4f9HUDW_yE3RRsz1VM_JO-sJydzjd_ie74O0UcABC9_kAccKsf8HJj18FKeoSQOePGJCDjpk7XPHR83iE1TB-_nDkUMoTZRzEnjgSXJTIrtmjgrgQa_O5jA-6G";
                	String receid2="fnCui4pXgSY:APA91bERrLM-n_ZBQ_LKSg9esBg3lwp2awhTEKVvoGi1KKg4wMkeCGBtlKJ-TmjmnIPsAyg5IxDa1zRhamX1TO1FHPFpvm-zI8OhGF6bGRUPvAG583_IjnZooG0DlGeYXiolEANL6Fqu";
                	
                	ArrayList<String> devs= new ArrayList<String>();
                	
                	devs.add(receid1);
                	devs.add(receid2);
                	
                	
                	Notification notification = new Notification();
                	
                	notification.setType("ping");
                	notification.setFileName("abc.png");
                	notification.setStorageID(1);
                	notification.setFragmentID(1);
                	notification.setSubType("send");
                	notification.setUrl("http://10.50.46.126:8080/DisCo/OnlineStatus");
                	
                	Gson gson = new Gson();
                	
                	String json = gson.toJson(notification);
                	             	
                    Sender sender = new  Sender(ServerKey);

                   
                    // use this to send message with payload data
                    Message message = new Message.Builder()
                    .collapseKey("message")
                    .timeToLive(3)
                    .delayWhileIdle(true)
                    .addData("message", "Sending data")
                    .addData("title", "Major Project Notification")
                    .addData("data",json)//you can get this message on client side app
                    .build();  
  
                    
                    //Use this code to send notification message to a single device
                    
                    Result result = sender.send(message,receid1,0);
                    
                    System.out.println("Message Result: "+result.toString()); //Print message result on console

                    //MulticastResult multicastResult = sender.send(message, devs, 0);
                    //System.out.println("Message Result: "+multicastResult.toString());//Print multicast message result on console

                    //result.
                    
                    /*
                    
                    //Use this code to send notification message to multiple devices
                    ArrayList<String> devicesList = new ArrayList<String>();
                    //add your devices RegisterationID, one for each device                
                    devicesList.add("APA91bEbKqwTbvvRuc24vAYljcrhslOw-jXBqozgH8C2OB3H8R7U00NbIf1xp151ptweX9VkZXyHMik022cNrEETm7eM0Z2JnFksWEw1niJ2sQfU3BjQGiGMq8KsaQ7E0jpz8YKJNbzkTYotLfmertE3K7RsJ1_hAA");    
                    devicesList.add("APA91bEVcqKmPnESzgnGpEstHHymcpOwv52THv6u6u2Rl-PaMI4mU3Wkb9bZtuHp4NLs4snBl7aXXVkNn-IPEInGO2jEBnBI_oKEdrEoTo9BpY0i6a0QHeq8LDZd_XRzGRSv_R0rjzzZ1b6jXY60QqAI4P3PL79hMg");    

                    //Use this code for multicast messages    
                    */
            }
        }    
    }