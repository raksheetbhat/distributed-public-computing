package com.example.raksheet.majorproject.Storage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.raksheet.majorproject.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.raksheet.majorproject.Login.RegisterActivity.USER_REGISTRATION;
import static com.example.raksheet.majorproject.MainActivity.IP_ADDRESS;

/**
 * Created by Raksheet on 12-04-2017.
 */

public class DownloadActivity extends AppCompatActivity {
    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> values = new ArrayList<>();

    HashMap<String,Integer> storageMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_download);

        listView = (ListView) findViewById(R.id.storage_list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, values);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("click position: "+position);
                String filename = values.get(position);
                System.out.println("clicked filename: "+filename);
                int storageID = storageMap.get(filename);
                System.out.println("clicked storage id: "+storageID);
                new FetchFile().execute(String.valueOf(storageID));

            }
        });

        new FetchList().execute();

        listView.setAdapter(adapter);
    }

    private class FetchList extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... params) {
            postData();
            return null;
        }

        void postData() {
            SharedPreferences prefs1 = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
            String server_url = "http://"+prefs1.getString("ip_address","")+":8080/DisCo";
            String server = server_url+"/FetchUserFiles";

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(server);

            SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
            String deviceID = String.valueOf(prefs.getInt("userID",0));

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userID", deviceID));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                //httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);

                Gson gson = new Gson();
                List<Storage> list = gson.fromJson(responseString, new TypeToken<List<Storage>>(){}.getType());
                //Storage storage = gson.fromJson(responseString,Storage.class);

                for(Storage storage : list){
                    String name = storage.getFilename();
                    storageMap.put(name,storage.getStorageID());
                    values.add(name);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });


            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private class FetchFile extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            postData(params[0]);
            return null;
        }

        void postData(String storageID) {

            SharedPreferences prefs1 = getSharedPreferences(IP_ADDRESS,MODE_PRIVATE);
            String server_url = "http://"+prefs1.getString("ip_address","")+":8080/DisCo";
            String server = server_url+"/FragmentFetcher";

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(server);

            SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
            String deviceID = String.valueOf(prefs.getInt("userID",0));

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userID", deviceID));
                nameValuePairs.add(new BasicNameValuePair("storageID", storageID));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                //httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString);

                JSONObject json = new JSONObject(responseString);
                String link = json.getString("link");
                String filepath = downloadFile(Integer.parseInt(storageID),link);
                System.out.println("filepath: "+filepath);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(filepath)), "image/*");
                startActivity(intent);

            } catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    public String downloadFile(int storageID,String link){
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
            String filePath = baseDir + File.separator + storageID + ".png";
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
}
