package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    Button resetbtn;
    EditText emailedittext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        FileOps fileOps=new FileOps(ResetPasswordActivity.this);
        resetbtn=findViewById(R.id.resetbtn);
        emailedittext=findViewById(R.id.emailedittext);
        emailedittext.setText(fileOps.readIntStorage("useremail.txt"));
        //emailedittext.setEnabled(false);
        //emailedittext.setAlpha(0.5F);
        resetbtn.setOnClickListener(view -> {
            Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
            FirebaseAuth mAuth=FirebaseAuth.getInstance();
            mAuth.sendPasswordResetEmail(emailedittext.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        // if isSuccessful then done message will be shown
                        // and you can change the password
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                        builder.setTitle("Verify Email")
                                .setMessage("If this email account exists, you would receive an email shortly")
                                .setPositiveButton("OK", (dialog, id) -> {
                                    dialog.cancel();
                                    startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else if (task.getException().getMessage().contains("There is no user record corresponding to this identifier")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);
                        builder.setTitle("Invalid Email")
                                .setMessage("This email account does not exist, please check the email and try again")
                                .setPositiveButton("Sign Up", (dialog, id) -> {
                                    dialog.cancel();
                                    startActivity(new Intent(ResetPasswordActivity.this, RegisterActivity.class));
                                })
                                .setNegativeButton("Cancel", (dialog, id) -> {
                                    dialog.cancel();
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else {
                        Toast.makeText(ResetPasswordActivity.this,task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //Toast.makeText(ResetPasswordActivity.this,"Error Failed",Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}