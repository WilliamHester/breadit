package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SharedPreferences mPrefs;
    private User mUser;


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
        if (mUser != null)
            Log.i("MainActivity", "User is not null");

        // TW
//        ArrayList<Subreddit> userSubreddits = new ArrayList();
//        try {
//             userSubreddits = mUser.getSubscribedSubreddits();
//        } catch (IOException ioexception) {
//        }
//        catch (NullPointerException e) {
//            Log.i("MainActivity", "mUser is null");
//        }
        mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mUser);
        mTitle = getTitle();

//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer_container,
//                (DrawerLayout) findViewById(R.id.drawer_layout));
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.navigation_drawer_container, mNavigationDrawerFragment).commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Bundle b = new Bundle();
        if (mUser != null)
            Log.i("MainActivity", mUser.getCookie());
        else
            // Log.i("MainActivity", "user is null");
        b.putParcelable("user", mUser);
        SubredditFragment sf = new SubredditFragment();
        sf.setArguments(b);
        fragmentManager.beginTransaction()
                .replace(R.id.container, sf)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
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
        }
        return super.onOptionsItemSelected(item);
    }
}
