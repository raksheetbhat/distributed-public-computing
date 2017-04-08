package com.example.raksheet.majorproject.Login;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.app.ActivityCompat;
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

import static com.example.raksheet.majorproject.Login.RegisterActivity.USER_REGISTRATION;
import static com.example.raksheet.majorproject.MainActivity.IP_ADDRESS;

/**
 * Created by Raksheet on 07-11-2016.
 */

public class LoginActivity extends Activity {
    private static final String LOADING_PREFERENCES = "loading_preferences";
    private String url_user = BeanService.server_url+"/Login";
    private String url_device = BeanService.server_url+"/DeviceRegistration";

    public static String ip = "10.100.52.200";

    Button loginButton,registerButton;
    EditText usernameEdit,passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        SharedPreferences.Editor editor = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE).edit();
        editor.putString("ip_address",ip);
        editor.apply();

        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);
        usernameEdit = (EditText) findViewById(R.id.login_id);
        passwordEdit = (EditText) findViewById(R.id.login_password);

        SharedPreferences mPrefs = LoginActivity.this.getSharedPreferences(LOADING_PREFERENCES,MODE_PRIVATE);
        boolean firstTime = mPrefs.getBoolean("FirstTime", true);
        if(!firstTime){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }

        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                1);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast.makeText(LoginActivity.this,"No internet",Toast.LENGTH_SHORT).show();
                }else if(usernameEdit.getText().toString().isEmpty() || passwordEdit.getText().toString().isEmpty()){
                    Toast.makeText(LoginActivity.this,"Fields cannot be empty",Toast.LENGTH_SHORT).show();
                }else{
                    String userName = usernameEdit.getText().toString();
                    String password = passwordEdit.getText().toString();

                    SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE).edit();
                    editor.putString("username", userName);
                    editor.putString("password", password);
                    editor.apply();

                    RegisterMaster registerMaster = new RegisterMaster(userName,password);
                    new UploadUserData().execute(registerMaster);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

    }

    private class UploadUserData extends AsyncTask<RegisterMaster,Void,Void> {

        @Override
        protected Void doInBackground(RegisterMaster... params) {
            postToServer(params[0]);
            return null;
        }
    }

    private void postToServer(RegisterMaster registerMaster){
        SharedPreferences prefs = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
        String server_url = "http://"+prefs.getString("ip_address","")+":8080/DisCo";
        String url_user = server_url+"/Login";

        // Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP Post
        HttpPost httpPost = new HttpPost(url_user);

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("username", registerMaster.getUserName()));
        nameValuePair.add(new BasicNameValuePair("password", registerMaster.getPassword()));
        nameValuePair.add(new BasicNameValuePair("action", "login"));

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
                    final String msg = result.getString("message");
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this,msg,Toast.LENGTH_SHORT).show();
                        }
                    });
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
        SharedPreferences prefs = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
        String server_url = "http://"+prefs.getString("ip_address","")+":8080/DisCo";
        String url_device = server_url+"/DeviceRegistration";

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

                SharedPreferences.Editor prefsEditor = getSharedPreferences(LOADING_PREFERENCES,MODE_PRIVATE).edit();
                prefsEditor.putBoolean("FirstTime",false);
                prefsEditor.apply();

                startActivity(new Intent(LoginActivity.this,MainActivity.class));
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
