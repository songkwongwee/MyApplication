package com.example.songkwongwee.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonSetting).setOnClickListener(new View.OnClickListener() {    //listener for settings button
            @Override
            public void onClick(View view) {
                settings();
            }
        });

        findViewById(R.id.buttonHost).setOnClickListener(new View.OnClickListener() {    //listener for settings button
            @Override
            public void onClick(View view) {
                host();
            }
        });

        findViewById(R.id.buttonChat).setOnClickListener(new View.OnClickListener() {    //listener for settings button
            @Override
            public void onClick(View view) {
                chat();
            }
        });
    }

    @Override   //Remembers if user is still logged in, bring to MainActivity if true
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();   //prevent user from being able to go back to login page
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void settings() {   //settings button action
        finish();
        startActivity(new Intent(this, ProfileActivity.class));
    }

    private void host() {   //settings button action
        finish();
        startActivity(new Intent(this, HostActivity.class));
    }

    private void chat() {   //settings button action
        finish();
        startActivity(new Intent(this, ChatActivity.class));
    }

}
