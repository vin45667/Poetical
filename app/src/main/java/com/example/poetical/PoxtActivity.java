package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PoxtActivity extends AppCompatActivity {
    EditText title, details;
    Button btn;
    DataPoems dataPoems;
    FileOps fileOps;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poxt);
        fileOps=new FileOps(PoxtActivity.this);
        title = findViewById(R.id.titleeditText);
        details = findViewById(R.id.detailseditText);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> {
            String poemtitle = title.getText().toString().trim();
            String poemdetails = details.getText().toString().trim();
            if (poemdetails.trim().equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PoxtActivity.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(PoxtActivity.this);
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
            }
            else{
                sendToDatabase(poemtitle,poemdetails);
            }
        });
    }

    public void sendToDatabase(String title,String details) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait..."); // Set your desired message
        progressDialog.setCancelable(false); // Set whether the dialog can be canceled with the back button
        progressDialog.show();
        dataPoems=new DataPoems();
        dataPoems.setEmail(fileOps.readIntStorage("useremail.txt"));
        dataPoems.setName(fileOps.readIntStorage("username.txt"));
        dataPoems.setTitle(title);
        dataPoems.setContent(details);
        dataPoems.setPhotoUrl(fileOps.readIntStorage("profileimage.txt"));
        dataPoems.setPhotoUrl2(fileOps.readIntStorage("coverimage.txt"));
        dataPoems.setAudioUrl("none");
        dataPoems.setLikeamount("0");
        dataPoems.setViewamount("0");
        dataPoems.setType("poxt");
        dataPoems.setVerified(fileOps.readIntStorage("userverif.txt"));
        firebaseDatabase=FirebaseDatabase.getInstance();
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
        Toast.makeText(this, "Poxt uploaded successfully", Toast.LENGTH_LONG).show();
        this.title.setText("");
        this.details.setText("");
        startActivity(new Intent(this,HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}