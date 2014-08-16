package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.utils.Utilities;

/**
 * Created by William on 4/12/14.
 */
public class Message implements Votable, Parcelable {

    public static final int ALL = 0;
    public static final int UNREAD = 1;
    public static final int MESSAGES = 2;
    public static final int COMMENT_REPLIES = 3;
    public static final int POST_REPLIES = 4;
    public static final int SENT = 5;
    public static final int MOD_MAIL = 6;

    private long mCreated;
    private long mCreatedUtc;
    private String mAuthor;
    private String mBody;
    private String mBodyHtml;
    private String mContext;
    private String mDestination;
    private String mLinkTitle;
    private String mName;
    private String mParentId;
    private String mReplies;
    private String mSubject;
    private String mSubreddit;
    private int mVoteStatus = Votable.NEUTRAL;
    private boolean mUnread;
    private boolean mWasComment;

    public Message(JsonObject data) {
        JsonObject dataObj = data.getAsJsonObject("data");
        mCreated = dataObj.get("created").getAsLong();
        mCreatedUtc = dataObj.get("created_utc").getAsLong();
        mAuthor = dataObj.get("author").getAsString();
        mBody = dataObj.get("body").getAsString();
        mBodyHtml = dataObj.get("body_html").getAsString();
        mContext = dataObj.get("context").getAsString();
        mDestination = dataObj.get("dest").getAsString();
        if (dataObj.get("link_title") != null)
            mLinkTitle = dataObj.get("link_title").getAsString();
        mName = dataObj.get("name").getAsString();
        if (!dataObj.get("parent_id").isJsonNull())
            mParentId = dataObj.get("parent_id").getAsString();
//        if (!dataObj.get("replies").isJsonNull())
//            mReplies = dataObj.get("replies").getAsString();
        mSubject = dataObj.get("subject").getAsString();
        mWasComment = dataObj.get("was_comment").getAsBoolean();
        if (mWasComment) {
            mSubreddit = dataObj.get("subreddit").getAsString();
            JsonElement je = dataObj.getAsJsonObject().get("likes");
            if (je.isJsonNull()) {
                mVoteStatus = NEUTRAL;
            } else if (je.getAsBoolean()) {
                mVoteStatus = UPVOTED;
            } else {
                mVoteStatus = DOWNVOTED;
            }
        }
        mUnread = dataObj.get("new").getAsBoolean();

    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getBody() {
        return mBody;
    }

    @Override
    public void setBody(String body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBeingEdited(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeingEdited() {
        return false;
    }

    @Override
    public void setBodyHtml(String body) {
        throw new UnsupportedOperationException();
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

    public void setIsRead(boolean read) {
        mUnread = read;
    }

    public boolean isComment() {
        return mWasComment;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getVoteStatus() {
        return mVoteStatus;
    }

    @Override
    public void setVoteStatus(int status) {
        mVoteStatus = status;
    }

    @Override
    public int getScore() {
        return -1;
    }

    @Override
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * This method returns a list of all of the messages for an account.
     *
     * @param before            The before= argument
     * @param after             The after= argument
     * @param account              The account. If the account is not connected, it will throw an exception.
     *
     * @return The list containing submissions
     *
     * @throws java.io.IOException      If connection fails
     */
    public static List<Message> getMessages(int type, String before, String after,
            Account account) throws IOException {
        String append = "";
        switch (type) {
            case ALL:
                append = "inbox/";
                break;
            case UNREAD:
                append = "unread/";
                break;
            case MESSAGES:
                append = "messages/";
                break;
            case COMMENT_REPLIES:
                append = "comments/";
                break;
            case POST_REPLIES:
                append = "selfreply/";
                break;
            case SENT:
                append = "sent/";
                break;
            case MOD_MAIL:
                append = "moderator/";
                break;
        }

        ArrayList<Message> messages = new ArrayList<Message>();
        String urlString = "http://www.reddit.com/message/" + append + ".json";

        if (after != null) {
            urlString += "after=" + after;
        } else if (before != null) {
            urlString += "before=" + before;
        }

        JsonObject object = new JsonParser().parse(Utilities.get(null, urlString,
                account.getCookie(), account.getModhash()))
                .getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray array = data.get("children").getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonData = (JsonObject)array.get(i);
            messages.add(new Message(jsonData));
        }

        return messages;
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
        dest.writeInt(this.mVoteStatus);
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
        this.mVoteStatus = in.readInt();
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
