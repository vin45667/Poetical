package com.example.poetical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
ImageView write;
TabLayout tabLayout;
ViewPager viewPager;
DataUser dataUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //addDatatoFirebasef();
        getUserDetails();
        write=findViewById(R.id.write);
        tabLayout=findViewById(R.id.tabLayout);
        viewPager=findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),HomeActivity.this);
        viewPager.setAdapter(adapter);

        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager);

        write.setOnClickListener(v -> {
            FileOps fileOps=new FileOps(HomeActivity.this);
            if(fileOps.readIntStorage("userlog.txt").equals("1")) {
                startActivity(new Intent(this, ChoosePoemType.class));
            }
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Login")
                        .setMessage("You need to login to your account to post on Poetical")
                        .setPositiveButton("Login", (dialog, id) -> {
                            dialog.cancel();
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        })
                        .setNegativeButton("Cancel", (dialog, id) -> {
                            dialog.cancel();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    private void prepareViewPager(ViewPager viewPager, ArrayList<String> arrayList) {
        // Initialize main adapter
        MainAdapter adapter=new MainAdapter(getSupportFragmentManager());

        // Initialize main fragment
        HomeFragment homeFragment=new HomeFragment(HomeActivity.this);

        // Use for loop
        for(int i=0;i<arrayList.size();i++)
        {
            // Initialize bundle
            Bundle bundle=new Bundle();

            // Put title
            bundle.putString("title",arrayList.get(i));

            // set argument
            homeFragment.setArguments(bundle);

            // Add fragment
            adapter.addFragment(homeFragment,arrayList.get(i));
            homeFragment=new HomeFragment();
        }
        // set adapter
        viewPager.setAdapter(adapter);
    }

    private class MainAdapter extends FragmentPagerAdapter {
        // Initialize arrayList
        ArrayList<Fragment> fragmentArrayList= new ArrayList<>();
        ArrayList<String> stringArrayList=new ArrayList<>();

        int[] imageList={R.drawable.home,R.drawable.wave,R.drawable.settings};

        // Create constructor
        public void addFragment(Fragment fragment,String s)
        {
            // Add fragment
            fragmentArrayList.add(fragment);
            // Add title
            stringArrayList.add(s);
        }

        public MainAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            // return fragment position
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            // Return fragment array list size
            return fragmentArrayList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {

            // Initialize drawable
            Drawable drawable= ContextCompat.getDrawable(getApplicationContext()
                    ,imageList[position]);

            // set bound
            drawable.setBounds(0,0,drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());

            // Initialize spannable image
            SpannableString spannableString=new SpannableString(""+stringArrayList.get(position));

            // Initialize image span
            ImageSpan imageSpan=new ImageSpan(drawable,ImageSpan.ALIGN_BOTTOM);

            // Set span
            spannableString.setSpan(imageSpan,0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // return spannable string
            return spannableString;
        }
    }
    private void getUserDetails() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FileOps fileOps = new FileOps(HomeActivity.this);
        String email = fileOps.readIntStorage("useremail.txt");
        Query query = usersRef.orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through the results
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    dataUser = snapshot.getValue(DataUser.class);

                    fileOps.writeToIntFile("useremail.txt", dataUser.getEmail());
                    fileOps.writeToIntFile("userverif.txt", dataUser.getVerified());
                    fileOps.writeToIntFile("username.txt", dataUser.getName());
                    fileOps.writeToIntFile("userfname.txt", dataUser.getFname());
                    fileOps.writeToIntFile("userlname.txt", dataUser.getLname());
                    fileOps.writeToIntFile("userbio.txt", dataUser.getAbout());
                    fileOps.writeToIntFile("userage.txt", dataUser.getAge());
                    fileOps.writeToIntFile("profileimage.txt", dataUser.getPhotoUrl());
                    fileOps.writeToIntFile("coverphoto.txt", dataUser.coverPhoto);
                    fileOps.writeToIntFile("usergender.txt", dataUser.getGender());
                    fileOps.writeToIntFile("usercountry.txt", dataUser.getCountry());
                    //fileOps.writeToIntFile("usercountrycode.txt",dataUser.getCountryCode());
                    fileOps.writeToIntFile("usernumber.txt", dataUser.getNumber());
                    fileOps.writeToIntFile("userdob.txt", dataUser.getDateOfBirth());
                    fileOps.writeToIntFile("userstate.txt", dataUser.getState());
                    fileOps.writeToIntFile("usercity.txt", dataUser.getCity());
                    fileOps.writeToIntFile("usercountrycode.txt", dataUser.getCountrycode());
                    fileOps.writeToIntFile("notif.txt", "1");
                    fileOps.writeToIntFile("partnernotif.txt", "1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that may occur
                System.out.println("Error: " + databaseError.getMessage());
            }
        });
    }
    private void addDatatoFirebasef() {
        FileOps fileOps = new FileOps(HomeActivity.this);
        String email = fileOps.readIntStorage("useremail.txt");
        FirebaseDatabase firebaseDatabasedis = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferencedis = firebaseDatabasedis.getReference("Users");
        DataUser dataUser1 = new DataUser();
        String key=databaseReferencedis.getKey();
        databaseReferencedis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataUser1.setEmail("email");
                dataUser1.setName("name");
                databaseReferencedis.child(key).setValue(dataUser1);
                Toast.makeText(HomeActivity.this, "added successfully", Toast.LENGTH_SHORT).show();
            }

            public void onCancelled(@NonNull DatabaseError error) {
                // if the data is not added or it is cancelled then
                // we are displaying a failure message.
                //Snackbar.make(layout, "Failed to add data " + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}