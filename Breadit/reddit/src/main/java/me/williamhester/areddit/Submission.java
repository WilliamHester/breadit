package me.williamhester.areddit;

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
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

public class Submission extends Thing implements Parcelable {

    public static final int UPVOTED = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTED = -1;

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

    private String mAuthor;
    private String mAuthorFlairCss;
    private String mAuthorFlairText;
    private String mDomain;
    private String mLinkFlairCss;
    private String mLinkFlairText;
    private String mPermalink;
    private String mSelftext;
    private String mSelftextHtml;
    private String mSubreddit;
    private String mSubredditId;
    private String mThumbnail;
    private String mTitle;
    private String mUrl;
    private String mDistinguished;
    private boolean mClicked;
    private boolean mEdited;
    private boolean mHidden;
    private boolean mIsSelf;
    private boolean mOver18;
    private boolean mSaved;
    private boolean mStickied;
    private int mCommentsCount;
    private int mScore;
    private int mVoteStatus;
    private long mCreated;
    private long mCreatedUtc;
    private long mUps;
    private long mDowns;

    private Submission(JsonObject data) {
        super(data);
    }

    public Submission(Parcel in) {
        Bundle b = in.readBundle();
        mAuthor = b.getString("author");
        mAuthorFlairCss = b.getString("author_flair_css");
        mAuthorFlairText = b.getString("author_flair_text");
        mDomain = b.getString("domain");
        mLinkFlairCss = b.getString("link_flair_css");
        mLinkFlairText = b.getString("link_flair_text");
        mPermalink = b.getString("permalink");
        mSelftext = b.getString("selftext");
        mSelftextHtml = b.getString("selftext_html");
        mSubreddit = b.getString("subreddit");
        mSubredditId = b.getString("subreddit_id");
        mThumbnail = b.getString("thumbnail");
        mTitle = b.getString("title");
        mUrl = b.getString("url");
        mDistinguished = b.getString("distinguished");
        mClicked = b.getBoolean("clicked");
        mHidden = b.getBoolean("hidden");
        mIsSelf = b.getBoolean("is_self");
        mOver18 = b.getBoolean("over_18");
        mSaved = b.getBoolean("saved");
        mStickied = b.getBoolean("stickied");
        mEdited = b.getBoolean("edited");
        mCommentsCount = b.getInt("comments_count");
        mScore = b.getInt("score");
        mVoteStatus = b.getInt("vote_status");
        mCreated = b.getLong("created");
        mCreatedUtc = b.getLong("created_utc");
        mUps = b.getLong("ups");
        mDowns = b.getLong("downs");
    }

