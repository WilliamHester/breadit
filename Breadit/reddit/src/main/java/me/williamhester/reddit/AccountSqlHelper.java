package me.williamhester.reddit;

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
    public static final String COLUMN_ACCOUNT_USERNAME = "name";
    public static final String COLUMN_ACCOUNT_COOKIE = "cookie";
    public static final String COLUMN_ACCOUNT_MODHASH = "modhash";
    public static final String COLUMN_SUBREDDIT_LIST = "subreddit_list";
    public static final String COLUMN_SAVED_SUBMISSIONS = "saved_submissions";

    public static final String[] ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_ACCOUNT_USERNAME,
            COLUMN_ACCOUNT_COOKIE,
            COLUMN_ACCOUNT_MODHASH,
            COLUMN_SUBREDDIT_LIST,
            COLUMN_SAVED_SUBMISSIONS
    };

    public static final String DATABASE_NAME = "Accounts.db";
    public static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_ACCOUNTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ACCOUNT_USERNAME + " text not null, "
            + COLUMN_ACCOUNT_COOKIE + " text not null, "
            + COLUMN_ACCOUNT_MODHASH + " text not null, "
            + COLUMN_SUBREDDIT_LIST + " text not null, "
            + COLUMN_SAVED_SUBMISSIONS + " text not null);";

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
