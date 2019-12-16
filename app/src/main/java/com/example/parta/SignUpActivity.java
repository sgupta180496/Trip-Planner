package com.example.parta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {

    ImageView imageView;
    EditText editTextNewUsername;
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextNewPassword;
    Button buttonSignUp;
    RadioGroup radioGroup;
    RadioButton radioButtonMale;
    RadioButton radioButtonFemale;
    String TAG = "SignUp";
    int CAMERA_KEY = 100;
    int GALLERY_KEY = 200;
    String path = "";
    String directoryName = "photos/";
    Bitmap bitmapUpload = null;
    String imageURL = "https://cdn4.iconfinder.com/data/icons/flatified/512/photos.png";
    Uri selectedImageUri;
    String gender = "Male";
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();
    private FirebaseAuth mAuth;
    boolean isValid = false;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Photo photoObject;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign Up");


        imageView = findViewById(R.id.imageViewSignUp);
        editTextNewUsername = findViewById(R.id.editTextCreateUsername);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextNewPassword = findViewById(R.id.editTextCreatePwd);
        buttonSignUp = findViewById(R.id.buttonSignUpPage);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);


        //RADIO BUTTON

        radioButtonMale.setChecked(true);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioButtonFemale:
                        gender = "Female";
                        break;
                    case R.id.radioButtonMale:
                        gender = "Male";
                        break;
                    default:
                        break;
                }
            }
        });


        //SIGN UP BUTTON

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    registerUser(editTextNewUsername.getText().toString(),
                            editTextNewPassword.getText().toString());
            }
        });


        //CLICK ON IMAGE VIEW

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("check", "select image");
                SelectImage();
            }
        });
    }


    //REGISTER USER USING EMAIL AND PASSWORD

    void registerUser(String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            Toast.makeText(SignUpActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user1 = mAuth.getCurrentUser();
                            String userId = user1.getUid();
                            onSuccessRegistration(userId);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed."
                                            + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                            isValid = false;
                        }
                    }
                });
    }


    // VALIDATIONS

    private boolean validateForm() {
        boolean valid = true;

        String email = editTextNewUsername.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editTextNewUsername.setError("Required Username");
            valid = false;
        }

        String password = editTextNewPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            editTextNewPassword.setError("Required Password.");
            valid = false;
        }

        String firstName = editTextFirstName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            editTextFirstName.setError("Required First Name.");
            valid = false;
        }

        String lastName = editTextLastName.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            editTextLastName.setError("Required Last Name.");
            valid = false;
        }
        return valid;
    }


    //CREATE USER AND STORE USING SHARED PREFERENCES

    public void onSuccessRegistration(String userId) {
        Log.v(TAG, "URL is.................." + imageURL);
        if(photoObject != null) {
            imageURL = photoObject.downloadURL ;
        } else {
            imageURL = "https://cdn4.iconfinder.com/data/icons/flatified/512/photos.png";
        }
        User user = new User(editTextNewUsername.getText().toString(),
                editTextFirstName.getText().toString(),
                editTextLastName.getText().toString(),
                gender, userId,
                imageURL
        );

        Gson gson = new Gson();
        String userDetailsGsonString = gson.toJson(user);
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                ApplicationConstants.SHARED_PREFERENCES_KEY, MODE_PRIVATE);

        // Put the json format string to SharedPreferences object.
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ApplicationConstants.SHARED_PREFERENCES_JSON_STUDEN_OBJECT_KEY,
                userDetailsGsonString);
        editor.commit();
        Intent dashboardIntent = new Intent(SignUpActivity.this,
                DashboardActivity.class);
        //Database call to add users
        addUsers(user);
        startActivity(dashboardIntent);
    }


    // ADD USER IN THE DATABASE

    public void addUsers(User user) {

        db.collection(ApplicationConstants.USERS)
                .document(user.getUsername())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Added User",Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getApplicationContext(), "Error Adding Users",Toast.LENGTH_LONG).show();
                    }
                });
    }


    //SELECT IMAGE USING CAMERA OR GALLERY

    public void SelectImage(){
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
            path = directoryName + UUID.randomUUID() + ".png";
            bitmapUpload = bitmap;
            photoObject = new Photo(bitmapUpload,path,null);
        }

        final StorageReference imageRepo = storageReference.child(photoObject.path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap photoBitmap = photoObject.photo;
        photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] photoData = baos.toByteArray();
        UploadTask uploadTask = imageRepo.putBytes(photoData);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return imageRepo.getDownloadUrl();
            }
        })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Log.v(TAG, "Image Download URL:::::::::"+ task.getResult());
                            imageURL = task.getResult().toString();
                            photoObject.downloadURL = imageURL;
                        }
                    }
                });
    }
}
