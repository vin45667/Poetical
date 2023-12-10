package com.example.poetical;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class RegisterGender extends AppCompatActivity {
    boolean male = false,female=false;
    private Button genderContinueButton;
    private Button maleSelectionButton;
    private Button femaleSelectionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_gender);
        maleSelectionButton = findViewById(R.id.maleSelectionButton);
        femaleSelectionButton = findViewById(R.id.femaleSelectionButton);
        genderContinueButton = findViewById(R.id.genderContinueButton);
        //By default male has to be selected so below code is added
        femaleSelectionButton.setAlpha(.5f);
        femaleSelectionButton.setBackgroundColor(Color.GRAY);
        maleSelectionButton.setAlpha(.5f);
        maleSelectionButton.setBackgroundColor(Color.GRAY);

        maleSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maleButtonSelected();
            }
        });

        femaleSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                femaleButtonSelected();
            }
        });

        genderContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreferenceEntryPage();
            }
        });

    }

    public void maleButtonSelected() {
        male = true;
        female=false;
        maleSelectionButton.setBackgroundColor(Color.parseColor("#FF0F7CD3"));
        maleSelectionButton.setAlpha(1.0f);
        femaleSelectionButton.setAlpha(.5f);
        femaleSelectionButton.setBackgroundColor(Color.GRAY);
        FileOps fileOps = new FileOps(RegisterGender.this);
        fileOps.writeToIntFile("usergender.txt", "male");
    }

    public void femaleButtonSelected() {
        male = false;
        female=true;
        femaleSelectionButton.setBackgroundColor(Color.parseColor("#FF0F7CD3"));
        femaleSelectionButton.setAlpha(1.0f);
        maleSelectionButton.setAlpha(.5f);
        maleSelectionButton.setBackgroundColor(Color.GRAY);
        FileOps fileOps = new FileOps(RegisterGender.this);
        fileOps.writeToIntFile("usergender.txt", "female");
    }

    public void openPreferenceEntryPage() {
        if(!male&!female){
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent intent = new Intent(this, RegisterAge.class);
            startActivity(intent);
        }
    }
}
