package me.williamhester.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.williamhester.models.Account;
import me.williamhester.models.RedditLive;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static void vote(Context context, Votable v, Account account) {
        Ion.with(context)
                .load("http://www.reddit.com/api/vote")
                .addHeader("Cookie", "reddit_session=" + account.getCookie())
                .addHeader("X-Modhash", account.getModhash().replace("\"", ""))
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

    public static void getRedditLiveData(Context context, Submission submission,
                                         FutureCallback<ResponseRedditWrapper<RedditLive>> callback) {
        Ion.with(context)
                .load(submission.getUrl() + "/about.json")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .as(new TypeToken<ResponseRedditWrapper<RedditLive>>() {})
                .setCallback(callback);
    }
}
