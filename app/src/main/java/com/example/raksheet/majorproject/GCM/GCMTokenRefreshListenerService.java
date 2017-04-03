package com.example.raksheet.majorproject.GCM;

/**
 * Created by Raksheet on 05-02-2017.
 */

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.iid.InstanceIDListenerService;

import static com.example.raksheet.majorproject.Login.RegisterActivity.USER_REGISTRATION;

public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    //If the token is changed registering the device again
    @Override
    public void onTokenRefresh() {
        SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION,MODE_PRIVATE);
        int deviceID = prefs.getInt("deviceID",0);

        if(deviceID != 0) {
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);
        }
    }
}
