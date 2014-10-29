package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.ForgotPasswordFragment;
import me.williamhester.ui.fragments.LogInPagerFragment;

/**
 * This Activity is just a container for the LogInPagerFragment. It really shouldn't exist, but
 * because the PreferencesFragment does not exist as an official support library, this is the
 * solution I'm going with for now.
 */
public class LogInActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        LogInPagerFragment fragment = LogInPagerFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, "LogIn")
                .commit();
    }

    public void onLoggedIn() {
        setResult(RESULT_OK);
        finish();
    }

    public void openForgotPassword(String username) {
        Fragment forgotFragment = ForgotPasswordFragment.newInstance(username);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, forgotFragment, "forgot")
                .addToBackStack("forgot")
                .commit();
    }
}
