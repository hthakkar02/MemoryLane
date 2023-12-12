package com.cs407.memorylane;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<String> friendRequests;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAcceptClick(int position);
        void onDeclineClick(int position);
    }

    public FriendRequestAdapter(List<String> friendRequests, OnItemClickListener listener) {
        this.friendRequests = friendRequests;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        Button acceptButton;
        Button declineButton;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.tvUsername);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            declineButton = itemView.findViewById(R.id.btnDecline);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String friendRequest = friendRequests.get(position);

        // Usage of userIDToUsername method
        dataTest dataTest = new dataTest();
        dataTest.userIDToUsername(friendRequest, new dataTest.OnUsernameRetrievedListener() {
            @Override
            public void onUsernameRetrieved(String username) {
                // Handle retrieved username
                Log.d("Retrieved Username from adapter: " , username);

                // Assuming the friend request string contains the username
                holder.usernameTextView.setText(username);

            }

            @Override
            public void onUsernameRetrievalFailure(String errorMessage) {
                // Handle retrieval failure
                System.out.println("Username retrieval failed: " + errorMessage);
            }
        });


        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(position);
            }
        });

        holder.declineButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeclineClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    // Helper method to update the data in the adapter
    public void updateData(List<String> newData) {
        friendRequests.clear();
        friendRequests.addAll(newData);
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        if (position >= 0 && position < friendRequests.size()) {
            return friendRequests.get(position);
        }
        return null;
    }
}
