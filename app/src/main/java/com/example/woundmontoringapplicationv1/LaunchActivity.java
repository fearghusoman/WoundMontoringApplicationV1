package com.example.woundmontoringapplicationv1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Launch screen activity as defined by Material Design
 */
public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);

        finish();
    }
}
