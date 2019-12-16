package com.example.parta;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class ViewProfileActivity extends AppCompatActivity {

    TextView textViewNewUsername;
    TextView textViewFirstName;
    TextView textViewLastName;
    TextView textViewGender;
    Button buttonEdit;
    ImageView imageView;
    Gson gson;
    String TAG = "ViewProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        setTitle("View Profile");
        textViewFirstName = findViewById(R.id.firstName);
        textViewLastName = findViewById(R.id.lastName);
        textViewGender = findViewById(R.id.gender);
        textViewNewUsername = findViewById(R.id.username);
        buttonEdit = findViewById(R.id.buttonEdit);
        imageView = findViewById(R.id.imageView);

        displayUI();
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Intent(ViewProfileActivity.this, EditActivity.class);
                Intent intent = new Intent(ViewProfileActivity.this, EditActivity.class);
                startActivityForResult(intent, 123);
            }
        });
    }

    public void displayUI() {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences
                (ApplicationConstants.SHARED_PREFERENCES_KEY, MODE_PRIVATE);

        final String userInfoListJsonString = sharedPreferences.getString
                (ApplicationConstants.SHARED_PREFERENCES_JSON_STUDEN_OBJECT_KEY, "");
        gson = new Gson();

        User userDetailsObject = gson.fromJson(userInfoListJsonString,
                User.class);

            textViewFirstName.setText(userDetailsObject.getFirstName());
            textViewLastName.setText(userDetailsObject.getLastName());
            textViewGender.setText(userDetailsObject.getGender());
            textViewNewUsername.setText(userDetailsObject.getUsername());
            Picasso.get().load(userDetailsObject.getImageURL()).into(imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 123 && resultCode == RESULT_OK) {
                displayUI();
            }
        }
    }
}
