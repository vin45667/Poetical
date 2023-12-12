package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPoemActivity extends AppCompatActivity {
    TextView poemtitle, poemcontent, follow, staramt, name, start, end, viewamt;
    int duration, currentProgress;
    CircleImageView profileImage;
    ConstraintLayout waveLayout;
    ImageView pauseplay, star,verif;
    SeekBar seekBar;
    String audioUrl;
    ProgressBar progressBar;
    MediaPlayer mediaPlayer = null;
    Handler handler;
    String endst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_poem);
        handler = new Handler();
        verif=findViewById(R.id.verif);
        start = findViewById(R.id.starttxt);
        end = findViewById(R.id.endtxt);
        viewamt = findViewById(R.id.viewsamt);
        poemtitle = findViewById(R.id.poemtitle);
        progressBar = findViewById(R.id.progressbar);
        seekBar = findViewById(R.id.seekbar);
        poemcontent = findViewById(R.id.poemcontent);
        follow = findViewById(R.id.followtxt);
        staramt = findViewById(R.id.likeamount);
        name = findViewById(R.id.name);
        profileImage = findViewById(R.id.profileImage);
        waveLayout = findViewById(R.id.wavelayout);
        pauseplay = findViewById(R.id.pauseplay);
        star = findViewById(R.id.likeimg);
        audioUrl = getIntent().getStringExtra("audiourl");
        name.setText(getIntent().getStringExtra("name"));
        poemtitle.setText(getIntent().getStringExtra("title"));
        poemcontent.setText(getIntent().getStringExtra("content"));
        staramt.setText(getIntent().getStringExtra("likeamount"));
        String viewamtst=getIntent().getStringExtra("viewsamount");
        String verified=getIntent().getStringExtra("verif");
        if(verified.equals("yes")){
            verif.setVisibility(View.VISIBLE);
        }
        else{
            verif.setVisibility(View.GONE);
        }
        int a=Integer.parseInt(viewamtst);
        viewamt.setText(String.valueOf(a+1));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Get a reference to the image in Firebase Storage
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + getIntent().getStringExtra("photo"));
        // Download the image data as a byte array
        final long FIFTEEN_MEGABYTE = 1024 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // Set the bitmap to an profileImage
                profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        profileImage.setOnClickListener(view -> {
            Intent intent = new Intent(ViewPoemActivity.this, UserProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("name", getIntent().getStringExtra("name"));
            intent.putExtra("photo", getIntent().getStringExtra("photo"));
            intent.putExtra("email", getIntent().getStringExtra("email"));
            startActivity(intent);
        });
        if (getIntent().getStringExtra("type").equals("poid") || getIntent().getStringExtra("type").equals("podio")) {
            waveLayout.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
        } else {
            waveLayout.setVisibility(View.GONE);
            seekBar.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
            end.setVisibility(View.GONE);
        }
        star.setOnClickListener(v -> {

        });
        follow.setOnClickListener(v -> {
            if(follow.getText().equals("Follow")){
                follow.setText("Following");
            }
            else if(follow.getText().equals("Following")){
                follow.setText("Follow");
            }
        });
        pauseplay.setOnClickListener(v -> {
            if (audioUrl != null) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        Drawable playimg = ContextCompat.getDrawable(ViewPoemActivity.this,R.drawable.play);
                        pauseplay.setImageDrawable(playimg);
                    }
                    else{
                        mediaPlayer.start();
                        Drawable pauseimg = ContextCompat.getDrawable(ViewPoemActivity.this,R.drawable.pause);
                        pauseplay.setImageDrawable(pauseimg);
                    }
                }
                else {
                    mediaPlayer=new MediaPlayer();
                    progressBar.setVisibility(View.VISIBLE);
                    pauseplay.setEnabled(false);
                    pauseplay.setAlpha(0.5F);
                    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                    StorageReference storageReference = firebaseStorage.getReference().child(   "audios/" + audioUrl);
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            try {

                                String url = uri.toString();
                                mediaPlayer.reset();
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setDataSource(url);
                                mediaPlayer.prepareAsync();
                                duration = mediaPlayer.getDuration();
                                endst = formatTime(duration);
                                end.setText(endst);
                                mediaPlayer.start();
                                progressBar.setVisibility(View.GONE);
                                pauseplay.setEnabled(true);
                                pauseplay.setAlpha(1F);

                            } catch (IOException e) {
                                Toast.makeText(ViewPoemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    duration = mediaPlayer.getDuration();
                                    seekBar.setMax(duration);
                                    endst = formatTime(duration);
                                    end.setText(endst);
                                    mediaPlayer.start();
                                    Drawable pauseimg = ContextCompat.getDrawable(ViewPoemActivity.this,R.drawable.pause);
                                    pauseplay.setImageDrawable(pauseimg);
                                    updateSeekBar(seekBar, start);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ViewPoemActivity.this, "failed to download", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            seekBar.setProgress(0);
                            Drawable playimg = ContextCompat.getDrawable(ViewPoemActivity.this,R.drawable.play);
                            pauseplay.setImageDrawable(playimg);
                        }
                    });
                }
            }
            else {
                Toast.makeText(this, "Couldn't get audio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateSeekBar(SeekBar seekBar, TextView start) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {

                    currentProgress = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentProgress);
                    start.setText(formatTime(currentProgress));
                }
                handler.postDelayed(this, 100);
                //updateSeekBar(seekBar, start);
            }
        }, 100);
    }

    public String formatTime(int progress) {
        int seconds = (progress / 1000) % 60;
        int minutes = ((progress / (1000 * 60)) % 60);
        int hours = ((progress / (1000 * 60 * 60)) % 24);
        return String.format("%02d:02d:02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer!=null) {
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}