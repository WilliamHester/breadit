package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.areddit.Account;

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
            mSubredditFragment = SubredditFragment.newInstance(mAccount, subreddit);
            getFragmentManager().beginTransaction()
                    .addToBackStack("prev_frag")
                    .replace(R.id.container, mSubredditFragment)
                    .commit();
        }
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
        if (mAccount == null) {
            menu.removeItem(R.id.action_my_account);
        }
//            restoreActionBar(); // I don't think we need this because we're already updating it
//            return true;
//        }
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
