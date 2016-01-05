package me.williamhester.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import me.williamhester.models.reddit.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.reddit.Subreddit;

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
        addSubscriptionToAccount(subreddit, AccountManager.getAccount());
    }

    public void addSubscriptionToAccount(Subreddit subreddit, Account account) {
        if (subreddit.getTableId() > 0) {
            ContentValues values = new ContentValues();

            values.put(AccountSqlHelper.COLUMN_USER_IS_MOD, subreddit.userIsModerator());
            values.put(AccountSqlHelper.COLUMN_USER_IS_BANNED, subreddit.userIsBanned());
            values.put(AccountSqlHelper.COLUMN_SUBREDDIT_ID, subreddit.getTableId());
            values.put(AccountSqlHelper.COLUMN_USER_ID, account.getId());

            mDatabase.insert(AccountSqlHelper.TABLE_SUBSCRIPTIONS, null, values);
        } else {
            throw new UnsupportedOperationException("Attempted to insert a subreddit with id 0 into the table");
        }
    }

    public Account getAccount(long id) {
        if (!mDatabase.isOpen())
            return null;
        Cursor c = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
                AccountSqlHelper.COLUMN_USER_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        return new Account(c);
    }

    public ArrayList<Subreddit> getCurrentAccountSubreddits() {
        ArrayList<Subreddit> subreddits = new ArrayList<>();
        String query = "SELECT "
                + AccountSqlHelper.TABLE_SUBREDDITS + "." + AccountSqlHelper.COLUMN_SUBREDDIT_ID + ", "
                + AccountSqlHelper.COLUMN_DISPLAY_NAME + ", "
                + AccountSqlHelper.COLUMN_OVER_18 + ", "
                + AccountSqlHelper.COLUMN_PUBLIC + ", "
                + AccountSqlHelper.COLUMN_NAME + ", "
                + AccountSqlHelper.COLUMN_CREATED + ", "
                + AccountSqlHelper.COLUMN_SUBMISSION_TYPES + ", "
                + AccountSqlHelper.COLUMN_USER_IS_MOD + ", "
                + AccountSqlHelper.COLUMN_USER_IS_BANNED
                + " FROM " + AccountSqlHelper.TABLE_SUBSCRIPTIONS + " LEFT JOIN "
                + AccountSqlHelper.TABLE_SUBREDDITS + " ON "
                + AccountSqlHelper.TABLE_SUBSCRIPTIONS + "." + AccountSqlHelper.COLUMN_SUBREDDIT_ID
                + " = "
                + AccountSqlHelper.TABLE_SUBREDDITS + "." + AccountSqlHelper.COLUMN_SUBREDDIT_ID
                + " WHERE " + AccountSqlHelper.COLUMN_USER_ID + " = "
                + AccountManager.getAccount().getId();

        Cursor cursor = mDatabase.rawQuery(query, null);

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
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS,
                AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
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

    public ArrayList<Subreddit> getAllSubreddits() {
        ArrayList<Subreddit> subreddits = new ArrayList<>();
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_SUBREDDITS,
                AccountSqlHelper.ALL_COLUMNS_SUBREDDITS,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Subreddit s = new Subreddit(cursor);
            subreddits.add(s);
            cursor.moveToNext();
            if (s.getDisplayName() == null) {
                Log.d("AccountDataSource", "Created subreddit's display name is null.");
            }
        }
        cursor.close();

        return subreddits;
    }

    public void deleteAccount(Account account) {
        mDatabase.delete(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.COLUMN_USER_ID
                + " = " + account.getId(), null);
    }

}
