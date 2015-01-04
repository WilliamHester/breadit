package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.UserFragment;

/**
 * Created by William on 4/13/14.
 */
public class UserActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (f != null) {
            return;
        }

        String username = null;
        if (getIntent().getExtras() != null) {
            username = getIntent().getExtras().getString("username");
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, UserFragment.newInstance(username))
                .commit();
    }
}
