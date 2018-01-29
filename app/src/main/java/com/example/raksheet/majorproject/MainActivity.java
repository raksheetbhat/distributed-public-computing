package com.example.raksheet.majorproject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.raksheet.majorproject.Database.DBTest;
import com.example.raksheet.majorproject.Database.DatabaseHandler;
import com.example.raksheet.majorproject.GCM.GCMRegistrationIntentService;
import com.example.raksheet.majorproject.Process.BeanService;
import com.example.raksheet.majorproject.Process.TaskMaster;
import com.example.raksheet.majorproject.Storage.StorageActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jaredrummler.android.device.DeviceName;

import android.Manifest.permission.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String USER_REGISTRATION = "user_registration";
    public static final String IP_ADDRESS = "ip address";

    Intent backService;
    private TextView contentText;

    CustomGauge ramGauge,storageGauge,publicStorageGauge;
    TextView ramGaugeText,storageGaugeText,publicStorageText;

    FloatingActionButton fab,fab1,fab2;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private Boolean isFabOpen = false;
    private RelativeLayout transparentBg;

    ImageView batteryImage,wifiImage,chargingImage;
    TextView batteryText,wifiText,chargingText;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        contentText = (TextView) findViewById(R.id.content_text_view);

        //floating action buttons
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        transparentBg = (RelativeLayout) findViewById(R.id.semi_white_bg);

        batteryImage = (ImageView) findViewById(R.id.battery_image);
        wifiImage = (ImageView) findViewById(R.id.wifi_image);
        chargingImage = (ImageView) findViewById(R.id.charging_image);
        batteryText = (TextView) findViewById(R.id.battery_text);
        wifiText = (TextView) findViewById(R.id.wifi_text);
        chargingText = (TextView) findViewById(R.id.charging_text);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,ProcessActivity.class));
                animateFAB();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,StorageActivity.class));
                animateFAB();
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        // service to load lists in arraylist
        backService = new Intent(getApplicationContext(),BackgroundService.class);
        //backService.putExtra("filepath", Environment.getExternalStorageDirectory().getPath());
        BackServiceReceiver registeredReceiver = new BackServiceReceiver(new Handler());
        backService.putExtra("receiver",registeredReceiver);
        registeredReceiver.setReceiver(new BackServiceReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    String resultValue = resultData.getString("resultValue");
                    getApplicationContext().stopService(backService);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // reading CPU usage
        //System.out.println(readUsage());
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        float availableMegs = mi.availMem / 1048576L;
        float totalMegs = mi.totalMem / 1048576L;
        float usedMegs = totalMegs - availableMegs;
        float percentRam = 270 - (availableMegs / totalMegs) * 270;

        // RAM gauge
        ramGauge = (CustomGauge) findViewById(R.id.ram_gauge);
        ramGaugeText = (TextView) findViewById(R.id.ram_text_view);
        ramGauge.setPointSize((int)percentRam);
        ramGaugeText.setText((int)usedMegs+"/"+(int)totalMegs+" MB");

        // Storage gauge
        SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE);
        String storageSize = prefs.getString("storage_size", null);
        int totalStorage = 300,usedStorage = 100;
        if(storageSize!=null)totalStorage = Integer.parseInt(storageSize);
        float percentStorage = ((float)usedStorage/totalStorage)*270;
        storageGauge = (CustomGauge) findViewById(R.id.storage_gauge);
        storageGaugeText = (TextView) findViewById(R.id.storage_text_view);
        storageGauge.setPointSize((int)percentStorage);
        storageGaugeText.setText(usedStorage+"/"+totalStorage+" MB");

        //public storage gauge
        int publicStorageUsed = 70,publicStorageTotal = 100;
        float percentPublic = ((float)publicStorageUsed/publicStorageTotal)*270;
        publicStorageGauge = (CustomGauge) findViewById(R.id.public_storage_gauge);
        publicStorageText = (TextView) findViewById(R.id.public_storage_text_view);
        publicStorageGauge.setPointSize((int)percentPublic);
        publicStorageText.setText(publicStorageUsed+"/"+publicStorageTotal+" MB");

        // conditions to start service
        if(getBatteryLevel() && connectedToWifi() && usedMegs > (0.2*totalMegs)){
            //start service

        }else{
            //stop service

        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //GCM set-up
        //Initializing our broadcast receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            //When the broadcast received
            //We are sending the broadcast from GCMRegistrationIntentService

            @Override
            public void onReceive(Context context, Intent intent) {
                //If the broadcast has received with success
                //that means device is registered successfully
                if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_SUCCESS)){
                    //Getting the registration token from the intent
                    String token = intent.getStringExtra("token");
                    //Displaying the token as toast
                    //Toast.makeText(getApplicationContext(), "Registration token:" + token, Toast.LENGTH_LONG).show();

                    //System.out.println(token);

                    //if the intent is not with success then displaying error messages
                } else if(intent.getAction().equals(GCMRegistrationIntentService.REGISTRATION_ERROR)){
                    Toast.makeText(getApplicationContext(), "GCM registration error!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_LONG).show();
                }
            }
        };

        //Checking play service is available or not
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        verifyStoragePermissions(this);

        //if play service is not available
        if(ConnectionResult.SUCCESS != resultCode) {
            //If play service is supported but not installed
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {
            //Starting intent to register device
            Intent intent = new Intent(this, GCMRegistrationIntentService.class);
            startService(intent);
        }

        Intent bean_intent = new Intent(this, BeanService.class);
        startService(bean_intent);

//        DatabaseHandler db = new DatabaseHandler(getApplication());
//        for(TaskMaster t : db.getAllTasks()){
//            System.out.println(t.getTaskID()+" , "+t.getPayload().getCode());
//        }

    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBatteryLevel();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(GCMRegistrationIntentService.REGISTRATION_ERROR));
    }

    public void animateFAB(){
        if(isFabOpen){
            transparentBg.setVisibility(View.GONE);
            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            transparentBg.setVisibility(View.VISIBLE);
            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }

    public boolean getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        float batteryLvl;
        if(level == -1 || scale == -1) {
            batteryLvl =  50.0f;
        }

        batteryLvl = ((float)level / (float)scale) * 100.0f;

        String message = "\nProperties: "+ "\nBattery level: " + batteryLvl + "\nCharging on A/C: " + acCharge + "\nCharging on USB: " + usbCharge
                + "\nConnected to wifi" + connectedToWifi();
        //contentText.setText(message);

        batteryImage.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        batteryText.setText(String.valueOf((int)batteryLvl)+"%");

        if(acCharge){
            //is charging
            chargingImage.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            chargingText.setText("Charging");
        }else{
            //not charging
            chargingImage.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            chargingText.setText("Not Charging");
        }

        if(connectedToWifi()){
            wifiImage.getDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
            wifiText.setText("Connected");
        }else{
            wifiImage.getDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
            wifiText.setText("Not connected");
        }

        return batteryLvl > 90 && acCharge;
    }

    public boolean connectedToWifi(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        }else if (id == R.id.action_info) {
            startActivity(new Intent(MainActivity.this,InformationActivity.class));
            return true;
        }else if (id == R.id.extras) {
            startActivity(new Intent(MainActivity.this,DBTest.class));
            return true;
        }else if(id == R.id.ip_address){
            final Dialog dialog = new Dialog(MainActivity.this);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Un-registering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Log.w("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }
}
