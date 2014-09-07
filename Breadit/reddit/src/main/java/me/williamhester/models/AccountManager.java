package me.williamhester.models;

import android.content.Context;
import android.content.SharedPreferences;

import me.williamhester.databases.AccountDataSource;

/**
 * Created by william on 9/5/14.
 */
public class AccountManager {

    private static Account mAccount;

    private AccountManager() { }

    public static void init(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        long id = prefs.getLong("accountId", -1);
        if (id != -1) {
            AccountDataSource dataSource = new AccountDataSource(context);
            dataSource.open();
            mAccount = dataSource.getAccount(id);
            dataSource.close();
        }
    }

    public static boolean isLoggedIn() {
        return mAccount != null;
    }

    public static Account getAccount() {
        return mAccount;
    }

    public static void setAccount(Account account) {
        mAccount = account;
    }
}