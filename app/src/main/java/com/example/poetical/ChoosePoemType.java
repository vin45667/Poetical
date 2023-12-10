package com.example.poetical;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoosePoemType extends AppCompatActivity {
ImageView audioimg,textimg,hybridimg;
TextView texttxt,texttxtt,audiotxt,audiotxtt,hybridtxt,hybridtxtt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_poem_type);
        audioimg=findViewById(R.id.audioimg);
        textimg=findViewById(R.id.textimg);
        hybridimg=findViewById(R.id.hybridimg);
        texttxt=findViewById(R.id.texttxt);
        texttxtt=findViewById(R.id.texttxtt);
        audiotxt=findViewById(R.id.audiotxt);
        audiotxtt=findViewById(R.id.audiotxtt);
        hybridtxt=findViewById(R.id.hybridtxt);
        hybridtxtt=findViewById(R.id.hybridtxtt);
        audiotxtt.setOnClickListener(v -> {
            audioPage();
        });
        audiotxt.setOnClickListener(v -> {
            audioPage();
        });
        audioimg.setOnClickListener(v -> {
            audioPage();
        });
        //
        texttxtt.setOnClickListener(v -> {
            textPage();
        });
        texttxt.setOnClickListener(v -> {
            textPage();
        });
        textimg.setOnClickListener(v -> {
            textPage();
        });
        //
        hybridtxtt.setOnClickListener(v -> {
            hybridPage();
        });
        hybridtxt.setOnClickListener(v -> {
            hybridPage();
        });
        hybridimg.setOnClickListener(v -> {
            hybridPage();
        });
    }
    public void audioPage(){
        startActivity(new Intent(this, PodioActivity.class));
    }
    public void textPage(){
        startActivity(new Intent(this, PoxtActivity.class));
    }
    public void hybridPage(){
        startActivity(new Intent(this, PoidActivity.class));
    }
}