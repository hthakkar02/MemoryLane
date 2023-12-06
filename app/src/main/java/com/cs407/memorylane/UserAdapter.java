package com.cs407.memorylane;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<String> userList;
    // Add a listener interface for button clicks (if needed)
    public interface OnFriendRequestClickListener {
        void onFriendRequestClick(String username);
    }
    private OnFriendRequestClickListener listener;

    // Constructor
    public UserAdapter(List<String> userList, OnFriendRequestClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = userList.get(position);
        holder.textView.setText(username);
        holder.button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRequestClick(username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvUsername);
            button = itemView.findViewById(R.id.btnSendRequest);
        }
    }
}

