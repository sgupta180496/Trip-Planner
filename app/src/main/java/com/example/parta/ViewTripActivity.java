package com.example.parta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewTripActivity extends AppCompatActivity
        implements TripAdapter.OnItemClickListener{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String TAG = "DisplayUsers";
    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore database;
    Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trip);
        setTitle("Trips");

        recyclerView = findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userId = user.getUid();
        Log.v(TAG, "user = " + user);
        database = FirebaseFirestore.getInstance();

        query = database.collection("trips");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                }
            }
        });

        tripAdapter = new TripAdapter(ViewTripActivity.this, query, user.getEmail());
        recyclerView.setAdapter(tripAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT |
                ItemTouchHelper.LEFT ) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Toast.makeText(ViewTripActivity.this, "OnMove!", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                if(tripAdapter.tripOne.getOwnerOfTrip().equals(user.getEmail())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewTripActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Delete Trip");
                    builder.setMessage("Are you sure ?" );

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tripAdapter.deleteItem(viewHolder.getAdapterPosition());
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(ViewTripActivity.this, ViewTripActivity.class));
                            tripAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewTripActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Delete Trip");
                    builder.setMessage("Are you sure ?" );

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when click positive button
                            tripAdapter.deleteItemNotOwner(viewHolder.getAdapterPosition());
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(ViewTripActivity.this, ViewTripActivity.class));
                            tripAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(tripAdapter != null) {
            tripAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(tripAdapter != null) {
            tripAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public List<String> displayTrips(List<Trip> trips) {
        List<String> tripNames = new ArrayList<>();
        for(Trip trip : trips) {
            tripNames.add(trip.getTripName());
        }
        return  tripNames;
    }

    @Override
    public void onTripItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Trip trip = tripAdapter.getItem(position);
        final Map<String,String> usersMap = trip.getUsersMap();
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user1 = mAuth.getCurrentUser();
        if(usersMap.containsKey(user1.getEmail())) {

            Intent intent  = new Intent(ViewTripActivity.this,
                    CreateChatroomActivity.class);
            intent.putExtra("TripDetails",trip);
            startActivity(intent);
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(ViewTripActivity.this);
            // Specify the dialog is not cancelable
            builder.setCancelable(false);

            // Set a title for alert dialog
            builder.setTitle("Join Trip");
            builder.setMessage("You are not a member of the trip " + trip.getTripName() +
                    " Would you like to join?" );

            // Set the positive/yes button click listener
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do something when click positive button
                    usersMap.put(user1.getEmail(),user1.getUid());
                    trip.setUsersMap(usersMap);
                    //update user in trip

                    db.collection(ApplicationConstants.TRIPS)
                            .document(trip.getTripName())
                            .set(trip)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Added Trips", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Error Adding Users", Toast.LENGTH_LONG).show();
                                }
                            });
                    //Call the CreateChatRoom Activity"

                    Intent intent  = new Intent(ViewTripActivity.this,
                            CreateChatroomActivity.class);
                    intent.putExtra("TripDetails",trip);
                    startActivity(intent);

                }

            });

            // Set the negative/no button click listener
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do something when click the negative button
                }
            });

            // Set the neutral/cancel button click listener
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do something when click the neutral button
                }
            });

            AlertDialog dialog = builder.create();

            // Display the alert dialog on interface
            dialog.show();


        }



    }
}






