package me.williamhester.models.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import com.google.gson.annotations.SerializedName;

import me.williamhester.models.bulletin.Message;

/**
 * Created by William on 4/12/14.
 */
public class RedditMessage implements Message, RedditVotable, Parcelable {

    public static final String ALL = "inbox";
    public static final String UNREAD = "unread";
    public static final String MESSAGES = "messages";
    public static final String COMMENT_REPLIES = "comments";
    public static final String POST_REPLIES = "selfreply";
    public static final String SENT = "sent";
    public static final String MOD_MAIL = "moderator";

    @SerializedName("created")
    private long mCreated;
    @SerializedName("created_utc")
    private long mCreatedUtc;
    @SerializedName("author")
    private String mAuthor;
    @SerializedName("body")
    private String mBody;
    @SerializedName("body_html")
    private String mBodyHtml;
    @SerializedName("context")
    private String mContext;
    @SerializedName("dest")
    private String mDestination;
    @SerializedName("id")
    private String mId;
    @SerializedName("link_title")
    private String mLinkTitle;
    @SerializedName("name")
    private String mName;
    @SerializedName("parent_id")
    private String mParentId;
    @SerializedName("replies")
    private String mReplies;
    @SerializedName("subject")
    private String mSubject;
    @SerializedName("subreddit")
    private String mSubreddit;
    @SerializedName("likes")
    private Boolean mVoteStatus;
    @SerializedName("new")
    private boolean mUnread;
    @SerializedName("was_comment")
    private boolean mWasComment;
    @SerializedName("first_message")
    private String mFirstMessage;
    private Spannable mSpannable;

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    @Override
    public void setMarkdownBody(String string) {
        mBody = string;
    }

    @Override
    public String getBodyMarkdown() {
        return mBody;
    }

    @Override
    public String getHtmlBody() {
        return mBodyHtml;
    }

    public String getContext() {
        return mContext;
    }

    @Override
    public String getRecipient() {
        return mDestination;
    }

    public String getLinkTitle() {
        return mLinkTitle;
    }

    public String getParentId() {
        return mParentId;
    }

    public String getReplies() {
        return mReplies;
    }

    @Override
    public String getSubject() {
        return mSubject;
    }

    @Override
    public String getSentDate() {
        return "fixme: sentDate";
    }

    @Override
    public String getMarkdownBody() {
        return mBody;
    }

    @Override
    public Spannable getSpannableBody() {
        return mSpannable;
    }

    @Override
    public String getBulletin() {
        return mSubreddit;
    }

    @Override
    public boolean isUnread() {
        return mUnread;
    }

    @Override
    public void setUnread(boolean unread) {
        mUnread = unread;
    }

    @Override
    public boolean isComment() {
        return mWasComment;
    }

    @Override
    public String getId() {
        return mName;
    }

    @Override
    public String getCommentId() {
        return mName;
    }

    @Override
    public String getSubmissionId() {
        return "fixme: submissionId";
    }

    @Override
    public String getSender() {
        return mAuthor;
    }

    @Override
    public int getVoteValue() {
        return mVoteStatus == null ? NEUTRAL : (mVoteStatus ? UPVOTED : DOWNVOTED);
    }

    @Override
    public void setVoteValue(int status) {
        if (status == RedditVotable.NEUTRAL) {
            mVoteStatus = null;
        } else {
            mVoteStatus = status == RedditVotable.UPVOTED;
        }
    }

    @Override
    public String getAuthor() {
        return mAuthor;
    }

    @Override
    public void setSpannableBody(Spannable body) {
        mSpannable = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mCreated);
        dest.writeLong(this.mCreatedUtc);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mBody);
        dest.writeString(this.mBodyHtml);
        dest.writeString(this.mContext);
        dest.writeString(this.mDestination);
        dest.writeString(this.mLinkTitle);
        dest.writeString(this.mName);
        dest.writeString(this.mParentId);
        dest.writeString(this.mReplies);
        dest.writeString(this.mSubject);
        dest.writeString(this.mSubreddit);
        dest.writeInt(this.mVoteStatus == null ? NEUTRAL : mVoteStatus ? UPVOTED : DOWNVOTED);
        dest.writeByte(mUnread ? (byte) 1 : (byte) 0);
        dest.writeByte(mWasComment ? (byte) 1 : (byte) 0);
    }

    private RedditMessage(Parcel in) {
        this.mCreated = in.readLong();
        this.mCreatedUtc = in.readLong();
        this.mAuthor = in.readString();
        this.mBody = in.readString();
        this.mBodyHtml = in.readString();
        this.mContext = in.readString();
        this.mDestination = in.readString();
        this.mLinkTitle = in.readString();
        this.mName = in.readString();
        this.mParentId = in.readString();
        this.mReplies = in.readString();
        this.mSubject = in.readString();
        this.mSubreddit = in.readString();
        setVoteValue(in.readInt());
        this.mUnread = in.readByte() != 0;
        this.mWasComment = in.readByte() != 0;
    }

    public static Parcelable.Creator<RedditMessage> CREATOR = new Parcelable.Creator<RedditMessage>() {
        public RedditMessage createFromParcel(Parcel source) {
            return new RedditMessage(source);
        }

        public RedditMessage[] newArray(int size) {
            return new RedditMessage[size];
        }
    };
}
