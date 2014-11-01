package me.williamhester.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;

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

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SubredditFragment mSubredditFragment;
    private String mSubreddit;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

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

        setSupportActionBar(mToolbar);

        if (mNavigationDrawerFragment == null) {
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance();
        }

        updateActionBar(null);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment,
                        "NavigationDrawer")
                .commit();

        if (getSupportFragmentManager().findFragmentByTag(mSubreddit) == null) {
            mSubredditFragment = SubredditFragment.newInstance(mSubreddit);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mSubredditFragment, mSubreddit)
                    .commit();
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
        if (mNavigationDrawerFragment.isOpen()) {
            mNavigationDrawerFragment.toggle();
        } else {
            super.onBackPressed();
        }
    }

    private void updateActionBar(String sub) {
        if (TextUtils.isEmpty(sub)) {
            mToolbar.setTitle("FrontPage");
        } else {
            mToolbar.setTitle("/r/" + sub);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onImagePagerFragmentCreated() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            drawerLayout.setEnabled(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }

    @Override
    public void onImagePagerFragmentDestroyed() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setEnabled(true);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onImageTapped() {
        getSupportFragmentManager().popBackStack();
    }
}
