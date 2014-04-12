package me.williamhester.reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import me.williamhester.areddit.Account;

public class AccountDataSource {

    private SQLiteDatabase mDatabase;
    private AccountSqlHelper mHelper;

    public AccountDataSource(Context context) {
        this(new AccountSqlHelper(context));
    }

    public AccountDataSource(AccountSqlHelper helper) {
        mHelper = helper;
    }

    public void open() throws SQLException {
        mDatabase = mHelper.getWritableDatabase();
    }

    public void close() {
        mHelper.close();
    }

    public void addAccount(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_ACCOUNT_USERNAME, account.getUsername());
        values.put(AccountSqlHelper.COLUMN_ACCOUNT_COOKIE, account.getCookie());
        values.put(AccountSqlHelper.COLUMN_ACCOUNT_MODHASH, account.getModhash());
        values.put(AccountSqlHelper.COLUMN_SUBREDDIT_LIST, "");
        values.put(AccountSqlHelper.COLUMN_SAVED_SUBMISSIONS, "");
        long id = mDatabase.insert(AccountSqlHelper.TABLE_ACCOUNTS, null, values);
        account.setId(id);
    }

    public Account getAccount(long id) {
        if (!mDatabase.isOpen())
            return null;
        Cursor c = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.ALL_COLUMNS,
                AccountSqlHelper.COLUMN_ID + " = " + id, null, null, null, null);
        return new Account(c);
    }

    public void setSubredditList(String subs, long id) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_SUBREDDIT_LIST, subs);

        String[] sId = { "" + id };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public void setSavedSubmissions(String submissions, long id) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_SAVED_SUBMISSIONS, submissions);

        String[] sId = { "" + id };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public void updateLoginInfo(String cookie, String modhash, long id) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_ACCOUNT_COOKIE, cookie);
        values.put(AccountSqlHelper.COLUMN_ACCOUNT_MODHASH, modhash);

        String[] sId = { "" + id };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }
}