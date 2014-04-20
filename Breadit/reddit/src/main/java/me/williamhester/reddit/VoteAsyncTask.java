package me.williamhester.reddit;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/1/14.
 */
public class VoteAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public static final int UPVOTE = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTE = -1;

    private String mFullname;
    private Account mAccount;
    private int mDir;

    public VoteAsyncTask(String fullname, Account account, int dir) {
        mDir = dir;
        mAccount = account;
        mFullname = fullname;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (mAccount == null) {
            Log.i("VoteAsyncTask", "Account is null");
            return false; // Can't vote if the user is not logged in.
        }
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
        apiParams.add(new BasicNameValuePair("dir", mDir + ""));
        apiParams.add(new BasicNameValuePair("id", mFullname));
        Utilities.post(apiParams, "http://www.reddit.com/api/vote", mAccount);
        return true; // If we get here, then we successfully voted.
    }
}
