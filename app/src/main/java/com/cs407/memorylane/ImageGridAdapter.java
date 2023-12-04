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

import java.util.ArrayList;
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

    private List<Uri> selectedUris = new ArrayList<>();

    // Interface for image selection
    interface OnImageSelectedListener {
        void onImageSelected(Uri uri);
    }

    private OnImageSelectedListener listener;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        holder.bind(imageUri);

        holder.itemView.setOnClickListener(v -> {
            if (selectedUris.contains(imageUri)) {
                selectedUris.remove(imageUri);
            } else {
                selectedUris.add(imageUri);
            }
            notifyItemChanged(position);
            if (listener != null) {
                listener.onImageSelected(imageUri);
            }
        });

        // Visual indication of selection
        holder.itemView.setAlpha(selectedUris.contains(imageUri) ? 0.5f : 1.0f);
    }

    public List<Uri> getSelectedUris() {
        return selectedUris;
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

