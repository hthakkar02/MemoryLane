package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;


import com.google.android.gms.tasks.Tasks;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class dataTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_test);

    }

    // Example LruCache initialization in your activity or fragment
    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    int cacheSize = maxMemory / 8;
    LruCache<String, Bitmap> imageCache = new LruCache<>(cacheSize);

    // Function to add image to cache
    protected void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            imageCache.put(key, bitmap);
        }
    }

    // Function to retrieve image from cache
    protected Bitmap getBitmapFromCache(String key) {
        return imageCache.get(key);
    }


    // Function to download an image from Firebase Storage
    protected void downloadImage(String imagePath, ImageDownloadedCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imagePath);

        // Download image into a Bitmap
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            // Cache the image
            addBitmapToCache(imagePath, bitmap);

            Log.d("ImageCache", "Image cached successfully");
            callback.onImageDownloaded(imagePath);
        }).addOnFailureListener(exception -> {
            // Handle errors
            exception.printStackTrace();
        });

    }

    public interface UsernameSearchCallback {
        void onSearchCompleted(List<String> usernames);
    }

    public void searchUsername(String searchString, UsernameSearchCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userDataCollection = db.collection("User Data");

        userDataCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> foundUsernames = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String username = document.getString("Username");
                    if (username != null && username.toLowerCase().contains(searchString.toLowerCase())) {
                        foundUsernames.add(username);
                    }
                }
                callback.onSearchCompleted(foundUsernames);
            } else {
                Log.e("USERNAME SEARCH", "Error searching for username: ", task.getException());
            }
        });
    }



//    protected List<String> searchUserByUsernameSubstring(String substring) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference userDataCollection = db.collection("User Data");
//
//        try {
//            QuerySnapshot querySnapshot = Tasks.await(
//                    userDataCollection.whereArrayContains("Username", substring).get()
//            );
//
//            List<String> matchingUsernames = new ArrayList<>();
//
//            for (QueryDocumentSnapshot document : querySnapshot) {
//                // Assuming "Username" is the field in your document
//                String foundUsername = document.getString("Username");
//                if (foundUsername != null) {
//                    Log.d("USERNAME FOUND", "Here is the username that matched: "+foundUsername);
//                    matchingUsernames.add(foundUsername);
//                }
//            }
//
//            return matchingUsernames;
//        } catch (Exception e) {
//            // Handle exceptions here
//            return new ArrayList<>();
//        }
//    }

    public interface OnImagesLoadedListener {
        void onImagesLoaded(ArrayList<String> imagePaths);
    }

    public interface ImageDownloadedCallback {
        void onImageDownloaded(String key);
    }

    protected void loadImagesFromUser(Context context, OnImagesLoadedListener listener, ImageDownloadedCallback imageDownloadedCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String owner = context.getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS).getString("userID", "User not logged in");
        DocumentReference ownerRef = db.collection("User Data").document(owner); // Create a reference to the owner document

        Log.d("STATUS", "Getting here");

        db.collection("All Photos")
                .whereEqualTo("Owner", ownerRef)
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                int totalImages = documents.size();
                                if (totalImages == 0) {
                                    listener.onImagesLoaded(new ArrayList<>()); // No images to load
                                }

                                AtomicInteger completedDownloads = new AtomicInteger(0);
                                for (DocumentSnapshot document : documents) {
                                    String path = document.getString("Path");
                                    downloadImage(path, key -> {
                                        imageDownloadedCallback.onImageDownloaded(key);
                                        if (completedDownloads.incrementAndGet() == totalImages) {
                                            // All images have been downloaded and cached
                                            listener.onImagesLoaded(new ArrayList<>(imageCache.snapshot().keySet()));
                                        }
                                    });
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
    protected void storeUserIDToSharedPreferences(String userId) {
        // Saves user data to shared preferences till app terminates
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userID", userId);
        editor.apply();
    }


    /**
     * Method to retrieve the information for one person via their userID
     *
     * @returns an arraylist as follows: [username, memories made]
     */
    protected void retrieveUserInfo(String userID, UserInfoCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference userDataCollection = db.collection("User Data");
        DocumentReference userDocument = userDataCollection.document(userID);

        userDocument.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Document exists, retrieve the username
                    String username = document.getString("Username");
                    Log.d("Profile Info Test", "Username: " + username);

                    // Retrieve ownerRef after getting username
                    DocumentReference ownerRef = db.collection("User Data").document(userID);

                    // Retrieve memories made from all photos
                    db.collection("All Photos")
                            .whereEqualTo("Owner", ownerRef)
                            .get()
                            .addOnCompleteListener(photoTask -> {
                                if (photoTask.isSuccessful()) {
                                    int totalMemories = photoTask.getResult().size();
                                    ArrayList<String> daInfo = new ArrayList<>();
                                    daInfo.add(username); // Add username to the ArrayList
                                    daInfo.add(String.valueOf(totalMemories)); // Add totalMemories to the ArrayList
                                    callback.onUserInfoRetrieved(daInfo);
                                } else {
                                    Log.e("Firestore", "Error getting photos: ", photoTask.getException());
                                }
                            });
                } else {
                    // Document does not exist
                    Log.d("Firestore", "No such document");
                }
            } else {
                // Handle errors
                Log.e("Firestore", "Error getting user data: ", task.getException());
            }
        });
    }

    public void sendFriendRequest(Context context, String friendUsername) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get owner's userID from SharedPreferences
        String ownerUserId = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userID", "User not logged in");
        if (ownerUserId.equals("User not logged in")) {
            Log.e("FriendRequest", "Owner user ID is not logged in or unavailable.");
            return;
        }

        // Reference to the "User Data" collection
        CollectionReference userDataCollection = db.collection("User Data");

        // Search for the user with the given username
        userDataCollection.whereEqualTo("Username", friendUsername).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Assuming 'Username' field uniquely identifies a user
                    DocumentReference friendDocRef = document.getReference();

                    // Add the current user's ID to the friend's "Friend Request" array
                    friendDocRef.update("Friend Request", FieldValue.arrayUnion(ownerUserId))
                            .addOnSuccessListener(aVoid -> Log.d("FriendRequest", "Friend request sent to: " + friendUsername))
                            .addOnFailureListener(e -> Log.e("FriendRequest", "Error updating document", e));
                }
            } else {
                Log.e("USERNAME SEARCH", "Error searching for username: ", task.getException());
            }
        });
    }
    interface UserInfoCallback {
        void onUserInfoRetrieved(ArrayList<String> daInfo);
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
                        // Toast message for successful upload
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful upload
                    Log.e("FirebaseUpload", "Upload failed: " + exception.getMessage());
                    // Toast message for failed upload
                    Toast.makeText(context, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
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
        newPhoto.put("Owner", db.collection("User Data").document(context.getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS).getString("userID", "User not logged in")));
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