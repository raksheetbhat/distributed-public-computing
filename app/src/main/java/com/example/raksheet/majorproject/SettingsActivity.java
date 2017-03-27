package com.example.raksheet.majorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Raksheet on 08-11-2016.
 */

public class SettingsActivity extends AppCompatActivity {
    private static final String USER_REGISTRATION = "user_registration";

    EditText usernameEdit,passwordEdit,ramEdit,storageEdit;
    Button settingsButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        usernameEdit = (EditText) findViewById(R.id.settings_username);
        passwordEdit = (EditText) findViewById(R.id.settings_password);
        ramEdit = (EditText) findViewById(R.id.settings_ram_size);
        storageEdit = (EditText) findViewById(R.id.settings_storage_space);
        settingsButton = (Button)findViewById(R.id.settings_submit);

        SharedPreferences prefs = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String password = prefs.getString("password", null);
        String ramSize = prefs.getString("ram_size", null);
        String storageSize = prefs.getString("storage_size", null);

        usernameEdit.setText(username);
        passwordEdit.setText(password);
        ramEdit.setText(ramSize);
        storageEdit.setText(storageSize);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE).edit();
                editor.putString("username", usernameEdit.getText().toString());
                editor.putString("password", passwordEdit.getText().toString());
                editor.putString("ram_size", ramEdit.getText().toString());
                editor.putString("storage_size", storageEdit.getText().toString());
                editor.commit();

                finish();
            }
        });

    }
}
