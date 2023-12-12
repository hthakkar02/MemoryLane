package com.cs407.memorylane;

import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class ImageSlideshowActivity extends AppCompatActivity implements SlideshowInfoFragment.OnFragmentInteractionListener {

    private ViewPager viewPager;
    private SlideshowPagerAdapter pagerAdapter;

    private int position;

    ArrayList<String> imagePathsForGroup;

    private SlideshowInfoFragment currentInfoFragment;

    ImageButton menuButton;

    double lat;
    double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_slideshow);

        // Initialize views
        ImageButton backButton = findViewById(R.id.back_button);
        menuButton = findViewById(R.id.menu_up_button);
        viewPager = findViewById(R.id.slideshow_image);

        dataTest dT = dataTest.getInstance();
        // Load images and set up ViewPager
        String groupKey = getIntent().getStringExtra("GROUP_KEY");
        lat = getIntent().getDoubleExtra("LATITUDE", 0.0);
        lon = getIntent().getDoubleExtra("LONGITUDE", 0.0);
        imagePathsForGroup = getIntent().getStringArrayListExtra("IMAGE_PATHS");


        // Use the image paths directly
        SlideshowPagerAdapter pagerAdapter = new SlideshowPagerAdapter(this, imagePathsForGroup, dT);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        updateImageCounter(0);

        // Add onPageChangeListener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                updateImageCounter(position);
                if (menuButton.getVisibility() == View.INVISIBLE)
                    handleInfoFragmentUpdate(position); // Handle the fragment update or creation
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        // Set click listeners
        backButton.setOnClickListener(view -> finish());
        menuButton.setOnClickListener(view -> {
            menuButton.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SlideshowInfoFragment(lat, lon, imagePathsForGroup, position))
                    .commit();
        });
    }

    private void handleInfoFragmentUpdate(int newPosition) {
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag("SlideshowInfoFragment");
        if (existingFragment instanceof SlideshowInfoFragment) {
            // Update existing fragment
            currentInfoFragment = (SlideshowInfoFragment) existingFragment;
            currentInfoFragment.updateImageDetails(imagePathsForGroup, newPosition);
        } else {
            // Replace existing fragment with a new one
            currentInfoFragment = new SlideshowInfoFragment(lat, lon, imagePathsForGroup, newPosition);
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, currentInfoFragment, "SlideshowInfoFragment")
                    .commit();
        }
    }

    @Override
    public void onFragmentClosed() {
        ImageButton menuButton = findViewById(R.id.menu_up_button);
        if (menuButton != null) {
            menuButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateImageCounter(int position) {
        this.position = position;
        if (pagerAdapter != null) {
            TextView imageCounter = findViewById(R.id.photo_count);
            String counterText = (position + 1) + "/" + pagerAdapter.getCount();
            imageCounter.setText(counterText);
        }
    }
}


