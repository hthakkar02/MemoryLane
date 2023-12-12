package com.cs407.memorylane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class dataTest extends AppCompatActivity {

    private static dataTest instance;

    // Private constructor to prevent instantiation
    private dataTest() {}

    // Static method to get the single instance of the class
    public static dataTest getInstance() {
        if (instance == null) {
            instance = new dataTest();
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_test);

    }

    // Example LruCache initialization in your activity or fragment
    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    int cacheSize = maxMemory / 8;
    LruCache<String, Bitmap> imageCache = new LruCache<>(cacheSize);

    private Map<String, List<String>> imageGroups = new HashMap<>();

    // Map to store image dates
    private HashMap<String, String> imageDates = new HashMap<>();

    // Map to store image locations
    private HashMap<String, GeoPoint> imageLocations = new HashMap<>();

    private HashMap<String, Integer> imageOrientations = new HashMap<>();



    // Method to retrieve the location for an image
    public GeoPoint getImageLocation(String imagePath) {
        return imageLocations.get(imagePath);
    }

    // Method to store the location for an image
    public void setImageLocation(String imagePath, GeoPoint location) {
        imageLocations.put(imagePath, location);
    }


    // Method to retrieve the date for an image
    public String getImageDate(String imagePath) {
        return imageDates.get(imagePath);
    }

    // Method to retrieve the date for an image
    public Integer getImageOrientation(String imagePath) {
        return imageOrientations.get(imagePath);
    }

    // Method to store the date for an image
    public void setImageDate(String imagePath, String date) {
        imageDates.put(imagePath, date);
    }

    // Function to add image to cache
    protected void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            imageCache.put(key, bitmap);
        }
    }

    public List<String> getImagesForGroup(String groupKey) {
        return imageGroups.getOrDefault(groupKey, new ArrayList<>());
    }



    // Function to retrieve image from cache
    protected Bitmap getBitmapFromCache(String key) {
        return imageCache.get(key);
    }


    // Function to download an image from Firebase Storage
    protected void downloadImage(int orientation, String imagePath, ImageDownloadedCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imagePath);

        // Download image into a Bitmap
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);
            // Cache the image
            addBitmapToCache(imagePath, rotatedBitmap);

            Log.d("ImageCache", "Image cached successfully"+imagePath);
            callback.onImageDownloaded(imagePath);
        }).addOnFailureListener(exception -> {
            // Handle errors
            exception.printStackTrace();
        });

    }

    private Bitmap rotateBitmap(Bitmap original, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return original;
        }
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
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

    public interface OnImagesLoadedListener {
        void onImagesLoaded(ArrayList<String> imagePaths);
        void onCentroidsCalculated(Map<String, LatLng> centroids);
    }

    public interface ImageDownloadedCallback {
        void onImageDownloaded(String key);
    }

    protected void loadFriendImages(double[] geoBounds, Context context, OnImagesLoadedListener listener, ImageDownloadedCallback imageDownloadedCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String owner = context.getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS).getString("userID", "User not logged in");
        DocumentReference ownerRef = db.collection("User Data").document(owner);
        imageLocations = new HashMap<>();
        imageDates = new HashMap<>();
        imageGroups.clear();

        ownerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> friendList = (List<String>) documentSnapshot.get("Friends"); // replace "Friends" with the actual field name
                if (friendList != null && !friendList.isEmpty()) {
                    List<DocumentReference> friendRefs = friendList.stream()
                            .map(friendId -> db.collection("User Data").document(friendId))
                            .collect(Collectors.toList());
                    db.collection("All Photos")
                            .whereIn("Owner", friendRefs)
                            .whereEqualTo("PrivacyLevel", "Friends Only")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                    if (documents.size() == 0) {
                                        listener.onImagesLoaded(new ArrayList<>()); // No images to load
                                        return;
                                    }

                                    AtomicInteger completedDownloads = new AtomicInteger(0);
                                    for (DocumentSnapshot document : documents) {
                                        String path = document.getString("Path");
                                        GeoPoint location = document.getGeoPoint("Location");
                                        String date = document.getString("Date");
                                        int orientation = document.getLong("Orientation").intValue();

                                        if (location != null) {
                                            imageLocations.put(path, location); // Store image location
                                            String groupKey = findGroupKeyForLocation(location, geoBounds);
                                            imageGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(path);
                                        }

                                        if (date != null)
                                            imageDates.put(path, date);

                                        downloadImage(orientation, path, key -> {
                                            imageDownloadedCallback.onImageDownloaded(key);
                                            if (completedDownloads.incrementAndGet() == documents.size()) {
                                                // All images have been downloaded and cached
                                                listener.onImagesLoaded(new ArrayList<>(imageCache.snapshot().keySet()));
                                                Map<String, LatLng> centroids = calculateCentroids(imageGroups, imageLocations);
                                                listener.onCentroidsCalculated(centroids);
                                            }
                                        });
                                    }
                                } else {
                                    Log.d("ERRORING", "Error getting documents: ", task.getException());
                                }
                            });
                } else {
                    listener.onImagesLoaded(new ArrayList<>()); // No friends to load images from
                }
            } else {
                listener.onImagesLoaded(new ArrayList<>()); // Owner document does not exist
            }
        }).addOnFailureListener(e -> {
            Log.d("ERRORING", "Error getting owner document: ", e);
        });
    }


    protected void loadGlobalImages(double[] geoBounds, OnImagesLoadedListener listener, ImageDownloadedCallback imageDownloadedCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        imageLocations = new HashMap<>();
        imageDates = new HashMap<>();
        imageGroups.clear();

        db.collection("All Photos")
                .whereEqualTo("PrivacyLevel", "Global")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.size() == 0) {
                            listener.onImagesLoaded(new ArrayList<>()); // No images to load
                            return;
                        }

                        AtomicInteger completedDownloads = new AtomicInteger(0);
                        for (DocumentSnapshot document : documents) {
                            String path = document.getString("Path");
                            GeoPoint location = document.getGeoPoint("Location");
                            String date = document.getString("Date");
                            int orientation = document.getLong("Orientation").intValue();

                            imageOrientations.put(path, orientation);

                            if (location != null) {
                                imageLocations.put(path, location); // Store image location
                                String groupKey = findGroupKeyForLocation(location, geoBounds);
                                imageGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(path);
                            }

                            if (date != null)
                                imageDates.put(path, date);

                            downloadImage(orientation, path, key -> {
                                imageDownloadedCallback.onImageDownloaded(key);
                                if (completedDownloads.incrementAndGet() == documents.size()) {
                                    // Call listeners after all images are processed
                                    listener.onImagesLoaded(new ArrayList<>(imageCache.snapshot().keySet()));
                                    Map<String, LatLng> centroids = calculateCentroids(imageGroups, imageLocations);
                                    listener.onCentroidsCalculated(centroids);
                                }
                            });
                        }
                    } else {
                        Log.d("ERRORING", "Error getting documents: ", task.getException());
                    }
                });
    }


    protected void loadImagesFromUser(double[] geoBounds, Context context, OnImagesLoadedListener listener, ImageDownloadedCallback imageDownloadedCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String owner = context.getSharedPreferences("MyPrefs", MODE_PRIVATE | MODE_MULTI_PROCESS).getString("userID", "User not logged in");
        DocumentReference ownerRef = db.collection("User Data").document(owner); // Create a reference to the owner document
        imageLocations = new HashMap<>();
        imageDates = new HashMap<>();
        imageGroups.clear();
        // Map to store centroids of each group

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
                                    GeoPoint location = document.getGeoPoint("Location");
                                    String date = document.getString("Date");
                                    Integer orientation = Objects.requireNonNull(document.getDouble("Orientation")).intValue();


                                    if (location != null) {
                                        // Sort the image into the proper group based on location
                                        imageLocations.put(path, location);
                                        String groupKey = findGroupKeyForLocation(location, geoBounds);
                                        imageGroups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(path);
                                    }


                                    if (date != null)
                                        imageDates.put(path, date);

                                    downloadImage(orientation, path, key -> {
                                        imageDownloadedCallback.onImageDownloaded(key);
                                        if (completedDownloads.incrementAndGet() == totalImages) {
                                            // All images have been downloaded and cached
                                            listener.onImagesLoaded(new ArrayList<>(imageCache.snapshot().keySet()));
                                        }
                                    });
                                }

