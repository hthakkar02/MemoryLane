package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String assetFileName = "Free_Sunset_Background_Vector.png";
        String destinationFileName = "uploaded_image.png";

        try {
            // Open asset file descriptor
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open(assetFileName);

            // Create a temporary file in internal storage
            File internalFile = new File(getFilesDir(), destinationFileName);
            FileOutputStream outputStream = new FileOutputStream(internalFile);

            // Copy the content from assets to the internal file
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            // Close streams
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // Get Uri for the internal file
            Uri fileUri = Uri.fromFile(internalFile);

            // Create a reference to the location in Firebase Storage where the file will be uploaded
            StorageReference imageRef = storageRef.child("images/" + destinationFileName);

            // Upload the file to Firebase Storage
            imageRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Handle successful upload
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            Log.d("FirebaseUpload", "Download URL: " + downloadUrl);
                            // Use the URL as needed
                        });
                    })
                    .addOnFailureListener(exception -> {
                        // Handle unsuccessful upload
                        Log.e("FirebaseUpload", "Upload failed: " + exception.getMessage());
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}