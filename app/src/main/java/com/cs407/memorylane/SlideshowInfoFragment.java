package com.cs407.memorylane;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.ArrayList;

public class SlideshowInfoFragment extends Fragment {

    double lat;
    double lon;

    ArrayList<String> imagePaths = new ArrayList();

    int position;

    TextView area;
    TextView date;

    TextView address;

    TextView username;

    public SlideshowInfoFragment(double lat, double lon, ArrayList<String> imagePaths, int position){
        this.lat = lat;
        this.lon = lon;
        this.imagePaths = imagePaths;
        this.position = position;
    }

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

        area = view.findViewById(R.id.area);
        area.setText(getLocality(getActivity(), lat, lon));

        date = view.findViewById(R.id.date);
        date.setText(getDate(imagePaths));

        address = view.findViewById(R.id.address);
        address.setText(getAddress(getActivity(), imagePaths));

        username = view.findViewById(R.id.user);
        username.setText(getUsername(imagePaths));
        return view;
    }

    private String getUsername(ArrayList<String> imagePaths) {
        dataTest dT = dataTest.getInstance();
        return dT.getImageOwner(imagePaths.get(position));
    }

    private String getAddress(Context context, ArrayList<String> imagePaths) {
        dataTest dT = dataTest.getInstance();
        GeoPoint location = dT.getImageLocation(imagePaths.get(position));
        return dT.getStreetFromCoordinates(context, location.getLatitude(), location.getLongitude());
    }

    private String getDate(ArrayList<String> imagePaths) {
        dataTest dT = dataTest.getInstance();
        return dT.getImageDate(imagePaths.get(position));
    }

    private String getLocality(Context context, double lat, double lon) {
        dataTest dT = dataTest.getInstance();
        Log.d("NEIGHBORHOOD", dT.getNeighborhoodFromCoordinates(context, lat, lon));
        return dT.getNeighborhoodFromCoordinates(context, lat, lon);
    }

    public void updateImageDetails(ArrayList<String> imagePaths, int newPosition) {
        this.position = newPosition;
        updateUI(); // Update the UI with new position details
    }

    private void updateUI() {
        if (getActivity() != null) {
            area.setText(getLocality(getActivity(), lat, lon));
            date.setText(getDate(imagePaths));
            address.setText(getAddress(getActivity(), imagePaths));
            username.setText(getUsername(imagePaths));
        }
    }
}