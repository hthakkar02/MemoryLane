package com.cs407.memorylane;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    /**
     * Tested with loadImageReferenceFromUser("/User Data/user000001");
     *
     * @param owner is the unique identifier for the user in the All Users collection
     */
    protected void loadImageReferenceFromUser(String owner) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ownerRef = db.document(owner); // Create a reference to the owner document

        Log.d("STATUS", "Getting here");


        db.collection("All Photos")
                .whereEqualTo("Owner", ownerRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String path = document.getString("Path");
                            // Retrieve the path to the image in Firebase Storage and display it in your app
                            Log.d("PhotoData", "This is the path: " + path);
                        }
                    } else {
                        Log.d("ERRORING", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadImageReferenceFromUser("/User Data/user000001");
    }
}