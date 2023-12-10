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
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RegisterLocation extends AppCompatActivity {
    TextView country;
    EditText city, state;
    static int LOCATION_PERMISSION_REQUEST_CODE = 101;
    Button continuebtn;
    String usercity, userstate;
    double latitude,longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_location);
        city = findViewById(R.id.cityedittxt);
        state = findViewById(R.id.stateedittxt);
        city.setEnabled(false);
        state.setEnabled(false);
        FileOps fileOps = new FileOps(RegisterLocation.this);
        country = findViewById(R.id.countrytextview);
        continuebtn = findViewById(R.id.continuebutton);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Location permission is already granted; you can proceed.
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            // Now, you have the user's coordinates; proceed to reverse geocoding.
                            getAddressFromCoordinates(latitude, longitude);
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Location Access");
                            builder.setMessage("Please turn on location");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Request the permission again
                                    startActivity(new Intent(RegisterLocation.this, RegisterLocation.class));
                                    finish();
                                }
                            });
                            builder.show();
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        // Handle location request failure
                    });
        } else {
            // Location permission is not granted; you need to request it.
            // The user will be prompted to grant or deny the permission.
            showPermissionRationale();
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission was previously denied by the user
            // You can show a rationale to explain why the permission is needed
            // After explaining, you can request the permission again
            showPermissionRationale();
        }


        continuebtn.setOnClickListener(view -> {
            usercity = city.getText().toString().trim();
            userstate = state.getText().toString().trim();
            if (usercity.equals("") || userstate.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Access");
                builder.setMessage("Please allow and turn on location");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Request the permission again
                        startActivity(new Intent(RegisterLocation.this, RegisterLocation.class));
                        finish();
                    }
                });
                builder.show();
            } else {
                fileOps.writeToIntFile("usercity.txt", usercity);
                fileOps.writeToIntFile("userstate.txt", userstate);
                startActivity(new Intent(this, RegisterGender.class));
            }
        });
    }

    private void getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String country = address.getCountryName();
                String state = address.getAdminArea();
                String city = address.getLocality();
                this.city.setText(city);
                this.state.setText(state);
                this.country.setText(country);
            }
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Now, you have the user's coordinates; proceed to reverse geocoding.
                                getAddressFromCoordinates(latitude, longitude);
                            }
                        })
                        .addOnFailureListener(this, e -> {
                            // Handle location request failure
                        });
            } else {
                showPermissionRationale();
            }
        }
    }
    private void showPermissionRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Needed");
        builder.setMessage("We need access to your location to provide awesome features. Please grant the permission.");
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
                Toast.makeText(RegisterLocation.this, "Permission Required", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.show();
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

}