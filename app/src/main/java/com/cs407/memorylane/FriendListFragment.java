package com.cs407.memorylane;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

        return view;
    }
}

