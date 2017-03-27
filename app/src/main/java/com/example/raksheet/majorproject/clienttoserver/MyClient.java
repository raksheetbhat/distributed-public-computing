package com.example.raksheet.majorproject.clienttoserver;

import android.os.AsyncTask;
import android.util.Log;

import com.example.raksheet.majorproject.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by sylumani on 03-07-2015.
 */

//This class was written by an intern to make http calls to the servlet, as during the initial stage, there were problems with the APIClient call
public class MyClient extends AsyncTask<Void,Void,String>
    {
        String st="Success";
        //String f="failed";
        char s;
        private List<? extends NameValuePair> parameters;
        public MyClient(List<? extends NameValuePair> parameters, char s){
            this.parameters=parameters; this.s=s;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            // replace with your url
            HttpPost httpPost;
           // if(s=='p') httpPost = new HttpPost("http://54.86.165.69:8000/test-jetty/save/user");
            if(s=='p') httpPost = new HttpPost("http://192.168.1.15:8080/populatelistservlet");
            else  httpPost = new HttpPost(Constants.SERVER_HOST_IMAGEFETCH + "save/post");
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(parameters));
            } catch (UnsupportedEncodingException e) {
                // log exception
                e.printStackTrace();
            }

            //making POST request.
            try {
                HttpResponse response = httpClient.execute(httpPost);
                // write response to log

                InputStream inputStream = response.getEntity().getContent();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();

                String bufferedStrChunk = null;

                while((bufferedStrChunk = bufferedReader.readLine()) != null){
                    stringBuilder.append(bufferedStrChunk);
                }
                Log.d("Http Post Response:", response.toString());
                System.out.println("Received content"+stringBuilder.toString());
                //If it works, check the format of string builder being printed, and process it and
                //And inititalise the contents(lists) required in phone_manu_fail2 screen and populate accordingly.
                return stringBuilder.toString();


            } catch (ClientProtocolException e) {
                // Log exception
                e.printStackTrace();
            } catch (IOException e) {
                // Log exception
                e.printStackTrace();
            }
            return st;
        }
}
