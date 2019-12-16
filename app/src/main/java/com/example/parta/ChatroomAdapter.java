package com.example.parta;
import android.content.Context;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatroomAdapter  extends FirestoreRecyclerAdapter<ChatroomMessage, ChatroomAdapter.MessageHolder> {
    private final int MESSAGE_IN_VIEW_TYPE  = 1;
    private final int MESSAGE_OUT_VIEW_TYPE = 2;
    String userId;
    String TAG = "ChatroomAdapter";
    String itemUser;
    String messageUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChatroomAdapter(@NonNull Context context, Query query, String userID) {
        super(new FirestoreRecyclerOptions.Builder<ChatroomMessage>()
                .setQuery(query, ChatroomMessage.class)
                .build());
        this.userId = userID;

    }


    @Override
    public int getItemViewType(int position) {
        //if message userId matches current userid, set view type 1 else set view type 2
        Log.v(TAG,"User id = " + getItem(position).getMessageUserId());
        itemUser = getItem(position).getMessageUserId();
        messageUser = getItem(position).getMessageUser();
        if(itemUser.equals(userId)){
            return MESSAGE_OUT_VIEW_TYPE;
        }
        return MESSAGE_IN_VIEW_TYPE;
    }




    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if(viewType==MESSAGE_IN_VIEW_TYPE){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_in_item, parent, false);
        }
        else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_out_item, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull ChatroomMessage model) {
        //Bind values from Message to the viewHolder


        holder.mText.setText(model.getMessageText());
        if(!model.getMessageText().equals("")) {
            holder.mImageView.setVisibility(View.INVISIBLE);
            model.setImageURL(null);
        }
        holder.mUsername.setText(model.getMessageUser());
        holder.mTiming.setText("" + getDateFromSystemTime(model.getMessageTiming()));
        if(!TextUtils.isEmpty(model.getImageURL())) {
            holder.mImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(model.getImageURL()).into(holder.mImageView);
        }
    }

    public void deleteItem(int position) {
        if(itemUser.equals(userId)){
            getSnapshots().getSnapshot(position).getReference().delete();
        }

    }

    public String getUser() {
        Log.v(TAG," itemUser = " + itemUser);
        return messageUser;
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView mText;
        TextView mUsername;
        ImageView mImageView;
        TextView mTiming;



        public MessageHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.textViewMessage);
            mUsername = itemView.findViewById(R.id.textViewUser);
            mImageView = itemView.findViewById(R.id.imageViewChat);
            mTiming = itemView.findViewById(R.id.textViewTime);
            mImageView.setVisibility(View.INVISIBLE);
        }
    }

    public String getDateFromSystemTime(long milliseconds) {
        Date currentDate = new Date(milliseconds);

        //printing value of Date
        System.out.println("current Date: " + currentDate);

        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
        return  dateFormat.format(currentDate);
    }

}
