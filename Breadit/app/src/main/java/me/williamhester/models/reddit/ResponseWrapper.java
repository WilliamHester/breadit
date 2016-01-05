package me.williamhester.models.reddit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import me.williamhester.tools.Url;

/**
 * Created by william on 7/30/14.
 */
public class ResponseWrapper {

    private String mKind;
    private Object mData;
    private boolean mError;

    public ResponseWrapper(JsonObject object, Gson gson) {
        if (object != null) {
            try {
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
                    TypeToken<Message> token = new TypeToken<Message>() {
                    };
                    mData = gson.fromJson(object.get("data"), token.getType());
                } else if (mKind.equals("t5")) {
                    mData = gson.fromJson(object.get("data"), Subreddit.class);
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
