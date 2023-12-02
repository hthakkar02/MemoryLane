package com.cs407.memorylane;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayAdapter<String> adapter;
    private List<String> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        SearchView searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Dummy data for testing - replace with real user data later
        userList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: Handle live search here
                return false;
            }
        });

        return view;
    }

    private void performSearch(String query) {
        // Dummy search logic - replace with actual search logic
        userList.clear();
        if (!query.isEmpty()) {
            userList.add("User1"); // Example data
            userList.add("User2"); // Example data
        }
        adapter.notifyDataSetChanged();
    }
}

