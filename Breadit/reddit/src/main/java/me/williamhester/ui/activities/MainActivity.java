package me.williamhester.ui.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.models.AccountManager;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ImageFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.NavigationDrawerFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ImagePagerFragment.ImagePagerCallbacks, ImageFragment.ImageTapCallbacks {

    public static final String SUBREDDIT = "subreddit";

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment mSubredditFragment;
    private String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            if (getIntent().getExtras() != null) {
                mSubreddit = getIntent().getExtras().getString(SUBREDDIT);
            } else {
                mSubreddit = getIntent().getDataString();
                if (mSubreddit != null)
                    mSubreddit = mSubreddit.substring(mSubreddit.indexOf("/subreddit/") + 11);
            }
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mSubreddit);
        } else {
            mSubreddit = "";
        }

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        if (mNavigationDrawerFragment == null)
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance();

        updateActionBar(null);

        getFragmentManager().beginTransaction()
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment,
                        "NavigationDrawer")
                .commit();

        if (getFragmentManager().findFragmentByTag(mSubreddit) == null) {
            mSubredditFragment = SubredditFragment.newInstance(mSubreddit);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mSubredditFragment, mSubreddit)
                    .commit();
        }

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
                if (fragment != null) {
                    mSubredditFragment = fragment;
                    if (fragment instanceof SubredditFragment) {
                        mSubreddit = ((SubredditFragment) mSubredditFragment).getSubreddit();
                        mNavigationDrawerFragment.setSubreddit(mSubreddit,
                                0,  // TODO: Fix this
                                0); // TODO: Fix this
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubredditFragment = getFragmentManager().findFragmentByTag(mSubreddit);
        mSubreddit = ((SubredditFragment) mSubredditFragment).getSubreddit();
        mNavigationDrawerFragment.setSubreddit(mSubreddit,
                0,  // TODO: Fix this
                0); // TODO: Fix this
        invalidateOptionsMenu();
    }

    @Override
    public void onNavigationDrawerItemSelected(String subreddit) {
        if ((mSubreddit == null && subreddit == null)
                || (mSubreddit != null && mSubreddit.equals(subreddit))) {
            ((SubredditFragment) mSubredditFragment).refreshData();
        } else {
            mSubreddit = subreddit;
            if (getFragmentManager().findFragmentByTag(subreddit) != null) {
                mSubredditFragment = getFragmentManager().findFragmentByTag(subreddit);
            } else {
                mSubredditFragment = SubredditFragment.newInstance(subreddit);
            }
            getFragmentManager().beginTransaction()
                    .addToBackStack(mSubreddit)
                    .replace(R.id.container, mSubredditFragment, mSubreddit)
                    .commit();
        }
    }

    @Override
    public void onBackPressed()  {
        Fragment fragment = getFragmentManager().findFragmentByTag("YouTubeFragment");
        if (fragment != null && fragment instanceof YouTubeFragment
                && ((YouTubeFragment) fragment).onBackPressed()) {
            return;
        }
        if (mNavigationDrawerFragment.isOpen()) {
            mNavigationDrawerFragment.toggle();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSortSelected(int sort) {
        ((SubredditFragment) mSubredditFragment).setPrimarySort(sort);
    }

    @Override
    public void onSubSortSelected(int sort) {
        ((SubredditFragment) mSubredditFragment).setSecondarySort(sort);
    }

    private void updateActionBar(String sub) {
        if (TextUtils.isEmpty(sub) && getActionBar() != null)
            getActionBar().setTitle("FrontPage");
        else if (getActionBar() != null)
            getActionBar().setTitle("/r/" + sub);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
//        if (!AccountManager.isLoggedIn()) {
//            menu.removeItem(R.id.action_my_account);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            Bundle b = new Bundle();
            i.putExtras(b);
            startActivity(i);
            return true;
//        } else if (id == R.id.action_my_account) {
//            Bundle b = new Bundle();
//            Intent i = new Intent(this, AccountActivity.class);
//            i.putExtras(b);
//            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImagePagerFragmentCreated() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setEnabled(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getActionBar().setHomeButtonEnabled(false);
        }
    }

    @Override
    public void onImagePagerFragmentDestroyed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onImageTapped() {
        getFragmentManager().popBackStack();
    }
}
