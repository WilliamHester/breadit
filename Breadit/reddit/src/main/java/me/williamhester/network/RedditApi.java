package me.williamhester.network;

import android.content.Context;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.future.ResponseFuture;

import java.util.logging.Logger;

import me.williamhester.models.Account;
import me.williamhester.models.Votable;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static void vote(Context context, Votable v, Account account) {
        Log.d("RedditApi", "" + v.getVoteStatus());
        Ion.with(context)
                .load("http://www.reddit.com/api/vote")
                .addHeader("Cookie", "reddit_session=" + account.getCookie())
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("X-Modhash", account.getModhash().replace("\"", ""))
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setBodyParameter("dir", String.valueOf(v.getVoteStatus()))
                .setBodyParameter("id", v.getName())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        Log.d("RedditApi", result);
                    }
                });
    }
}
