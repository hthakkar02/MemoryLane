package com.cs407.memorylane;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView selectedImage;

    public UploadFragment() {
        // Required empty public constructor
    }

    private RecyclerView recyclerView;
    private ImageGridAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        checkPermissionsAndLoadImages();

        adapter.setOnImageSelectedListener(uri -> {
            Log.d("Location Hopefully", "This shit works");
            InputStream inputStream = null; // 'uri' is the Uri of your image
            try {
                inputStream = getContext().getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (inputStream != null) {
                ExifInterface exifInterface = null;
                try {
                    exifInterface = new ExifInterface(inputStream);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                float[] latLong = new float[2];
                boolean hasLatLong = exifInterface.getLatLong(latLong);
                Log.d("HenryLocation", "" + latLong[0]);
                Log.d("HENRYBOOLEAN", "" + hasLatLong);
            }
        });

        // Assuming you have a button to trigger upload
        Button uploadButton = view.findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(v -> uploadSelectedImages());

        return view;
    }

    private void uploadSelectedImages() {
        List<Uri> selectedUris = adapter.getSelectedUris();
        for (Uri uri : selectedUris) {
            dataTest dT = new dataTest();
            dT.uploadLocalPhoto(getContext(), uri);
        }
    }

    private File uriToFile(Uri uri) {
        String filePath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        return filePath != null ? new File(filePath) : null;
    }

    private void extractLocationFromImage(Uri imageUri) {
        try (InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri)) {
            if (inputStream != null) {
                ExifInterface exifInterface = new ExifInterface(inputStream);
                float[] latLong = new float[2];
                boolean hasLatLong = exifInterface.getLatLong(latLong);
                if (hasLatLong) {
                    // Use latitude and longitude as needed
                    float latitude = latLong[0];
                    float longitude = latLong[1];
                    Log.d("ImageLocation", "Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Log.d("ImageLocation", "No location data available for this image");
                }
            }
        } catch (IOException e) {
            Log.e("ImageLocation", "Error reading EXIF data", e);
        }
    }


    private void loadImages() {
        List<Uri> imageUris = new ArrayList<>();
        String[] projection = new String[]{MediaStore.Images.Media._ID};

        try (Cursor cursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    extractLocationFromImage(contentUri);
                    imageUris.add(contentUri);
                }
            } else {
                Log.d("UploadFragment", "Cursor is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("UploadFragment", "Number of images: " + imageUris.size());
        adapter = new ImageGridAdapter(imageUris, getContext());
        recyclerView.setAdapter(adapter);
    }

    private static final int PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1;
    private void checkPermissionsAndLoadImages() {
        boolean hasReadMediaImagesPermission = ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED;
        boolean hasAccessMediaLocationPermission = ContextCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_MEDIA_LOCATION") == PackageManager.PERMISSION_GRANTED;

        if (hasReadMediaImagesPermission && hasAccessMediaLocationPermission) {
            loadImages();
        } else {
            List<String> permissionsToRequest = new ArrayList<>();
            if (!hasReadMediaImagesPermission) {
                permissionsToRequest.add("android.permission.READ_MEDIA_IMAGES");
            }
            if (!hasAccessMediaLocationPermission) {
                permissionsToRequest.add("android.permission.ACCESS_MEDIA_LOCATION");
            }
            requestPermissions(permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_READ_MEDIA_IMAGES);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && allPermissionsGranted(grantResults)) {
                loadImages();
            } else {
                // Explain to the user that the feature is unavailable because the permission is denied
            }
        }
    }

    private boolean allPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
