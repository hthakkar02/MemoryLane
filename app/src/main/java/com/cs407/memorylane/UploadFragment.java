package com.cs407.memorylane;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

        return view;
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
        if (ContextCompat.checkSelfPermission(getContext(), "android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED) {
            loadImages();
        } else {
            requestPermissions(new String[]{"android.permission.READ_MEDIA_IMAGES"}, PERMISSIONS_REQUEST_READ_MEDIA_IMAGES);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages();
            } else {
                // Explain to the user that the feature is unavailable because the permission is denied
            }
        }
    }





}
