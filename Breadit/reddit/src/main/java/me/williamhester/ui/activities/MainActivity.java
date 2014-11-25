package me.williamhester.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ImageFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.NavigationDrawerFragment;
import me.williamhester.ui.fragments.SidebarFragment;
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
    private SidebarFragment mSidebarFragment;
    private String mSubredditTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            if (getIntent().getExtras() != null) {
                mSubredditTitle = getIntent().getExtras().getString(SUBREDDIT);
            } else {
                mSubredditTitle = getIntent().getDataString();
                if (mSubredditTitle != null)
                    mSubredditTitle = mSubredditTitle.substring(mSubredditTitle.indexOf("/subreddit/") + 11);
            }
        } else {
            mSubredditTitle = "";
        }

        setSupportActionBar(toolbar);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        if (f == null) {
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mSubredditTitle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, mNavigationDrawerFragment, "NavigationDrawer")
                    .commit();
        } else {
            mNavigationDrawerFragment = (NavigationDrawerFragment) f;
        }

        Fragment sub = getSupportFragmentManager().findFragmentByTag(mSubredditTitle);
        if (sub == null) {
            mSubredditFragment = SubredditFragment.newInstance(mSubredditTitle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mSubredditFragment, mSubredditTitle)
                    .commit();
        } else {
            mSubredditFragment = (SubredditFragment) sub;
        }

        Fragment side = getSupportFragmentManager().findFragmentById(R.id.right_drawer);
        if (side == null) {
            mSidebarFragment = SidebarFragment.newInstance(mSubredditTitle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, mSidebarFragment, "side")
                    .commit();
        } else {
            mSidebarFragment = (SidebarFragment) side;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                if (fragment != null && fragment instanceof SubredditFragment) {
                    mSubredditFragment = (SubredditFragment) fragment;
                    onSubredditSelected(mSubredditFragment.getSubreddit());
//                    mNavigationDrawerFragment.setSubreddit(mSubredditTitle);
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
    public void onSubredditSelected(String subreddit) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
        }

        if ((mSubredditTitle == null && subreddit == null)
                || (mSubredditTitle != null && mSubredditTitle.equals(subreddit))) {
//            mSubredditFragment.refreshData();
        } else {
            if (TextUtils.isEmpty(subreddit)) {
                mNavigationDrawerFragment.setSubreddit(Subreddit.FRONT_PAGE);
            } else {
                RedditApi.getSubredditDetails(this, subreddit, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        ResponseRedditWrapper response = new ResponseRedditWrapper(result, new Gson());
                        if (response.getData() instanceof Subreddit) {
                            mNavigationDrawerFragment.setSubreddit((Subreddit) response.getData());
                            mSidebarFragment.setSubreddit((Subreddit) response.getData());
                            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                                    Gravity.END);
                        } else {
                            mSubredditFragment.showSubredditDoesNotExist();
                            mNavigationDrawerFragment.setSubreddit((Subreddit) null);
                        }
                    }
                });
            }
            mSubredditTitle = subreddit;
            if (getSupportFragmentManager().findFragmentByTag(subreddit) != null) {
                mSubredditFragment = (SubredditFragment) getSupportFragmentManager().findFragmentByTag(subreddit);
                if (!mSubredditFragment.isResumed()) {
                    getSupportFragmentManager().beginTransaction()
                            .addToBackStack(mSubredditTitle)
                            .replace(R.id.container, mSubredditFragment, mSubredditTitle)
                            .commit();
                }
            } else {
                mSubredditFragment = SubredditFragment.newInstance(subreddit);
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(mSubredditTitle)
                        .replace(R.id.container, mSubredditFragment, mSubredditTitle)
                        .commit();
            }
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSuggestionsAdapter(new SearchAdapter(MainActivity.this));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

    private static class SearchAdapter extends SimpleCursorAdapter {

        private CharSequence mConstraint = "";

        public SearchAdapter(Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line, null, new String[]{""}, null, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);

        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            mConstraint = constraint;
            return null;
        }

        @Override
        public Object getItem(int position) {
            switch (position) {
                case 0:
                    return "/r/" + mConstraint;
                case 1:
                    return "/u/" + mConstraint;
            }
            return "";
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
