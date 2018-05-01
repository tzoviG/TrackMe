package com.example.gamer.trackme;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class popActivity extends AppCompatActivity {

    private static final String TAG = "PopActivity";

    Float level = 0.2f;
    Float distance;
    Long mTime;
    ArrayList<LatLng> mTripPath;
    Bundle bundle = new Bundle();

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        //set width and height of the window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.6));

        //get time from MapActivity
        mTime = getIntent().getExtras().getLong("time");
        //get sum of distance from the MapActivity
        distance = getIntent().getExtras().getFloat("distance");
        setTextViewPoints();
        //get the path from the MapActivity
        mTripPath = getIntent().getParcelableArrayListExtra("path");

        //set the dropdown menu
        Spinner spinner = findViewById(R.id.pop_spn_transport);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transport, R.layout.spinner_pop);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        level = 0.8f;
                        setTextViewPoints();
                        break;
                    case 1:
                        level = 0.6f;
                        setTextViewPoints();
                        break;
                    case 2:
                        level = 0.2f;
                        setTextViewPoints();
                        break;
                    default:
                        level = 0.2f;
                        setTextViewPoints();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        //set the Buttons
        Button btn_save = findViewById(R.id.pop_btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate trip name
                EditText edtxt = findViewById(R.id.pop_edtxt_name);
                if(edtxt.getText().toString().equals("")) {
                    Toast.makeText(popActivity.this,"Please fill TRIP name first!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Write a message to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference db = database.getReference("users");

                //fix rate
                Float distance = getIntent().getExtras().getFloat("distance");
                Integer rate = (int)(distance * level);
                Integer meters = Math.round(distance);

                String email = getCurrentUser();
                String[] separated = email.split("@");
                //put in database
                db.child(separated[0]).child(edtxt.getText().toString()).child("meters").setValue(Integer.toString(meters));
                db.child(separated[0]).child(edtxt.getText().toString()).child("rate").setValue(Integer.toString(rate));
                db.child(separated[0]).child(edtxt.getText().toString()).child("time").setValue(mTime.toString());
                //add path to firebase
//                mTripPath = bundle.getParcelableArrayList("path");
                for (int i = 0 ; i < mTripPath.size(); i++){
                    db.child(separated[0]).child(edtxt.getText().toString())
                            .child("Route").child(Integer.toString(i)).child("Lat").setValue(mTripPath.get(i).latitude);
                    db.child(separated[0]).child(edtxt.getText().toString())
                            .child("Route").child(Integer.toString(i)).child("Lng").setValue(mTripPath.get(i).longitude);
                }
                //fix everything else
                edtxt.setText("");
                Toast.makeText(popActivity.this, "Your trip is successfully updated!",
                        Toast.LENGTH_LONG).show();
            }
        });

        //check
        if ((getCurrentUser() == null) || (distance == 0)){
            btn_save.setEnabled(false);
            btn_save.setTextColor(R.color.colorGrey);
            Toast.makeText(popActivity.this, "You must SIGN IN to update your trip!",
                    Toast.LENGTH_LONG).show();
        }

        Button btn_exit = findViewById(R.id.pop_btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText txtEdit = findViewById(R.id.pop_edtxt_name);

        txtEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
                    setTextViewPoints();
//                }
            }
        });
    }

    String mtxt_track = "";
    String mRank = "0";
    String mTimeToFinish = "0";

    private void setTextViewPoints(){
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        TextView txt_track = findViewById(R.id.pop_edtxt_name);
        mtxt_track = txt_track.getText().toString();


        // Read from the database
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //set details
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    String tree = ds.getKey();
                    if(tree.equals("tracks")){
                        for (DataSnapshot ds_child : ds.getChildren()) {
                            String trip = ds_child.getKey();
                            if (mtxt_track.equals(trip)) {
                                for (DataSnapshot ds_trip : ds_child.getChildren()) {
                                    //find way to do the trip
                                    String mTimeAll = "";
                                    if (level == 0.8f) {
                                        mTimeAll = "Time";
                                    } else if(level == 0.6f){
                                        mTimeAll = "TimeBic";
                                    } else if (level == 0.2f) {
                                        mTimeAll = "TimeCar";
                                    }

                                    //fill vars for calculate points
                                    if (ds_trip.getKey().equals("Rank")) {mRank = ds_trip.getValue(String.class);}
                                    if (ds_trip.getKey().equals(mTimeAll)) {
                                        mTimeToFinish = ds_trip.getValue(String.class);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //set the text of the textview
        TextView txt_title = findViewById(R.id.pop_txt_title);

        //Calculate the points
        //mTime Long              // ο χρόνος που έκανε
        //mRank String            // βαθμός δυσκολίας της διαδρομής
        //mTimeToFinish String    // χρόνος στόχος της διαδρομής
        //distance Float          // απόσταση που διένυσε
        Integer rate = (int)(distance * level);
        int dist =  Math.round(distance);
        String mTimeToText =  Long.toString(Math.round(mTime/60));
        rate = rate + Math.round((rate * Integer.parseInt(mRank))/100);
        if (mTime <= (Long.parseLong(mTimeToFinish)*60)) {rate += 1000;}
        txt_title.setText("Έχετε διανύσει: " + dist
                + " μέτρα σε " + mTimeToText  + " λεπτά."
                + "\n" + "Ο στόχος είναι " + mTimeToFinish + " λεπτά."
                + "\n" + "Ο βαθμός δυσκολίας δίνει " + mRank + "%."
                + "\n" + "Η βαθμολογία σας είναι: " + Integer.toString(rate));

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

            return email;
        } else {
            return null;
        }
    }
}
