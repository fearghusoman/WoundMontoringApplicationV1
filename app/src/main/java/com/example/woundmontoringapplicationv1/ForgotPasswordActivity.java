package com.example.woundmontoringapplicationv1;

import android.content.Intent;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.cketti.mailto.EmailIntentBuilder;

/**
 *
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;

    Button button;

    EditText editText;

    String emailHolder;

    String emailBody = "Hello there, we are sorry you've misplaced your password. Follow the link below to reset your password:";

    String emailSubject = "Password Reset: ";
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        floatingActionButton = findViewById(R.id.backToMenu);
        button = findViewById(R.id.btn_forgotpassword);
        editText = findViewById(R.id.plain_text_input_email);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailHolder = editText.getText().toString().trim();

                //set the text of the email's body
                String body = emailBody;

                emailSubject = emailSubject + " " + emailHolder;

                /**
                //create the email send intent
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + emailHolder +
                                                "&subject=" + Uri.encode(emailSubject) +
                                                "&body=" + Uri.encode(body)));

                try{
                    startActivity(emailIntent);
                }
                catch(ActivityNotFoundException e){
                    e.printStackTrace();
                }
                 **/

                //using emailBuilder
                boolean success = EmailIntentBuilder.from(getApplicationContext())
                                        .to(emailHolder)
                                        .subject(emailSubject)
                                        .body(body)
                                        .start();
            }
        });
    }
}
