package com.example.poetical;

// ExamplePagerAdapter.java

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // Return the fragment for the given position
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new PeelsFragment();
            case 2:
                return new SettingsFragment();
            // Add more cases for additional tabs
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // Return the total number of tabs
        return 3; // Change this based on the number of tabs you have
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Set tab titles if needed
        int[] tabIcons = {R.drawable.home, R.drawable.wave, R.drawable.settings}; // Replace with your icon drawables

        String[] tabTitles = {"Home", "Peels", "Settings"}; // Replace with your tab names

        Drawable image = context.getResources().getDrawable(tabIcons[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());

        SpannableString spannableString = new SpannableString(" " + tabTitles[position]); // Add space for the icon
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_CENTER);
        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Vertically center the text
        spannableString.setSpan(new RelativeSizeSpan(1.3f), 1, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;


//        switch (position) {
//            case 0:
//                return "Home";
//            case 1:
//                return "Podio";
//            case 2:
//                return "Settings";
//            // Add more titles for additional tabs
//            default:
//                return null;
//        }

    }
}

