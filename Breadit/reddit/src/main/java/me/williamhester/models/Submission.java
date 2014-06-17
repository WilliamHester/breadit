package me.williamhester.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.utils.Utilities;

public class Submission extends Thing implements Parcelable, Votable {

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

    private String author;
    private String author_flair_css_class;
    private String author_flair_text;
    private String domain;
    private String link_flair_css_class;
    private String link_flair_text;
    private String permalink;
    private String selftext;
    private String selftext_html;
    private String subreddit;
    private String subreddit_id;
    private String thumbnail;
    private String title;
    private String url;
    private String distinguished;
    private boolean clicked;
    private String edited;
    private boolean hidden;
    private boolean is_self;
    private boolean over_18;
    private boolean saved;
    private boolean stickied;
    private int num_comments;
    private int score;
    private long created;
    private long created_utc;
    private long ups;
    private long downs;
    private Boolean likes;
    private int mVoteStatus;
    private boolean mIsBeingEdited = false;

    public Submission() { }

    private Submission(JsonObject data) {
        super(data);
    }

    public Submission(Parcel in) {
        Bundle b = in.readBundle();
        name = b.getString("name");
        author = b.getString("author");
        author_flair_css_class = b.getString("author_flair_css");
        author_flair_text = b.getString("author_flair_text");
        domain = b.getString("domain");
        link_flair_css_class = b.getString("link_flair_css");
        link_flair_text = b.getString("link_flair_text");
        permalink = b.getString("permalink");
        selftext = b.getString("selftext");
        selftext_html = b.getString("selftext_html");
        subreddit = b.getString("subreddit");
        subreddit_id = b.getString("subreddit_id");
        thumbnail = b.getString("thumbnail");
        title = b.getString("title");
        url = b.getString("url");
        distinguished = b.getString("distinguished");
        clicked = b.getBoolean("clicked");
        hidden = b.getBoolean("hidden");
        is_self = b.getBoolean("is_self");
        over_18 = b.getBoolean("over_18");
        saved = b.getBoolean("saved");
        stickied = b.getBoolean("stickied");
//        edited = b.getBoolean("edited");
        num_comments = b.getInt("comments_count");
        score = b.getInt("score");
        mVoteStatus = b.getInt("vote_status");
        created = b.getLong("created");
        created_utc = b.getLong("created_utc");
        ups = b.getLong("ups");
        downs = b.getLong("downs");
    }

    @Deprecated
    public static Submission fromJsonString(JsonObject data) {
        Submission submission = new Submission(data);
        if (!data.get("data").getAsJsonObject().get("author").isJsonNull())
            submission.author = data.get("data").getAsJsonObject().get("author").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_css_class").isJsonNull())
            submission.author_flair_css_class = data.get("data").getAsJsonObject().get("author_flair_css_class").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_text").isJsonNull())
            submission.author_flair_text = data.get("data").getAsJsonObject().get("author_flair_text").getAsString();
        submission.domain = data.get("data").getAsJsonObject().get("domain").getAsString();
        if (!data.get("data").getAsJsonObject().get("link_flair_css_class").isJsonNull())
            submission.link_flair_css_class = data.get("data").getAsJsonObject().get("link_flair_css_class").getAsString();
        if (!data.get("data").getAsJsonObject().get("link_flair_text").isJsonNull())
            submission.link_flair_text = data.get("data").getAsJsonObject().get("link_flair_text").getAsString();
        submission.permalink = data.get("data").getAsJsonObject().get("permalink").getAsString();
        submission.selftext = data.get("data").getAsJsonObject().get("selftext").getAsString();
        if (!data.get("data").getAsJsonObject().get("selftext_html").isJsonNull())
            submission.selftext_html = data.get("data").getAsJsonObject().get("selftext_html").getAsString();
        submission.subreddit = data.get("data").getAsJsonObject().get("subreddit").getAsString();
        submission.subreddit_id = data.get("data").getAsJsonObject().get("subreddit_id").getAsString();
        submission.thumbnail = data.get("data").getAsJsonObject().get("thumbnail").getAsString();
        submission.title = data.get("data").getAsJsonObject().get("title").getAsString();
        submission.url = data.get("data").getAsJsonObject().get("url").getAsString();
        if (!data.get("data").getAsJsonObject().get("distinguished").isJsonNull())
            submission.distinguished = data.get("data").getAsJsonObject().get("distinguished").getAsString();
        submission.clicked = data.get("data").getAsJsonObject().get("clicked").getAsBoolean();
        submission.hidden = data.get("data").getAsJsonObject().get("hidden").getAsBoolean();
        submission.is_self = data.get("data").getAsJsonObject().get("is_self").getAsBoolean();
        submission.over_18 = data.get("data").getAsJsonObject().get("over_18").getAsBoolean();
        submission.saved = data.get("data").getAsJsonObject().get("saved").getAsBoolean();
        submission.stickied = data.get("data").getAsJsonObject().get("stickied").getAsBoolean();
        submission.num_comments = data.get("data").getAsJsonObject().get("num_comments").getAsInt();
        submission.score = data.get("data").getAsJsonObject().get("score").getAsInt();
//        submission.edited = data.get("data").getAsJsonObject().get("edited").getAsBoolean();
        submission.created = data.get("data").getAsJsonObject().get("created").getAsLong();
        submission.created_utc = data.get("data").getAsJsonObject().get("created_utc").getAsLong();
        submission.ups = data.get("data").getAsJsonObject().get("ups").getAsLong();
        submission.downs = data.get("data").getAsJsonObject().get("downs").getAsLong();
        JsonElement je = data.get("data").getAsJsonObject().get("likes");
        if (je.isJsonNull()) {
            submission.mVoteStatus = NEUTRAL;
        } else if (je.getAsBoolean()) {
            submission.mVoteStatus = UPVOTED;
        } else {
            submission.mVoteStatus = DOWNVOTED;
        }
        return submission;
    }

