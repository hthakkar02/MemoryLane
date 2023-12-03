package com.cs407.memorylane;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;


public class homeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switchToFragment(item.getItemId());
            return true;
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // Initialize with the map fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapsFragment())
                    .commit();
        }
    }

    private void switchToFragment(int itemId) {
        Fragment newFragment = null;

        if (itemId == R.id.navigation_friends) {
            newFragment = new FriendListFragment();
        } else if (itemId == R.id.navigation_home) {
            newFragment = new MapsFragment();
        } else if (itemId == R.id.navigation_camera) {
            newFragment = new CameraFragment();
        } else if (itemId == R.id.navigation_upload) {
            newFragment = new UploadFragment();
        } else if (itemId == R.id.navigation_search) {
            newFragment = new SearchFragment();
        }

        if (newFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .commit();
        }
    }


}

