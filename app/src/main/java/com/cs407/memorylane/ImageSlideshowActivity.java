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
import androidx.viewpager.widget.ViewPager;

public class ImageSlideshowActivity extends AppCompatActivity implements SlideshowInfoFragment.OnFragmentInteractionListener {

    private ViewPager viewPager;
    private SlideshowPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_slideshow);

        // Initialize views
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton menuButton = findViewById(R.id.menu_up_button);
        viewPager = findViewById(R.id.slideshow_image);

        // Load images and set up ViewPager
        dataTest dT = new dataTest();
        dT.loadImagesFromUser(this, imagePaths -> {
            Log.d("ArrayList", imagePaths.toString());
            pagerAdapter = new SlideshowPagerAdapter(this, imagePaths, dT);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(0);
            updateImageCounter(0);
        }, key -> {
            // This is where you can handle UI updates if necessary
            // when each individual image is downloaded and cached.
        });

        // Add onPageChangeListener
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                updateImageCounter(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        // Set click listeners
        backButton.setOnClickListener(view -> finish());
        menuButton.setOnClickListener(view -> {
            menuButton.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SlideshowInfoFragment())
                    .commit();
        });
    }

    @Override
    public void onFragmentClosed() {
        ImageButton menuButton = findViewById(R.id.menu_up_button);
        if (menuButton != null) {
            menuButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateImageCounter(int position) {
        if (pagerAdapter != null) {
            TextView imageCounter = findViewById(R.id.photo_count);
            String counterText = (position + 1) + "/" + pagerAdapter.getCount();
            imageCounter.setText(counterText);
        }
    }
}


