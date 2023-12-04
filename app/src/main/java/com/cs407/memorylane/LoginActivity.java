package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        Button loginButton = findViewById(R.id.loginButton); // Assuming you have a TextView to switch to the login page
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, homeActivity.class);

                //check fields are not empty
                TextView emailTextView = findViewById(R.id.emailInput);
                TextView passwordTextView = findViewById(R.id.passwordInput);

                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();

                if(email == "" || password == ""){
                    Log.e("Missing info","either the password or email is missing");
                }


                //do login
                authTests authTests = new authTests();

                authTests.signInUser(email, password)
                        .thenAccept(isSignInSuccessful -> {
                            if (isSignInSuccessful) {
                                // Code to execute when sign-in is successful
                                Log.d("User Type", "");
                                startActivity(intent);
                                finish();
                            } else {
                                // Code to execute when sign-in fails
                                Log.e("User Sign In Error", "Error during sign-in");
                            }
                        });

            }
        });

        Button signUpButton = findViewById(R.id.signUpButton); // Assuming you have a TextView to switch to the sign-up page
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}