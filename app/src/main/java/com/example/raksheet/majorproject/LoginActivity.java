package com.example.raksheet.majorproject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Raksheet on 07-11-2016.
 */

public class LoginActivity extends Activity {
    private static final String LOADING_PREFERENCES = "loading_preferences";

    Button loginButton,registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.register_button);

        SharedPreferences mPrefs = LoginActivity.this.getSharedPreferences(LOADING_PREFERENCES,MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        boolean firstTime = mPrefs.getBoolean("FirstTime", true);
        if(!firstTime){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }
        prefsEditor.putBoolean("FirstTime",false);
        prefsEditor.apply();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

    }
}
