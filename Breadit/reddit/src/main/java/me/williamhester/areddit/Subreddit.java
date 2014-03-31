package me.williamhester.areddit;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Subreddit extends Thing implements Parcelable {

    public Subreddit(JsonObject data) {
        super(data);
    }

    public Subreddit(Parcel in) {
        this(new JsonParser().parse(in.readBundle().getString("jsonData")).getAsJsonObject());
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
        return Long.parseLong(mData.get("data").getAsJsonObject().get("subscribers").getAsString());
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
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("user_is_contributor").getAsString());
    }

    public String getPublicDescription() {
        return  mData.get("data").getAsJsonObject().get("public_description").getAsString();
    }

    public long getCommentScoreHideMins() {
        return Long.parseLong(mData.get("data").getAsJsonObject().get("comment_score_hide_mins").getAsString());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle b = new Bundle();
        b.putString("jsonData", mData.toString());
        parcel.writeBundle(b);
    }

    public static final Parcelable.Creator<Subreddit> CREATOR
            = new Parcelable.Creator<Subreddit>() {
        public Subreddit createFromParcel(Parcel in) {
            return new Subreddit(in);
        }

        public Subreddit[] newArray(int size) {
            return new Subreddit[size];
        }
    };

}
