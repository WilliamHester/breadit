package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
            setUpActionBar();
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
    public void onSortSelected(int sort) {
        mSubredditFragment.setPrimarySort(sort);
    }

    @Override
    public void onSubSortSelected(int sort) {
        mSubredditFragment.setSecondarySort(sort);
    }

    private void setUpActionBar() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        mTitleView = new TextView(this);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mTitleView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        mTitleView.setTextColor(getResources().getColor(R.color.mid_gray));
        updateActionBar(mSubreddit);
        ImageView saveImage = new ImageView(this);
        saveImage.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_save));
        ImageView historyImage = new ImageView(this);
        historyImage.setImageDrawable(getResources()
                .getDrawable(android.R.drawable.ic_menu_recent_history));

        Bundle args = new Bundle();
        args.putParcelable("account", mAccount);

        mTabView = (TabView) v.findViewById(R.id.tabs);
        mTabView.addTab(SubredditFragment.newInstance(mAccount, mSubreddit),
                TabView.TAB_TYPE_MAIN, mTitleView, "subreddit");
        mTabView.addTab(SubredditFragment.newInstance(mAccount, SubredditFragment.SAVED),
                TabView.TAB_TYPE_MINOR, saveImage, "saved");
        mTabView.addTab(SubredditFragment.newInstance(mAccount, SubredditFragment.HISTORY),
                TabView.TAB_TYPE_MINOR, historyImage, "history");

        getFragmentManager().beginTransaction()
                .replace(R.id.container, mTabView.getFragment("subreddit"))
                .commit();

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(v);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("FrontPage");
    }

    private void updateActionBar(String sub) {
        if (sub == null)
            mTitleView.setText("FrontPage");
        else
            mTitleView.setText("/r/" + sub);
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
