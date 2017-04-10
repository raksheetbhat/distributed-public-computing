package com.example.raksheet.majorproject.Login;

import android.app.ActivityManager;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.raksheet.majorproject.MainActivity;
import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.R;
import com.jaredrummler.android.device.DeviceName;

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

import static com.example.raksheet.majorproject.Login.LoginActivity.ip;
import static com.example.raksheet.majorproject.MainActivity.IP_ADDRESS;

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

        SharedPreferences.Editor editor = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE).edit();
        editor.putString("ip_address",ip);
        editor.apply();

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
                        //editor.putString("ram_size", ramEdit.getText().toString());
                        editor.putString("ram_size", "1000");
                        //editor.putString("storage_size", storageEdit.getText().toString());
                        editor.putString("storage_size", "100");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.ip_address_register){
            final Dialog dialog = new Dialog(RegisterActivity.this);
            dialog.setContentView(R.layout.ip_layout);
            final EditText ipEdit = (EditText) dialog.findViewById(R.id.ip_edit);
            Button cancel = (Button) dialog.findViewById(R.id.ip_cancel);
            Button accept = (Button) dialog.findViewById(R.id.ip_accept);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String ip_address = ipEdit.getText().toString();
                    SharedPreferences.Editor editor = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE).edit();
                    editor.putString("ip_address",ip_address);
                    editor.apply();
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        return super.onOptionsItemSelected(item);

    }

    private class UploadUserData extends AsyncTask<RegisterMaster,Void,Void>{

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

        final String[] modelName = {""};

        DeviceName.with(RegisterActivity.this).request(new DeviceName.Callback() {
            @Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
                String manufacturer = info.manufacturer;  // "Samsung"
                String name = info.marketName;            // "Galaxy S7 Edge"
                String model = info.model;                // "SAMSUNG-SM-G935A"
                String codename = info.codename;          // "hero2lte"
                String deviceName = info.getName();       // "Galaxy S7 Edge"
                // FYI: We are on the UI thread.
                modelName[0] = model;
            }
        });

        System.out.println("model name: "+modelName[0]);

        // Building post parameters
        // key and value pair
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair("userID", userID));
        nameValuePair.add(new BasicNameValuePair("mac", imei));
        nameValuePair.add(new BasicNameValuePair("model", modelName[0]));
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
