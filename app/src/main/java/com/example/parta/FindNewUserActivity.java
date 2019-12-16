package com.example.parta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindNewUserActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "DisplayUsers";
    List<User> usersList;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_new_user);
        setTitle("Registered Users");

        listView = findViewById(R.id.listViewUsers);

        Log.v(TAG, " FindNewUser111 ");
        db.collection(ApplicationConstants.USERS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        usersList = queryDocumentSnapshots.toObjects(User.class);
                        ArrayAdapter<String> adapter = new ArrayAdapter(FindNewUserActivity.this,
                                android.R.layout.simple_list_item_1, createUserNames(usersList)) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view.findViewById(android.R.id.text1);
                                text.setTextColor(Color.WHITE);
                                return view;
                            }


                        };


                        listView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v(TAG, "Error writing document", e);
                        Log.v(TAG, " FindNewUser Failure ");
                        //Toast.makeText(context, AppConstants.GET_MOVIE_ERROR,Toast.LENGTH_LONG).show();

                    }
                });



    }



    public List<String> createUserNames(List<User> users) {
        List<String> userNames = new ArrayList<>();
        for (User user : users) {
            userNames.add(user.getUsername());

        }
        return userNames;
    }
}
