package com.example.parta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText editTextFirstName;
    EditText editTextLastName;
    TextView textViewUsername;
    Button buttonSave;
    RadioGroup radioGroup;
    RadioButton radioButtonMale;
    RadioButton radioButtonFemale;
    ImageView imageView;
    String gender = "";
    int CAMERA_KEY = 100;
    int GALLERY_KEY = 200;
    String TAG = "EditActivity";
    private FirebaseAuth mAuth;
    Gson gson;
    String imageURL = "https://cdn4.iconfinder.com/data/icons/flatified/512/photos.png";
    String path = "";
    String directoryName = "photos/";
    Bitmap bitmapUpload = null;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    final StorageReference storageReference = firebaseStorage.getReference();
    Uri selectedImageUri;
    Photo photoObject = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setTitle("Edit Profile");

        editTextFirstName = findViewById(R.id.editFirstName);
        editTextLastName = findViewById(R.id.editLastName);
        buttonSave = findViewById(R.id.buttonSaveProfile);
        imageView = findViewById(R.id.editImage);
        textViewUsername = findViewById(R.id.defaultUsername);
        radioGroup = findViewById(R.id.radioGroup2);
        radioButtonMale = findViewById(R.id.radioButton);
        radioButtonFemale = findViewById(R.id.radioButton2);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });


        final SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences
                (ApplicationConstants.SHARED_PREFERENCES_KEY, MODE_PRIVATE);

        final String userInfoListJsonString = sharedPreferences.getString
                (ApplicationConstants.SHARED_PREFERENCES_JSON_STUDEN_OBJECT_KEY, "");
        gson = new Gson();
        final User userDetailsObject = gson.fromJson(userInfoListJsonString,
                User.class);


        if (userDetailsObject != null) {
            textViewUsername.setText(userDetailsObject.getUsername());
            editTextFirstName.setText(userDetailsObject.getFirstName());
            editTextLastName.setText(userDetailsObject.getLastName());
            if (userDetailsObject.getGender().equals("Male")) {
                radioButtonMale.setChecked(true);
                gender = "Male";
            } else {
                radioButtonFemale.setChecked(true);
                gender = "Female";
            }
            Picasso.get().load(userDetailsObject.getImageURL()).into(imageView);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioButton2:
                        gender = "Female";
                        break;
                    case R.id.radioButton:
                        gender = "Male";
                        break;
                    default:
                        break;
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!validateForm()) {
                    Toast.makeText(EditActivity.this, "Enter Valid Input", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    db.collection("users")
                            .document(userDetailsObject.getUsername())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {

                                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                            Map<String, Object> userMap;
                                            userMap = document.getData();
                                            Log.v(TAG, "!!!!!imageURL  before saving = " + imageURL);

                                            if (photoObject != null) {
                                                imageURL = photoObject.downloadURL;
                                            } else {
                                                if (!userDetailsObject.getImageURL().equals("")) {
                                                    imageURL = userDetailsObject.getImageURL();
                                                } else {
                                                    imageURL = "https://cdn4.iconfinder.com/data/icons/flatified/512/photos.png";
                                                }
                                            }
                                            final User userNew = new User(
                                                    textViewUsername.getText().toString(),
                                                    editTextFirstName.getText().toString(),
                                                    editTextLastName.getText().toString(),
                                                    gender, (String) userMap.get("userId"),
                                                    imageURL
                                            );
                                            Log.v("edit", " first name = " + editTextFirstName.getText().toString());
                                            String userDetailsGsonString = gson.toJson(userNew);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(ApplicationConstants.SHARED_PREFERENCES_JSON_STUDEN_OBJECT_KEY,
                                                    userDetailsGsonString);
                                            editor.commit();
                                            Intent intent = new Intent();
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        } else {
                                            Log.d(TAG, "No such document");
                                        }
                                    } else {
                                        Log.d(TAG, "Failed!!!! ", task.getException());
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String firstName = editTextFirstName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("Required First Name.");
            valid = false;
        } else {
            editTextFirstName.setError(null);
        }
        String lastName = editTextLastName.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Required Last Name.");
            valid = false;
        } else {
            editTextLastName.setError(null);
        }

        return valid;
    }


    public void SelectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_KEY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Picasso.get().load(selectedImageUri).into(imageView);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            photoObject = new Photo(bitmapUpload, path, null);
        }

        if (requestCode == CAMERA_KEY && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            Log.v(TAG, "Uploading image now...............");
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            photoObject = new Photo(bitmapUpload, path, null);
            Log.v(TAG, "!!!!before  uploadImage imageURL = " + imageURL);
        }
        final StorageReference imageRepo = storageReference.child(photoObject.path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap photoBitmap = photoObject.photo;
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data1 = baos.toByteArray();
        UploadTask uploadTask = imageRepo.putBytes(data1);
        Log.v(TAG, "!!!!BEginnning -->inside  uploadImage imageURL = " + imageURL);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                Log.d(TAG, "Image Download URL" + imageRepo.getDownloadUrl());
                return imageRepo.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Image Download URL" + task.getResult());
                    imageURL = task.getResult().toString();
                    photoObject.downloadURL = imageURL;
                }
            }


        });
        Log.v(TAG, "!!!!ending  uploadImage imageURL = " + imageURL);
    }
}
