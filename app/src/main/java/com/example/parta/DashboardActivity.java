package com.example.parta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    Button buttonViewProfile;
    Button buttonCreateTrip;
    Button buttonViewUsers;
    Button buttonLogout;
    Button buttonViewTrips;
    String TAG = "dashboard";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setTitle("Dashboard");

        buttonViewProfile = findViewById(R.id.buttonEditProfile);
        buttonCreateTrip = findViewById(R.id.buttonCreateTrip);
        buttonViewUsers = findViewById(R.id.buttonViewUsers);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonViewTrips = findViewById(R.id.buttonViewTrips);

        //View Profile
        buttonViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, ViewProfileActivity.class));
            }
        });

        //Create Trip
        buttonCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, CreateTripActivity.class));
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            }
        });

        buttonViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, FindNewUserActivity.class));
            }
        });

        buttonViewTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, ViewTripActivity.class));
            }
        });
    }
}
