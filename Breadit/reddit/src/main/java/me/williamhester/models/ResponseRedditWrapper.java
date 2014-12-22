package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;

import me.williamhester.tools.Url;

/**
 * Created by william on 7/30/14.
 */
public class ResponseRedditWrapper {

    private String mKind;
    private Object mData;

    public ResponseRedditWrapper(JsonObject object, Gson gson) {
        if (object != null) {
            mKind = object.get("kind").getAsString();
            if (mKind.equals("t1")) {
                mData = new Comment(object.get("data").getAsJsonObject(), gson);
            } else if (mKind.equals("more")) {
                mData = new MoreComments(object.get("data").getAsJsonObject());
            } else if (mKind.equals("Listing")) {
                mData = new Listing(object.get("data").getAsJsonObject(), gson);
            } else if (mKind.equals("t2")) {
                // Account
            } else if (mKind.equals("t3")) {
                Submission s = gson.fromJson(object.get("data"), Submission.class);
                if (!s.isSelf()) {
                    s.setLinkDetails(new Url(s.getUrl()));
                }
                mData = s;
            } else if (mKind.equals("t4")) {
                mData = new Message(object.get("data").getAsJsonObject());
            } else if (mKind.equals("t5")) {
                mData = gson.fromJson(object.get("data"), Subreddit.class);
            } else if (mKind.equals("t6")) {
                // Award
            } else if (mKind.equals("t8")) {
                // Promo Campaign
            } else if (mKind.equals("LiveUpdateEvent")) {
                TypeToken<RedditLive> token = new TypeToken<RedditLive>() { };
                mData = gson.fromJson(object.get("data"), token.getType());
            }
        }
    }

    public String getKind() {
        return mKind;
    }

    public Object getData() {
        return mData;
    }
}
