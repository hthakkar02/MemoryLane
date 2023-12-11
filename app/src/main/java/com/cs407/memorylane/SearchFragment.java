package com.cs407.memorylane;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<String> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, username -> {
            // Handle friend request button click
            sendFriendRequest(username);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });

        return view;
    }

    private void sendFriendRequest(String username) {
        dataTest dT = dataTest.getInstance();
        // Implement sending friend request logic
        dT.sendFriendRequest(getContext(), username);
    }

    private void performSearch(String query) {
        dataTest dT = dataTest.getInstance();
        dT.searchUsername(query, new dataTest.UsernameSearchCallback() {
            @Override
            public void onSearchCompleted(List<String> usernames) {
                userList.clear();
                userList.addAll(usernames);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
