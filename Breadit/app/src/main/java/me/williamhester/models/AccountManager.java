package me.williamhester.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.reddit.RedditAccount;

/**
 * Created by william on 9/5/14.
 */
public class AccountManager {

    private static RedditAccount mRedditAccount;
    private static ArrayList<RedditAccount> mRedditAccounts;
    private static SharedPreferences mPrefs;

    private AccountManager() { }

    public static void init(Context context) {
        mPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        long id = mPrefs.getLong("accountId", -1);
        AccountDataSource dataSource = new AccountDataSource(context);
        dataSource.open();
        if (id != -1) {
            mRedditAccount = dataSource.getAccount(id);
        }
        mRedditAccounts = dataSource.getAllAccounts();
        dataSource.close();
    }

    public static boolean isLoggedIn() {
        return mRedditAccount != null;
    }

    public static RedditAccount getAccount() {
        return mRedditAccount;
    }

    public static ArrayList<RedditAccount> getAccounts() {
        return mRedditAccounts;
    }

    public static void setAccount(RedditAccount redditAccount) {
        mRedditAccount = redditAccount;
        long id = -1;
        if (mRedditAccount != null) {
            id = mRedditAccount.getId();
        }
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong("accountId", id);
        editor.apply();
    }
}
