package com.cs407.memorylane;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class SlideshowInfoFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slideshow_info, container, false);

        // Find the hide button in the menu fragment and set its click listener
        ImageButton hideButton = view.findViewById(R.id.menu_down_button);
        hideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the menu fragment
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(SlideshowInfoFragment.this)
                        .commit();
            }
        });

        return view;
    }
}