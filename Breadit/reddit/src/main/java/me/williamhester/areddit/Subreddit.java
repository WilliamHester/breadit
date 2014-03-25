package me.williamhester.areddit;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import me.williamhester.areddit.utils.Utilities;

/**
 * Created with IntelliJ IDEA.
 * User: William
 * Date: 1/3/14
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Subreddit extends Thing {

    public Subreddit(JsonObject data) {
        super(data);
    }

//    public HTML getSubmitTextHtml() {
//        return (HTML) mData.get("data").getAsJsonObject().get("submit_text_html").getAsString();
//    }

    public boolean userIsBanned() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("user_is_banned").getAsString());
    }

    public String getDisplayName() {
        return  mData.get("data").getAsJsonObject().get("display_name").getAsString();
    }

    public String getHeaderImgUrl() {
        return  mData.get("data").getAsJsonObject().get("header_img").getAsString();
    }

//    public HTML getDescripionHtml() {
//        return new HTML()mData.get("data").getAsJsonObject().get("description_html").getAsString();
//    }

    public String getTitle() {
        return  mData.get("data").getAsJsonObject().get("title").getAsString();
    }

    public boolean isNsfw() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("over18").getAsString());
    }

    public boolean userIsModerator() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("user_is_moderator").getAsString());
    }

    public String getHeaderTitle() {
        return  mData.get("data").getAsJsonObject().get("header_title").getAsString();
    }

    public String getDescription() {
        return  mData.get("data").getAsJsonObject().get("description").getAsString();
    }

    public String getSubmitLinkLabel() {
        return  mData.get("data").getAsJsonObject().get("submit_link_label").getAsString();
    }

    public boolean isPublicTraffic() {
        return Boolean.parseBoolean( mData.get("data").getAsJsonObject().get("public_traffic").getAsString());
    }

    //Todo add a header_size getter. It is formatted like [160, 64]

    public long getSubscriberCount() {
        return Long.parseLong( mData.get("data").getAsJsonObject().get("subscribers").getAsString());
    }

    public String getSubmitTextLabel() {
        return  mData.get("data").getAsJsonObject().get("submit_text_label").getAsString();
    }

    public String getUrl() {
        return  mData.get("data").getAsJsonObject().get("url").getAsString();
    }

    //Todo probably should not be of type String, but I'm not sure if it's a float or what
    public String getCreated() {
        return  mData.get("data").getAsJsonObject().get("created").getAsString();
    }

    //Todo probably should not be of type String, but I'm not sure if it's a float or what
    public String getCreatedUtc() {
        return  mData.get("data").getAsJsonObject().get("created_utc").getAsString();
    }

    public boolean userIsContributor() {
        return Boolean.parseBoolean( mData.get("data").getAsJsonObject().get("user_is_contributor").getAsString());
    }

    public String getPublicDescription() {
        return  mData.get("data").getAsJsonObject().get("public_description").getAsString();
    }

    public long getCommentScoreHideMins() {
        return Long.parseLong( mData.get("data").getAsJsonObject().get("comment_score_hide_mins").getAsString());
    }

    public String getSubredditType() {
        return  mData.get("data").getAsJsonObject().get("subreddit_type").getAsString();
    }

    public String getSubmissionType() {
        return  mData.get("data").getAsJsonObject().get("submission_type").getAsString();
    }

    public boolean userIsSubscriber() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("user_is_subscriber").getAsString());
    }

}
