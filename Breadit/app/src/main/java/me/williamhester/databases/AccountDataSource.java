package me.williamhester.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import me.williamhester.models.reddit.RedditAccount;
import me.williamhester.models.AccountManager;
import me.williamhester.models.reddit.RedditSubreddit;

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

    public void addAccount(RedditAccount redditAccount) {
        ContentValues values = new ContentValues();
        values.put(AccountSqlHelper.COLUMN_USERNAME, redditAccount.getUsername());
        values.put(AccountSqlHelper.COLUMN_COOKIE, redditAccount.getCookie());
        values.put(AccountSqlHelper.COLUMN_MODHASH, redditAccount.getModhash());
        long id = mDatabase.insert(AccountSqlHelper.TABLE_ACCOUNTS, null, values);
        redditAccount.setId(id);
    }

    public void addSubreddit(RedditSubreddit redditSubreddit) {
        ContentValues values = new ContentValues();

        values.put(AccountSqlHelper.COLUMN_DISPLAY_NAME, redditSubreddit.getDisplayName());
        values.put(AccountSqlHelper.COLUMN_OVER_18, redditSubreddit.isNsfw() ? 1 : 0);
        values.put(AccountSqlHelper.COLUMN_PUBLIC, redditSubreddit.isPublicTraffic() ? 1 : 0);
        values.put(AccountSqlHelper.COLUMN_NAME, redditSubreddit.getName());
        values.put(AccountSqlHelper.COLUMN_CREATED, redditSubreddit.getCreated());
        values.put(AccountSqlHelper.COLUMN_SUBMISSION_TYPES, redditSubreddit.getSubmissionType());

        long id = mDatabase.insert(AccountSqlHelper.TABLE_SUBREDDITS, null, values);
        redditSubreddit.setTableId(id);
    }

    public void addSubscriptionToCurrentAccount(RedditSubreddit redditSubreddit) {
        addSubscriptionToAccount(redditSubreddit, AccountManager.getAccount());
    }

    public void addSubscriptionToAccount(RedditSubreddit redditSubreddit, RedditAccount redditAccount) {
        if (redditSubreddit.getTableId() > 0) {
            ContentValues values = new ContentValues();

            values.put(AccountSqlHelper.COLUMN_USER_IS_MOD, redditSubreddit.userIsModerator());
            values.put(AccountSqlHelper.COLUMN_USER_IS_BANNED, redditSubreddit.userIsBanned());
            values.put(AccountSqlHelper.COLUMN_SUBREDDIT_ID, redditSubreddit.getTableId());
            values.put(AccountSqlHelper.COLUMN_USER_ID, redditAccount.getId());

            mDatabase.insert(AccountSqlHelper.TABLE_SUBSCRIPTIONS, null, values);
        } else {
            throw new UnsupportedOperationException("Attempted to insert a redditSubreddit with id 0 into the table");
        }
    }

    public RedditAccount getAccount(long id) {
        if (!mDatabase.isOpen())
            return null;
        Cursor c = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
                AccountSqlHelper.COLUMN_USER_ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        return new RedditAccount(c);
    }

    public ArrayList<RedditSubreddit> getCurrentAccountSubreddits() {
        ArrayList<RedditSubreddit> redditSubreddits = new ArrayList<>();
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
            redditSubreddits.add(new RedditSubreddit(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return redditSubreddits;
    }

    public ArrayList<RedditAccount> getAllAccounts() {
        ArrayList<RedditAccount> redditAccounts = new ArrayList<>();
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_ACCOUNTS,
                AccountSqlHelper.ALL_COLUMNS_ACCOUNT,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RedditAccount redditAccount = new RedditAccount(cursor);
            redditAccounts.add(redditAccount);
            cursor.moveToNext();
        }

        cursor.close();
        return redditAccounts;
    }

    public ArrayList<RedditSubreddit> getAllSubreddits() {
        ArrayList<RedditSubreddit> redditSubreddits = new ArrayList<>();
        Cursor cursor = mDatabase.query(AccountSqlHelper.TABLE_SUBREDDITS,
                AccountSqlHelper.ALL_COLUMNS_SUBREDDITS,
                null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RedditSubreddit s = new RedditSubreddit(cursor);
            redditSubreddits.add(s);
            cursor.moveToNext();
            if (s.getDisplayName() == null) {
                Log.d("AccountDataSource", "Created subreddit's display name is null.");
            }
        }
        cursor.close();

        return redditSubreddits;
    }

    public void deleteAccount(RedditAccount redditAccount) {
        mDatabase.delete(AccountSqlHelper.TABLE_ACCOUNTS, AccountSqlHelper.COLUMN_USER_ID
                + " = " + redditAccount.getId(), null);
    }

}
