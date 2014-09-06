package me.williamhester;

import android.app.Application;
import android.content.SharedPreferences;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;

/**
 * Created by william on 9/5/14.
 */
public class BreaditApplication extends Application {

    private Account mAccount;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
        long id = prefs.getLong("accountId", -1);
        if (id != -1) {
            AccountDataSource dataSource = new AccountDataSource(this);
            dataSource.open();
            mAccount = dataSource.getAccount(id);
            dataSource.close();
        }
    }

    public boolean isLoggedIn() {
        return mAccount != null;
    }

    public Account getAccount() {
        return mAccount;
    }

    public void setAccount(Account account) {
        mAccount = account;
    }

}
