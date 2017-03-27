package com.example.raksheet.majorproject;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.widget.Toast;

import com.example.raksheet.majorproject.clienttoserver.APIClient;
import com.example.raksheet.majorproject.clienttoserver.APIResponse;
import com.example.raksheet.majorproject.clienttoserver.APIResponseListner;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Raksheet on 24-09-2016.
 */
public class BackgroundService extends IntentService implements APIResponseListner<APIResponse> {

    public BackgroundService(){
        super("load-files-service");
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BackgroundService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Extract the receiver passed into the service
        ResultReceiver rec = intent.getParcelableExtra("receiver");
        // Extract additional values from the bundle
        sendToServer();

        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "files loaded");
        // Here we call send passing a resultCode and the bundle of extras
        rec.send(Activity.RESULT_OK, bundle);
    }

    public void sendToServer(){
        final ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>(1);
        //parameters.add(new BasicNameValuePair("emailid", userCredentials.getEmailId()));

        final APIClient client = new APIClient(APIClient.REQUEST_METHOD.POST,
                Constants.SERVER_HOST + "/", parameters,BackgroundService.this);
    }


    @Override
    public void onApiCallSucceded(APIResponse response) {
        Toast.makeText(getApplicationContext(),"api call succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onApiCallFailed() {
        Toast.makeText(getApplicationContext(),"api call failed",Toast.LENGTH_SHORT).show();
    }
}

