package com.cs407.memorylane;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendRequestFragment extends Fragment {

    private String userID = "";

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;

    public FriendRequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_request_page, container, false);

        ImageButton friendListButton = view.findViewById(R.id.friend_list_menu);
        ImageButton friendRequestButton = view.findViewById(R.id.friend_accept_menu);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

        userID = sharedPreferences.getString("userID", "user not logged in");


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


        fetchFriendRequestsForUser(userID);


        //handleFriendRequestAcceptance();


        recyclerView = view.findViewById(R.id.friend_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnItemClickListener() {
            @Override
            public void onAcceptClick(int position) {
                dataTest dataTest = new dataTest();
                String friendRequesterUserID = adapter.getItem(position);
                Log.d("Friend Accept: ", friendRequesterUserID);
                dataTest.onFriendRequestAccepted(userID, friendRequesterUserID);

            }

            @Override
            public void onDeclineClick(int position) {
                dataTest dataTest = new dataTest();
                String friendRequesterUserID = adapter.getItem(position);
                Log.d("Friend Decline: ", friendRequesterUserID);
                dataTest.onFriendRequestAccepted(userID, friendRequesterUserID);
            }
        });

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Fetch friend requests for the user
        fetchFriendRequestsForUser(userID);

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

                adapter.updateData(friendRequests);

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

}
