package me.williamhester.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ImageFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.NavigationDrawerFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ImagePagerFragment.ImagePagerCallbacks, ImageFragment.ImageTapCallbacks,
        SubredditFragment.OnSubredditSelectedListener {

    public static final String SUBREDDIT = "subreddit";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SubredditFragment mSubredditFragment;
    private String mSubreddit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            if (getIntent().getExtras() != null) {
                mSubreddit = getIntent().getExtras().getString(SUBREDDIT);
            } else {
                mSubreddit = getIntent().getDataString();
                if (mSubreddit != null)
                    mSubreddit = mSubreddit.substring(mSubreddit.indexOf("/subreddit/") + 11);
            }
        } else {
            mSubreddit = "";
        }

        setSupportActionBar(toolbar);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        if (f == null) {
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mSubreddit);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, mNavigationDrawerFragment, "NavigationDrawer")
                    .commit();
        } else {
            mNavigationDrawerFragment = (NavigationDrawerFragment) f;
        }

        Fragment sub = getSupportFragmentManager().findFragmentByTag(mSubreddit);
        if (sub == null) {
            mSubredditFragment = SubredditFragment.newInstance(mSubreddit);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mSubredditFragment, mSubreddit)
                    .commit();
        } else {
            mSubredditFragment = (SubredditFragment) sub;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment != null && fragment instanceof SubredditFragment) {
                    mSubredditFragment = (SubredditFragment) fragment;
                    mSubreddit = mSubredditFragment.getSubreddit();
                    mNavigationDrawerFragment.setSubreddit(mSubreddit);
                }
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    @Override
    public void onResume() {
        super.onResume();
        mSubredditFragment = (SubredditFragment) getSupportFragmentManager().findFragmentByTag(mSubreddit);
        mSubreddit = mSubredditFragment.getSubreddit();
        mNavigationDrawerFragment.setSubreddit(mSubreddit);
        invalidateOptionsMenu();
    }

    @Override
    public void onSubredditSelected(String subreddit) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        if ((mSubreddit == null && subreddit == null)
                || (mSubreddit != null && mSubreddit.equals(subreddit))) {
            mSubredditFragment.refreshData();
        } else {
            mSubreddit = subreddit;
            if (getSupportFragmentManager().findFragmentByTag(subreddit) != null) {
                mSubredditFragment = (SubredditFragment) getSupportFragmentManager().findFragmentByTag(subreddit);
            } else {
                mSubredditFragment = SubredditFragment.newInstance(subreddit);
            }
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(mSubreddit)
                    .replace(R.id.container, mSubredditFragment, mSubreddit)
                    .commit();
        }
    }

    @Override
    public void onAccountChanged() {
        mSubredditFragment.onAccountChanged();
    }

    @Override
    public void onBackPressed()  {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("YouTubeFragment");
        if (fragment != null && fragment instanceof YouTubeFragment
                && ((YouTubeFragment) fragment).onBackPressed()) {
            return;
        }
        if (mDrawerLayout.isDrawerOpen(Gravity.START) || mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(R.id.right_drawer)) {
                mDrawerLayout.closeDrawer(Gravity.END);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImagePagerFragmentCreated() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setEnabled(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    @Override
    public void onImagePagerFragmentDestroyed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onImageTapped() {
        getSupportFragmentManager().popBackStack();
    }
}
