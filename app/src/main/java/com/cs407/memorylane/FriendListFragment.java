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

public class FriendListFragment extends Fragment {

    public FriendListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list_page, container, false);

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

        String userID = "0jx1wTDB1mRLyFMnimQp"; // TODO: Replace with the actual user ID via shared preferences

        dataTest.retrieveFriendsArray(userID, new com.cs407.memorylane.dataTest.OnFriendsListRetrievedListener() {
            @Override
            public void onFriendsListRetrieved(ArrayList<String> friendsList) {
                // Handle retrieved friends list
                for (String friend : friendsList) {
                    Log.d("Friend", friend);
                }

                //TODO: do UI updates here.
            }

            @Override
            public void onFriendsListRetrievalFailure(String errorMessage) {
                // Handle retrieval failure
                Log.e("Friend Retrieval", "Failed to retrieve friends: " + errorMessage);
            }
        });
    }


}

