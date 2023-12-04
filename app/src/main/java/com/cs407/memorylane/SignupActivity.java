package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SignupActivity extends AppCompatActivity {
    TextView emailTextView;
    TextView passwordTextView;
    TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailTextView = findViewById(R.id.emailInput);
        passwordTextView = findViewById(R.id.passwordInput);
        usernameTextView = findViewById(R.id.usernameInput);

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra("email")) {
                String email = intent.getStringExtra("email");
                Log.d("Received email", email);
                if (!email.equals("")) {
                    emailTextView.setText(email);
                }
            }

            if (intent.hasExtra("password")) {
                String password = intent.getStringExtra("password");
                Log.d("Received password", "got password");
                if (!password.equals("")) {
                    passwordTextView.setText(password);
                }
            }
        }

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, homeActivity.class);

                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();
                String username = usernameTextView.getText().toString();

                authTests authTests = new authTests();

                authTests.signUpUser(email, password)
                        .thenAccept(isSignUpSuccessful -> {
                            if (isSignUpSuccessful) {
                                authTests.createNewUserDuringSignUp(username, email);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("User Sign Up Error", "Error during sign-up");
                            }
                        });

            }
        });
    }
}
