package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class RegisterActivity extends AppCompatActivity {
    private String email, fname, lname, number, country, countryCode, countryNameCode;
    private EditText mEmail, mFname, mLname, mNumber;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private String append = "";
    CountryCodePicker countryCodePicker;

    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    CheckBox checkBox;
    int READ_CONTACTS_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
        } else {
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
            showPermissionRationale();
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // Permission was previously denied by the user
            // You can show a rationale to explain why the permission is needed
            // After explaining, you can request the permission again
            showPermissionRationale();
        }
        initWidgets();
        init();
        checkBox = findViewById(R.id.checkBox);
        TextView linkTextView = findViewById(R.id.linkTextView);

        // Add a click listener to the link TextView
        linkTextView.setOnClickListener(v -> {
            // Replace "https://example.com/terms" with the actual URL of your terms and conditions
            String url = "https://sites.google.com/view/peepzprivacypolicy/home";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }


    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = mEmail.getText().toString().trim();
                fname = mFname.getText().toString().trim();
                lname = mLname.getText().toString().trim();
                number = mNumber.getText().toString().trim();

                String fullname = fname + " " + lname;
                if (checkInputs(email, fullname, number)) {
                    FileOps fileOps = new FileOps(RegisterActivity.this);
                    if (!email.endsWith("@gmail.com")) {
                        Toast.makeText(RegisterActivity.this, "Please use a gmail address", Toast.LENGTH_LONG).show();
                    } else if (!checkBox.isChecked()) {
                        Toast.makeText(RegisterActivity.this, "Read and agree to the privacy policy", Toast.LENGTH_LONG).show();
                    } else {
                        fileOps.writeToIntFile("useremail.txt", email);
                        country = countryCodePicker.getSelectedCountryName();
                        countryNameCode = countryCodePicker.getSelectedCountryNameCode();
                        countryCode = countryCodePicker.getSelectedCountryCode();

                        fileOps.writeToIntFile("username.txt", fullname);
                        fileOps.writeToIntFile("userfname.txt", fname);
                        fileOps.writeToIntFile("userlname.txt", lname);
                        fileOps.writeToIntFile("usernumber.txt", number);
                        fileOps.writeToIntFile("usercountry.txt", country);
                        fileOps.writeToIntFile("usercountrynamecode.txt", countryNameCode);
                        fileOps.writeToIntFile("usercountrycode.txt", countryCode);

                        Intent intent = new Intent(RegisterActivity.this, RegisterLocation.class);
//                    User user = new User("", "", "", "",fullname, email, false, false, false, false, "", "", "", latitude, longtitude);
//                    intent.putExtra("password", password);
//                    intent.putExtra("classUser", user);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private boolean checkInputs(String email, String name, String number) {
        if (email.equals("") || name.equals("") || number.equals("")) {
            Toast.makeText(RegisterActivity.this, "All fields must be filed out.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Below code checks if the email id is valid or not.
        if (!email.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid email address, enter valid email id and click on Continue", Toast.LENGTH_SHORT).show();
            return false;

        }


        return true;
    }

    private void initWidgets() {
        FileOps fileOps = new FileOps(RegisterActivity.this);
        mEmail = findViewById(R.id.input_email);
        btnRegister = findViewById(R.id.btn_register);
        mFname = findViewById(R.id.input_fname);
        mLname = findViewById(R.id.input_lname);
        mNumber = findViewById(R.id.input_number);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        //TODO: countryCodePicker.setCountryForNameCode("");
        if (!fileOps.readIntStorage("useremail.txt").equals("")) {
            mEmail.setText(fileOps.readIntStorage("useremail.txt"));
        }
        if (!fileOps.readIntStorage("userfname.txt").equals("")) {
            mFname.setText(fileOps.readIntStorage("userfname.txt"));
        }
        if (!fileOps.readIntStorage("userlname.txt").equals("")) {
            mLname.setText(fileOps.readIntStorage("userlname.txt"));
        }
        if (!fileOps.readIntStorage("usernumber.txt").equals("")) {
            mNumber.setText(fileOps.readIntStorage("usernumber.txt"));
        }
        if (!fileOps.readIntStorage("usercountrycode.txt").equals("")) {
            countryCodePicker.setCountryForPhoneCode(Integer.parseInt(fileOps.readIntStorage("usercountrycode.txt")));
        }


    }

    public void onLoginClicked(View view) {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
    private void showPermissionRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed");
        builder.setMessage("We need access to your contacts to provide awesome features. Please grant the permission.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Request the permission again
                requestPermission();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(RegisterActivity.this, "Permission Required", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.show();
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_REQUEST_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
            else{
                showPermissionRationale();
            }
        }
    }
}
