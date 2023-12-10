package com.example.poetical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeelsAdapterStacked extends ArrayAdapter<DataPoems> {
    Context mContext;
    ArrayList<DataPoems> poemList = new ArrayList<>();
    public static CircleImageView btnInfo;
    FileOps fileOps;
    MediaPlayer mediaPlayer;
    android.os.Handler handler;
    ValueEventListener valueEventListener = null;
    String startst,endst;
    int duration,currentProgress;
    StorageReference audioRef;
    File localFile;


    public PeelsAdapterStacked(@NonNull Context context, int resource, @NonNull ArrayList<DataPoems> poemList) {
        super(context, resource, poemList);
        this.mContext = context;
        this.poemList.clear();
        this.poemList = poemList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Toast.makeText(mContext, "Hello heree", Toast.LENGTH_SHORT).show();
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.peels_layout, parent, false);
            Toast.makeText(mContext, "it is null", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(mContext, "Hello not null", Toast.LENGTH_SHORT).show();
        }
//        DataPoems poemContents = poemList.get(position);
//        TextView name=convertView.findViewById(R.id.name);
//        TextView email=convertView.findViewById(R.id.email);
//        TextView photo=convertView.findViewById(R.id.phototxt);
//        TextView title=convertView.findViewById(R.id.poemtitle);
//        TextView audiotxt=convertView.findViewById(R.id.audiotxt);
//        TextView content=convertView.findViewById(R.id.poemcontent);
//        TextView end=convertView.findViewById(R.id.endtxt);
//        TextView start=convertView.findViewById(R.id.starttxt);
//        ImageView profileImage=convertView.findViewById(R.id.imageView);
//        ImageView pauseplay=convertView.findViewById(R.id.pauseplay);
//        ImageView verif=convertView.findViewById(R.id.verif);
//        SeekBar seekBar=convertView.findViewById(R.id.seekbar);
//        ProgressBar progressBar=convertView.findViewById(R.id.progressbar);
//        name.setText(poemContents.getName());
//        Toast.makeText(mContext, "Hello", Toast.LENGTH_SHORT).show();
//        email.setText(poemContents.getEmail());
//        photo.setText(poemContents.getPhotoUrl());
//        title.setText(poemContents.getTitle());
//        audiotxt.setText(poemContents.getAudioUrl());
//        content.setText(poemContents.getContent());
//        if(poemContents.getVerified().equals("yes")){
//            verif.setVisibility(View.VISIBLE);
//        }
//        else{
//            verif.setVisibility(View.INVISIBLE);
//        }
//        //retrieve user from Users database
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        // Get a reference to the image in Firebase Storage
//        StorageReference storageRef = storage.getReference();
//        StorageReference imageRef = storageRef.child("images/" + photo.getText().toString());
//        // Download the image data as a byte array
//        final long FIFTEEN_MEGABYTE = 1024 * 15360;
//        imageRef.getBytes(FIFTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                // Convert the byte array to a bitmap
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                // Set the bitmap to an profileImage
//                profileImage.setImageBitmap(bitmap);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//            }
//        });
//
//
//        profileImage.setOnClickListener(view -> {
//            Intent intent = new Intent(mContext, UserProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("name", name.getText().toString().trim());
//            intent.putExtra("photo", photo.getText().toString().trim());
//            intent.putExtra("email", email.getText().toString().trim());
//            mContext.startActivity(intent);
//        });
//        name.setOnClickListener(view -> {
//            profileImage.callOnClick();
//        });
//
//        pauseplay.setOnClickListener(v -> {
//            progressBar.setVisibility(View.VISIBLE);
//            pauseplay.setEnabled(false);
//            pauseplay.setAlpha(0.5F);
//            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//            StorageReference storageReference = firebaseStorage.getReference();
//            audioRef = storageReference.child("audios/" + audiotxt.getText().toString());
//            try {
//                localFile = File.createTempFile(audiotxt.getText().toString(), "mp3");
//                audioRef.getFile(localFile)
//                        .addOnSuccessListener(taskSnapshot -> {
//                            try {
//                                mediaPlayer = new MediaPlayer();
//                                mediaPlayer.setDataSource(localFile.getAbsolutePath());
//                                mediaPlayer.prepare();
//                                duration= mediaPlayer.getDuration();
//                                endst=formatTime(duration);
//                                end.setText(endst);
//                                seekBar.setMax(duration);
//                                mediaPlayer.start();
//                                progressBar.setVisibility(View.GONE);
//                                pauseplay.setEnabled(true);
//                                pauseplay.setAlpha(1F);
//                            } catch (IOException e) {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                                builder.setTitle("Invalid 3").setMessage(e.getMessage()).setPositiveButton("OK", (dialog, id) -> {
//                                    dialog.cancel();
//                                });
//                                AlertDialog dialog = builder.create();
//                                dialog.show();
//                            }
//
//                        })
//                        .addOnFailureListener(exception -> {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                            builder.setTitle("Invalid 2").setMessage(exception.getMessage() + " " + audiotxt.getText()).setPositiveButton("OK", (dialog, id) -> {
//                                dialog.cancel();
//                            });
//                            AlertDialog dialog = builder.create();
//                            dialog.show();
//                        });
//            } catch (IOException e) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                builder.setTitle("Invalid 1").setMessage(e.getMessage()).setPositiveButton("OK", (dialog, id) -> {
//                    dialog.cancel();
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        });
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser){
//                    mediaPlayer.seekTo(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//        handler=new android.os.Handler(Looper.getMainLooper());
//        updateSeekBar(seekBar,start);
        return convertView;
    }
    public void updateSeekBar(SeekBar seekBar,TextView start){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer!=null&& mediaPlayer.isPlaying()){

                    currentProgress= mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentProgress);
                    start.setText(formatTime(currentProgress));
                }
                updateSeekBar(seekBar,start);
            }
        },100);
    }
    public String formatTime(int progress){
        int seconds=(progress/1000)%60;
        int minutes=((progress/(1000*60))%60);
        int hours=((progress/(1000*60*60))%24);
        return String.format("%02d:02d:02d",hours,minutes,seconds);
    }



}
