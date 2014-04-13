package me.williamhester.reddit;

import android.app.Activity;
import android.os.Bundle;

import me.williamhester.areddit.Account;

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
        if (getIntent().getExtras() != null) {
            mAccount = getIntent().getExtras().getParcelable("account");
            mUsername = getIntent().getExtras().getString("username");
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, UserFragment.newInstance(mUsername, mAccount))
                .commit();
    }
}
