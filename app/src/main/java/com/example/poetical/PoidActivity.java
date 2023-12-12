package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class PoidActivity extends AppCompatActivity {
    TextView success, audiotxt;
    EditText title, details;
    ImageView audio;
    Button btn;
    static final int PICK_AUDIO_REQUEST = 101;
    private Uri audioUri;
    DataPoems dataPoems;
    FileOps fileOps;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poid);
        fileOps=new FileOps(PoidActivity.this);
        title = findViewById(R.id.titleeditText);
        details = findViewById(R.id.detailseditText);
        audio = findViewById(R.id.imageView);
        audiotxt = findViewById(R.id.audiotxt);
        success = findViewById(R.id.successtxt);
        success.setVisibility(View.INVISIBLE);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> {
            String poemtitle = title.getText().toString().trim();
            String poemdetails = details.getText().toString().trim();
            if (poemdetails.trim().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                builder.setTitle("Error")
                        .setMessage("Please fill in poem details")
                        .setCancelable(false)
                        .setPositiveButton("FIX", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
            //
            else if (poemtitle.trim().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                builder.setTitle("Error")
                        .setMessage("Please fill in poem title")
                        .setCancelable(false)
                        .setPositiveButton("FIX", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
            } else {
                if(audioUri==null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                    builder.setTitle("Error")
                            .setMessage("Please choose an audio file")
                            .setCancelable(false)
                            .setPositiveButton("FIX", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
                else{
                    uploadAudio(poemtitle,poemdetails,audioUri);
                }
            }
        });
        audio.setOnClickListener(v -> {
            openFileChooser();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();
            success.setVisibility(View.VISIBLE);
            // Now you can upload the audio file to Firebase
        }
    }
    private void uploadAudio(String poemtitle,String poemcontents,Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait..."); // Set your desired message
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled with the back button
        progressDialog.show();

        btn.setEnabled(false);
        ContentResolver contentResolver = getContentResolver();
        String[] projection = {MediaStore.Audio.Media.SIZE};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            if (size > 1024 * 15360) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                builder.setTitle("Invalid Audio").setMessage("Audio is too large, please use an audio less than 15mb").setPositiveButton("OK", (dialog, id) -> {
                    dialog.cancel();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                String a = UUID.randomUUID().toString() + ".mp3";
                StorageReference imagesRef = storageRef.child("audios/" + poemtitle+a);
                UploadTask uploadTask = imagesRef.putFile(uri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Handle successful upload of image
                        uploadToFirebase(poemtitle,poemcontents,poemtitle+a);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        if (exception.toString().contains("An unknown error occurred")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                            builder.setTitle("Invalid Image").setMessage("Couldn't save image, please try again later").setPositiveButton("OK", (dialog, id) -> {
                                dialog.cancel();
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            btn.setEnabled(true);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PoidActivity.this);
                            builder.setTitle("Invalid Image").setMessage("Couldn't process image " + exception.toString()).setPositiveButton("OK", (dialog, id) -> {
                                dialog.cancel();
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            btn.setEnabled(true);
                        }
                    }
                });
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }
    private void uploadToFirebase(String poemtitle,String poemcontent,String uri) {
        dataPoems=new DataPoems();
        dataPoems.setEmail(fileOps.readIntStorage("useremail.txt"));
        dataPoems.setName(fileOps.readIntStorage("username.txt"));
        dataPoems.setTitle(poemtitle);
        dataPoems.setContent(poemcontent);
        dataPoems.setPhotoUrl(fileOps.readIntStorage("profileimage.txt"));
        dataPoems.setAudioUrl(uri);
        dataPoems.setType("poid");
        dataPoems.setLikeamount("0");
        dataPoems.setViewamount("0");
        dataPoems.setVerified(fileOps.readIntStorage("userverif.txt"));
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("All Poems");
        String key=databaseReference.push().getKey();
        dataPoems.setPostKey(key);
        valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(key).setValue(dataPoems);
                databaseReference.removeEventListener(valueEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        Toast.makeText(this, "Poid uploaded successfully", Toast.LENGTH_SHORT).show();
        title.setText("");
        details.setText("");
        finish();
    }
}