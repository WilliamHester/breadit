package me.williamhester.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.williamhester.ui.fragments.MessageDialogFragment;
import me.williamhester.ui.fragments.MessagesFragment;
import me.williamhester.ui.fragments.UserFragment;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.TabView;

public class AccountActivity extends Activity implements TabView.TabSwitcher {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setUpActionBarTabs();
    }

    public void setUpActionBarTabs() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        View inboxView = inflater.inflate(R.layout.tab_inbox, null);
        View profileView = inflater.inflate(R.layout.tab_my_profile, null);

        TabView tabView = (TabView) v.findViewById(R.id.tabs);
        tabView.addTab(MessagesFragment.class, null, TabView.TAB_TYPE_MAIN, inboxView, "inbox");
        tabView.addTab(UserFragment.class, null, TabView.TAB_TYPE_MAIN, profileView, "sent");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }

        tabView.selectTab("inbox");
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
                MessageDialogFragment messageDialogFragment = MessageDialogFragment.newInstance(null);
                messageDialogFragment.show(getFragmentManager(), "message_dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(String tag, Fragment fragment) {
        Fragment f = getFragmentManager().findFragmentByTag(tag);
        if (getFragmentManager().findFragmentByTag(tag) != null) {
            getFragmentManager().beginTransaction()
                    .attach(f)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        }
    }

    @Override
    public void onTabReselected(String tag, Fragment fragment) { }

    @Override
    public void onTabUnSelected(String tag) {
        Fragment f = getFragmentManager().findFragmentByTag(tag);
        if (f != null) {
            getFragmentManager().beginTransaction()
                    .detach(f)
                    .commit();
        }
    }
}
