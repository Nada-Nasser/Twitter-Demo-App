package com.example.twitterdemoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
EditText etName;
    EditText etEmail;
    EditText etPassword;
    ImageView ivUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         etName=(EditText)findViewById(R.id.etName);
          etEmail=(EditText)findViewById(R.id.etEmail);
          etPassword=(EditText)findViewById(R.id.etPassword);
         ivUserImage=(ImageView) findViewById(R.id.ivUserImage);
        ivUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//TODO: load photo from gallary
             //   CheckUserPermsions();
            }
        });

//TODO: user register into firebase

    }

    public void buLogin(View view) {
//TODO: user login
    }


    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();

    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();

    }





}
