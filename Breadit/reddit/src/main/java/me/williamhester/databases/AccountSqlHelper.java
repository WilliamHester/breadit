package me.williamhester.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by William on 4/11/14.
 */
public class AccountSqlHelper  extends SQLiteOpenHelper {

    public static final String TABLE_ACCOUNTS = "Accounts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_COOKIE = "cookie";
    public static final String COLUMN_MODHASH = "modhash";
    public static final String COLUMN_SUBREDDIT_LIST = "subreddit_list";
    public static final String COLUMN_SAVED_SUBMISSIONS = "saved_submissions";
    public static final String COLUMN_SAVED_COMMENTS = "saved_comments";
    public static final String COLUMN_HISTORY = "history";

    public static final String[] ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_USERNAME,
            COLUMN_COOKIE,
            COLUMN_MODHASH,
            COLUMN_SUBREDDIT_LIST,
            COLUMN_SAVED_SUBMISSIONS,
            COLUMN_SAVED_COMMENTS,
            COLUMN_HISTORY
    };

    public static final String DATABASE_NAME = "Accounts.db";
    public static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ACCOUNTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_USERNAME + " text not null, "
            + COLUMN_COOKIE + " text not null, "
            + COLUMN_MODHASH + " text not null, "
            + COLUMN_SUBREDDIT_LIST + " text not null, "
            + COLUMN_SAVED_SUBMISSIONS + " text not null, "
            + COLUMN_SAVED_COMMENTS + " text not null, "
            + COLUMN_HISTORY + " text not null);";

    public AccountSqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                           int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public AccountSqlHelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(AccountSqlHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
    }

}
