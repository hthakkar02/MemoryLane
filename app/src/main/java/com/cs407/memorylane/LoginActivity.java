package com.cs407.memorylane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

        TextView forgotPassword = findViewById(R.id.forgotPasswordText);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptResetPassword();
            }
        });

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
                authTests.signInUser(LoginActivity.this, email, password)
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

                                
                                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS);
                                String storedData = preferences.getString("key", "Default value if key not found");

                                startActivity(intent);
                                finish();
                            } else {
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
    private void promptResetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email to receive reset instructions");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", (dialog, which) -> resetPassword(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void resetPassword(String email) {
        authTests aT = new authTests();
        aT.sendPasswordResetEmail(this, email);
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
            Toast.makeText(this, "Email or password is empty", Toast.LENGTH_SHORT).show();
            Log.e("Missing info", "Either the password or email is missing");
            return false;
        }
        return true;
    }

}