package com.cs407.memorylane;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<String> friendRequestUserIDs;
    private List<String> usernames;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAcceptClick(int position);
        void onDeclineClick(int position);
    }

    public FriendRequestAdapter(List<String> friendRequestUserIDs, OnItemClickListener listener) {
        this.friendRequestUserIDs = friendRequestUserIDs;
        this.usernames = new ArrayList<>();
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

    public void addData(String userID, String username) {
        friendRequestUserIDs.add(userID);
        usernames.add(username);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = usernames.get(position);

        // Usage of userIDToUsername method
        dataTest dT = dataTest.getInstance();


        // Assuming the friend request string contains the username
        holder.usernameTextView.setText(username);

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

    public void clearData() {
        friendRequestUserIDs.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return friendRequestUserIDs.size();
    }

    // Helper method to update the data in the adapter
    public void updateData(String newUser) {
        usernames.add(newUser);
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        if (position >= 0 && position < friendRequestUserIDs.size()) {
            return friendRequestUserIDs.get(position);
        }
        return null;
    }
}

//neighbor, seq number, topology, forwarding table, socket
//linkstate message is the problem where the flooding happens, the IP is wrong