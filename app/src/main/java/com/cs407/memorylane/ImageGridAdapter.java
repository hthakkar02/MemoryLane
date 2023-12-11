package com.cs407.memorylane;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
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

    private OnItemSelectedListener itemSelectedListener;

    public interface OnItemSelectedListener {
        void onItemSelected(boolean isAnyItemSelected);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }
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

        // Calculate the size of the ImageView to create a square
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels / 3; // for 3 columns grid
        ViewGroup.LayoutParams layoutParams = holder.imageView.getLayoutParams();
        layoutParams.height = width; // This will make the height equal to the width
        holder.imageView.setLayoutParams(layoutParams);

        // Set selection state
        final boolean isSelected = selectedUris.contains(imageUri);
        holder.selectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.imageView.setAlpha(isSelected ? 0.5f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            boolean isSelected1;
            if (selectedUris.contains(imageUri)) {
                selectedUris.remove(imageUri);
                isSelected1 = !selectedUris.isEmpty();
            } else {
                selectedUris.add(imageUri);
                isSelected1 = true;
            }
            notifyItemChanged(position);
            if (itemSelectedListener != null) {
                itemSelectedListener.onItemSelected(isSelected1);
            }
        });
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
        ImageView selectedOverlay;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
        }

        void bind(Uri imageUri) {
            Glide.with(itemView.getContext())
                    .load(imageUri)
                    .into(imageView);
        }
    }
}

