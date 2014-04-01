package me.williamhester.reddit;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.User;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/1/14.
 */
public class VoteAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public static final int UPVOTE = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTE = -1;

    private String mFullname;
    private User mUser;
    private int mDir;

    public VoteAsyncTask(String fullname, User user, int dir) {
        mDir = dir;
        mUser = user;
        mFullname = fullname;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        if (mUser == null)
            return false; // Can't vote if the user is not logged in.
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
        apiParams.add(new BasicNameValuePair("dir", mDir + ""));
        apiParams.add(new BasicNameValuePair("id", mFullname));
        Log.i("VoteAsyncTask", Utilities.post(apiParams, "http://www.reddit.com/api/vote", mUser.getCookie(),
                mUser.getModhash()));
        return true; // If we get here, then we successfully voted.
    }
}
