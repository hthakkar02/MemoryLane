package com.cs407.memorylane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class authTests extends AppCompatActivity {

    protected FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;

    /**
     * This method handles sign in and sign up via google.
     * It will also launch a pop up for google sign in/up.
     */
    protected void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Add this method to handle the result of the Google Sign-In

    /**
     * This method handles the result of the Google Sign-In
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign In Failed", "Google sign in failed", e);
                // Handle Google Sign-In failure here
            }
        }
    }

    /**
     * Helper method for handling google sign in result
     *
     *
     * @param idToken
     */
    private void firebaseAuthWithGoogle(String idToken) {
        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Google Sign In Success", "signInWithCredential:success");
                            // Handle Google Sign-In success here
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Update UI accordingly
                        } else {
                            Log.w("Google Sign In Failed", "signInWithCredential:failure", task.getException());
                            // Handle Google Sign-In failure here
                        }
                    }
                });
    }


    /**
     * Method dedicated to checking if a username is unique
     * @param username
     */
    interface UsernameCheckCallback {
        void onUsernameChecked(boolean isUnique);
    }

    protected void checkUsernameUniqueness(String username, UsernameCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userDataCollection = db.collection("User Data");

        // Check if the provided username already exists
        Query query = userDataCollection.whereEqualTo("Username", username);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    // Username already exists
                    callback.onUsernameChecked(false);
                } else {
                    // Username is unique
                    callback.onUsernameChecked(true);
                }
            } else {
                // Handle the query failure for checking existing username
                Log.e("Check Username", "Error checking username existence: " + task.getException().getMessage());
                // Pass false indicating non-uniqueness due to query failure
                callback.onUsernameChecked(false);
            }
        });
    }



    /**
     * Sign up user via email and password
     *
     * @param email
     * @param password
     * @param username
     */
    protected CompletableFuture<Boolean> signUpUser(Context context, String email, String password, String username) {
        mAuth = FirebaseAuth.getInstance();
        CompletableFuture<Boolean> signUpFuture = new CompletableFuture<>();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Successful User Sign up", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Write contents of user to a if needed

                        signUpFuture.complete(true); // Sign-up successful
                    } else {
                        Toast.makeText(context, "Failed to Sign up", Toast.LENGTH_SHORT).show();
                        Log.w("Failed User Sign up", "createUserWithEmail:failure", task.getException());
                        signUpFuture.complete(false); // Sign-up failed
                    }
                });


        return signUpFuture;
    }
    protected void sendPasswordResetEmail(Context context, String email) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Reset instructions sent to your email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    /**
     *
     */
    protected void createNewUserDuringSignUp(String username, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the "User Data" collection
        CollectionReference collectionReference = db.collection("User Data");

        // Create a map representing the new user data
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("Email", email);
        newUser.put("Username", username);
        List<String> friendsList = new ArrayList<>();
        newUser.put("Friends", friendsList);
        List<String> friendsRequestList = new ArrayList<>();
        newUser.put("Friend Request", friendsRequestList);

        // Add the new document to the "User Data" collection
        collectionReference.add(newUser)
                .addOnSuccessListener(documentReference -> {
                    // Document added with ID: documentReference.getId()
                    Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                    dataTest dT = dataTest.getInstance();
                    //dataTest.storeUserIDToSharedPreferences(documentReference.getId());

                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error adding document", e);
                });
    }





    /**
     * Sign in user via email and password.
     *
     * @returns true when sign in is a success
     *
     * @param email
     * @param password
     */
    protected CompletableFuture<Boolean> signInUser(Context context, String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        CompletableFuture<Boolean> signInFuture = new CompletableFuture<>();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Successful User Sign In", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Write contents of user to a

                        signInFuture.complete(true); // Sign-in successful
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(context, "Failed to sign-in", Toast.LENGTH_SHORT).show();
                        Log.w("Failed User Sign In", "signInWithEmail:failure", task.getException());

                        signInFuture.complete(false); // Sign-in failed
                    }
                });

        return signInFuture;
    }


    /**
     * This method gets back a specified users info
     *
     * TODO: change user to be a parameter of the function
     */
    protected void accessUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_tests);



        //setContentView(R.layout.authTest);


        //signUpUser("mehul@gmail.com", "hehehaha");
        // signInUser("mehul@gmail.com", "hehehaha");
        googleSignIn();
        //TODO add method to extract user data

    }
}