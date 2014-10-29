package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.williamhester.reddit.R;

/**
 * Created by William on 10/28/14.
 */
public class LogInPagerFragment extends Fragment {

    public static LogInPagerFragment newInstance() {
        return new LogInPagerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login_pager, container, false);
        ViewPager pager = (ViewPager) v.findViewById(R.id.view_pager);
        pager.setAdapter(new LogInPagerAdapter(getChildFragmentManager()));
        return v;
    }

    private static class LogInPagerAdapter extends FragmentPagerAdapter {

        public LogInPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                return LogInFragment.newInstance();
            } else {
                return RegisterFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
