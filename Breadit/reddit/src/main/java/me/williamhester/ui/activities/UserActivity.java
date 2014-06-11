package me.williamhester.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.models.Account;
import me.williamhester.ui.fragments.MessageDialogFragment;
import me.williamhester.ui.fragments.UserFragment;
import me.williamhester.reddit.R;

/**
 * Created by William on 4/13/14.
 */
public class UserActivity extends Activity {

    private Account mAccount;
    private String mUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        if (getIntent() != null && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            mUsername = getIntent().getDataString();
            mUsername = mUsername.substring(mUsername.indexOf("/user/") + 6);
        } else if (getIntent().getExtras() != null) {
            mAccount = getIntent().getExtras().getParcelable("account");
            mUsername = getIntent().getExtras().getString("username");
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, UserFragment.newInstance(mUsername, mAccount))
                .commit();

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_compose_message:
                MessageDialogFragment messageDialogFragment = MessageDialogFragment
                        .newInstance(mAccount, mUsername);
                messageDialogFragment.show(getFragmentManager(), "message_dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
