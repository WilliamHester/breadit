package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.OauthLoginFragment;

/**
 * This Activity is just a container for the LogInPagerFragment. It really shouldn't exist, but
 * because the PreferencesFragment does not exist as an official support library, this is the
 * solution I'm going with for now.
 */
public class LogInActivity2 extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (f == null) {
            f = OauthLoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, f, "LoginFragment")
                    .commit();
        }
    }
}
