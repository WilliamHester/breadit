package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ForgotPasswordFragment;
import me.williamhester.ui.fragments.LogInFragment;
import me.williamhester.ui.fragments.RegisterFragment;
import me.williamhester.ui.views.SlidingTabLayout;

/**
 * This Activity is just a container for the LogInPagerFragment. It really shouldn't exist, but
 * because the PreferencesFragment does not exist as an official support library, this is the
 * solution I'm going with for now.
 */
public class LogInActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
        pager.setAdapter(new LogInPagerAdapter(getSupportFragmentManager()));

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.auburn_orange);
            }
        });
        tabs.setViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLoggedIn() {
        setResult(RESULT_OK);
        finish();
    }

    public void openForgotPassword(String username) {
        Fragment forgotFragment = ForgotPasswordFragment.newInstance(username);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, forgotFragment, "forgot")
                .addToBackStack("forgot")
                .commit();
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
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Log in";
            } else {
                return "Register";
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
