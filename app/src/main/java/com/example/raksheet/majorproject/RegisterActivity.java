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
 * Created by Raksheet on 07-11-2016.
 */

public class RegisterActivity extends AppCompatActivity {
    private static final String USER_REGISTRATION = "user_registration";
    Button registerButton;
    EditText usernameEdit,passwordEdit,ramEdit,storageEdit;
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
                SharedPreferences.Editor editor = getSharedPreferences(USER_REGISTRATION, MODE_PRIVATE).edit();
                editor.putString("username", usernameEdit.getText().toString());
                editor.putString("password", passwordEdit.getText().toString());
                editor.putString("ram_size", ramEdit.getText().toString());
                editor.putString("storage_size", storageEdit.getText().toString());
                editor.apply();

                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}
