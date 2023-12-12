package com.cs407.memorylane;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<String> userList;
    private Map<String, Boolean> friendRequestSent = new HashMap<>();
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

    public void setFriendRequestSent(String username) {
        friendRequestSent.put(username, true);
        notifyDataSetChanged(); // Refresh the entire list
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = userList.get(position);
        holder.textView.setText(username);

        boolean isRequestSent = friendRequestSent.getOrDefault(username, false);
        holder.button.setVisibility(isRequestSent ? View.GONE : View.VISIBLE);
        holder.button.setOnClickListener(v -> {
            if (listener != null && !isRequestSent) {
                listener.onFriendRequestClick(username);
                friendRequestSent.put(username, true);
                notifyItemChanged(position);
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

