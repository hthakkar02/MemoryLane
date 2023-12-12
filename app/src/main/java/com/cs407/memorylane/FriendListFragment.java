package com.cs407.memorylane;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class FriendListFragment extends Fragment {

    private String userID = "";
    private ListView friendListView;
    private ArrayAdapter<String> friendAdapter;

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list_page, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

        userID = sharedPreferences.getString("userID", "user not logged in");
        String username = "";



        Log.d("User ID is:", userID);

        friendListView = view.findViewById(R.id.friend_list);
        friendAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        friendListView.setAdapter(friendAdapter);





        ImageButton friendListButton = view.findViewById(R.id.friend_list_menu);
        ImageButton friendRequestButton = view.findViewById(R.id.friend_accept_menu);

        friendListButton.setOnClickListener(v -> {
            // Switch to FriendListFragment (or refresh the same fragment)
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendListFragment());
            transaction.commit();
        });

        friendRequestButton.setOnClickListener(v -> {
            // Switch to FriendRequestFragment
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendRequestFragment());
            transaction.commit();
        });


        retrieveFriends();

        return view;
    }



    protected void retrieveFriends() {
        dataTest dataTest = new dataTest();

        dataTest.retrieveFriendsArray(userID, new com.cs407.memorylane.dataTest.OnFriendsListRetrievedListener() {
            @Override
            public void onFriendsListRetrieved(ArrayList<String> friendsList) {
                ArrayList<String> usernamesList = new ArrayList<>();


                // Handle retrieved friends list
                for (String friend : friendsList) {
                    Log.d("Friend", friend);

                    dataTest.userIDToUsername(friend, new dataTest.OnUsernameRetrievedListener() {

                        @Override
                        public void onUsernameRetrieved(String username) {
                            usernamesList.add(username);
                            Log.d("Username of Friend:", username);
                        }

                        @Override
                        public void onUsernameRetrievalFailure(String errorMessage) {
                            Log.d("Username retrieval failed: ", errorMessage);
                            String username = "Manoj2";
                            //adapter.addData(userID, username);
                            //Log.d("Username retrieval sent: ", username);


                        }
                    });


                }

                friendAdapter.clear();

                //go through friends_list and change each userID to username


                friendAdapter.addAll(usernamesList);

                // Notify the adapter that the data has changed
                friendAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFriendsListRetrievalFailure(String errorMessage) {
                // Handle retrieval failure
                Log.e("Friend Retrieval", "Failed to retrieve friends: " + errorMessage);
            }
        });
    }


}

