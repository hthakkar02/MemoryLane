package com.cs407.memorylane;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class SlideshowInfoFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        void onFragmentClosed();
    }

    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
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

                // Notify the activity
                if (mListener != null) {
                    mListener.onFragmentClosed();
                }
            }
        });

        return view;
    }
}