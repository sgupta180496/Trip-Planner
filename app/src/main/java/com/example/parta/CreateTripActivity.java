package com.example.parta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateTripActivity extends AppCompatActivity {

    ImageView tripImage;
    EditText editTextTripName;
    EditText editTextLocationLatitude;
    EditText editTextLocationLongitude;
    Button buttonCreateTrip;
    int CAMERA_KEY = 100;
    int GALLERY_KEY = 200;
    Uri selectedImageUri;
    String path = "";
    String directoryName = "photos/";
    Bitmap bitmapUpload = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Button buttonAddUsers;
    List<User> usersList;
    String imageURL = "https://cdn4.iconfinder.com/data/icons/flatified/512/photos.png";
    Map<String, String> mapUserDetails = new HashMap<>();
    List<String> emailAddressList = new ArrayList<>();
    String TAG = "CreateTripActivity";
    private FirebaseAuth mAuth;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);
        setTitle("Create Trip");
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user1 = mAuth.getCurrentUser();

        tripImage = findViewById(R.id.tripImage);
        editTextTripName = findViewById(R.id.editTextTripName);
        editTextLocationLatitude = findViewById(R.id.editTextLocationLatitude);
        editTextLocationLongitude = findViewById(R.id.editTextLocationLongitude);
        buttonCreateTrip = findViewById(R.id.buttonCreateTrip);
        buttonAddUsers = findViewById(R.id.buttonAddUsers);
        listAllUsers();
        mapUserDetails.put(user1.getEmail(), user1.getUid());

        tripImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        buttonAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTripActivity.this);
                final String[] usersEmailArray = new String[emailAddressList.size() - 1];
                Log.v(TAG,"***Size == " + emailAddressList.size());
                int j = 0;
                for (int i = 0; i < emailAddressList.size(); i++) {
                    Log.v(TAG," User Email = " + user1.getEmail()
                            + " emailAddressList = " + emailAddressList.get(i));
                    //usersEmailArray[i] = emailAddressList.get(i);
                    if( !user1.getEmail().equals(emailAddressList.get(i))) {
                        usersEmailArray[j++] = emailAddressList.get(i);
                        Log.v(TAG,"  Added i= " + i );
                    }
                }

                for (int i = 0; i < usersEmailArray.length; i++) {
                    Log.v(TAG,"  usersEmailArray ==> " + usersEmailArray[i] );
                }

                builder.setMultiChoiceItems(usersEmailArray, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    db.collection(ApplicationConstants.USERS)
                                            .whereEqualTo("username", usersEmailArray[which])
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                            String key = (String) document.getData().get("username");
                                                            String value = (String) document.getData().get("userId");
                                                            mapUserDetails.put(
                                                                    key, value
                                                            );
                                                        }
                                                    } else {
                                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                }
                            }
                        });


                // Specify the dialog is not cancelable
                builder.setCancelable(false);

                // Set a title for alert dialog
                builder.setTitle("Add Users");

                // Set the positive/yes button click listener
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something when click positive button
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
        });

        buttonCreateTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    Log.v("Check", "Trip imageURL = " + imageURL);
                    Trip trip = new Trip(imageURL, editTextTripName.getText().toString(),
                            editTextLocationLatitude.getText().toString(),
                            editTextLocationLongitude.getText().toString(),
                            mapUserDetails,
                            user1.getEmail()
                    );
                    addTrips(trip);
                    //finish();
                    Intent intent = new Intent(CreateTripActivity.this, CreateChatroomActivity.class);
                    intent.putExtra("TripDetails", trip);
                    startActivity(intent);
                    Log.v("Check", "Finishing Trip Activity!");
                    finish();
                } else {
                    return;
                }
            }
        });
    }

    public void createTrip() {
    }

    public Boolean isValid() {
        boolean isvalid = true;

        String tripName = editTextTripName.getText().toString();
        if (TextUtils.isEmpty(tripName)) {
            editTextTripName.setError("Enter Valid Trip Name");
            isvalid = false;
        }

        String latitude = editTextLocationLatitude.getText().toString();
        if (TextUtils.isEmpty(latitude) || Double.parseDouble(latitude) < -90 || Double.parseDouble(latitude) > 90) {
            editTextLocationLatitude.setError("Enter Valid Coordinates");
            isvalid = false;
        }

        String longitude = editTextLocationLongitude.getText().toString();
        if (TextUtils.isEmpty(longitude) || Double.parseDouble(longitude) < -180 || Double.parseDouble(longitude) > 180) {
            editTextLocationLongitude.setError("Enter Valid Coordinates");
            isvalid = false;
        }

        return isvalid;
    }

    public void addTrips(Trip trip) {

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

    }

    public void listAllUsers() {

        db.collection(ApplicationConstants.USERS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        usersList = queryDocumentSnapshots.toObjects(User.class);

                        Log.v("Usersssss", "Users List = " + usersList);
                        //Creating a List of emailAddress
                        for (User user : usersList) {
                            emailAddressList.add(user.getUsername());
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //Toast.makeText(context, AppConstants.GET_MOVIE_ERROR,Toast.LENGTH_LONG).show();

                    }
                });

    }

    //SELECT IMAGE USING CAMERA OR GALLERY

    public void SelectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, CAMERA_KEY);
                    }
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_KEY);
                } else if (items[i].equals("Cancel")) {
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
            Picasso.get().load(selectedImageUri).into(tripImage);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tripImage.setImageBitmap(bitmap);
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            Photo photoObject = new Photo(bitmapUpload, path, null);
            uploadImage(photoObject);
        }

        if (requestCode == CAMERA_KEY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            tripImage.setImageBitmap(bitmap);
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            Photo photoObject = new Photo(bitmapUpload, path, null);
            uploadImage(photoObject);
        }
    }


    private void uploadImage(final Photo photo) {
        final StorageReference imageRepo = storageReference.child(photo.path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap photoBitmap = photo.photo;
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imageRepo.putBytes(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Image Download URL:::::::::" + task.getResult());
                    imageURL = task.getResult().toString();
                    photo.downloadURL = imageURL;
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
