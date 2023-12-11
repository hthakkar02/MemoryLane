package com.cs407.memorylane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, homeActivity.class);

                // Check if fields are not empty
                if (!emptyFieldsCheck()) {
                    return;
                }

                // Get user input
                TextView emailTextView = findViewById(R.id.emailInput);
                TextView passwordTextView = findViewById(R.id.passwordInput);
                String email = emailTextView.getText().toString().toLowerCase();
                String password = passwordTextView.getText().toString();

                // Do login
                authTests authTests = new authTests();
                authTests.signInUser(email, password)
                        .thenAccept(isSignInSuccessful -> {
                            if (isSignInSuccessful) {
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
                            } else {
                                // Code to execute when sign-in fails
                                Log.e("User Sign In Error", "Error during sign-in");
                            }
                        });
            }
        });

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);

                TextView emailTextView = findViewById(R.id.emailInput);
                TextView passwordTextView = findViewById(R.id.passwordInput);

                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();

                intent.putExtra("email", email);
                intent.putExtra("password", password);

                startActivity(intent);
            }
        });
    }

    /**
     * checks fields for login
     *
     * @returns false when fields are empty
     */
    /**
     * Checks fields for login
     *
     * @returns false when fields are empty
     */
    private boolean emptyFieldsCheck() {
        // Check if fields are not empty
        TextView emailTextView = findViewById(R.id.emailInput);
        TextView passwordTextView = findViewById(R.id.passwordInput);

        String email = emailTextView.getText().toString().trim();
        String password = passwordTextView.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Log.e("Missing info", "Either the password or email is missing");
            return false;
        }
        return true;
    }

}