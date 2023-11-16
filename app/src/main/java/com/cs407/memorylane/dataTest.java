package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class dataTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_test);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the "User Data" collection
        CollectionReference collectionReference = db.collection("User Data");

        // Retrieve all documents in the collection
        collectionReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Access each document here
                    String documentId = document.getId();
                    Map<String, Object> data = document.getData();

                    // Print document ID
                    Log.d("Firestore", "Document ID: " + documentId);

                    // Print document data
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        Log.d("Firestore", entry.getKey() + ": " + entry.getValue());
                    }
                }
            } else {
                Log.e("Firestore", "Error getting documents: " + task.getException());
            }
        });


    }
}