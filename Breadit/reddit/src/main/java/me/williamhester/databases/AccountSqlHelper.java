package me.williamhester.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by William on 4/11/14.
 */
public class AccountSqlHelper  extends SQLiteOpenHelper {

    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_SUBREDDITS = "subreddits";
    public static final String TABLE_SUBSCRIPTIONS = "subscriptions";
    public static final String TABLE_SAVED_SUBMISSIONS = "saved_submissions";
    public static final String TABLE_SAVED_COMMENTS = "saved_comments";

    public static final String COLUMN_ID = "_id"; // shared unique id key

    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_COOKIE = "cookie";
    public static final String COLUMN_MODHASH = "modhash";
    public static final String COLUMN_SUBREDDIT_LIST = "subreddit_list";
    public static final String COLUMN_SAVED_SUBMISSIONS = "saved_submissions";
    public static final String COLUMN_SAVED_COMMENTS = "saved_comments";
    public static final String COLUMN_HISTORY = "history";

    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_OVER_18 = "over18";
    public static final String COLUMN_PUBLIC = "public";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CREATED = "created";
    public static final String COLUMN_SUBMISSION_TYPES = "submission_type";

    public static final String COLUMN_USER_ID = "user_id"; // Shared user id for the relational tables

    public static final String COLUMN_USER_IS_MOD = "user_is_mod";
    public static final String COLUMN_USER_IS_BANNED = "user_is_banned";
    public static final String COLUMN_SUBREDDIT_ID = "subreddit_id"; // the subreddit's id in the table

    public static final String COLUMN_SUBMISSION_NAME = "submission_name";

    public static final String COLUMN_COMMENT_NAME = "comment_name";

    public static final String[] ALL_COLUMNS_ACCOUNT = {
            COLUMN_ID,
            COLUMN_USERNAME,
            COLUMN_COOKIE,
            COLUMN_MODHASH,
            COLUMN_SAVED_SUBMISSIONS,
            COLUMN_SAVED_COMMENTS,
            COLUMN_HISTORY
    };

    public static final String[] ALL_COLUMNS_SUBREDDITS = {
            COLUMN_ID,
            COLUMN_DISPLAY_NAME,
            COLUMN_OVER_18,
            COLUMN_PUBLIC,
            COLUMN_NAME,
            COLUMN_CREATED,
            COLUMN_SUBMISSION_TYPES
    };

    public static final String[] ALL_COLUMNS_SUBSCRIPTIONS = {
            COLUMN_ID,
            COLUMN_USER_IS_MOD,
            COLUMN_USER_IS_BANNED,
            COLUMN_SUBREDDIT_ID,
            COLUMN_USER_ID
    };

    public static final String[] ALL_COLUMNS_SAVED_SUBMISSIONS = {
            COLUMN_ID,
            COLUMN_USER_ID,
            COLUMN_SUBMISSION_NAME
    };

    public static final String[] ALL_COLUMNS_SAVED_COMMENTS = {
            COLUMN_ID,
            COLUMN_USER_ID,
            COLUMN_COMMENT_NAME
    };

    public static final String DATABASE_NAME = "Accounts.db";
    public static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE_ACCOUNTS = "CREATE TABLE "
            + TABLE_ACCOUNTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_USERNAME + " text not null, "
            + COLUMN_COOKIE + " text not null, "
            + COLUMN_MODHASH + " text not null);";

    private static final String DATABASE_CREATE_SUBREDDITS = "CREATE TABLE "
            + TABLE_SUBREDDITS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DISPLAY_NAME + " text not null, "
            + COLUMN_OVER_18 + " integer not null, "
            + COLUMN_PUBLIC + " integer not null, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_CREATED + " integer not null, "
            + COLUMN_SUBMISSION_TYPES + " text not null);";

    public static final String DATABASE_CREATE_SUBSCRIPTIONS = "CREATE TABLE "
            + TABLE_SUBSCRIPTIONS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SUBREDDIT_ID + " integer not null, "
            + COLUMN_USER_ID + " integer not null, "
            + COLUMN_USER_IS_MOD + " integer not null, "
            + COLUMN_USER_IS_BANNED + " integer not null);";

    public static final String DATABASE_CREATE_SAVED_SUBMISSIONS = "CREATE TABLE "
            + TABLE_SAVED_SUBMISSIONS + "("
            + COLUMN_ID + " ingeger primary key autoincrement, "
            + COLUMN_USER_ID + " integer not null, "
            + COLUMN_SUBMISSION_NAME + " text not null);";

    public static final String DATABASE_CREATE_SAVED_COMMENTS = "CREATE TABLE "
            + TABLE_SAVED_SUBMISSIONS + "("
            + COLUMN_ID + " ingeger primary key autoincrement, "
            + COLUMN_USER_ID + " integer not null, "
            + COLUMN_COMMENT_NAME + " text not null);";

    public AccountSqlHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                           int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public AccountSqlHelper(Context context) {
        this(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_ACCOUNTS);
        db.execSQL(DATABASE_CREATE_SUBREDDITS);
        db.execSQL(DATABASE_CREATE_SUBSCRIPTIONS);
        db.execSQL(DATABASE_CREATE_SAVED_SUBMISSIONS);
        db.execSQL(DATABASE_CREATE_SAVED_COMMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(AccountSqlHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
    }

}
