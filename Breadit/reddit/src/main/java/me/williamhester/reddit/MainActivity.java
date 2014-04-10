package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Subreddit;
import me.williamhester.areddit.User;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SubredditFragment mSubredditFragment;
    private SharedPreferences mPrefs;
    private User mUser;
    private String mSubreddit;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = getSharedPreferences("preferences", MODE_PRIVATE);

        if (getIntent() != null && getIntent().getExtras() != null) { // If the user just completed the setup
            boolean b = getIntent().getExtras().getBoolean("finishedSetup");
            mUser = getIntent().getExtras().getParcelable("user");
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean("finishedSetup", b);
            if (mUser != null) {
                edit.putString("username", mUser.getUsername());
                edit.putString("cookie", mUser.getCookie());
                edit.putString("modhash", mUser.getModhash());
            }
            edit.commit();

            FragmentManager fragmentManager = getFragmentManager();
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", mUser);
            SubredditFragment sf = new SubredditFragment();
            sf.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.container, sf)
                    .commit();
        } else if (!mPrefs.getBoolean("finishedSetup", false)) { // If the user has not completed the setup
            Intent i = new Intent(this, SetupActivity.class);
            startActivity(i);
        } else { // If the user has completed the setup
            String username = mPrefs.getString("username", "");
            String cookie = mPrefs.getString("cookie", "");
            String modhash = mPrefs.getString("modhash", "");
            if (!username.equals("")) {
                mUser = new User(username, modhash, cookie);
            }
        }

        mSubredditFragment = SubredditFragment.newInstance(mUser, mSubreddit);
        mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mUser);
        mTitle = getTitle();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.navigation_drawer_container,
                mNavigationDrawerFragment)
                .commit();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mSubredditFragment)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(String subreddit) {
        mSubreddit = subreddit;
        mSubredditFragment.setSubreddit(mSubreddit);
        updateActionBar(subreddit);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("FrontPage");
    }

    private void updateActionBar(String sub) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            if (sub == null)
                actionBar.setTitle("FrontPage");
            else
                actionBar.setTitle("/r/" + sub);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
          getMenuInflater().inflate(R.menu.main, menu);
//            restoreActionBar(); // I don't think we need this because we're already updating it
//            return true;
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_submit) {
            SubmitDialogFragment sf = SubmitDialogFragment.newInstance(mUser, mSubreddit);
            sf.show(getFragmentManager(), "submit_fragment");
        }
        return super.onOptionsItemSelected(item);
    }
}
