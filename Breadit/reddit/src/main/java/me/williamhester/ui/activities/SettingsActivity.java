package me.williamhester.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.models.Account;
import me.williamhester.ui.fragments.SettingsFragment;
import me.williamhester.reddit.R;


public class SettingsActivity extends Activity {

    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Display the fragment as the main content.
        if (getIntent().getExtras() != null) {
            mAccount = getIntent().getExtras().getParcelable("account");
        }
        SettingsFragment fragment = SettingsFragment.newInstance(mAccount);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        if (getActionBar() != null)
            getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
