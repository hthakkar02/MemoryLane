package com.cs407.memorylane;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.runner.manipulation.Ordering;

public class CustomBaseAdapter extends BaseAdapter {

    // Finish the custom list once users are imported https://www.youtube.com/watch?v=aUFdgLSEl0g
    Context context;
    String users[];
    String userNames[];
    int profilePics[];
    LayoutInflater inflater;
    public CustomBaseAdapter(Context ctx, String[] user, String [] name, int [] images){
        this.context = ctx;
        this.users = user;
        this.userNames = name;
        this.profilePics = images;
        inflater = LayoutInflater.from(ctx);
    }
    @Override
    public int getCount() {
        return users.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.activity_custom_friends_list, null);
        TextView txtView = (TextView) convertView.findViewById(R.id.list_user_name);
        TextView txtView2 = (TextView) convertView.findViewById(R.id.list_user_name2);
        ImageView profileImg = (ImageView) convertView.findViewById(R.id.list_profile_pic);
        txtView.setText(users[position]);
        profileImg.setImageResource(profilePics[position]);
        return convertView;
    }
}
