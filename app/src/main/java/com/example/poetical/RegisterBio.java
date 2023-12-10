package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterBio extends AppCompatActivity {
    EditText editText;
    String bio;
    Button regbio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bio);
        editText = findViewById(R.id.input_bio);
        FileOps fileOps = new FileOps(RegisterBio.this);
        if (!fileOps.readIntStorage("userbio.txt").equals("")) {
            editText.setText(fileOps.readIntStorage("userbio.txt"));
        }
        regbio = findViewById(R.id.bioContinueButton);
        regbio.setOnClickListener(view -> {
            bio = editText.getText().toString().trim();
            if (bio.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this); // context is the current activity or application context
                builder.setTitle("Invalid Credentials").setMessage("Please fill in bio").setPositiveButton("Ok", (dialog, id) -> {
                    dialog.cancel();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                regbio.setEnabled(false);
                Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
                fileOps.writeToIntFile("userbio.txt", bio);
                signUpUser(fileOps.readIntStorage("useremail.txt"), "12345678");
            }
        });
    }

    public void signUpUser(String email, String password) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterBio.this, "Successful", Toast.LENGTH_SHORT).show();
                        //password reset mail
                        auth.sendPasswordResetEmail(email.trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // if isSuccessful then done message will be shown
                                    // and you can change the password
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this);
                                    builder.setTitle("Verify Email").setMessage("You would receive an email shortly, please use that to set your password and log in to your account").setPositiveButton("OK", (dialog, id) -> {
                                        dialog.cancel();
                                        startActivity(new Intent(RegisterBio.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else if (task.getException().getMessage().contains("There is no user record corresponding to this identifier")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this);
                                    builder.setTitle("Invalid Email").setMessage("This email account does not exist, please check the email and try again").setPositiveButton("Sign Up", (dialog, id) -> {
                                        dialog.cancel();
                                    }).setNegativeButton("Cancel", (dialog, id) -> {
                                        dialog.cancel();
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                } else {
                                    Toast.makeText(RegisterBio.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //Toast.makeText(ResetPasswordActivity.this,"Error Failed",Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (task.getException().getMessage().contains("The email address is already in use by another account")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this);
                        builder.setTitle("Invalid Email").setMessage("This email account exists already").setPositiveButton("Login", (dialog, id) -> {
                            dialog.cancel();
                            startActivity(new Intent(RegisterBio.this, LoginActivity.class));
                        }).setNegativeButton("Cancel", (dialog, id) -> {
                            dialog.cancel();
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else if (task.getException().getMessage().contains("Password should be at least 6 characters")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this);
                        builder.setTitle("Invalid Password").setMessage("Password should be at least 6 characters").setPositiveButton("OK", (dialog, id) -> {
                            dialog.cancel();
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(RegisterBio.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}