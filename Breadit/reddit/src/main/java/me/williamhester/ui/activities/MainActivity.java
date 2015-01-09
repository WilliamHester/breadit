package me.williamhester.ui.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.SettingsManager;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Friend;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.notifications.MessageNotificationBroadcastReceiver;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.AccountFragment;
import me.williamhester.ui.fragments.ContentFragment;
import me.williamhester.ui.fragments.FriendsFragment;
import me.williamhester.ui.fragments.MessagesFragment;
import me.williamhester.ui.fragments.NavigationDrawerFragment;
import me.williamhester.ui.fragments.SidebarFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.UserFragment;

public class MainActivity extends BaseActivity implements
        MessagesFragment.MessageFragmentCallbacks,
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        ContentFragment.ContentFragmentCallbacks {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Fragment mCurrentFragment;
    private FriendsFragment mFriendsFragment;
    private MessagesFragment mMessagesFragment;
    private NavigationDrawerFragment mNavigationFragment;
    private SubredditFragment mSubredditFragment;
    private UserFragment mMyAccountFragment;
    private SidebarFragment mSidebarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String startFrom;
        if (getIntent().getExtras() != null) {
            startFrom = getIntent().getExtras().getString("startFrom", "subreddit");
        } else {
            startFrom = "subreddit";
        }

        Fragment ndf = getSupportFragmentManager().findFragmentById(R.id.left_drawer);
        if (ndf == null) {
            mNavigationFragment = NavigationDrawerFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer, mNavigationFragment, "drawer")
                    .commit();
        } else {
            mNavigationFragment = (NavigationDrawerFragment) ndf;
        }

        Fragment sub = getSupportFragmentManager().findFragmentByTag("subreddit");
        if (sub == null) {
            mSubredditFragment = SubredditFragment.newInstance("");
        } else {
            mSubredditFragment = (SubredditFragment) sub;
        }

        Fragment messages = getSupportFragmentManager().findFragmentByTag("messages");
        if (messages == null) {
            mMessagesFragment = MessagesFragment.newInstance();
        } else {
            mMessagesFragment = (MessagesFragment) messages;
        }

        Fragment myAccount = getSupportFragmentManager().findFragmentByTag("myAccount");
        if (myAccount == null) {
            mMyAccountFragment = UserFragment.newInstance();
        } else {
            mMyAccountFragment = (UserFragment) myAccount;
        }

        Fragment friends = getSupportFragmentManager().findFragmentByTag("friends");
        if (friends == null) {
            mFriendsFragment = FriendsFragment.newInstance();
        } else {
            mFriendsFragment = (FriendsFragment) friends;
        }

        Fragment side = getSupportFragmentManager().findFragmentById(R.id.right_drawer);
        if (side == null) {
            mSidebarFragment = SidebarFragment.newInstance("");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.right_drawer, mSidebarFragment, "sidebar")
                    .commit();
        } else {
            mSidebarFragment = (SidebarFragment) side;
        }

        if (savedInstanceState == null) {
            switch (startFrom) {
                case "inbox":
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, mMessagesFragment, "messages")
                            .commit();
                    mCurrentFragment = mMessagesFragment;
                    break;
                case "myAccount":
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, mMyAccountFragment, "messages")
                            .commit();
                    mCurrentFragment = mMessagesFragment;
                    break;
                default:
                case "subreddit":
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_container, mSubredditFragment, "subreddit")
                            .commit();
                    mCurrentFragment = mSubredditFragment;
                    break;
            }
        } else {
            mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.main_container);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
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
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (AccountManager.isLoggedIn()) {
            startNotificationService();
        }
    }

    private void startNotificationService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MessageNotificationBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        if (SettingsManager.getNotificationInterval() == -1) {
            alarmManager.cancel(pendingIntent);
        } else {
            long interval = SettingsManager.getNotificationInterval() * 60 * 1000;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval,
                    pendingIntent);
        }
    }

    @Override
    public void onSubredditSelected(String subreddit) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
        }

        mSubredditFragment.loadSubreddit(subreddit);

        if (!TextUtils.isEmpty(subreddit)) {
            RedditApi.getSubredditDetails(this, subreddit, new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    ResponseRedditWrapper response = new ResponseRedditWrapper(result, new Gson());
                    if (response.getData() instanceof Subreddit) {
                        mSidebarFragment.setSubreddit((Subreddit) response.getData());
                        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                                Gravity.END);
                    } else {
                        mSubredditFragment.showSubredditDoesNotExist();
                    }
                }
            });
        }
    }

    @Override
    public void onHomeClicked() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START | Gravity.END)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    @Override
    public void onAccountChanged() {
        if (mCurrentFragment instanceof AccountFragment) {
            ((AccountFragment) mCurrentFragment).onAccountChanged();
        }
    }

    @Override
    public void onHomeSelected() {
        if (mSubredditFragment.isDetached()) {
            getSupportFragmentManager().beginTransaction()
                    .attach(mSubredditFragment)
                    .detach(mCurrentFragment)
                    .commit();
            mCurrentFragment = mSubredditFragment;
        }
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    @Override
    public void onMessagesSelected() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mMessagesFragment.isDetached()) {
            ft.attach(mMessagesFragment)
                    .detach(mCurrentFragment)
                    .commit();
        } else if (!mMessagesFragment.isAdded()) {
            ft.add(R.id.main_container, mMessagesFragment, "messages")
                    .detach(mCurrentFragment)
                    .commit();
        }
        mDrawerLayout.closeDrawer(Gravity.START);
        mCurrentFragment = mMessagesFragment;
    }

    @Override
    public void onMyAccountSelected() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mMyAccountFragment.isDetached()) {
            ft.attach(mMyAccountFragment)
                    .detach(mCurrentFragment)
                    .commit();
        } else if (!mMyAccountFragment.isAdded()) {
            ft.add(R.id.main_container, mMyAccountFragment, "myAccount")
                    .detach(mCurrentFragment)
                    .commit();
        }
        mDrawerLayout.closeDrawer(Gravity.START);
        mCurrentFragment = mMyAccountFragment;
    }

    @Override
    public void onFriendsSelected() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mFriendsFragment.isDetached()) {
            ft.attach(mFriendsFragment)
                    .detach(mCurrentFragment)
                    .commit();
        } else if (!mFriendsFragment.isAdded()) {
            ft.add(R.id.main_container, mFriendsFragment, "friends")
                    .detach(mCurrentFragment)
                    .commit();
        }
        mDrawerLayout.closeDrawer(Gravity.START);
        mCurrentFragment = mFriendsFragment;
    }

    @Override
    public void onBackPressed()  {
        if (mDrawerLayout.isDrawerOpen(Gravity.HORIZONTAL_GRAVITY_MASK)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMessageReadCountChanged(int newCount) {
        if (mNavigationFragment != null) {
            mNavigationFragment.setUnreadCount(newCount);
        }
    }

    @Override
    public void onContentFragmentOpened() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onContentFragmentClosed() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}
