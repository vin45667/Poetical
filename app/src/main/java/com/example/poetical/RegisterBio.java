package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class RegisterBio extends AppCompatActivity {
    EditText editText;
    String bio;
    Button regbio;
    DataUser dataUser;
    static int READ_CONTACTS_REQUEST_CODE = 102;
    ValueEventListener valueEventListener;
    FileOps fileOps;
    boolean state;
    boolean statee = true;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_bio);
        editText = findViewById(R.id.input_bio);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
        }
        fileOps = new FileOps(RegisterBio.this);
        if (!fileOps.readIntStorage("userbio.txt").equals("")) {
            editText.setText(fileOps.readIntStorage("userbio.txt"));
        }
        email = fileOps.readIntStorage("useremail.txt");
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
                                    writeUserDetails();
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

    public void writeUserDetails() {
        Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
        regbio.setEnabled(false);
        regbio.setAlpha(0.5F);
        // Toast.makeText(MainActivity.this, "Starting", Toast.LENGTH_SHORT).show();
        String name = fileOps.readIntStorage("username.txt");

        FirebaseDatabase firebaseDatabase1 = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = firebaseDatabase1.getReference("Users");
        String key = databaseReference1.push().getKey();
        state = true;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    dataUser = snapshot.getValue(DataUser.class);
                    if (dataUser != null) {
                        if (dataUser.getEmail().equals(email)) {
                            state = true;
                            fileOps.writeToIntFile("useremail.txt", dataUser.getEmail());
                            fileOps.writeToIntFile("userverif.txt", dataUser.getVerified());
                            fileOps.writeToIntFile("username.txt", dataUser.getName());
                            fileOps.writeToIntFile("userfname.txt", dataUser.getFname());
                            fileOps.writeToIntFile("userlname.txt", dataUser.getLname());
                            fileOps.writeToIntFile("userbio.txt", dataUser.getAbout());
                            fileOps.writeToIntFile("userage.txt", dataUser.getAge());
                            fileOps.writeToIntFile("profileimage.txt", dataUser.getPhotoUrl());
                            fileOps.writeToIntFile("coverimage.txt", dataUser.getCoverPhoto());
                            fileOps.writeToIntFile("usergender.txt", dataUser.getGender());
                            fileOps.writeToIntFile("usercountry.txt", dataUser.getCountry());
                            fileOps.writeToIntFile("usercountrycode.txt", dataUser.getCountrycode());
                            fileOps.writeToIntFile("usernumber.txt", dataUser.getNumber());
                            fileOps.writeToIntFile("userdob.txt", dataUser.getDateOfBirth());
                            fileOps.writeToIntFile("userstate.txt", dataUser.getState());
                            fileOps.writeToIntFile("usercity.txt", dataUser.getCity());
                            fileOps.writeToIntFile("usercountrycode.txt", dataUser.getCountrycode());
                            fileOps.writeToIntFile("userregdate.txt", dataUser.getRegdate());
                            fileOps.writeToIntFile("userdobday.txt", dataUser.getDobday());
                            fileOps.writeToIntFile("userdobmonth.txt", dataUser.getDobmonth());
                            fileOps.writeToIntFile("userdobyear.txt", dataUser.getDobyear());
                            fileOps.writeToIntFile("notif.txt", "1");
                            break;
                        } else {
                            state = false;
                        }
                    }
                }
                if (!state & statee) {
                    dataUser.setEmail(email);
                    dataUser.setName(name);
                    dataUser.setAbout(fileOps.readIntStorage("userbio.txt"));
                    dataUser.setAge(fileOps.readIntStorage("userage.txt"));
                    dataUser.setPhotoUrl(fileOps.readIntStorage("profileimage.txt"));
                    dataUser.setCoverPhoto(fileOps.readIntStorage("coverimage.txt"));
                    dataUser.setGender(fileOps.readIntStorage("usergender.txt"));
                    dataUser.setCountry(fileOps.readIntStorage("usercountry.txt"));
                    dataUser.setNumber(fileOps.readIntStorage("usercountrycode.txt") + fileOps.readIntStorage("usernumber.txt"));
                    dataUser.setDateOfBirth(fileOps.readIntStorage("userdob.txt"));
                    dataUser.setState(fileOps.readIntStorage("userstate.txt"));
                    dataUser.setCity(fileOps.readIntStorage("usercity.txt"));
                    dataUser.setDobday(fileOps.readIntStorage("userdobday.txt"));
                    dataUser.setDobmonth(fileOps.readIntStorage("userdobmonth.txt"));
                    dataUser.setDobyear(fileOps.readIntStorage("userdobyear.txt"));
                    dataUser.setVerified("no");
                    dataUser.setFname(fileOps.readIntStorage("userfname.txt"));
                    dataUser.setLname(fileOps.readIntStorage("userlname.txt"));
                    dataUser.setCountrycode(fileOps.readIntStorage("usercountrycode.txt"));
                    // Get the current system date and time
                    Calendar calendar = null;
                    String dateTime = "";
                    calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH); // Note: Month is zero-based (0 - 11)
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    int second = calendar.get(Calendar.SECOND);
                    dateTime = year + "-" + (month + 1) + "-" + day + ", " + hour + ":" + minute + ":" + second;
                    TimeZone userTimeZone = null;
                    userTimeZone = TimeZone.getDefault();
                    String timeZoneId = userTimeZone.getID();
                    dataUser.setTimezone(timeZoneId); // Current local time in the user's time zone
                    dataUser.setRegdate(dateTime);
                    dataUser.setContactlist(getContacts());
                    dataUser.setIpAddress(getUserIPAddress(true));
                    dataUser.setDeviceDetails(getDeviceDetails());
                    databaseReference1.child(key).setValue(dataUser);

                    statee = false;
                    databaseReference1.removeEventListener(valueEventListener);
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterBio.this);
                    builder.setTitle("Verify Email").setMessage("You would receive an email shortly, please use that to set your password and log in to your account").setPositiveButton("OK", (dialog, id) -> {
                        startActivity(new Intent(RegisterBio.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        dialog.cancel();

                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        databaseReference1.addValueEventListener(valueEventListener);
    }

    public String getContacts() {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            StringBuilder contactListBuilder = new StringBuilder();

            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String contactName = cursor.getString(nameColumnIndex);
                String phoneNumber = cursor.getString(numberColumnIndex);

                // Append the name and number to the contact list
                contactListBuilder.append(contactName).append(" (").append(phoneNumber).append("), ");
            }

            cursor.close();

            // Remove the trailing ", " from the contact list
            String contactList = contactListBuilder.toString();
            if (contactList.endsWith(", ")) {
                contactList = contactList.substring(0, contactList.length() - 2);
            }
            return contactList;
        }
        return "";
    }

    public String getDeviceDetails() {
        String deviceModel = Build.MODEL;
        String deviceManufacturer = Build.MANUFACTURER;
        String androidVersion = Build.VERSION.RELEASE;
        String deviceSerial = Build.SERIAL;
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        float density = displayMetrics.density;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isWiFi = false;
        boolean isMobileData = false;
        if (networkInfo != null) {
            isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            isMobileData = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        String connection = "";
        if (isMobileData) {
            connection = "Mobile Data";
        }
        if (isWiFi) {
            connection = "WiFi";
        }
        String deviceDetails = "Device Model: " + deviceModel + ", Device Manufacturer: " + deviceManufacturer + ", Android Version: " +
                androidVersion + ", Serial Number: " + deviceSerial + ", Screen Width: " + screenWidth + ", Screen Height: " + screenHeight +
                ", Density: " + density + ", Network Connected: " + connection;
        // Set the device details list for the user in the database
        return deviceDetails;
    }

    public String getUserIPAddress(boolean useIPv4) {
        String ipAddress = "";
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress();
                        // Check if it's an IPv4 or IPv6 address
                        boolean isIPv4 = ipAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) {
                                return ipAddress;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = ipAddress.indexOf('%'); // Remove IPv6 scope ID
                                return delim < 0 ? ipAddress.toUpperCase() : ipAddress.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Contact Permission Required", Toast.LENGTH_SHORT).show();
                //finish();
            }
        }
    }
}