package com.cs407.memorylane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

                if (!emptyFieldsCheck()) {
                    return;
                }

                Intent intent = new Intent(SignupActivity.this, homeActivity.class);

                String email = emailTextView.getText().toString().toLowerCase();
                String password = passwordTextView.getText().toString();
                String username = usernameTextView.getText().toString();

                authTests authTests = new authTests();

                authTests.signUpUser(email, password)
                        .thenAccept(isSignUpSuccessful -> {
                            if (isSignUpSuccessful) {
                                authTests.createNewUserDuringSignUp(username, email);

                                {
                                    // Query Firestore for matching email
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    CollectionReference userData = db.collection("User Data");

                                    userData.whereEqualTo("Email", email).get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful() && task.getResult() != null) {
                                                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                                            String userDocId = document.getId();
                                                            Log.d("Document ID", userDocId);

                                                            // Saves user data to shared preferences till app terminates
                                                            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS);
                                                            SharedPreferences.Editor editor = preferences.edit();
                                                            editor.putString("userID", document.getId());
                                                            editor.apply();
                                                            Log.d("User Document ID saved to preferences", userDocId);
                                                        }
                                                    }
                                                }
                                            });

                                    // Continue with your existing code...
                                    SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS);
                                    String storedData = preferences.getString("key", "Default value if key not found");

                                    startActivity(intent);
                                    finish();
                                }

                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("User Sign Up Error", "Error during sign-up");
                            }
                        });

            }
        });
    }

    private boolean emptyFieldsCheck() {
        // Check if fields are not empty
        TextView emailTextView = findViewById(R.id.emailInput);
        TextView passwordTextView = findViewById(R.id.passwordInput);
        TextView usernameTextView = findViewById(R.id.usernameInput);

        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();
        String username = usernameTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Log.e("Missing info", "Either the password or email is missing");
            return false;
        }
        return true;
    }
}
