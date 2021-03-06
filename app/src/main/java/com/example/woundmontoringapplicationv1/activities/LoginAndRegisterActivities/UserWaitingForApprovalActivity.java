package com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.woundmontoringapplicationv1.R;

/**
 * This activity  is called when a user attempts to log into an account
 * that has been registered, but not yet approved by the clinician through the
 * web application.
 */
public class UserWaitingForApprovalActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_waiting_for_approval);

        button = findViewById(R.id.btn_login);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
