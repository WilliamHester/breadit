package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.williamhester.areddit.Account;

public class AccountActivity extends Activity implements TabView.TabSwitcher {

    private Account mAccount;
    private TabView mTabView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if (getIntent().getExtras() != null) {
            mAccount = getIntent().getExtras().getParcelable("account");
        }
        setUpActionBarTabs();
    }

    public void setUpActionBarTabs() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        View inboxView = inflater.inflate(R.layout.tab_inbox, null);
        View profileView = inflater.inflate(R.layout.tab_my_profile, null);

        Bundle args = new Bundle();
        args.putParcelable("account", mAccount);

        mTabView = (TabView) v.findViewById(R.id.tabs);
        mTabView.addTab(MessagesFragment.class, args, TabView.TAB_TYPE_MAIN, inboxView, "inbox");
        mTabView.addTab(UserFragment.class, args, TabView.TAB_TYPE_MAIN, profileView, "sent");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }

        mTabView.selectTab("inbox");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_compose_message:
                MessageDialogFragment messageDialogFragment = MessageDialogFragment
                        .newInstance(mAccount, null);
                messageDialogFragment.show(getFragmentManager(), "message_dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(String tag, Fragment fragment) {
        if (getFragmentManager().findFragmentByTag(tag) != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, getFragmentManager().findFragmentByTag(tag))
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        }
    }

    @Override
    public void onTabReselected(String tag, Fragment fragment) {

    }

    @Override
    public void onTabUnSelected(String tag) {

    }
}
