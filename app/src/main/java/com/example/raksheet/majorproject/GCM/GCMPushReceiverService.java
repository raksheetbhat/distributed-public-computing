package com.example.raksheet.majorproject.GCM;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.raksheet.majorproject.Database.DatabaseHandler;
import com.example.raksheet.majorproject.MainActivity;
import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.Process.ClientTask;
import com.example.raksheet.majorproject.Process.TaskClass;
import com.example.raksheet.majorproject.Process.TaskMaster;
import com.example.raksheet.majorproject.R;
import com.example.raksheet.majorproject.Storage.StorageMaster;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.example.raksheet.majorproject.Login.RegisterActivity.USER_REGISTRATION;

//Class is extending GcmListenerService
public class GCMPushReceiverService extends GcmListenerService {

    public static final String SERVER_URL = "server url";
    private ProgressDialog pDialog;

    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //check type of message received
        Gson gson = new Gson();
        com.example.raksheet.majorproject.GCM.Notification notification = gson.fromJson(data.getString("data"),
                com.example.raksheet.majorproject.GCM.Notification.class);

        String type = notification.getType();

        SharedPreferences.Editor editor = getSharedPreferences(SERVER_URL, MODE_PRIVATE).edit();
        editor.putString("url", notification.getUrl());
        editor.apply();

        if(type!=null){
            switch (type) {
                case "ping":
                    Log.i("PushService", "control in ping");
                    String post_url = notification.getUrl();
                    postToServer(post_url);
                    break;
                case "store":
                    Log.i("PushService", "control in store");
                    String subtype = notification.getSubType();
                    Log.i("URL", notification.getUrl());

                    String filePath = "";
                    if (subtype.equalsIgnoreCase("receive")) {
                        //new DownloadFileFromURL().execute(notification);
                        filePath = downloadFile(notification);
                        Log.i("finalPath", filePath);
                        File file = new File(filePath);
                        StorageMaster storageMaster = new StorageMaster(notification.getStorageID(), notification.getFragmentID(),
                                notification.getFileName(), file.length() / 1024);

                        //write to database
                        DatabaseHandler databaseHandler = new DatabaseHandler(getApplication());
                        Log.i("Databasehandler", "writing to database");
                        databaseHandler.addStorageUser(storageMaster);
                        databaseHandler.close();

                    }
                    break;
                case "compute":
                    Log.i("PushService", "control in compute");
                    post_url = notification.getUrl();

                    //response is list of jobs to run on user phone
                    JSONArray response = postToServer(post_url);
                    ArrayList<ClientTask> clientsArray = new ArrayList<>();

                    List<String> paths = new ArrayList<>();
                    System.out.println("task response: "+response);

                    if(response!=null && response.length()>0) {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject j = (JSONObject) response.get(i);
                                Gson g = new Gson();
                                ClientTask ct = g.fromJson(j.toString(), ClientTask.class);
                                clientsArray.add(ct);
                                String link = ct.getData() + "?taskID=" + ct.getTaskID();
                                paths.add(downloadFile(ct.getTaskID(), link));
                                System.out.println("path: " + i + " : " + paths.get(i));
                                System.out.println("inside compute: " + i + " , " + ct.getTaskID());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    DatabaseHandler databaseHandler = new DatabaseHandler(getApplication());
                    Log.i("Databasehandler", "writing to database");
                    int i=0;
                    for(ClientTask clientTask : clientsArray){
                        TaskMaster taskMaster = new TaskMaster(clientTask.getTaskID(),
                                clientTask.getCode(),paths.get(i),0,clientTask.getComplexity(),0);
                        databaseHandler.addTask(taskMaster);
                        i++;
                    }
                    int num = databaseHandler.getTaskCount();
                    databaseHandler.close();

                    for(int j=0;j<num;j++){
                        System.out.println("inside");
                        startService(new Intent(this, BeanService.class));
                    }

//                    //start the other service to run the tasks
//                    DatabaseHandler db = new DatabaseHandler(getApplication());
//                    for(TaskMaster t : db.getAllTasks()){
//                        System.out.println(t.getTaskID());
//                    }

            }
        }

        //Getting the message from the bundle
        String message = data.getString("message");
        String title = data.getString("title");

        Log.i("GCM bundle",data.toString());
        //Displaying a notification with the message
        sendNotification(title,message);

        //Parsing notification message
    }



    public String downloadFile(com.example.raksheet.majorproject.GCM.Notification file){
        int count;
        String finalFilePath = "";
        try {
            URL url = new URL(file.getUrl());
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String filePath = baseDir + File.separator + file.getFileName();
            OutputStream output = new FileOutputStream(filePath);

            finalFilePath = filePath;

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress(""+(int)((total*100)/lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return finalFilePath;
    }

    public String downloadFile(int taskID,String link){
        int count;
        String finalFilePath = "";
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            String filePath = baseDir + File.separator + taskID + "_original.csv";
            OutputStream output = new FileOutputStream(filePath);

            finalFilePath = filePath;

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                //publishProgress(""+(int)((total*100)/lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return finalFilePath;
    }

    //This method is generating a notification and displaying the notification
    private void sendNotification(String title,String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build()); //0 = ID of notification
    }

    private JSONArray postToServer(String url){
        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(url);

        SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
        String deviceID = String.valueOf(prefs.getInt("deviceID",0));

        DatabaseHandler db = new DatabaseHandler(this);
        int tasks = db.countRemainingTasks();

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("deviceID", deviceID));
        nameValuePair.add(new BasicNameValuePair("tasks", String.valueOf(tasks)));

        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // writing error to Log
            e.printStackTrace();
        }

        // Making HTTP Request
        try {
            HttpResponse response = httpClient.execute(httpPost);

            HttpEntity entity = response.getEntity();

            JSONArray result = new JSONArray();
            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                // parsing JSON
                result = new JSONArray(retSrc);
                System.out.println("result "+result);
            }
            return result;
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
