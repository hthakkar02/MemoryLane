package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class dataTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_test);

    }

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



    /**
     *
     */
    protected void createNewUserDocument() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the "User Data" collection
        CollectionReference collectionReference = db.collection("User Data");

        // Create a map representing the new user data
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("Email", "newuser@example.com");
        newUser.put("Name", "New User");
        newUser.put("UserID", "67890");
        newUser.put("Username", "newuser");
        List<String> friendsList = new ArrayList<>();
        friendsList.add("friend3@example.com");
        friendsList.add("friend4@example.com");
        newUser.put("Friends", friendsList);

        // Add the new document to the "User Data" collection
        collectionReference.add(newUser)
                .addOnSuccessListener(documentReference -> {
                    // Document added with ID: documentReference.getId()
                    Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error adding document", e);
                });
    }


    /**
     * Stores the userID till app terminates. userID = document name in user data collection from DB in firestore
     *
     * @param userId
     */
    protected void storeUserIDToSharedPreferences(String userId){
        // Saves user data to shared preferences till app terminates
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userID", userId);
        editor.apply();
    }

    /**
     *
     */
    protected void retrieveCollectionContent() {
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

    /**
     *
     */
    protected void uploadLocalPhoto(Context context, Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String destinationFileName = fileUri.getLastPathSegment(); // Adjust to extract the file name correctly
        StorageReference imageRef = storageRef.child("images/" + destinationFileName);

        imageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Handle successful upload
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        Log.d("FirebaseUpload", "Download URL: " + downloadUrl);
                        addPhotoToCollection(context, "images/" + destinationFileName, fileUri);
                        // Use the URL as needed
                    });
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful upload
                    Log.e("FirebaseUpload", "Upload failed: " + exception.getMessage());
                });
    }



    /**
     * @param referencePath
     * @param fileUri
     */
    protected void addPhotoToCollection(Context context, String referencePath, Uri fileUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("All Photos");
        GeoPoint location = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri); // 'uri' is the Uri of your image
            if (inputStream != null) {
                ExifInterface exifInterface = new ExifInterface(inputStream);
                float[] latLong = new float[2];
                boolean hasLatLong = exifInterface.getLatLong(latLong);
                Log.d("HenryLocation", ""+latLong[0]);
                Log.d("HENRYBOOLEAN", ""+hasLatLong);
                if (hasLatLong) {
                    float latitude = latLong[0];
                    float longitude = latLong[1];
                    // Use latitude and longitude as needed
                    location = new GeoPoint(latitude, longitude);

                } else {
                    // No location information available
                    Log.d("No Location", "Photo doesn't have a location");
                    location = new GeoPoint(0.0, 0.0);
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> newPhoto = new HashMap<>();
        newPhoto.put("Description", "This is a beautiful photo");
        newPhoto.put("Location", location);
        newPhoto.put("Owner", db.collection("User Data").document("user000001"));
        newPhoto.put("Path", referencePath);

        collectionReference.add(newPhoto)
                .addOnSuccessListener(documentReference -> {
                    // Document added with ID: documentReference.getId()
                    Log.d("Firestore", "Document added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e("Firestore", "Error adding document", e);
                });
    }

}