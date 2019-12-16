package com.example.parta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class CreateChatroomActivity extends AppCompatActivity {

    ImageView tripImage;
    private RecyclerView recyclerView;
    private ChatroomAdapter chatroomAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String userId;
    TextView latitudeDisplay;
    TextView longitudeDisplay;
    FirebaseAuth auth;
    FirebaseUser user;
    String TAG = "CreateChatroomActivity";
    FirebaseFirestore database;
    Query query;
    private TextView textViewName;
    Trip trip;
    FloatingActionButton floatingActionButton;
    private EditText editTextMessage;
    String collectionName;
    FloatingActionButton floatingAttachmentButton;
    int CAMERA_KEY = 100;
    int GALLERY_KEY = 200;
    String path = "";
    String directoryName = "photos/";
    Bitmap bitmapUpload = null;
    String imageURL = "";
    Uri selectedImageUri;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();
    Photo photoObject = null;



    public CreateChatroomActivity() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chatroom);
        setTitle("Chat Room");

        if(getIntent()!= null) {
            if(getIntent().getExtras() != null) {
                Bundle bundle = getIntent().getExtras();
                trip = bundle.getParcelable("TripDetails");
            }
        }
        collectionName = "messages" + trip.getTripName();

        tripImage = findViewById(R.id.imageView2);
        latitudeDisplay = findViewById(R.id.latitudeDisplay);
        longitudeDisplay = findViewById(R.id.longitudeDisplay);

        latitudeDisplay.setText("Latitude: " + trip.getLatitude());
        longitudeDisplay.setText("Longitude: " + trip.getLongitude());
        Log.v(TAG, "url is......." + trip.getImageURL());
        Picasso.get().load(trip.getImageURL()).into(tripImage);
        textViewName = findViewById(R.id.textViewName);
        textViewName.setText(trip.getTripName());
        recyclerView = findViewById(R.id.my_recycler_view);
        floatingActionButton = findViewById(R.id.floatingActionButtonChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = editTextMessage.getText().toString();
                if (TextUtils.isEmpty(msg) &&
                        TextUtils.isEmpty(imageURL)) {
                    editTextMessage.setError("Enter a message Or attach image!");
                }
                else {
                    String message = editTextMessage.getText().toString();
                    if (TextUtils.isEmpty(message)) {
                        Toast.makeText(CreateChatroomActivity.this, "Post is posted",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Log.v(TAG, "user = " + user.getDisplayName());
                    database.collection(collectionName)
                            .add(new ChatroomMessage
                                    (message, user.getEmail(), userId,new Date().getTime(),imageURL));
                    editTextMessage.setText("");
                }
            }
        });

        floatingAttachmentButton = findViewById(R.id.floatingAttachmentButton);
        floatingAttachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();

            }
        });
        Log.v(TAG,"In CreateChatroomActivity ");
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        userId = user.getUid();
        Log.v(TAG, "user = " + user);
        database = FirebaseFirestore.getInstance();

        query = database.collection(collectionName).orderBy("messageTiming");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    //pgBar.setVisibility(View.GONE);
                }
            }
        });

        chatroomAdapter = new ChatroomAdapter(CreateChatroomActivity.this, query, userId);
        recyclerView.setAdapter(chatroomAdapter);

         new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT |
                ItemTouchHelper.LEFT ) {

             @Override
             public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                 Log.v(TAG,"***getMovementFlags*** user.getEmail() = " + user.getEmail() );
                 if(chatroomAdapter.getUser().equals(user.getEmail())){
                     Log.v(TAG,"It is Admin User so should delete " );
                     return makeMovementFlags(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
                 } else {
                     Log.v(TAG,"It is NOT Admin User so should NOT delete " );
                     return 0;
                 }
             }

             @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Toast.makeText(CreateChatroomActivity.this, "OnMove!", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {


                if(chatroomAdapter.getUser().equals(user.getEmail())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateChatroomActivity.this);
                    // Specify the dialog is not cancelable
                    builder.setCancelable(false);

                    // Set a title for alert dialog
                    builder.setTitle("Delete Message");
                    builder.setMessage("Are you sure ?" );

                    // Set the positive/yes button click listener
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something when click positive button
                           chatroomAdapter.deleteItem(viewHolder.getAdapterPosition());
                        }

                    });

                    // Set the negative/no button click listener
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do something
                            chatroomAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    startActivity(new Intent(CreateChatroomActivity.this, CreateChatroomActivity.class));
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(chatroomAdapter != null) {
            chatroomAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(chatroomAdapter != null) {
            chatroomAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void selectImage(){
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Camera")){

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, CAMERA_KEY);
                    }
                } else if(items[i].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_KEY);
                } else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        }).show();
    }

    // RETURNING FROM GALLERY OR CAMERA

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_KEY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            //Picasso.get().load(selectedImageUri).into(imageView);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
           // imageView.setImageBitmap(bitmap);
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            photoObject = new Photo(bitmapUpload, path, null);

        }

        if (requestCode == CAMERA_KEY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            //imageView.setImageBitmap(bitmap);
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            photoObject = new Photo(bitmapUpload,path,null);

        }

        final StorageReference imageRepo = storageReference.child(photoObject.path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap photoBitmap = photoObject.photo;
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] dataByteArray = baos.toByteArray();
        //upload to storage
        UploadTask uploadTask = imageRepo.putBytes(dataByteArray);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
//                Log.d(TAG, "Image Download URL:::::::::::"+ imageRepo.getDownloadUrl());
//                imageURL = imageRepo.getDownloadUrl().toString();

                return imageRepo.getDownloadUrl();
            }
        })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "Image Download URL:::::::::"+ task.getResult());
                            imageURL = task.getResult().toString();
                            photoObject.downloadURL = imageURL;
                            String message = editTextMessage.getText().toString();
                            if (TextUtils.isEmpty(message) &&
                                    TextUtils.isEmpty(imageURL)) {
                                editTextMessage.setError("Enter a message Or attach image!");
                                return;
                            }


                            Log.v(TAG, "user = " + user.getDisplayName());
                            database.collection(collectionName)
                                    .add(new ChatroomMessage
                                            (message, user.getEmail(), userId,new Date().getTime(),imageURL));
                            editTextMessage.setText("");
                        }
                    }
                });


        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }

        });
    }

}

