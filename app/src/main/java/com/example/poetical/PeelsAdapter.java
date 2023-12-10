package com.example.poetical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Handler;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeelsAdapter extends RecyclerView.Adapter<PeelsAdapter.ViewHolder> {
    ArrayList<DataPoems> poemList;
    Context context;
    DataUser dataUser;
    MediaPlayer mediaPlayer;
    android.os.Handler handler;
    ValueEventListener valueEventListener = null;
    String startst, endst;
    int duration, currentProgress;
    StorageReference audioRef;
    File localFile;

    public PeelsAdapter(ArrayList<DataPoems> poemList, Context context) {
        this.poemList = poemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.peels_layout, parent, false);
        mediaPlayer = new MediaPlayer();
        startst = "00:00";
        endst = "00:00";
        duration = 0;
        currentProgress = 0;
        localFile = null;
        audioRef = null;
        try {
            mediaPlayer.setDataSource("");
        } catch (IOException e) {
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FileOps fileOps = new FileOps(context);
        DataPoems poemContents = poemList.get(position);
        holder.name.setText(poemContents.getName());
        holder.email.setText(poemContents.getEmail());
        holder.photo.setText(poemContents.getPhotoUrl());
        holder.title.setText(poemContents.getTitle());
        holder.audiotxt.setText(poemContents.getAudioUrl());
        holder.content.setText(poemContents.getContent());
        if (poemContents.getVerified().equals("yes")) {
            holder.verif.setVisibility(View.VISIBLE);
        } else {
            holder.verif.setVisibility(View.INVISIBLE);
        }
        //retrieve user from Users database
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Get a reference to the image in Firebase Storage
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/" + holder.photo.getText().toString());
        // Download the image data as a byte array
        final long FIFTEEN_MEGABYTE = 1024 * 15360;
        imageRef.getBytes(FIFTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the byte array to a bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                // Set the bitmap to an profileImage
                holder.profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        holder.profileImage.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("name", holder.name.getText().toString().trim());
            intent.putExtra("photo", holder.photo.getText().toString().trim());
            intent.putExtra("email", holder.email.getText().toString().trim());
            context.startActivity(intent);
        });
        holder.name.setOnClickListener(view -> {
            holder.profileImage.callOnClick();
        });

        holder.pauseplay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.pauseplay.setEnabled(false);
            holder.pauseplay.setAlpha(0.5F);
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child("audios/" + holder.audiotxt.getText().toString());
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
                        holder.end.setText(endst);
                        mediaPlayer.start();
                        holder.progressBar.setVisibility(View.GONE);
                        holder.pauseplay.setEnabled(true);
                        holder.pauseplay.setAlpha(1F);

                    } catch (IOException e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            duration= mediaPlayer.getDuration();
                            holder.seekBar.setMax(duration);
                            endst=formatTime(duration);
                            holder.end.setText(endst);
                            mediaPlayer.start();
                            updateSeekBar(holder.seekBar, holder.start);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "failed to download", Toast.LENGTH_SHORT).show();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    holder.seekBar.setProgress(0);
                }
            });
            //audioRef = storageReference.child("audios/" + holder.audiotxt.getText().toString());
//                try {
//                    localFile = File.createTempFile(holder.audiotxt.getText().toString(), "mp3");
//                    audioRef.getFile(localFile)
//                            .addOnSuccessListener(taskSnapshot -> {
//                                try {
//                                    mediaPlayer = new MediaPlayer();
//                                    mediaPlayer.setDataSource(localFile.getAbsolutePath());
//                                    mediaPlayer.prepare();
//                                    duration= mediaPlayer.getDuration();
//                                    endst=formatTime(duration);
//                                    holder.end.setText(endst);
//                                    holder.seekBar.setMax(duration);
//                                    mediaPlayer.start();
//                                    holder.progressBar.setVisibility(View.GONE);
//                                    holder.pauseplay.setEnabled(true);
//                                    holder.pauseplay.setAlpha(1F);
//                                } catch (IOException e) {
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                    builder.setTitle("Invalid 3").setMessage(e.getMessage()).setPositiveButton("OK", (dialog, id) -> {
//                                        dialog.cancel();
//                                    });
//                                    AlertDialog dialog = builder.create();
//                                    dialog.show();
//                                }
//
//                            })
//                            .addOnFailureListener(exception -> {
//                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                                builder.setTitle("Invalid 2").setMessage(exception.getMessage() + " " + holder.audiotxt.getText()).setPositiveButton("OK", (dialog, id) -> {
//                                    dialog.cancel();
//                                });
//                                AlertDialog dialog = builder.create();
//                                dialog.show();
//                            });
//                } catch (IOException e) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("Invalid 1").setMessage(e.getMessage()).setPositiveButton("OK", (dialog, id) -> {
//                        dialog.cancel();
//                    });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                }
        });
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        handler = new android.os.Handler(Looper.getMainLooper());


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
                handler.postDelayed(this,100);
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
    public int getItemCount() {
        return poemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        SeekBar seekBar;
        ConstraintLayout waveLayout;
        ImageView verif, pauseplay, starimg;
        ProgressBar progressBar;
        TextView name, title, content, email, photo, staramt, follow, audiotxt, start, end;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            start = itemView.findViewById(R.id.starttxt);
            end = itemView.findViewById(R.id.endtxt);
            profileImage = itemView.findViewById(R.id.profileImage);
            verif = itemView.findViewById(R.id.verif);
            seekBar = itemView.findViewById(R.id.seekbar);
            pauseplay = itemView.findViewById(R.id.pauseplay);
            title = itemView.findViewById(R.id.poemtitle);
            content = itemView.findViewById(R.id.poemcontent);
            email = itemView.findViewById(R.id.email);
            waveLayout = itemView.findViewById(R.id.wavelayout);
            photo = itemView.findViewById(R.id.phototxt);
            staramt = itemView.findViewById(R.id.likeamount);
            starimg = itemView.findViewById(R.id.likeimg);
            follow = itemView.findViewById(R.id.followtxt);
            progressBar = itemView.findViewById(R.id.progressbar);
            audiotxt = itemView.findViewById(R.id.audiotxt);
        }
    }

    public void deleteBookmark(String bookmarker, String emailValue) {
        // Create a query to find the node with matching "bookmarker" and "email" values
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child("Bookmarks")
                .orderByChild("bookmarker")
                .equalTo(bookmarker);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Check if the "email" value matches the desired value
                    String email = snapshot.child("email").getValue(String.class);
                    if (email != null && email.equals(emailValue)) {
                        // Delete the node
                        snapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });
    }

    public void streamAudio(StorageReference storageReference) {
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    String url = uri.toString();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "failed to download", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
