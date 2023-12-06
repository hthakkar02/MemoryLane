package com.cs407.memorylane;
//import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;


public class SlideshowPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> photoUrls;

    private dataTest dT;
    // Holds keys/identifiers for images in the cache

    public SlideshowPagerAdapter(Context context, ArrayList<String> photoUrls, dataTest dT) {
        this.context = context;
        this.photoUrls = photoUrls;
        this.dT = dT;
    }

    @Override
    public int getCount() {
        return photoUrls.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_slide, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);

        String cacheKey = photoUrls.get(position);
        Bitmap cachedImage = dT.getBitmapFromCache(cacheKey);
        if (cachedImage != null) {
            imageView.setImageBitmap(cachedImage);
        } else {
            Log.d("IMAGE ISSUE", "image not in cache");
        }

        dT.downloadImage(cacheKey, newKey -> {
            Bitmap newCachedImage = dT.getBitmapFromCache(newKey);
            if (newCachedImage != null) {
                imageView.setImageBitmap(newCachedImage);
            }
        });

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
