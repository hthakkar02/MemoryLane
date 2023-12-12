package com.cs407.memorylane;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        
        userID = sharedPreferences.getString("userID", "user not logged in");

        Log.d("User ID is:", userID);

        // Access TextView here after the view has been created
        TextView userName = view.findViewById(R.id.tvUserName);
        TextView memoryCount = view.findViewById(R.id.tvMemoriesMade);

        
        userName.setText("User: " + userID);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.clear();
                editor.apply();
            }
        });

        setProfileInfo();
    }


    public void setProfileInfo() {
        dataTest dT = dataTest.getInstance();
        dT.retrieveUserInfo(userID, new dataTest.UserInfoCallback() {
            @Override
            public void onUserInfoRetrieved(ArrayList<String> userInfo) {
                if (userInfo != null && !userInfo.isEmpty()) {
                    // Assuming the userInfo array contains the username at index 0 and memories made at index 1
                    String username = userInfo.get(0);
                    String memoriesMade = userInfo.get(1);

                    // Update UI here with the fetched data
                    TextView userName = getView().findViewById(R.id.tvUserName);
                    TextView memoryCount = getView().findViewById(R.id.tvMemoriesMade);

                    userName.setText(username);
                    memoryCount.setText("Memories Made: " + memoriesMade);
                }
            }
        });
    }
}





            //dataTest dataTest = new dataTest();
//        dataTest.retrieveUserInfo(userID, info -> {
//            for (String element : info) {
//                Log.d("Profile Info Test", element);
//            }
//        });


        //username
        //memories created 0814


