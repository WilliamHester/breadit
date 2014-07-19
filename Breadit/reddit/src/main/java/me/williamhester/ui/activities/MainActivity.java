package me.williamhester.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import me.williamhester.models.Account;
import me.williamhester.ui.fragments.NavigationDrawerFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.SubmitDialogFragment;
import me.williamhester.databases.AccountDataSource;
import me.williamhester.reddit.R;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment mSubredditFragment;
    private Account mAccount;
    private String mSubreddit;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);

        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            mSubreddit = getIntent().getDataString();
            if (mSubreddit != null)
                mSubreddit = mSubreddit.substring(mSubreddit.indexOf("/subreddit/") + 11);
            long id = prefs.getLong("accountId", -1);
            if (id != -1) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            }
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mAccount, mSubreddit);
        } else if (getIntent() != null && getIntent().getExtras() != null) { // If the user just completed the setup
            boolean b = getIntent().getExtras().getBoolean("finishedSetup");
            mAccount = getIntent().getExtras().getParcelable("account");
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("finishedSetup", b);
            if (mAccount != null) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                dataSource.addAccount(mAccount);
                dataSource.close();
                edit.putLong("accountId", mAccount.getId());
            }
            edit.commit();
        } else if (!prefs.getBoolean("finishedSetup", false)) { // If the user has not completed the setup
            Intent i = new Intent(this, SetupActivity.class);
            startActivity(i);
        } else { // If the user has completed the setup
            long id = prefs.getLong("accountId", -1);
            if (id != -1) {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            }
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        if (mNavigationDrawerFragment == null)
            mNavigationDrawerFragment = NavigationDrawerFragment.newInstance(mAccount);

        updateActionBar(null);

        getFragmentManager().beginTransaction()
                .add(R.id.navigation_drawer_container, mNavigationDrawerFragment,
                        "NavigationDrawer")
                .commit();

        if (getFragmentManager().findFragmentByTag("SubredditFragment") != null) {
            mSubredditFragment = getFragmentManager().findFragmentByTag("SubredditFragment");
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mSubredditFragment, "SubredditFragment")
                    .commit();
        } else {
            mSubredditFragment = SubredditFragment.newInstance(mAccount, mSubreddit);
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mSubredditFragment, "SubredditFragment")
                    .commit();
        }

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                mSubredditFragment = getFragmentManager().findFragmentByTag("SubredditFragment");
                mSubreddit = ((SubredditFragment) mSubredditFragment).getSubreddit();
                mNavigationDrawerFragment.setSubreddit(mSubreddit,
                        ((SubredditFragment) mSubredditFragment).getPrimarySortType(),
                        ((SubredditFragment) mSubredditFragment).getSecondarySortType());
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        mSubredditFragment = getFragmentManager().findFragmentByTag("SubredditFragment");
        mSubreddit = ((SubredditFragment) mSubredditFragment).getSubreddit();
        mNavigationDrawerFragment.setSubreddit(mSubreddit,
                ((SubredditFragment) mSubredditFragment).getPrimarySortType(),
                ((SubredditFragment) mSubredditFragment).getSecondarySortType());
        SharedPreferences prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        long id = prefs.getLong("accountId", -1);
        if (id != -1) {
            try {
                AccountDataSource dataSource = new AccountDataSource(this);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            } catch (NullPointerException e) {
                Log.e("Breadit", "Error opening database");
            }
        } else {
            mAccount = null;
        }
        invalidateOptionsMenu();
    }


    @Override
    public void onNavigationDrawerItemSelected(String subreddit) {
        Log.i("Main", subreddit + " selected");
        if ((mSubreddit == null && subreddit == null)
                || (mSubreddit != null && mSubreddit.equals(subreddit))) {
            ((SubredditFragment) mSubredditFragment).refreshData();
        } else {
            mSubreddit = subreddit;
            Fragment frag = getFragmentManager().findFragmentByTag(subreddit);
            if (frag != null) {
                mSubredditFragment = frag;
                getFragmentManager().beginTransaction()
                        .addToBackStack(subreddit)
                        .replace(R.id.container, frag, "SubredditFragment")
                        .commit();
            } else {
                mSubredditFragment = SubredditFragment.newInstance(mAccount, subreddit);
                getFragmentManager().beginTransaction()
                        .addToBackStack(subreddit)
                        .replace(R.id.container, mSubredditFragment, "SubredditFragment")
                        .commit();
            }
        }
        mSubreddit = subreddit;
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
        ((SubredditFragment) mSubredditFragment).setPrimarySort(sort);
    }

    @Override
    public void onSubSortSelected(int sort) {
        ((SubredditFragment) mSubredditFragment).setSecondarySort(sort);
    }

    private void updateActionBar(String sub) {
        if (sub == null && getActionBar() != null)
            getActionBar().setTitle("FrontPage");
        else if (getActionBar() != null)
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
            if (mAccount != null) {
                SubmitDialogFragment sf = SubmitDialogFragment.newInstance(mAccount, mSubreddit);
                sf.show(getFragmentManager(), "submit_fragment");
            } else {
                Toast.makeText(this, R.string.must_be_logged_in, Toast.LENGTH_SHORT).show();
            }
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
