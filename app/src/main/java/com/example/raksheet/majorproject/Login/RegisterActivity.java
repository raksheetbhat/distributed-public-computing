package com.example.raksheet.majorproject.Login;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.raksheet.majorproject.MainActivity;
import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.R;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raksheet on 07-11-2016.
 */

public class RegisterActivity extends AppCompatActivity {
    public static final String USER_REGISTRATION = "user_registration";
    Button registerButton;
    EditText usernameEdit,passwordEdit,ramEdit,storageEdit;
    private String url_user = BeanService.server_url+"/Login";
    private String url_device = BeanService.server_url+"/DeviceRegistration";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        usernameEdit = (EditText) findViewById(R.id.register_username);
        passwordEdit = (EditText) findViewById(R.id.register_password);
        ramEdit = (EditText) findViewById(R.id.register_ram_size);
        storageEdit = (EditText) findViewById(R.id.register_storage_space);
        registerButton = (Button) findViewById(R.id.register_submit);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    if (usernameEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()) {
                        Toast.makeText(RegisterActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE).edit();
                        editor.putString("username", usernameEdit.getText().toString());
                        editor.putString("password", passwordEdit.getText().toString());
                        editor.putString("ram_size", ramEdit.getText().toString());
                        editor.putString("storage_size", storageEdit.getText().toString());
                        editor.apply();

                        RegisterMaster registerMaster = new RegisterMaster(usernameEdit.getText().toString(),
                                passwordEdit.getText().toString());
                        new UploadUserData().execute(registerMaster);
                    }
                }else{
                    Toast.makeText(RegisterActivity.this,"No internet",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class UploadUserData extends AsyncTask<RegisterMaster,Void,Void>{

        @Override
        protected Void doInBackground(RegisterMaster... params) {
            postToServer(params[0]);
            return null;
        }
    }

    private void postToServer(RegisterMaster registerMaster){
        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(url_user);

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("username", registerMaster.getUserName()));
        nameValuePair.add(new BasicNameValuePair("password", registerMaster.getPassword()));
        nameValuePair.add(new BasicNameValuePair("action", "create"));

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

            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                // parsing JSON
                System.out.println("response: "+retSrc);
                JSONObject result = new JSONObject(retSrc);
                boolean res = result.getBoolean("result");
                if(res){
                    //success
                    int userID = result.getInt("userID");
                    SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE).edit();
                    editor.putInt("userID",userID);
                    editor.apply();

                    new UploadDeviceData().execute(String.valueOf(userID));
                }else{
                    String msg = result.getString("message");
                    Toast.makeText(RegisterActivity.this,msg,Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private class UploadDeviceData extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            postToServer1(params[0]);
            return null;
        }
    }

    private void postToServer1(String userID){
        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(url_device);

        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        int totalMegs = (int) (mi.totalMem / 1048576L);

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("userID", userID));
        nameValuePair.add(new BasicNameValuePair("mac", imei));
        nameValuePair.add(new BasicNameValuePair("ram", String.valueOf(totalMegs)));

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

            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                // parsing JSON
                System.out.println("response: "+retSrc);
                JSONObject result = new JSONObject(retSrc);
                int deviceID = result.getInt("deviceID");
                SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE).edit();
                editor.putInt("deviceID",deviceID);
                editor.apply();

                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                finish();
            }
        } catch (ClientProtocolException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (IOException e) {
            // writing exception to log
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
