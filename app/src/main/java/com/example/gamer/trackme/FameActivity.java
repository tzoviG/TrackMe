package com.example.gamer.trackme;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FameActivity extends AppCompatActivity {

    private static final String TAG = "FameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fame);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        // Read from the database
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        Button btn_exit = findViewById(R.id.fame_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showData(DataSnapshot dataSnapshot) {
        Integer sumRate = 0;
        String mUserName = getCurrentUser();
        TableLayout table = findViewById(R.id.fame_tbl_fame);
        int count = table.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }

        //set titles
        TextView mColUser = new TextView(this);
        mColUser.setText("USER");
        mColUser.setTextColor(this.getResources().getColor(R.color.colorRedRed));
        mColUser.setBackgroundColor(this.getResources().getColor(R.color.colorYellow));
        mColUser.setTextSize(20);
        mColUser.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView mColPoints = new TextView(this);
        mColPoints.setText("POINTS");
        mColPoints.setTextColor(this.getResources().getColor(R.color.colorRedRed));
        mColPoints.setBackgroundColor(this.getResources().getColor(R.color.colorYellow));
        mColPoints.setTextSize(20);
        mColPoints.setGravity(Gravity.CENTER_HORIZONTAL);
        //initialize table
        TableRow rowHeader = new TableRow(this);
        rowHeader.addView(mColUser);
        rowHeader.addView(mColPoints);
        table.addView(rowHeader);

        //set details
        for(DataSnapshot ds: dataSnapshot.getChildren()){
            String userBranch = ds.getKey();
            if (userBranch.equals("users")) {
                for (DataSnapshot ds_child : ds.getChildren()) {
                    String user = ds_child.getKey();

                    for (DataSnapshot ds_trip : ds_child.getChildren()) {
                        TripActivity ut = new TripActivity();

                        ut.setMeters(ds_trip.getValue(TripActivity.class).getMeters());
                        ut.setRate(ds_trip.getValue(TripActivity.class).getRate());

                        sumRate += Integer.parseInt(ut.getRate());
                    }

                    //first column for user name
                    TextView txt_user = new TextView(this);
                    txt_user.setText(user);
                    if (user.equals(mUserName)) {
                        txt_user.setTextColor(this.getResources().getColor(R.color.colorRed));
                        //txt_user.setBackgroundColor(this.getResources().getColor(R.color.colorYellowBackground));
                    } else {
                        txt_user.setTextColor(this.getResources().getColor(R.color.colorWhite));
                        //txt_user.setBackgroundColor(this.getResources().getColor(R.color.colorYellowBackground));
                    }
                    txt_user.setTextSize(18);
                    txt_user.setGravity(Gravity.CENTER_HORIZONTAL);
                    //second column for points
                    TextView txt_points = new TextView(this);
                    txt_points.setText(sumRate.toString());
                    if (user.equals(mUserName)) {
                        txt_points.setTextColor(this.getResources().getColor(R.color.colorRed));
                        //txt_points.setBackgroundColor(this.getResources().getColor(R.color.colorYellowBackground));
                    } else {
                        txt_points.setTextColor(this.getResources().getColor(R.color.colorWhite));
                        //txt_points.setBackgroundColor(this.getResources().getColor(R.color.colorYellowBackground));
                    }
                    txt_points.setTextSize(18);
                    txt_points.setGravity(Gravity.CENTER_HORIZONTAL);
                    //add to table
                    rowHeader = new TableRow(this);
                    rowHeader.addView(txt_user);
                    rowHeader.addView(txt_points);
                    table.addView(rowHeader);
                    //reset sumurize
                    sumRate = 0;
                }
            }
        }
    }

    private String getCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();

            String[] userName = email.split("@");
            return  userName[0];
        } else {
            return null;
        }
    }
}
