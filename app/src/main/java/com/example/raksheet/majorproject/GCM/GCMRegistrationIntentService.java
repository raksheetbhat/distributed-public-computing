package com.example.raksheet.majorproject.GCM;

/**
 * Created by Raksheet on 05-02-2017.
 */

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import static com.example.raksheet.majorproject.Login.RegisterActivity.USER_REGISTRATION;
import static com.example.raksheet.majorproject.MainActivity.IP_ADDRESS;

public class GCMRegistrationIntentService extends IntentService {
    //Constants for success and errors
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";
    private String server = BeanService.server_url+"/UpdateGCM";

    //Class constructor
    public GCMRegistrationIntentService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
        int deviceID = prefs.getInt("deviceID",0);
        //Registering gcm to the device
        if(deviceID!=0) registerGCM();
    }

    private void registerGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successful registration
        String token = null;
        try {
            //Creating an instanceid
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

            //Getting the token from the instance id
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            //Displaying the token in the log so that we can copy it to send push notification
            //You can also extend the app by storing the token in to your server
            Log.w("GCMRegIntentService", "token:" + token);

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            //Putting the token to the intent
            registrationComplete.putExtra("token", token);

            new UploadToken().execute(token);
        } catch (Exception e) {
            //If any error occurred
            Log.w("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private class UploadToken extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

        void postData(String token) {

            SharedPreferences prefs1 = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
            String server_url = "http://"+prefs1.getString("ip_address","")+":8080/DisCo";
            String server = server_url+"/UpdateGCM";

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(server);

            SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
            String deviceID = String.valueOf(prefs.getInt("deviceID",0));

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("deviceID", deviceID));
                nameValuePairs.add(new BasicNameValuePair("GCMID", token));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                //httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}