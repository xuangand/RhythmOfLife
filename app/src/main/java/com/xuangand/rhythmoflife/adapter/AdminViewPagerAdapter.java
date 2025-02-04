package com.xuangand.rhythmoflife.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.xuangand.rhythmoflife.fragment.admin.AdminAccountFragment;
import com.xuangand.rhythmoflife.fragment.admin.AdminArtistFragment;
import com.xuangand.rhythmoflife.fragment.admin.AdminCategoryFragment;
import com.xuangand.rhythmoflife.fragment.admin.AdminFeedbackFragment;
import com.xuangand.rhythmoflife.fragment.admin.AdminSongFragment;

public class AdminViewPagerAdapter extends FragmentStateAdapter {

    public AdminViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AdminArtistFragment();

            case 2:
                return new AdminSongFragment();

            case 3:
                return new AdminFeedbackFragment();

            case 4:
                return new AdminAccountFragment();

            default:
                return new AdminCategoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
