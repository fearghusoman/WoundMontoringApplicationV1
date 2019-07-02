package com.example.woundmontoringapplicationv1.activities.MainActivities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity;

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
