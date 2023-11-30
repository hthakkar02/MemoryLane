package com.cs407.memorylane;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ViewHolder> {

    private List<Uri> imageUris;
    private Context context;

    public ImageGridAdapter(List<Uri> imageUris, Context context) {
        this.imageUris = imageUris;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.bind(imageUri);
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }

        void bind(Uri imageUri) {
            Log.d("ViewHolder", "Binding image: " + imageUri.toString());
            Glide.with(itemView.getContext())
                    .load(imageUri)
                    .into(imageView);
        }
    }
}

