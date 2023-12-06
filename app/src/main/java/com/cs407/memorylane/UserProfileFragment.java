package com.cs407.memorylane;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class UserProfileFragment extends Fragment {

    private String userID = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Accessing SharedPreferences from the activity
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);

        // Now you can use sharedPreferences to read or write data
        userID = sharedPreferences.getString("userID", "user not logged in");

        Log.d("User ID is:", userID);


        setProfileInfo();
    }


    public void setProfileInfo() {
        // Retrieve information about dude from userID
        dataTest dataTest = new dataTest();
        dataTest.retrieveUserInfo(userID, info -> {
            for (String element : info) {
                Log.d("Profile Info Test", element);
            }
        });
    }







}
