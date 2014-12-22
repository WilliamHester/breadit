package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.utils.Utilities;

/**
 * Created by William on 4/12/14.
 */
public class Message implements Votable, Parcelable {

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

    public Message(JsonObject data) {
        mCreated = data.get("created").getAsLong();
        mCreatedUtc = data.get("created_utc").getAsLong();
        mAuthor = data.get("author").getAsString();
        mBody = data.get("body").getAsString();
        mBodyHtml = data.get("body_html").getAsString();
        mContext = data.get("context").getAsString();
        mDestination = data.get("dest").getAsString();
        if (data.get("link_title") != null)
            mLinkTitle = data.get("link_title").getAsString();
        mName = data.get("name").getAsString();
        if (!data.get("parent_id").isJsonNull())
            mParentId = data.get("parent_id").getAsString();
//        if (!data.get("replies").isJsonNull())
//            mReplies = data.get("replies").getAsString();
        mId = data.get("id").getAsString();
        mSubject = data.get("subject").getAsString();
        mWasComment = data.get("was_comment").getAsBoolean();
        if (mWasComment) {
            mSubreddit = data.get("subreddit").getAsString();
            JsonElement je = data.getAsJsonObject().get("likes");
            if (je.isJsonNull()) {
                mVoteStatus = null;
            } else {
                mVoteStatus = je.getAsBoolean();
            }
        }
        mUnread = data.get("new").getAsBoolean();

    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    @Override
    public String getRawMarkdown() {
        return null;
    }

    @Override
    public void setRawMarkdown(String string) {

    }

    public String getBody() {
        return mBody;
    }

    public String getBodyHtml() {
        return mBodyHtml;
    }

    public String getContext() {
        return mContext;
    }

    public String getDestination() {
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

    public String getSubject() {
        return mSubject;
    }

    public String getmSubreddit() {
        return mSubreddit;
    }

    public boolean isUnread() {
        return mUnread;
    }

    public void setUnread(boolean unread) {
        mUnread = unread;
    }

    public boolean isComment() {
        return mWasComment;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getId() {
        return mId;
    }

    public int getVoteStatus() {
//        Log.d("Message.getVoteStatus()", "" + mVoteStatus);
        return mVoteStatus == null ? NEUTRAL : (mVoteStatus ? UPVOTED : DOWNVOTED);
    }

    public void setVoteStatus(int status) {
        if (status == Votable.NEUTRAL) {
            mVoteStatus = null;
        } else {
            mVoteStatus = status == Votable.UPVOTED;
        }
    }

    public int getScore() {
        return -1;
    }

    @Override
    public String getAuthor() {
        return mAuthor;
    }

    public void setSpannableBody(Spannable body) {

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

    private Message(Parcel in) {
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
        setVoteStatus(in.readInt());
        this.mUnread = in.readByte() != 0;
        this.mWasComment = in.readByte() != 0;
    }

    public static Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
