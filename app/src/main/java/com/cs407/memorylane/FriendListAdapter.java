package com.cs407.memorylane;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class FriendListAdapter extends ArrayAdapter<String> {
    private String userID;
    public FriendListAdapter(Context context, String userID, List<String> friends) {
        super(context, 0, friends);
        this.userID = userID;
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

    protected void deleteFriend(String friendUsername) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userDataCollection = db.collection("User Data"); 
        Query query = userDataCollection.whereEqualTo("Username", friendUsername);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    // Do something with the document ID (e.g., print or use it as needed)
                    Log.d("Document ID for deletion", documentId);

                    // Perform the deletion here, inside the completion block
                    dataTest.getInstance().deleteFriend(userID, documentId);

                    // Optionally, remove the friend from the adapter and refresh the list
                    remove(friendUsername);
                    notifyDataSetChanged();
                }
            } else {
                // Handle errors
                Exception exception = task.getException();
                if (exception != null) {
                    exception.printStackTrace();
                }
            }
        });
    }

}


