package com.example.raksheet.majorproject.Process;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.raksheet.majorproject.Database.DatabaseHandler;
import com.example.raksheet.majorproject.R;
import com.example.raksheet.majorproject.Storage.StorageActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.ParseException;
import bsh.TargetError;

import static com.example.raksheet.majorproject.GCM.GCMPushReceiverService.SERVER_URL;
import static com.example.raksheet.majorproject.MainActivity.IP_ADDRESS;

/**
 * Created by Raksheet on 21-02-2017.
 */

public class BeanService extends IntentService {

    public static String server_url = "http://192.168.1.102:8080/DisCo";

    private String javaCode;
    private final static Interpreter interpreter = new Interpreter();
    private String server = server_url+"/UpdateTask";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BeanService(String name) {
        super(name);
    }

    public BeanService(){
        super("");
    }

    private void initBeanshell(String readPath,String writePath) {
        try {
            interpreter.set("context", getApplication());//set any variable, you can refer to it directly from string
            interpreter.set("read_path",readPath);
            interpreter.set("write_path",writePath);
            if (interpreter.get("portnum") == null) { // server not set
                interpreter.set("portnum", 1234);
                interpreter.eval("setAccessibility(true)"); // turn off access restrictions
                interpreter.eval("server(portnum)");
            }
        } catch (TargetError e) {
            Throwable t = e.getTarget();
            Toast.makeText(BeanService.this, "ScriptThrewException:" + t, Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            Toast.makeText(BeanService.this, "ParseError:" + e.getErrorText(), Toast.LENGTH_SHORT).show();
        } catch (EvalError e) {
            Toast.makeText(BeanService.this, "EvalError:" + e.getErrorText(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {//handle exception
            e.printStackTrace();
        }
    }

    private String runString(String code) {
        try {
            System.out.println("entered beanshell");
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Compute started",Toast.LENGTH_SHORT).show();
                }
            });
            return (String) interpreter.eval(code);
        } catch (TargetError e) {
            Throwable t = e.getTarget();
            Toast.makeText(BeanService.this, "ScriptThrewException:" + t, Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            Toast.makeText(BeanService.this, "ParseError:" + e.getErrorText(), Toast.LENGTH_SHORT).show();
        } catch (EvalError e) {
            Toast.makeText(BeanService.this, "EvalError:" + e.getErrorText(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {//handle exception
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("bean service started");
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler(getApplication());
            TaskMaster task = databaseHandler.fetchMaxTaskRaw();
            databaseHandler.close();
            if(task!=null) System.out.println("task: "+task.getTaskID());
            if(task!=null) {
                String code = task.getCode();
                String readPath = task.getData();

                System.out.println("code: "+code);

                String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                String filePath = baseDir + File.separator + task.getTaskID() + "_changed.csv";

                File file = new File(filePath);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                String writePath = filePath;

                initBeanshell(readPath, writePath);
                long start = System.currentTimeMillis();
                String result = runString(code);
                long end = System.currentTimeMillis();
                System.out.println("time: "+(end-start));

                System.out.println("result: " + result);
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Compute ended",Toast.LENGTH_SHORT).show();
                    }
                });

                //write to db
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                task.setStatus(1);
                task.setData(writePath);
                task.setCode(code);
                db.updateTask(task);
                db.close();

                //send results back to server
                System.out.println("sending result to server: "+writePath);
                postData(new File(writePath),String.valueOf(task.getTaskID()));
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        try{
            //fetch tasks not sent to server and send
            DatabaseHandler db = new DatabaseHandler(this);
            List<TaskMaster> tasksUnsent = db.fetchRemainingTasks();
            if(tasksUnsent!=null && tasksUnsent.size()>0 && isNetworkAvailable()){
                for(TaskMaster taskMaster : tasksUnsent){
                    postData(new File(taskMaster.getData()),String.valueOf(taskMaster.getTaskID()));
                }
            }
            db.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void weightedQueueing(){

    }

    public void postData(File file,String taskID){
        try
        {
            SharedPreferences prefs = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
            String server_url = "http://"+prefs.getString("ip_address","")+":8080/DisCo";
            String server = server_url+"/UpdateTask";

            System.out.println("url: "+server);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(server);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addBinaryBody("uploadfile", file);
            entityBuilder.addPart("taskID",new StringBody(taskID, ContentType.TEXT_PLAIN));

            HttpEntity entity = entityBuilder.build();
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();

            String result = EntityUtils.toString(httpEntity);
            Log.v("post result", result);
            JSONObject res = new JSONObject(result);
            boolean sent = res.getBoolean("result");
            if(sent){
                //success
                DatabaseHandler db = new DatabaseHandler(this);
                TaskMaster taskMaster = db.getTask(Integer.parseInt(taskID));
                taskMaster.setSentToServer(1);
                db.updateTask(taskMaster);
                db.close();

                //delete original file from phone
//                if(file.exists()){
//                    String filename = file.getName();
//                    if(file.delete())System.out.println(filename + "deleted");
//                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
