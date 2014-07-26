package me.williamhester.network;

import android.content.Context;

import com.koushikdutta.ion.Ion;

import me.williamhester.models.Account;
import me.williamhester.models.Votable;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static void vote(Context context, final Votable v, Account account, final int direction) {
        Ion.with(context)
                .load("http://www.reddit.com/api/vote")
                .addHeader("Cookie", "reddit_session=" + account.getCookie())
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("X-Modhash", account.getModhash().replace("\"", ""))
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .setBodyParameter("dir", String.valueOf(direction))
                .setBodyParameter("id", v.getName());
    }
}
