package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /**
     * Tested with loadImageReferenceFromUser("/User Data/user000001");
     * @param owner is the unique identifier for the user in the All Users collection
     */
    protected void loadImageReferenceFromUser(String owner){
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
                            Log.d("PhotoData", "This is the path: "+ path);
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