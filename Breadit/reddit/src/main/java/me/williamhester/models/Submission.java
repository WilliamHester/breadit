package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import me.williamhester.tools.Url;

@SuppressWarnings("unused")
public class Submission implements Votable, Parcelable {

    private static final int NO_IMGUR_DATA = 0;
    private static final int IMGUR_ALBUM = 1;
    private static final int IMGUR_IMAGE = 2;

    public static final String LIVE_UPDATE = "liveupdate";

    private String author;
    private String author_flair_css_class;
    private String author_flair_text;
    private String domain;
    private String id;
    @SerializedName("name")
    private String mName;
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
    private String edited;
    private boolean clicked;
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
    private boolean mSelftextIsOpen;
    private boolean mIsNsfwShowing = false;
    private Url mLinkDetails;
    private Object mImgurData;
    private int mImgurDataType = NO_IMGUR_DATA;

    public Submission() { }

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

    @Override
    public String getRawMarkdown() {
        return selftext;
    }

    public void setRawMarkdown(String markdown) {
        selftext = markdown;
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

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getId() {
        return id;
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

    public void setIsNsfw(boolean isNsfw) {
        over_18 = isNsfw;
    }

    public boolean isStickied() {
        return stickied;
    }

    public void setIsStickied(boolean stickied) {
        this.stickied = stickied;
    }

    public void setShowNsfwContent() {
        mIsNsfwShowing = true;
    }

    public boolean isShowingNsfw() {
        return mIsNsfwShowing;
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

    public void setImgurData(Object data) {
        mImgurData = data;
    }

    public Media getMedia() {
        return media;
    }

    public Url getLinkDetails() {
        return mLinkDetails;
    }

    public void setLinkDetails(Url link) {
        mLinkDetails = link;
    }

    public Object getImgurData() {
        return mImgurData;
    }

    public void update(Submission submission) {
        num_comments = submission.num_comments;
        created = submission.created;
        created_utc = submission.created_utc;
        score = submission.score;
    }

    public static class Media implements Parcelable {
        private static final long serialVersionUID = -3883427725988406001L;

        @SerializedName("type")
        private String mType;
        @SerializedName("event_id")
        private String mEventId;

        public Media() {

        }

        public String getType() {
            return mType;
        }

        public String getEventId() {
            return mEventId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mType);
            dest.writeString(this.mEventId);
        }

        private Media(Parcel in) {
            this.mType = in.readString();
            this.mEventId = in.readString();
        }

        public static final Creator<Media> CREATOR = new Creator<Media>() {
            public Media createFromParcel(Parcel source) {
                return new Media(source);
            }

            public Media[] newArray(int size) {
                return new Media[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.author_flair_css_class);
        dest.writeString(this.author_flair_text);
        dest.writeString(this.domain);
        dest.writeString(this.link_flair_css_class);
        dest.writeString(this.link_flair_text);
        dest.writeString(this.permalink);
        dest.writeString(this.selftext);
        dest.writeString(this.selftext_html);
        dest.writeString(this.subreddit);
        dest.writeString(this.subreddit_id);
        dest.writeString(this.thumbnail);
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeString(this.distinguished);
        dest.writeByte(clicked ? (byte) 1 : (byte) 0);
        dest.writeString(this.edited);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeByte(is_self ? (byte) 1 : (byte) 0);
        dest.writeByte(over_18 ? (byte) 1 : (byte) 0);
        dest.writeByte(saved ? (byte) 1 : (byte) 0);
        dest.writeByte(stickied ? (byte) 1 : (byte) 0);
        dest.writeInt(this.num_comments);
        dest.writeInt(this.score);
        dest.writeLong(this.created);
        dest.writeLong(this.created_utc);
        dest.writeLong(this.ups);
        dest.writeLong(this.downs);
        dest.writeValue(this.likes);
        dest.writeParcelable(this.media, 0);
        dest.writeInt(this.mVoteStatus);
        dest.writeByte(mSelftextIsOpen ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsNsfwShowing ? (byte) 1 : (byte) 0);
        dest.writeString(this.id);
        dest.writeString(this.mName);
        dest.writeParcelable(mLinkDetails, 0);
        if (mImgurData != null) {
            if (mImgurData instanceof ImgurAlbum) {
                dest.writeInt(IMGUR_ALBUM);
                dest.writeParcelable((ImgurAlbum) this.mImgurData, flags);
            } else if (mImgurData instanceof ImgurImage) {
                dest.writeInt(IMGUR_IMAGE);
                dest.writeParcelable((ImgurImage) this.mImgurData, flags);
            }
        } else {
            dest.writeInt(NO_IMGUR_DATA);
        }
    }

    private Submission(Parcel in) {
        this.author = in.readString();
        this.author_flair_css_class = in.readString();
        this.author_flair_text = in.readString();
        this.domain = in.readString();
        this.link_flair_css_class = in.readString();
        this.link_flair_text = in.readString();
        this.permalink = in.readString();
        this.selftext = in.readString();
        this.selftext_html = in.readString();
        this.subreddit = in.readString();
        this.subreddit_id = in.readString();
        this.thumbnail = in.readString();
        this.title = in.readString();
        this.url = in.readString();
        this.distinguished = in.readString();
        this.clicked = in.readByte() != 0;
        this.edited = in.readString();
        this.hidden = in.readByte() != 0;
        this.is_self = in.readByte() != 0;
        this.over_18 = in.readByte() != 0;
        this.saved = in.readByte() != 0;
        this.stickied = in.readByte() != 0;
        this.num_comments = in.readInt();
        this.score = in.readInt();
        this.created = in.readLong();
        this.created_utc = in.readLong();
        this.ups = in.readLong();
        this.downs = in.readLong();
        this.likes = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.media = in.readParcelable(Media.class.getClassLoader());
        this.mVoteStatus = in.readInt();
        this.mSelftextIsOpen = in.readByte() != 0;
        this.mIsNsfwShowing = in.readByte() != 0;
        this.id = in.readString();
        this.mName = in.readString();
        this.mLinkDetails = in.readParcelable(Url.class.getClassLoader());
        int flag = in.readInt();
        if (flag != NO_IMGUR_DATA) {
            if (flag == IMGUR_ALBUM) {
                mImgurData = in.readParcelable(ImgurAlbum.class.getClassLoader());
            } else if (flag == IMGUR_IMAGE) {
                mImgurData = in.readParcelable(ImgurImage.class.getClassLoader());
            }
        }
    }

    public static final Creator<Submission> CREATOR = new Creator<Submission>() {
        public Submission createFromParcel(Parcel source) {
            return new Submission(source);
        }

        public Submission[] newArray(int size) {
            return new Submission[size];
        }
    };
}
