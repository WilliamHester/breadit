package me.williamhester.areddit;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

public class Submission extends Thing implements Parcelable {

    public static final int HOT = 0;
    public static final int NEW = 1;
    public static final int RISING = 2;
    public static final int CONTROVERSIAL = 3;
    public static final int TOP = 4;

    public static final int HOUR = 0;
    public static final int DAY = 1;
    public static final int WEEK = 2;
    public static final int MONTH = 3;
    public static final int YEAR = 4;
    public static final int ALL = 5;

    public static final int FRONTPAGE = 0;

    public Submission(JsonObject data) {
        super(data);
    }

    public Submission(Parcel in) {
        this(new JsonParser().parse(in.readBundle().getString("jsonData")).getAsJsonObject());
    }

    public String getUrl() { 
        return mData.get("data").getAsJsonObject().get("url").getAsString();
    }

    public long getUpVotes() { 
        return Long.parseLong(mData.get("data").getAsJsonObject().get("ups").getAsString());
    }

    public long getDownVotes() { 
        return Long.parseLong(mData.get("data").getAsJsonObject().get("downs").getAsString());
    }

    public long getScore() { 
        return Long.parseLong(mData.get("data").getAsJsonObject().get("score").getAsString());
    }

    public String getAuthor() { 
        return mData.get("data").getAsJsonObject().get("author").getAsString();
    }

    public String getTitle() { 
        return mData.get("data").getAsJsonObject().get("title").getAsString();
    }

    public String getDomain() {
        return mData.get("data").getAsJsonObject().get("domain").getAsString();
    }

    public String getBannedBy() {
        return mData.get("data").getAsJsonObject().get("banned_by").getAsString();
    }

    public String getMediaEmbed() {
        return mData.get("data").getAsJsonObject().get("media_embed").getAsString();
    }

    public String getPermalink() {
        return mData.get("data").getAsJsonObject().get("permalink").getAsString();
    }

    public String getSubredditName() {
        return mData.get("data").getAsJsonObject().get("subreddit").getAsString();
    }

    public String getSelfTextHtml() {
        return mData.get("data").getAsJsonObject().get("selftext_html").getAsString();
    }

    public String getSelfText() {
        return mData.get("data").getAsJsonObject().get("selftext").getAsString();
    }

    public String getLikes() {
        return mData.get("data").getAsJsonObject().get("likes").getAsString();
    }

    public String getSecureMedia() {
        return mData.get("data").getAsJsonObject().get("secure_media").getAsString();
    }

    public String getLinkFlairText() {
        return mData.get("data").getAsJsonObject().get("link_flair_text").getAsString();
    }

    public String getSecureMediaEmbed() {
        return mData.get("data").getAsJsonObject().get("secure_media_embed").getAsString();
    }

    public String getMedia() {
        return mData.get("data").getAsJsonObject().get("media").getAsString();
    }

    public String getApprovedBy() {
        return mData.get("data").getAsJsonObject().get("approved_by").getAsString();
    }

    public boolean isNsfw() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("over_18").getAsString());
    }

    public boolean isHidden() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("hidden").getAsString());
    }

    public String getThumbnailUrl() {
        return mData.get("data").getAsJsonObject().get("thumbnail").getAsString();
    }

    public String getSubredditId() {
        return mData.get("data").getAsJsonObject().get("subreddit_id").getAsString();
    }

    public double getEdited() {
        return Double.parseDouble(mData.get("data").getAsJsonObject().get("edited").getAsString());
    }

    public boolean isSaved() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("saved").getAsString());
    }

    public boolean isSelf() {
        return Boolean.parseBoolean(mData.get("data").getAsJsonObject().get("is_self").getAsString());
    }

    public int getNumberOfComments() {
        return Integer.parseInt(mData.get("data").getAsJsonObject().get("num_comments").getAsString());
    }

    public long getCreated() {
        return mData.get("data").getAsJsonObject().get("created").getAsLong();
    }

    public long getCreatedUtc() {
        return mData.get("data").getAsJsonObject().get("created_utc").getAsLong();
    }

    /**
     * This function returns a list containing the submissions on a given
     * subreddit and page.
     *
     * @param subredditName     The subreddit's name; null if frontpage
     * @param sortType          A constant for the type of the sorting of the top
     * @param typeArgs          If the sortType is TOP or CONTROVERSIAL, then this must be specified. Specifies how long
     *                              since the current date back the subreddits should go.
     * @param before            The before= argument
     * @param after             The after= argument
     * @param user              The user. If the user is not connected, it will throw an exception.
     *
     * @return The list containing submissions
     *
     * @throws java.io.IOException      If connection fails
     */
    public static List<Submission> getSubmissions(String subredditName,
                                                  int sortType,
                                                  int typeArgs,
                                                  String before,
                                                  String after,
                                                  User user)
            throws IOException {

        String append;
        if (subredditName == null) {
            append = "";
        } else if (subredditName.equals("")) {
            append = subredditName;
        } else {
            append = "r/" + subredditName;
        }

        ArrayList<Submission> submissions = new ArrayList<Submission>();
        String urlString = "http://www.reddit.com/" + append;

        List<NameValuePair> apiParams = new ArrayList<NameValuePair>();

        switch (sortType) {
            case HOT:
                urlString += ".json?";
                break;
            case NEW:
                urlString += "new/.json?";
                break;
            case RISING:
                urlString += "rising/.json?";
                break;
            case CONTROVERSIAL:
                urlString += "controversial/.json?";
                break;
            case TOP:
                urlString += "top/.json?";
                break;
        }

        if (sortType == CONTROVERSIAL || sortType == TOP) {
            switch (typeArgs) {
                case HOUR:
                    apiParams.add(new BasicNameValuePair("t", "hour"));
                    break;
                case DAY:
                    apiParams.add(new BasicNameValuePair("t", "day"));
                    break;
                case WEEK:
                    apiParams.add(new BasicNameValuePair("t", "week"));
                    break;
                case MONTH:
                    apiParams.add(new BasicNameValuePair("t", "month"));
                    break;
                case YEAR:
                    apiParams.add(new BasicNameValuePair("t", "year"));
                    break;
                case ALL:
                    apiParams.add(new BasicNameValuePair("t", "all"));
                    break;
                default: break;
            }
        }

        if (after != null) {
            urlString += "after=" + after;
        } else if (before != null) {
            urlString += "before=" + before;
        }

        JsonObject object = new JsonParser().parse(Utilities.get(null, urlString, user.getCookie(), user.getModhash()))
                .getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray array = data.get("children").getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonData = (JsonObject)array.get(i);
            submissions.add(new Submission(jsonData));
        }

        return submissions;
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

    public static final Parcelable.Creator<Submission> CREATOR
            = new Parcelable.Creator<Submission>() {
        public Submission createFromParcel(Parcel in) {
            return new Submission(in);
        }

        public Submission[] newArray(int size) {
            return new Submission[size];
        }
    };

}
