package com.example.poetical;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterAge extends AppCompatActivity {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
    private DatePicker ageSelectionPicker;
    private Button ageContinueButton;
    // age limit attribute
    private int ageLimit = 13;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_age);
        Intent intent = getIntent();
        ageSelectionPicker = findViewById(R.id.ageSelectionPicker);


        ageContinueButton = findViewById(R.id.ageContinueButton);

        ageContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHobbiesEntryPage();
            }
        });
        FileOps fileOps=new FileOps(RegisterAge.this);
        String dobday=fileOps.readIntStorage("userdobday.txt");
        String dobmonth=fileOps.readIntStorage("userdobmonth.txt");
        String dobyear=fileOps.readIntStorage("userdobyear.txt");
        if(dobday.equals("")){
            dobday="1";
        }
        if(dobmonth.equals("")){
            dobmonth="1";
        }
        if(dobyear.equals("")){
            dobyear="2002";
        }
        Calendar calendar=Calendar.getInstance();
        int dobdayint=Integer.parseInt(dobday);
        int dobmonthint=Integer.parseInt(dobmonth);
        int dobyearint=Integer.parseInt(dobyear);
        calendar.set(dobyearint,dobmonthint,dobdayint);
        ageSelectionPicker.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void openHobbiesEntryPage() {
        int age = getAge(ageSelectionPicker.getYear(), ageSelectionPicker.getMonth()+1, ageSelectionPicker.getDayOfMonth());

        // if user is above 13 years old then only he/she will be allowed to register to the system.
        if (age > ageLimit) {
            FileOps fileOps=new FileOps(RegisterAge.this);
            String userdobyear=String.valueOf(ageSelectionPicker.getYear());
            String userdobmonth=String.valueOf(ageSelectionPicker.getMonth());
            String userdobday=String.valueOf(ageSelectionPicker.getDayOfMonth());
            String userdob=userdobday+"/"+userdobmonth+"/"+userdobyear;
            fileOps.writeToIntFile("userdob.txt",userdob);
            fileOps.writeToIntFile("userage.txt",String.valueOf(age));
            fileOps.writeToIntFile("userdobday.txt",userdobday);
            fileOps.writeToIntFile("userdobmonth.txt",userdobmonth);
            fileOps.writeToIntFile("userdobyear.txt",userdobyear);
            // code for converting date to string
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, ageSelectionPicker.getYear());
            cal.set(Calendar.MONTH, ageSelectionPicker.getMonth());
            cal.set(Calendar.DAY_OF_MONTH, ageSelectionPicker.getDayOfMonth());
            Date dateOfBirth = cal.getTime();
            String strDateOfBirth = dateFormatter.format(dateOfBirth);

            // code to set the dateOfBirthAttribute.
            //user.setDateOfBirth(strDateOfBirth);

            Intent intent = new Intent(this, ProPicActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "You are underaged and can't use Poetical", Toast.LENGTH_SHORT).show();
        }

    }

    // method to get the current age of the user.
    private int getAge(int year, int month, int day) {
        Calendar dateOfBirth = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dateOfBirth.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
