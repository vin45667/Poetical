package com.example.poetical;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProPicActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 101;
    CircleImageView profileImage;
    ImageView edit, profileImageLarge;
    ProgressBar progressBar;
    Uri imageUri=null;
    Button continuebtn;
    String prevfilename;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_pic);
        continuebtn=findViewById(R.id.continuebtn);
        continuebtn.setEnabled(false);
        FileOps fileOps = new FileOps(ProPicActivity.this);
        prevfilename = fileOps.readIntStorage("profileimage.txt");
        edit = findViewById(R.id.edit);
        progressBar = findViewById(R.id.progressBar);
        profileImage = findViewById(R.id.profileImage);
        profileImageLarge = findViewById(R.id.profileImageLarge);
        profileImage.setOnClickListener(view -> {
            if(imageUri!=null) {
                profileImageLarge.setVisibility(View.VISIBLE);
                continuebtn.setVisibility(View.INVISIBLE);
            }
        });
        edit.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        continuebtn.setOnClickListener(view -> {
            if(imageUri==null){
                Toast.makeText(ProPicActivity.this, "Please add a profile image", Toast.LENGTH_SHORT).show();
            }
            else{
                startActivity(new Intent(this, CoverPicActivity.class));
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (profileImageLarge.getVisibility() == View.VISIBLE) {
                    profileImageLarge.setVisibility(View.INVISIBLE);
                    continuebtn.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            profileImageLarge.setImageURI(imageUri);
            continuebtn.setEnabled(false);
            Toast.makeText(ProPicActivity.this, " Updating profile photo", Toast.LENGTH_SHORT).show();
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        String[] projection = {MediaStore.Images.Media.SIZE};
        Cursor cursor = contentResolver.query(imageUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
            if (size > 1024 * 15360) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProPicActivity.this);
                builder.setTitle("Invalid Image").setMessage("Image is too large, please use an image less than 15mb").setPositiveButton("OK", (dialog, id) -> {
                    dialog.cancel();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                String a = UUID.randomUUID().toString() + ".jpg";
                StorageReference imagesRef = storageRef.child("images/" + a);
                FileOps fileOps = new FileOps(ProPicActivity.this);
                fileOps.writeToIntFile("profileimage.txt", a);
                UploadTask uploadTask = imagesRef.putFile(imageUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle successful upload of image
                        progressBar.setVisibility(View.INVISIBLE);
                        replacePhotoUrl();
                        Toast.makeText(ProPicActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                        continuebtn.setEnabled(true);
                        FirebaseStorage storage2 = FirebaseStorage.getInstance();
                        StorageReference storageRef2 = storage2.getReference();
                        StorageReference imagesRef2 = storageRef2.child("images/" + prevfilename);
                        Task<Void> task = imagesRef2.delete();
                        task.addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //Toast.makeText(ChangeProPicActivity.this, "prev photo deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Toast.makeText(ProPicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception.toString().contains("An unknown error occurred")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProPicActivity.this);
                            builder.setTitle("Invalid Image").setMessage("Couldn't save image, please try again later").setPositiveButton("OK", (dialog, id) -> {
                                dialog.cancel();
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProPicActivity.this);
                            builder.setTitle("Invalid Image").setMessage("Couldn't process image " + exception.toString()).setPositiveButton("OK", (dialog, id) -> {
                                dialog.cancel();
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void replacePhotoUrl() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        FileOps fileOps = new FileOps(ProPicActivity.this);
        String emailToSearch = fileOps.readIntStorage("useremail.txt");
        Query query = usersRef.orderByChild("email").equalTo(emailToSearch);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if there is a user with the matching email address
                if (dataSnapshot.exists()) {
                    // Get a reference to the user node with the matching email address
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    DatabaseReference userRef = userSnapshot.getRef();
                    // Update the photourl value for the user
                    userRef.child("photoUrl").setValue(fileOps.readIntStorage("profileimage.txt"));
                } else {
                    // Handle the case where no user with the matching email address is found
                    // Toast.makeText(ProPicActivity.this, "User doesn't exist " + emailToSearch, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error case
                // Toast.makeText(ProPicActivity.this, "Ex: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}