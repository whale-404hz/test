package com.example.campusexpensemanager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new SearchFragment();
            case 2:
                return new SettingsFragment();
            case 3:
                return new ProfileFragment();
            case 4:
                return new MoreFragment();
            default:
                return new HomeFragment(); // Mặc định trả về HomeFragment nếu không xác định được vị trí
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Số lượng Fragment
    }
}
