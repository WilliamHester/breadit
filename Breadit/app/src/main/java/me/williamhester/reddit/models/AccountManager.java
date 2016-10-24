package me.williamhester.reddit.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import me.williamhester.reddit.databases.AccountDataSource;
import me.williamhester.reddit.models.reddit.Account;

/**
 * Created by william on 9/5/14.
 */
public class AccountManager {

  private static Account mAccount;
  private static ArrayList<Account> mAccounts;
  private static SharedPreferences mPrefs;

  private AccountManager() {
  }

  public static void init(Context context) {
    mPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    long id = mPrefs.getLong("accountId", -1);
    AccountDataSource dataSource = new AccountDataSource(context);
    dataSource.open();
    if (id != -1) {
      mAccount = dataSource.getAccount(id);
    }
    mAccounts = dataSource.getAllAccounts();
    dataSource.close();
  }

  public static boolean isLoggedIn() {
    return mAccount != null;
  }

  public static Account getAccount() {
    return mAccount;
  }

  public static ArrayList<Account> getAccounts() {
    return mAccounts;
  }

  public static void setAccount(Account account) {
    mAccount = account;
    long id = -1;
    if (mAccount != null) {
      id = mAccount.getId();
    }
    SharedPreferences.Editor editor = mPrefs.edit();
    editor.putLong("accountId", id);
    editor.apply();
  }
}
