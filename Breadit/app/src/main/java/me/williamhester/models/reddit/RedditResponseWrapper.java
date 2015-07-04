package me.williamhester.models.reddit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import me.williamhester.tools.Url;

/**
 * Created by william on 7/30/14.
 */
public class RedditResponseWrapper {

    private String mKind;
    private Object mData;
    private boolean mError;

    public RedditResponseWrapper(JsonObject object, Gson gson) {
        if (object != null) {
            try {
                mKind = object.get("kind").getAsString();
                if (mKind.equals("t1")) {
                    mData = new RedditComment(object.get("data").getAsJsonObject(), gson);
                } else if (mKind.equals("more")) {
                    mData = new RedditMoreComments(object.get("data").getAsJsonObject());
                } else if (mKind.equals("RedditListing")) {
                    mData = new RedditListing(object.get("data").getAsJsonObject(), gson);
                } else if (mKind.equals("t2")) {
                    // Account
                } else if (mKind.equals("t3")) {
                    RedditSubmission s = gson.fromJson(object.get("data"), RedditSubmission.class);
                    if (!s.isSelf()) {
                        s.setLinkDetails(new Url(s.getUrl()));
                    }
                    mData = s;
                } else if (mKind.equals("t4")) {
                    TypeToken<RedditMessage> token = new TypeToken<RedditMessage>() {
                    };
                    mData = gson.fromJson(object.get("data"), token.getType());
                } else if (mKind.equals("t5")) {
                    mData = gson.fromJson(object.get("data"), RedditSubreddit.class);
                } else if (mKind.equals("t6")) {
                    // Award
                } else if (mKind.equals("t8")) {
                    // Promo Campaign
                } else if (mKind.equals("LiveUpdateEvent")) {
                    TypeToken<RedditLive> token = new TypeToken<RedditLive>() {
                    };
                    mData = gson.fromJson(object.get("data"), token.getType());
                }
            } catch (Exception e) {
                mError = true;
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
