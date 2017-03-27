package com.example.raksheet.majorproject.Process;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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

/**
 * Created by Raksheet on 21-02-2017.
 */

public class BeanService extends Service {

    private String javaCode;
    private final static Interpreter interpreter = new Interpreter();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BeanService(String name) {

    }

    public BeanService(){

    }

    private void initBeanshell(String path) {
        try {
            interpreter.set("context", getApplication());//set any variable, you can refer to it directly from string
            //interpreter.set("rootView", findViewById(android.R.id.content));
            interpreter.set("path",path);
            //interpreter.set("button", findViewById(R.id.buttonRun));
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

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        //your code
        try {
            DatabaseHandler databaseHandler = new DatabaseHandler(getApplication());
            TaskMaster task = databaseHandler.fetchMaxTask();
            String code = task.getCode();
            String path = task.getData();

            initBeanshell(path);
            String result = runString(code);

            System.out.println("result: "+result);

            task.setStatus(1);
            task.setData(result);
            task.setCode(code);
            databaseHandler.updateTask(task);
            databaseHandler.close();
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        return START_STICKY;
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

//    @Override
//    protected void onHandleIntent(Intent intent) {
////        String javaCode = "import android.widget.Toast; \n" +
////                "import android.support.design.widget.Snackbar;\n " +
////                "import android.view.View;\n" +
////                "import android.view.View.OnClickListener; \n" +
////                "return s";
////
////        String jCode = "for(t:tasks){\n"+
////                "print(t.code + \" \" + t.data);}";
////
////        TaskClass t1 = new TaskClass("data1","code1");
////        TaskClass t2 = new TaskClass("data2","code2");
////        TaskClass t3 = new TaskClass("data3","code3");
////
////        initBeanshell(Arrays.asList(t1,t2,t3));
////        runString(jCode);
//
//        try {
//            DatabaseHandler databaseHandler = new DatabaseHandler(getApplication());
//            TaskMaster task = databaseHandler.fetchMaxTask();
//            String code = task.getCode();
//            String path = task.getData();
//
//            initBeanshell(path);
//            String result = runString(code);
//
//            task.setStatus(1);
//            task.setData(result);
//            task.setCode(code);
//            databaseHandler.updateTask(task);
//            databaseHandler.close();
//        }catch(NullPointerException e){
//            e.printStackTrace();
//        }
//    }
}
