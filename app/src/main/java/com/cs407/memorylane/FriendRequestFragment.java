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

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestFragment extends Fragment {

    private String userID = "";

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private List<String> usernames = new ArrayList<>();

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


        String userID = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("userID", "User not logged in");
        fetchFriendRequestsForUser(userID);


        //handleFriendRequestAcceptance();

        recyclerView = view.findViewById(R.id.friend_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendRequestAdapter(new ArrayList<>(), new FriendRequestAdapter.OnItemClickListener() {

            @Override
            public void onAcceptClick(int position) {
                dataTest dT = dataTest.getInstance();
                String friendRequesterUserID = adapter.getItem(position);
                Log.d("Friend Accept: ", friendRequesterUserID);

                dT.onFriendRequestAccepted(userID, friendRequesterUserID);
                adapter.removeItem(position);
                adapter.notifyItemRemoved(position);

            }
             //friendRequesterUserID.replace(adapter.getItem(position),"");


            @Override
            public void onDeclineClick(int position) {
                dataTest dT = dataTest.getInstance();
                String friendRequesterUserID = adapter.getItem(position);
                Log.d("Friend Decline1: ", userID);
                Log.d("Friend Decline2: ", friendRequesterUserID);
                dT.rejectFriendRequest(userID, friendRequesterUserID);
                adapter.removeItem(position);
                adapter.notifyItemRemoved(position);

                //friendRequesterUserID.replace(adapter.getItem(position),"");

            }
        });

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);

        // Fetch friend requests for the user
        //fetchFriendRequestsForUser(userID);

        return view;
    }


    public void fetchFriendRequestsForUser(String userID) {
        dataTest dT = dataTest.getInstance();

        com.cs407.memorylane.dataTest.OnFriendRequestsRetrievedListener listener = new com.cs407.memorylane.dataTest.OnFriendRequestsRetrievedListener() {
            @Override
            public void onFriendRequestsRetrieved(ArrayList<String> friendRequestUserIDs) {
                // Handle the retrieved friend requests here
                adapter.clearData();
                //usernames.clear();

                for (String userID : friendRequestUserIDs) {
                    // Process each friend request as needed
                    Log.d("Friend Request: ", userID);
                    dT.userIDToUsername(userID, new dataTest.OnUsernameRetrievedListener() {

                        @Override
                        public void onUsernameRetrieved(String username) {
                            adapter.addData(userID, username);
                            //adapter.updateData(userID);
                        }

                        @Override
                        public void onUsernameRetrievalFailure(String errorMessage) {
                            Log.d("Username retrieval failed: ", errorMessage);
                            String username = "Manoj2";
                            adapter.addData(userID, username);
                            //Log.d("Username retrieval sent: ", username);


                        }
                    });
                }
            }

            @Override
            public void onFriendRequestsRetrievalFailure(String errorMessage) {
                // Handle the failure to retrieve friend requests
                Log.d("Friend requests retrieval failed: ", errorMessage);
            }
        };

        // Use the method to retrieve friend requests for the given userID
        dT.retrieveFriendRequestsArray(userID, listener);
    }

    protected void handleFriendRequestAcceptance(){

        // Assume userID and friendsUserID are obtained or set somehow
        String userID = "UXnCetghE7VaSs1ZLzmI";
        String friendsUserID = "ADsMGJPvOJdyzLQJdz0X";

        dataTest dT = dataTest.getInstance();
        dT.onFriendRequestAccepted(userID, friendsUserID);

    }
}
