package com.example.poetical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDrm;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    ArrayList<DataPoems> poemList;
    Context context;
    DataUser dataUser;
    ValueEventListener valueEventListener = null;

    public HomeAdapter(ArrayList<DataPoems> poemList, Context context) {
        this.poemList = poemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.poxt_layout, parent, false);

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
        holder.content.setText(poemContents.getContent());
        holder.verifiedtxt.setText(poemContents.getVerified());
        holder.likeamt.setText(poemContents.getLikeamount());
        holder.viewsamt.setText(poemContents.getViewamount());
        holder.audiourl.setText(poemContents.getAudioUrl());
        holder.type.setText(poemContents.getType());
        if(poemContents.getVerified().equals("yes")){
            holder.verif.setVisibility(View.VISIBLE);
        }
        else{
            holder.verif.setVisibility(View.INVISIBLE);
        }
        if(poemContents.getType().equals("podio")){
            holder.waveLayout.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.GONE);
        }
        else if(poemContents.getType().equals("poid")){
            holder.waveLayout.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
        }
        else if(poemContents.getType().equals("poxt")){
            holder.waveLayout.setVisibility(View.GONE);
            holder.content.setVisibility(View.VISIBLE);
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
        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewPoemActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("name", holder.name.getText().toString().trim());
            intent.putExtra("photo", holder.photo.getText().toString().trim());
            intent.putExtra("email", holder.email.getText().toString().trim());
            intent.putExtra("title", holder.title.getText().toString().trim());
            intent.putExtra("type", holder.title.getText().toString().trim());
            intent.putExtra("content", holder.content.getText().toString().trim());
            intent.putExtra("verif", holder.verifiedtxt.getText().toString().trim());
            intent.putExtra("audiourl", holder.audiourl.getText().toString().trim());
            intent.putExtra("likeamount", holder.likeamt.getText().toString().trim());
            intent.putExtra("viewsamount", holder.viewsamt.getText().toString().trim());
            intent.putExtra("type", holder.type.getText().toString().trim());
            context.startActivity(intent);
        });
        holder.name.setOnClickListener(view -> {
            holder.profileImage.callOnClick();
        });
    }

    @Override
    public int getItemCount() {
        return poemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        ConstraintLayout waveLayout;
        ImageView verif;
        TextView name, title, content, email, photo,verifiedtxt,audiourl,likeamt,viewsamt,type;
        ConstraintLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            type=itemView.findViewById(R.id.type);
            layout=itemView.findViewById(R.id.layout);
            profileImage = itemView.findViewById(R.id.profileImage);
            verif = itemView.findViewById(R.id.verif);
            likeamt=itemView.findViewById(R.id.likeamount);
            viewsamt=itemView.findViewById(R.id.viewsamt);
            audiourl=itemView.findViewById(R.id.audiourl);
            verifiedtxt=itemView.findViewById(R.id.verifiedtxt);
            title = itemView.findViewById(R.id.poemtitle);
            content = itemView.findViewById(R.id.poemcontent);
            email = itemView.findViewById(R.id.email);
            waveLayout = itemView.findViewById(R.id.wavelayout);
            photo = itemView.findViewById(R.id.phototxt);
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
    public void increaseViewAmt(String key){

    }
}
