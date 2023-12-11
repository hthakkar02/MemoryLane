package com.cs407.memorylane;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class FriendRequestFragment extends Fragment {

    public FriendRequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request_page, container, false);

        ImageButton friendListButton = view.findViewById(R.id.friend_list_menu);
        ImageButton friendRequestButton = view.findViewById(R.id.friend_accept_menu);

        friendListButton.setOnClickListener(v -> {
            // Switch to FriendListFragment
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendListFragment());
            transaction.commit();
        });

        friendRequestButton.setOnClickListener(v -> {
            // Switch to FriendRequestFragment (or refresh the same fragment)
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendRequestFragment());
            transaction.commit();
        });


        fetchFriendRequestsForUser("ADsMGJPvOJdyzLQJdz0X");


        handleFriendRequestAcceptance();


        return view;
    }


    public void fetchFriendRequestsForUser(String userID) {
        dataTest dataTest = new dataTest();

        com.cs407.memorylane.dataTest.OnFriendRequestsRetrievedListener listener = new com.cs407.memorylane.dataTest.OnFriendRequestsRetrievedListener() {
            @Override
            public void onFriendRequestsRetrieved(ArrayList<String> friendRequests) {
                // Handle the retrieved friend requests here
                for (String request : friendRequests) {
                    // Process each friend request as needed
                    Log.d("Friend Request: ", request);
                }
            }

            @Override
            public void onFriendRequestsRetrievalFailure(String errorMessage) {
                // Handle the failure to retrieve friend requests
                Log.d("Friend requests retrieval failed: ", errorMessage);
            }
        };

        // Use the method to retrieve friend requests for the given userID
        dataTest.retrieveFriendRequestsArray(userID, listener);
    }

    protected void handleFriendRequestAcceptance(){

        // Assume userID and friendsUserID are obtained or set somehow
        String userID = "UXnCetghE7VaSs1ZLzmI";
        String friendsUserID = "ADsMGJPvOJdyzLQJdz0X";

        dataTest dataTest = new dataTest();
        dataTest.onFriendRequestAccepted(userID, friendsUserID);

    }
}
