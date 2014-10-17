package me.williamhester.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Subreddit;

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
        values.put(AccountSqlHelper.COLUMN_USERNAME, account.getUsername());
        values.put(AccountSqlHelper.COLUMN_COOKIE, account.getCookie());
        values.put(AccountSqlHelper.COLUMN_MODHASH, account.getModhash());
        values.put(AccountSqlHelper.COLUMN_SUBREDDIT_LIST, account.getCommaSepSubs());
        values.put(AccountSqlHelper.COLUMN_SAVED_SUBMISSIONS, "");
        values.put(AccountSqlHelper.COLUMN_SAVED_COMMENTS, "");
        values.put(AccountSqlHelper.COLUMN_HISTORY, "");
        long id = mDatabase.insert(AccountSqlHelper.TABLE_ACCOUNTS, null, values);
        account.setId(id);
    }

    public void addSubreddit(Subreddit subreddit) {
        ContentValues values = new ContentValues();

        values.put(AccountSqlHelper.COLUMN_DISPLAY_NAME, subreddit.getDisplayName());
        values.put(AccountSqlHelper.COLUMN_OVER_18, subreddit.isNsfw() ? 1 : 0);
        values.put(AccountSqlHelper.COLUMN_PUBLIC, subreddit.isPublicTraffic() ? 1 : 0);
        values.put(AccountSqlHelper.COLUMN_NAME, subreddit.getName());
        values.put(AccountSqlHelper.COLUMN_CREATED, subreddit.getCreated());
        values.put(AccountSqlHelper.COLUMN_SUBMISSION_TYPES, subreddit.getSubmissionType());

        long id = mDatabase.insert(AccountSqlHelper.TABLE_SUBREDDITS, null, values);
        subreddit.setTableId(id);
    }

    public void addSubscriptionToCurrentAccount(Subreddit subreddit) {

    }

    public void addSubscription()

    public Account getAccount(long id) {
        if (!mDatabase.isOpen())
            return null;
        Cursor c = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
                AccountSqlHelper.COLUMN_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        return new Account(c);
    }

    public void setHistory(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_HISTORY, account.getHistory());

        String[] sId = { "" + account.getId() };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public void setSubredditList(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_SUBREDDIT_LIST, account.getCommaSepSubs());

        String[] sId = { "" + account.getId() };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public void setSavedComments(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_SAVED_COMMENTS, account.getSavedComments());

        String[] sId = { "" + account.getId() };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public void setSavedSubmissions(Account account) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_SAVED_SUBMISSIONS, account.getSavedSubmissions());

        String[] sId = { "" + account.getId() };
        String where = AccountSqlHelper.COLUMN_ID + "=?";
        mDatabase.update(AccountSqlHelper.TABLE_ACCOUNTS, values, where, sId);
    }

    public ArrayList<Subreddit> getCurrentAccountSubreddits() {
        ArrayList<Subreddit> subreddits = new ArrayList<>();
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_SUBREDDITS,
                AccountSqlHelper.ALL_COLUMNS_SUBREDDITS,
                AccountSqlHelper.COLUMN_USER_ID + " = " + AccountManager.getAccount().getId(),
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            subreddits.add(new Subreddit(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return subreddits;
    }

    public ArrayList<Account> getAllAccounts() {
        ArrayList<Account> accounts = new ArrayList<>();
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Account account = new Account(cursor);
            accounts.add(account);
            cursor.moveToNext();
        }

        cursor.close();
        return accounts;
    }

    public void deleteAccount(Account account) {
        mDatabase.delete(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.COLUMN_ID
                + " = " + account.getId(), null);
    }

}