package me.williamhester.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ComposeMessageFragment;
import me.williamhester.ui.fragments.UserFragment;

/**
 * Created by William on 4/13/14.
 */
public class UserActivity extends ActionBarActivity {

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
            mUsername = getIntent().getExtras().getString("username");
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, UserFragment.newInstance(mUsername))
                .commit();
    }
}
