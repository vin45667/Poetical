package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    Button login;
    EditText password, email;
    DataUser dataUser;
    TextView forgotpass,signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.btn_login);
        forgotpass=findViewById(R.id.forgotpass);
        signup=findViewById(R.id.link_signup);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        forgotpass.setOnClickListener(v -> {
            startActivity(new Intent(this,ResetPasswordActivity.class));
        });
        signup.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        login.setOnClickListener(v -> {
            String emailst = email.getText().toString().trim();
            String passwordst = password.getText().toString().trim();
            if (emailst.equals("") || passwordst.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this); // context is the current activity or application context
                builder.setTitle("Invalid Credentials")
                        .setMessage("Please fill in all fields")
                        .setPositiveButton("Ok", (dialog, id) -> {
                            dialog.cancel();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                //check database if values match
                email.setEnabled(false);
                email.setAlpha(0.5F);
                password.setEnabled(false);
                password.setAlpha(0.5F);
                Toast.makeText(LoginActivity.this, "Please Wait", Toast.LENGTH_SHORT).show();
                try {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(emailst, passwordst).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FileOps fileOps = new FileOps(LoginActivity.this);
                                fileOps.writeToIntFile("useremail.txt", email.getText().toString().trim());
                                fileOps.writeToIntFile("userlog.txt", "1");
                                getUserDetails();
                                //save all details to users database
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else if (task.getException().getMessage().contains("There is no user record corresponding to this identifier")||task.getException().getMessage().contains("The supplied auth credential is incorrect, malformed or has expired")) {
                                email.setEnabled(true);
                                password.setEnabled(true);
                                email.setAlpha(1F);
                                password.setAlpha(1F);
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Invalid Email")
                                        .setMessage("This email account does not exist")
                                        .setPositiveButton("Sign Up", (dialog, id) -> {
                                            dialog.cancel();
                                            startActivity(new Intent(LoginActivity.this, RegisterActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        })
                                        .setNegativeButton("Cancel", (dialog, id) -> {
                                            dialog.cancel();
                                            email.setEnabled(true);
                                            password.setEnabled(true);
                                            email.setAlpha(1F);
                                            password.setAlpha(1F);
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (task.getException().getMessage().contains("The password is invalid")) {
                                email.setEnabled(true);
                                email.setAlpha(1F);
                                password.setEnabled(true);
                                password.setAlpha(1F);
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Invalid Password")
                                        .setMessage("Password is not correct")
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            dialog.cancel();
                                        })
                                        .setNegativeButton("Reset password", (dialog, id) -> {
                                            dialog.cancel();
                                            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else if (task.getException().getMessage().contains("network error")) {
                                email.setEnabled(true);
                                email.setAlpha(1F);
                                password.setEnabled(true);
                                password.setAlpha(1F);
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Network Problems")
                                        .setMessage("Please check your network connection")
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            dialog.cancel();
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                email.setEnabled(true);
                                password.setEnabled(true);
                                email.setAlpha(1F);
                                password.setAlpha(1F);
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("Error")
                                        .setMessage(task.getException().getMessage())
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            dialog.cancel();
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                                //Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUserDetails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FileOps fileOps = new FileOps(LoginActivity.this);
        String email = fileOps.readIntStorage("useremail.txt");
        Query query = usersRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through the results
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    dataUser = snapshot.getValue(DataUser.class);

                    fileOps.writeToIntFile("useremail.txt", dataUser.getEmail());
                    fileOps.writeToIntFile("userverif.txt", dataUser.getVerified());
                    fileOps.writeToIntFile("username.txt", dataUser.getName());
                    fileOps.writeToIntFile("userfname.txt", dataUser.getFname());
                    fileOps.writeToIntFile("userlname.txt", dataUser.getLname());
                    fileOps.writeToIntFile("userbio.txt", dataUser.getAbout());
                    fileOps.writeToIntFile("userage.txt", dataUser.getAge());
                    fileOps.writeToIntFile("profileimage.txt", dataUser.getPhotoUrl());
                    fileOps.writeToIntFile("coverphoto.txt", dataUser.coverPhoto);
                    fileOps.writeToIntFile("usergender.txt", dataUser.getGender());
                    fileOps.writeToIntFile("usercountry.txt", dataUser.getCountry());
                    //fileOps.writeToIntFile("usercountrycode.txt",dataUser.getCountryCode());
                    fileOps.writeToIntFile("usernumber.txt", dataUser.getNumber());
                    fileOps.writeToIntFile("userdob.txt", dataUser.getDateOfBirth());
                    fileOps.writeToIntFile("userstate.txt", dataUser.getState());
                    fileOps.writeToIntFile("usercity.txt", dataUser.getCity());
                    fileOps.writeToIntFile("usercountrycode.txt", dataUser.getCountrycode());
                    fileOps.writeToIntFile("notif.txt", "1");
                    fileOps.writeToIntFile("partnernotif.txt", "1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur
                System.out.println("Error: " + databaseError.getMessage());
            }
        });

    }
}