    public static Submission fromJsonString(JsonObject data) {
        Submission submission = new Submission(data);
        if (!data.get("data").getAsJsonObject().get("author").isJsonNull())
            submission.mAuthor = data.get("data").getAsJsonObject().get("author").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_css_class").isJsonNull())
            submission.mAuthorFlairCss = data.get("data").getAsJsonObject().get("author_flair_css_class").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_text").isJsonNull())
            submission.mAuthorFlairText = data.get("data").getAsJsonObject().get("author_flair_text").getAsString();
        submission.mDomain = data.get("data").getAsJsonObject().get("domain").getAsString();
        if (!data.get("data").getAsJsonObject().get("link_flair_css_class").isJsonNull())
            submission.mLinkFlairCss = data.get("data").getAsJsonObject().get("link_flair_css_class").getAsString();
        if (!data.get("data").getAsJsonObject().get("link_flair_text").isJsonNull())
            submission.mLinkFlairText = data.get("data").getAsJsonObject().get("link_flair_text").getAsString();
        submission.mPermalink = data.get("data").getAsJsonObject().get("permalink").getAsString();
        submission.mSelftext = data.get("data").getAsJsonObject().get("selftext").getAsString();
        if (!data.get("data").getAsJsonObject().get("selftext_html").isJsonNull())
            submission.mSelftextHtml = data.get("data").getAsJsonObject().get("selftext_html").getAsString();
        submission.mSubreddit = data.get("data").getAsJsonObject().get("subreddit").getAsString();
        submission.mSubredditId = data.get("data").getAsJsonObject().get("subreddit_id").getAsString();
        submission.mThumbnail = data.get("data").getAsJsonObject().get("thumbnail").getAsString();
        submission.mTitle = data.get("data").getAsJsonObject().get("title").getAsString();
        submission.mUrl = data.get("data").getAsJsonObject().get("url").getAsString();
        if (!data.get("data").getAsJsonObject().get("distinguished").isJsonNull())
            submission.mDistinguished = data.get("data").getAsJsonObject().get("distinguished").getAsString();
        submission.mClicked = data.get("data").getAsJsonObject().get("clicked").getAsBoolean();
        submission.mHidden = data.get("data").getAsJsonObject().get("hidden").getAsBoolean();
        submission.mIsSelf = data.get("data").getAsJsonObject().get("is_self").getAsBoolean();
        submission.mOver18 = data.get("data").getAsJsonObject().get("over_18").getAsBoolean();
        submission.mSaved = data.get("data").getAsJsonObject().get("saved").getAsBoolean();
        submission.mStickied = data.get("data").getAsJsonObject().get("stickied").getAsBoolean();
        submission.mCommentsCount = data.get("data").getAsJsonObject().get("num_comments").getAsInt();
        submission.mScore = data.get("data").getAsJsonObject().get("score").getAsInt();
        submission.mEdited = data.get("data").getAsJsonObject().get("edited").getAsBoolean();
        submission.mCreated = data.get("data").getAsJsonObject().get("created").getAsLong();
        submission.mCreatedUtc = data.get("data").getAsJsonObject().get("created_utc").getAsLong();
        submission.mUps = data.get("data").getAsJsonObject().get("ups").getAsLong();
        submission.mDowns = data.get("data").getAsJsonObject().get("downs").getAsLong();
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
        return mUrl;
    }

    public long getUpVotes() { 
        return mUps;
    }

    public long getDownVotes() { 
        return mDowns;
    }

    public long getScore() { 
        return mScore;
    }

    public String getAuthor() { 
        return mAuthor;
    }

    public String getTitle() { 
        return mTitle;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getPermalink() {
        return mPermalink;
    }

    public String getSubredditName() {
        return mSubreddit;
    }

    public String getSelfTextHtml() {
        return mSelftextHtml;
    }

    public String getSelfText() {
        return mSelftext;
    }

    public boolean getLikes() {
        return false;
//        return data.get("data").getAsJsonObject().get("likes").getAsBoolean();
    }

    public String getLinkFlairText() {
        return mLinkFlairText;
    }

    public boolean isNsfw() {
        return mOver18;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public String getThumbnailUrl() {
        return mThumbnail;
    }

    public String getSubredditId() {
        return mSubredditId;
    }

    public boolean getEdited() {
        return mEdited;
    }

    public boolean isSaved() {
        return mSaved;
    }

    public boolean isSelf() {
        return mIsSelf;
    }

    public int getNumberOfComments() {
        return mCommentsCount;
    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
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
        b.putString("author", mAuthor);
        b.putString("author_flair_css", mAuthorFlairCss);
        b.putString("author_flair_text", mAuthorFlairText);
        b.putString("domain", mDomain);
        b.putString("link_flair_css", mLinkFlairCss);
        b.putString("link_flair_text", mLinkFlairText);
        b.putString("permalink", mPermalink);
        b.putString("selftext", mSelftext);
        b.putString("selftext_html", mSelftextHtml);
        b.putString("subreddit", mSubreddit);
        b.putString("subreddit_id", mSubredditId);
        b.putString("thumbnail", mThumbnail);
        b.putString("title", mTitle);
        b.putString("url", mUrl);
        b.putString("distinguished", mDistinguished);
        b.putBoolean("clicked", mClicked);
        b.putBoolean("edited", mEdited);
        b.putBoolean("hidden", mHidden);
        b.putBoolean("is_self", mIsSelf);
        b.putBoolean("over_18", mOver18);
        b.putBoolean("saved", mSaved);
        b.putBoolean("stickied", mStickied);
        b.putInt("comments_count", mCommentsCount);
        b.putInt("score", mScore);
        b.putInt("vote_status", mVoteStatus);
        b.putLong("created", mCreated);
        b.putLong("created_utc", mCreatedUtc);
        b.putLong("ups", mUps);
        b.putLong("downs", mDowns);
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
