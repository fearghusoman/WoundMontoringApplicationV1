package com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.MainActivities.HomeActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity.SHARED_PREFS;

/**
 * This activity prompts the user to confirm that they wish to log out of the
 * application. If they chose to do so the Firebase signout method is used
 */
public class LogoutActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;

    Button button;

    FirebaseAuth firebaseAuth;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        floatingActionButton = findViewById(R.id.backToMenu);
        button = findViewById(R.id.button_logout);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("message", "yourdatafragment");
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("email", "");
                editor.putString("status", "logged_out");
                editor.apply();

                //also set the firebase listener to signOut
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
