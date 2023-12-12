package com.cs407.memorylane;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class FriendListAdapter extends ArrayAdapter<String> {
    private Map<String, String> usernameToUserIDMap;
    private String userID;
    public FriendListAdapter(Context context, Map<String,String> usernameToUserIDMap, List<String> friends) {
        super(context, 0, friends);
        this.usernameToUserIDMap = usernameToUserIDMap;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String friend = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friend, parent, false);
        }
        // Lookup view for data population
        TextView tvFriendName = convertView.findViewById(R.id.FriendName);
        Button btnDeleteFriend = convertView.findViewById(R.id.btnDeleteFriend);
        // Populate the data into the template view using the data object
        tvFriendName.setText(friend);
        // Set up delete friend button click listener
        btnDeleteFriend.setOnClickListener(v -> {
            // Call method to handle friend deletion
            deleteFriend(friend);
        });
        // Return the completed view to render on screen
        return convertView;
    }

    private void deleteFriend(String friendUsername) {

        String friendUserID = usernameToUserIDMap.get(friendUsername);

        dataTest.getInstance().deleteFriend(userID, friendUserID);
        // Optionally, remove the friend from the adapter and refresh the list
        remove(friendUserID);
        notifyDataSetChanged();
    }

}


