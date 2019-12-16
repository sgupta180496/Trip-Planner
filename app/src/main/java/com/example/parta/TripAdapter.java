package com.example.parta;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class TripAdapter extends FirestoreRecyclerAdapter<Trip, TripAdapter.TripHolder> {
    private String userEmailId;
    String TAG = "TripAdapter";
    private String tripName = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Trip tripOne;
    private OnItemClickListener onItemClickListener;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser user1 = mAuth.getCurrentUser();

    public TripAdapter(@NonNull OnItemClickListener onItemClickListener, Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(query, Trip.class)
                .build());
        this.userEmailId = userID;
        this.onItemClickListener = onItemClickListener;
    }


    public boolean deleteItem(final int position) {

        //delete trip
        //final String tripName = getSnapshots().getSnapshot(position).getReference().getId();
        Task<DocumentSnapshot> documentSnapshotTask = getSnapshots()
                .getSnapshot(position)
                .getReference()
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            String owner = (String) documentSnapshot.get("ownerOfTrip");
                            Log.v(TAG, "####Owner = " + owner);
                            if (user1.getEmail().equals(owner)) {
                                deleteTrip(position);
                                deleteCorrespondingChatroom();
                            } else {
                                Map<String, String> usersMap = (Map<String, String>) documentSnapshot.get("usersMap");
                                usersMap.remove(user1.getEmail());
                            }
                            Log.d(TAG, "Cached document data: " + documentSnapshot.getData());
                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                        }
                    }
                });
        return true;
    }

    public boolean deleteItemNotOwner(final int position) {

        final String tripName = getSnapshots().getSnapshot(position).getReference().getId();
        Task<DocumentSnapshot> documentSnapshotTask = getSnapshots()
                .getSnapshot(position)
                .getReference()
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            Map<String, String> usersMap = (Map<String, String>) documentSnapshot.get("usersMap");
                            usersMap.remove(user1.getEmail());
                            FirebaseFirestore.getInstance()
                                    .collection("trips")
                                    .document(tripName)
                                    .update("usersMap", usersMap);
                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                        }
                    }
                });

        return true;
    }

    @Override
    protected void onBindViewHolder(@NonNull TripHolder tripHolder, int i, @NonNull Trip trip) {
        tripOne = trip;
        Log.v(TAG, "onBindViewHolder My Trip = " + tripOne.getTripName());
        tripName = trip.getTripName();
        tripHolder.mText.setText(trip.getTripName());
    }

    @NonNull
    @Override
    public TripHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trip_item, parent, false);

        return new TripHolder(view);
    }

    public class TripHolder extends RecyclerView.ViewHolder {
        TextView mText;

        public TripHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.textViewTripName);
            mText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onTripItemClick(null, view, getAdapterPosition(), view.getId());
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onTripItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    public void deleteTrip(int position) {
        Log.v(TAG, "####deleteTrip ");
        if (getItem(position).getOwnerOfTrip().equals(userEmailId)) {
            getSnapshots().getSnapshot(position).getReference().delete();
        }
    }

    public void deleteCorrespondingChatroom() {
        Log.v(TAG, "####deleteCorrespondingChatroom ");

//Delete Chatroom messages
        db.collection("messages" + tripName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        for (int i = 0; i < documentSnapshots.size(); i++) {
                            String chatId = documentSnapshots.get(i).getId();
                            Log.v(TAG, "chat message  = " + documentSnapshots.get(i).getId());
                            db.collection("messages" + tripName)
                                    .document(chatId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error deleting document", e);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}
