package com.cs407.memorylane;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private FrameLayout appGuideOverlay;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private final float DEFAULT_ZOOM = 15;

    private RadioGroup modeSelector;

    private double geoBounds[] = new double[4];

    private int mode = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map); // Make sure you have a <fragment> with id="@+id/map" in your fragment_map.xml
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        appGuideOverlay = rootView.findViewById(R.id.appGuideOverlay);

        ImageButton btnAppGuide = rootView.findViewById(R.id.btnAppGuide);
        btnAppGuide.setOnClickListener(v -> toggleAppGuideOverlay());

        ImageButton btnUserProfile = rootView.findViewById(R.id.btnUserProfile);
        btnUserProfile.setOnClickListener(v -> navigateToUserProfile());

        modeSelector = rootView.findViewById(R.id.modeSelector);
        modeSelector.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.globalMode) {
                mode = 2;
                createMarkers();
            } else if (checkedId == R.id.friendsMode) {
                mode = 1;
                createMarkers();
            } else if (checkedId == R.id.privateMode) {
                mode = 0;
                createMarkers();
            }
        });
        return rootView;
    }

    private void createMarkers() {
        dataTest dT = dataTest.getInstance();
        mMap.clear();
        // Log the bounds of the visible region
        LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //Min Lat
        geoBounds[0] = visibleBounds.southwest.latitude;
        //Max Lat
        geoBounds[1] = visibleBounds.northeast.latitude;
        //Min Lon
        geoBounds[2] = visibleBounds.southwest.longitude;
        //Max Lon
        geoBounds[3] = visibleBounds.northeast.longitude;

        Log.d("MapClick", "Visible Region - MinLat: " + geoBounds[0] +
                ", MaxLat: " + geoBounds[1] +
                ", MinLong: " + geoBounds[2] +
                ", MaxLong: " + geoBounds[3]);

        if (mode == 0) {
            dT.loadImagesFromUser(geoBounds, getActivity(), new dataTest.OnImagesLoadedListener() {
                @Override
                public void onImagesLoaded(ArrayList<String> imagePaths) {
                    Log.d("ArrayList", imagePaths.toString());
                }

                @Override
                public void onCentroidsCalculated(Map<String, LatLng> centroids) {
                    for (Map.Entry<String, LatLng> entry : centroids.entrySet()) {
                        String groupKey = entry.getKey(); // The group key
                        LatLng centroid = entry.getValue(); // The centroid LatLng

                        // Create a marker at the centroid
                        Marker marker = mMap.addMarker(new MarkerOptions().position(centroid).title("Group: " + groupKey));

                        // Set the group key as the tag of the marker
                        marker.setTag(groupKey);
                    }
                }
            }, key -> {
                // Handle individual image download callback
            });
        } else if (mode == 1) {
            dT.loadFriendImages(geoBounds, getActivity(), new dataTest.OnImagesLoadedListener() {
                @Override
                public void onImagesLoaded(ArrayList<String> imagePaths) {
                    Log.d("ArrayList", imagePaths.toString());
                }

                @Override
                public void onCentroidsCalculated(Map<String, LatLng> centroids) {
                    for (Map.Entry<String, LatLng> entry : centroids.entrySet()) {
                        String groupKey = entry.getKey(); // The group key
                        LatLng centroid = entry.getValue(); // The centroid LatLng

                        // Create a marker at the centroid
                        Marker marker = mMap.addMarker(new MarkerOptions().position(centroid).title("Group: " + groupKey));

                        // Set the group key as the tag of the marker
                        marker.setTag(groupKey);
                    }
                }
            }, key -> {
                // Handle individual image download callback
            });
        } else {
            dT.loadGlobalImages(geoBounds, new dataTest.OnImagesLoadedListener() {
                @Override
                public void onImagesLoaded(ArrayList<String> imagePaths) {
                    Log.d("ArrayList", imagePaths.toString());
                }

                @Override
                public void onCentroidsCalculated(Map<String, LatLng> centroids) {
                    for (Map.Entry<String, LatLng> entry : centroids.entrySet()) {
                        String groupKey = entry.getKey(); // The group key
                        LatLng centroid = entry.getValue(); // The centroid LatLng

                        // Create a marker at the centroid
                        Marker marker = mMap.addMarker(new MarkerOptions().position(centroid).title("Group: " + groupKey));

                        // Set the group key as the tag of the marker
                        marker.setTag(groupKey);
                    }
                }
            }, key -> {
                // Handle individual image download callback
            });
        }
    }

    private void toggleAppGuideOverlay() {
        if (appGuideOverlay.getVisibility() == View.GONE) {
            appGuideOverlay.setVisibility(View.VISIBLE);
        } else {
            appGuideOverlay.setVisibility(View.GONE);
        }
    }

    private void navigateToUserProfile() {
        Fragment userProfileFragment = new UserProfileFragment(); // Assuming you have created this
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, userProfileFragment); // Replace with your container ID
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        dataTest dT = dataTest.getInstance();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
             createMarkers();
            }
        });



        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(getActivity(), ImageSlideshowActivity.class);
            String groupKey = (String) marker.getTag();
            ArrayList<String> imagePathsForGroup = (ArrayList<String>) dT.getImagesForGroup(groupKey); // Get images for the group

            intent.putExtra("GROUP_KEY", groupKey);
            intent.putStringArrayListExtra("IMAGE_PATHS", imagePathsForGroup);

            LatLng position = marker.getPosition();
            intent.putExtra("LATITUDE", position.latitude);
            intent.putExtra("LONGITUDE", position.longitude);

            startActivity(intent);
            return true;
        });

    }

    public boolean onMarkerClick(Marker marker) {

    Intent intent = new Intent(getActivity(), ImageSlideshowActivity.class);

    startActivity(intent);

    return true;
    }
//        // Replace with the desired fragment
//        Fragment newFragment = new SlideshowInfoFragment(); // Replace with your target fragment
//
//        // Perform the fragment transaction
//        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, newFragment); // Replace 'your_fragment_container' with your actual container ID
//        transaction.addToBackStack(null); // Add this transaction to the back stack (optional)
//        transaction.commit();
//
//        return true; // Return true to indicate that we have handled this event
//    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location lastKnownLocation = task.getResult();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                        LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        // Add a marker at the current location
                    } else {
                        Log.d("MapFragment", "Current location is null. Using defaults.");
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                // Enable zoom controls
                mMap.getUiSettings().setZoomControlsEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 0);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }


}