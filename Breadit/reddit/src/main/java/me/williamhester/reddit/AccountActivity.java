package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Message;

/**
 * Created by William on 4/12/14.
 */
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

        ImageView inboxImage = new ImageView(this);
        inboxImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_email));
        ImageView sentImage = new ImageView(this);
        sentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_read));
        ImageView modMail = new ImageView(this);
        modMail.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_share)); // This will be changed when a mod mail icon is found

        Bundle args = new Bundle();
        args.putParcelable("account", mAccount);

        mTabView = (TabView) v.findViewById(R.id.tabs);
        mTabView.addTab(MessagesFragment.class, args, TabView.TAB_TYPE_MINOR, inboxImage, "inbox");
        args.putInt("filter_by", Message.SENT);
        mTabView.addTab(MessagesFragment.class, args, TabView.TAB_TYPE_MINOR, sentImage, "sent");
        args.putInt("filter_by", Message.MOD_MAIL);
        mTabView.addTab(MessagesFragment.class, args, TabView.TAB_TYPE_MINOR, modMail, "modmail");

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.container, mTabView.getFragment("inbox"))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(String tag, Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onTabReselected(String tag, Fragment fragment) {

    }

    @Override
    public void onTabUnSelected(String tag) {

    }
}
