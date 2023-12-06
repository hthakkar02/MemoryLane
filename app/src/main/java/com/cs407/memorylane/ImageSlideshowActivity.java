package com.cs407.memorylane;

import android.os.Bundle;
import android.transition.Slide;
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
        setContentView(R.layout.image_slideshow); // Set the layout XML for the Activity

        //ViewPager photoImageView = findViewById(R.id.slideshow_image);
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton menuButton = findViewById(R.id.menu_up_button);
        TextView imageCounter = findViewById(R.id.photo_count);

        String[] photoUrls = {
                "https://images.fineartamerica.com/images/artworkimages/mediumlarge/3/spongebob-destiny-mendoza.jpg",
                "https://64.media.tumblr.com/tumblr_m0hmtp7spe1qbtwyto1_1280.jpg", "https://i.pinimg.com/736x/d6/70/4e/d6704e8577ccec97ca9a354d9abce057.jpg"
        };

        // Initialize the ViewPager and adapter
        viewPager = findViewById(R.id.slideshow_image);
        pagerAdapter = new SlideshowPagerAdapter(this, photoUrls);
        viewPager.setAdapter(pagerAdapter);

        // Set an initial position (e.g., show the first photo)
        viewPager.setCurrentItem(0);

        // Optional: Implement left and right swipe gestures
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateImageCounter(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });




        // Set click listeners for buttons or implement functionality as needed
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle back button click
                finish(); // Close the activity or navigate back as required
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuButton.setVisibility(View.INVISIBLE);
                // Handle menu button click by starting the MenuFragment
                SlideshowInfoFragment menuFragment = new SlideshowInfoFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, menuFragment)
                        .commit();
            }
        });

        // You can also programmatically update the image counter
        updateImageCounter(0); // Update the image counter text as needed
    }

    @Override
    public void onFragmentClosed() {
        ImageButton menuButton = findViewById(R.id.menu_up_button);
        if (menuButton != null) {
            menuButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateImageCounter(int position) {
        // Update the image counter text based on the current position
        TextView imageCounter = findViewById(R.id.photo_count);
        String counterText = (position + 1) + "/" + pagerAdapter.getCount();
        imageCounter.setText(counterText);
    }
}

