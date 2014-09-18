package me.williamhester.models;

import android.text.Spannable;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Submission extends Thing implements Votable, Serializable {

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

    public static final String LIVE_UPDATE = "liveupdate";

    private static final long serialVersionUID = -112181006397201414L;

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
    private Media media;
    private int mVoteStatus;
    private boolean mIsBeingEdited;
    private boolean mSelftextIsOpen;
    private Object mImgurData;
    private Spannable mSpannableBody;

    public Submission() { }

    private Submission(JsonObject data) {
        super(data);
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

    public int getScore() {
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

    @Override
    public void setSpannableBody(Spannable body) {
        mSpannableBody = body;
    }

    public Spannable getSpannableBody() {
        return mSpannableBody;
    }

    public void setSelftextOpen(boolean open) {
        mSelftextIsOpen = open;
    }

    public boolean isSelftextOpen() {
        return mSelftextIsOpen;
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

    public void setImgurData(Object data) {
        mImgurData = data;
    }

    public Media getMedia() {
        return media;
    }

    public Object getImgurData() {
        return mImgurData;
    }

    public static class Media implements Serializable {
        private static final long serialVersionUID = -3883427725988406001L;

        @SerializedName("type")
        private String mType;
        @SerializedName("event_id")
        private String mEventId;

        public String getType() {
            return mType;
        }

        public String getEventId() {
            return mEventId;
        }
    }

}