    public String getUrl() { 
        return url;
    }

    public long getUpVotes() { 
        return ups;
    }

    public long getDownVotes() { 
        return downs;
    }

    public long getScore() { 
        return score;
    }

    public String getAuthor() { 
        return author;
    }

    public String getTitle() { 
        return title;
    }

    public String getDomain() {
        return domain;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getSubredditName() {
        return subreddit;
    }

    public String getBodyHtml() {
        return selftext_html;
    }

    public String getBody() {
        return selftext;
    }

    public void setBody(String body) {
        selftext = body;
    }

    public void setBodyHtml(String body) {
        selftext_html = body;
    }

    public int getVoteStatus() {
        if (likes == null) {
            return NEUTRAL;
        } else if (likes) {
            return UPVOTED;
        } else {
            return DOWNVOTED;
        }
    }

    public void setVoteStatus(int status) {
        score -= mVoteStatus - status;
        switch (status) {
            case NEUTRAL:
                likes = null;
                break;
            case UPVOTED:
                likes = true;
                break;
            case DOWNVOTED:
                likes = false;
                break;
        }
    }

    public String getLinkFlairText() {
        return link_flair_text;
    }

    public boolean isNsfw() {
        return over_18;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getThumbnailUrl() {
        return thumbnail;
    }

    public String getSubredditId() {
        return subreddit_id;
    }

    public String getEdited() {
        return edited;
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean isSelf() {
        return is_self;
    }

    public int getNumberOfComments() {
        return num_comments;
    }

    public long getCreated() {
        return created;
    }

    public long getCreatedUtc() {
        return created_utc;
    }

    public boolean isMeta() {
        return url.contains("reddit.com") && url.contains("comments");
    }

    public void setBeingEdited(boolean b) {
        mIsBeingEdited = b;
    }

    public boolean isBeingEdited() {
        return mIsBeingEdited;
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
     * @param account              The account. If the account is not connected, it will throw an exception.
     *
     * @return The list containing submissions
     *
     * @throws java.io.IOException      If connection fails
     */
    public static List<Submission> getSubmissions(String subredditName, int sortType,
                                                  int typeArgs, String before, String after,
                                                  Account account) throws IOException {
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

        JsonObject object = new JsonParser().parse(Utilities.get(null, urlString, account.getCookie(), account.getModhash()))
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
        b.putString("name", name);
        b.putString("author", author);
        b.putString("author_flair_css", author_flair_css_class);
        b.putString("author_flair_text", author_flair_text);
        b.putString("domain", domain);
        b.putString("link_flair_css", link_flair_css_class);
        b.putString("link_flair_text", link_flair_text);
        b.putString("permalink", permalink);
        b.putString("selftext", selftext);
        b.putString("selftext_html", selftext_html);
        b.putString("subreddit", subreddit);
        b.putString("subreddit_id", subreddit_id);
        b.putString("thumbnail", thumbnail);
        b.putString("title", title);
        b.putString("url", url);
        b.putString("distinguished", distinguished);
        b.putBoolean("clicked", clicked);
//        b.putBoolean("edited", edited);
        b.putBoolean("hidden", hidden);
        b.putBoolean("is_self", is_self);
        b.putBoolean("over_18", over_18);
        b.putBoolean("saved", saved);
        b.putBoolean("stickied", stickied);
        b.putInt("comments_count", num_comments);
        b.putInt("score", score);
        b.putInt("vote_status", mVoteStatus);
        b.putLong("created", created);
        b.putLong("created_utc", created_utc);
        b.putLong("ups", ups);
        b.putLong("downs", downs);
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
