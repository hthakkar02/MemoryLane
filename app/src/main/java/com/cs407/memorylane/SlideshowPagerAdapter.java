package com.cs407.memorylane;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;


public class SlideshowPagerAdapter extends PagerAdapter {

    private Context context;

    //For testing purpose only
    private String[] photoUrls;

    public SlideshowPagerAdapter(Context context, String[] photoUrls) {
        this.context = context;
        this.photoUrls = photoUrls;
    }

    @Override
    public int getCount() {
        return photoUrls.length;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_slide, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        // Load and display the photo using Picasso (replace with your image loading library)
        Picasso.get().load(photoUrls[position]).into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