//                                if (completedDownloads.get() == totalImages) {
                                    // Calculate centroids here after all images are loaded
                                    Map<String, LatLng> centroids = calculateCentroids(imageGroups, imageLocations);
                                    listener.onCentroidsCalculated(centroids);


//                                Log.d("Important Images:", ""+imageGroups.toString());
                            } else {
                        Log.d("ERRORING", "Error getting documents: ", task.getException());
                    }
                });
    }

    private Map<String, LatLng> calculateCentroids(Map<String, List<String>> imageGroups, Map<String, GeoPoint> imageLocations) {
        Map<String, LatLng> centroids = new HashMap<>();

        // Iterate through each group
        for (Map.Entry<String, List<String>> entry : imageGroups.entrySet()) {
            String groupKey = entry.getKey();
            List<String> imagePaths = entry.getValue();

            double totalLat = 0.0;
            double totalLng = 0.0;
            int count = 0;

            // Calculate the sum of latitudes and longitudes for the images in the group
            for (String path : imagePaths) {
                GeoPoint location = imageLocations.get(path);
                if (location != null) {
                    totalLat += location.getLatitude();
                    totalLng += location.getLongitude();
                    count++;
                }
            }

            // Calculate the centroid for the group
            if (count > 0) {
                LatLng centroid = new LatLng(totalLat / count, totalLng / count);
                centroids.put(groupKey, centroid);
            }
        }

        return centroids;
    }


    private String findGroupKeyForLocation(GeoPoint location, double[] geoBounds) {
        int latIndex = (int)((location.getLatitude() - geoBounds[0]) / ((geoBounds[1] - geoBounds[0]) / 10));
        int lngIndex = (int)((location.getLongitude() - geoBounds[2]) / ((geoBounds[3] - geoBounds[2]) / 10));
        return "Group_" + latIndex + "_" + lngIndex;
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
    protected void uploadLocalPhoto(Context context, Uri fileUri, String privacyLevel) {
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
                        addPhotoToCollection(context, "images/" + destinationFileName, fileUri, privacyLevel);
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
    protected void addPhotoToCollection(Context context, String referencePath, Uri fileUri, String privacyLevel) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = db.collection("All Photos");
        GeoPoint location = null;
        String imageDate = null;
        int orientation = 0;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri); // 'uri' is the Uri of your image
            if (inputStream != null) {
                ExifInterface exifInterface = new ExifInterface(inputStream);
                float[] latLong = new float[2];
                boolean hasLatLong = exifInterface.getLatLong(latLong);
                orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                if (hasLatLong) {
                    float latitude = latLong[0];
                    float longitude = latLong[1];
                    // Use latitude and longitude as needed
                    location = new GeoPoint(latitude, longitude);

                    imageDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                    if (imageDate != null && !imageDate.isEmpty()) {
                        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                        SimpleDateFormat targetFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
                        try {
                            Date date = originalFormat.parse(imageDate);
                            if (date != null) {
                                imageDate = targetFormat.format(date);
                            }
                        } catch (ParseException e) {
                            Log.e("Date Parsing", "Error parsing the date: " + imageDate, e);
                        }
                    }
                    inputStream.close();

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
        newPhoto.put("PrivacyLevel", privacyLevel);
        newPhoto.put("Orientation", orientation);
        if (imageDate != null) {
            newPhoto.put("Date", imageDate); // Add the date to the photo document
        } else {
            newPhoto.put("Date", "No Date Provided");
        }

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
    public String getStreetFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String fullAddress = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                fullAddress = formatFullAddress(address);
            } else {
                fullAddress = "No address found";
            }
        } catch (IOException e) {
            Log.e("StreetConverter", "Error getting address from coordinates", e);
        }

        return fullAddress;
    }

    private String formatFullAddress(Address address) {
        String addressname = "";
        if (address != null) {
            String streetNumber = address.getFeatureName(); // Often the street number
            String streetName = address.getThoroughfare(); // Street name

            if (streetNumber != null) {
                addressname+=streetNumber;
            }
            if (streetNumber != null && streetName != null) {
                addressname+= " ";
            }
            if (streetName != null) {
                addressname+=streetName;
            }
            if (addressname.equals("")) {
                return "Address not available";
            }
            return addressname;
        }
        return "Address not available";
    }


    public String getNeighborhoodFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String neighborhoodName = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Extract the neighborhood name
                neighborhoodName = addressLocality(address);
            } else {
                neighborhoodName = "No neighborhood found";
            }
        } catch (IOException e) {
            Log.e("NeighborhoodConverter", "Error getting neighborhood from coordinates", e);
        }

        return neighborhoodName;
    }

    private String addressLocality(Address address) {
        Log.d("NEIGHBORHOOD", ""+address);
        if (address != null && address.getLocality() != null) {
            return address.getLocality();
        } else {
            return "Locality not available";
        }
    }

}