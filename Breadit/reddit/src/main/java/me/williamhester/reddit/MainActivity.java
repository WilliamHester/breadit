package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Message;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SubredditFragment mSubredditFragment;
    private SharedPreferences mPrefs;
    private Account mAccount;
    private String mSubreddit;
    private TabView mTabView;
    private TextView mTitleView;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = getSharedPreferences("preferences", MODE_PRIVATE);

        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            mSubreddit = getIntent().getDataString();
            mSubreddit = mSubreddit.substring(mSubreddit.indexOf("/r/") + 3);
            Log.i("MainActivity", mSubreddit);
            long id = mPrefs.getLong("accountId", -1);
            if (id != -1) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            }
        } else if (getIntent() != null && getIntent().getExtras() != null) { // If the user just completed the setup
            boolean b = getIntent().getExtras().getBoolean("finishedSetup");
            mAccount = getIntent().getExtras().getParcelable("account");
            SharedPreferences.Editor edit = mPrefs.edit();
            edit.putBoolean("finishedSetup", b);
            if (mAccount != null) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                dataSource.addAccount(mAccount);
                dataSource.close();
                edit.putLong("accountId", mAccount.getId());
            }
            edit.commit();
        } else if (!mPrefs.getBoolean("finishedSetup", false)) { // If the user has not completed the setup
            Intent i = new Intent(this, SetupActivity.class);
            startActivity(i);
        } else { // If the user has completed the setup
            long id = mPrefs.getLong("accountId", -1);
            if (id != -1) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            }
        }

        mSubredditFragment = SubredditFragment.newInstance(mAccount, mSubreddit);
        mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mAccount);
        mTitle = getTitle();

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        updateActionBar(null);

        getFragmentManager().beginTransaction()
                .replace(R.id.navigation_drawer_container, mNavigationDrawerFragment)
                .commit();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mSubredditFragment)
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(String subreddit) {
        if (mSubreddit == subreddit || (mSubreddit != null && mSubreddit.equals(subreddit))) {
            mSubredditFragment.refreshData();
        } else {
            mSubreddit = subreddit;
            Fragment frag = getFragmentManager().findFragmentByTag(subreddit);
            if (frag != null) {
                getFragmentManager().beginTransaction()
                        .addToBackStack(subreddit)
                        .replace(R.id.container, frag)
                        .commit();
            } else {
                mSubredditFragment = SubredditFragment.newInstance(mAccount, subreddit);
                getFragmentManager().beginTransaction()
                        .addToBackStack(subreddit)
                        .replace(R.id.container, mSubredditFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mNavigationDrawerFragment.isOpen()) {
                mNavigationDrawerFragment.toggle();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSortSelected(int sort) {
        mSubredditFragment.setPrimarySort(sort);
    }

    @Override
    public void onSubSortSelected(int sort) {
        mSubredditFragment.setSecondarySort(sort);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("FrontPage");
    }

    private void updateActionBar(String sub) {
        if (sub == null)
            getActionBar().setTitle("FrontPage");
        else
            getActionBar().setTitle("/r/" + sub);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (mAccount == null) {
            menu.removeItem(R.id.action_my_account);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("account", mAccount);
            i.putExtras(b);
            startActivity(i);
            return true;
        } else if (id == R.id.action_submit) {
            SubmitDialogFragment sf = SubmitDialogFragment.newInstance(mAccount, mSubreddit);
            sf.show(getFragmentManager(), "submit_fragment");
        } else if (id == R.id.action_my_account) {
            Bundle b = new Bundle();
            b.putParcelable("account", mAccount);
            Intent i = new Intent(this, AccountActivity.class);
            i.putExtras(b);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
