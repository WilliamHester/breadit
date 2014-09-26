package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class Comment implements Votable, Parcelable {

    private static final int DOES_NOT_HAVE_CHILDREN = 0;
    private static final int HAS_CHILDREN = 1;

    public static final int BEST = 0;
    public static final int TOP = 1;
    public static final int HOT = 2;
    public static final int CONTROVERSIAL = 3;
    public static final int NEW = 4;
    public static final int OLD = 5;

    private ResponseRedditWrapper mReplies;
    private String mApprovedBy;
    private String mAuthor;
    private String mAuthorFlairCss;
    private String mAuthorFlairText;
    private String mBannedBy;
    private String mBodyHtml;
    private String mSubreddit;
    private String mSubredditId;
    private String mLinkAuthor;
    private String mLinkId;
    private String mLinkTitle;
    private String mLinkUrl;
    private String mName;
    private String mDistinguished;
    private boolean mSaved;
    private Boolean mVoteStatus;
    private long mCreated;
    private long mCreatedUtc;
    private String mUps;
    private int mScore;
    private Spannable mSpannableBody;
    private ArrayList<Comment> mChildren;

    private int mLevel = 0;
    private boolean mIsHidden = false;
    private boolean mIsBeingEdited = false;
    private String mReplyText;

    protected Comment() {}

    /**
     * Creates a comment from a JsonObject. Because a comment is a tree node, we also need to pass
     * the Gson object down the tree in case more of the tree needs to be constructed. We don't want
     * 500 different Gson objects floating around at runtime.
     *
     * @param object the JsonObject that holds the comment
     * @param gson the Gson object
     */
    public Comment(JsonObject object, Gson gson) {
        if (!object.get("approved_by").isJsonNull()) {
            mApprovedBy = object.get("approved_by").getAsString();
        }
        if (!object.get("author").isJsonNull()) {
            mAuthor = object.get("author").getAsString();
        }
        if (!object.get("author_flair_text").isJsonNull()) {
            mAuthorFlairText = object.get("author_flair_text").getAsString();
        }
        if (!object.get("author_flair_css_class").isJsonNull()) {
            mAuthorFlairCss = object.get("author_flair_css_class").getAsString();
        }
        if (!object.get("banned_by").isJsonNull()) {
            mBannedBy = object.get("banned_by").getAsString();
        }
        if (!object.get("body_html").isJsonNull()) {
            mBodyHtml = object.get("body_html").getAsString();
        }
        if (!object.get("subreddit").isJsonNull()) {
            mSubreddit = object.get("subreddit").getAsString();
        }
        if (!object.get("subreddit_id").isJsonNull()) {
            mSubredditId = object.get("subreddit_id").getAsString();
        }
        if (!object.get("link_id").isJsonNull()) {
            mLinkId = object.get("link_id").getAsString();
        }
        if (!object.get("distinguished").isJsonNull()) {
            mDistinguished = object.get("distinguished").getAsString();
        }
        mName = object.get("name").getAsString();
        mScore = object.get("score").getAsInt();
        mCreated = object.get("created").getAsLong();
        mCreatedUtc = object.get("created_utc").getAsLong();
        if (!object.get("ups").isJsonNull()) {
            mUps = object.get("ups").getAsString();
        }
        try {
            mVoteStatus = object.get("likes").getAsBoolean();
        } catch (UnsupportedOperationException e) {
            mVoteStatus = null;
        }
        try {
            JsonObject replies = object.get("replies").getAsJsonObject();
            mReplies = new ResponseRedditWrapper(replies, gson);
        } catch (IllegalStateException e) {
            mReplies = null;
        }
    }

    // For use when replying to comments
    public Comment(Account account, int level) {
        mAuthor = account.getUsername();
        mUps = "";
        mCreatedUtc = System.currentTimeMillis() / 1000;
        mBodyHtml = "";
        mLevel = level;
        mIsBeingEdited = true;
    }

    @Override
    public String getName() {
        return mName;
    }

    public int getVoteStatus() {
        if (mVoteStatus == null) {
            return NEUTRAL;
        } else if (mVoteStatus) {
            return UPVOTED;
        } else {
            return DOWNVOTED;
        }
    }

    public void setVoteStatus(int status) {
        if (status == NEUTRAL) {
            mVoteStatus = null;
        } else {
            mVoteStatus = status == UPVOTED;
        }
    }

    public ResponseRedditWrapper getReplies() {
        return mReplies;
    }

    public String getBodyHtml() {
        return mBodyHtml;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getSubredditId() {
        return mSubredditId;
    }

    public String getLinkAuthor() {
        return mLinkAuthor;
    }

    public String getLinkId() {
        return mLinkId;
    }

    public String getLinkTitle() {
        return mLinkTitle;
    }

    public String getLinkUrl() {
        return mLinkUrl;
    }

    public int getScore() {
        return mScore;
    }

    public String getAuthor() { 
        return mAuthor;
    }

    public boolean hasReplies() {
        return mReplies != null;
    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setHidden(boolean hidden) {
        mIsHidden = hidden;
    }

    public boolean isHidden() {
        return mIsHidden;
    }

    public void setBeingEdited(boolean b) {
        mIsBeingEdited = b;
    }

    public boolean isBeingEdited() {
        return mIsBeingEdited;
    }

    public void setReplies(ResponseRedditWrapper replies) {
        mReplies = replies;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    @Override
    public void setBodyHtml(String body) {
        mBodyHtml = body;
    }

    public void setSpannableBody(Spannable body) {
        mSpannableBody = body;
    }

    public Spannable getSpannableBody() {
        return mSpannableBody;
    }

    public void hide(ArrayList<Comment> children) {
        mIsHidden = true;
        mChildren = children;
    }

    public ArrayList<Comment> unhideComment() {
        mIsHidden = false;
        ArrayList<Comment> children = mChildren;
        mChildren = null;
        return children;
    }

    public static class CommentIterator implements Iterator<Comment> {

        private Stack<ResponseRedditWrapper> mStack;

        public CommentIterator(ResponseRedditWrapper root) {
            mStack = new Stack<>();
            mStack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !mStack.isEmpty();
        }

        @Override
        public Comment next() {
            Object object = mStack.peek().getData();
            if (object instanceof MoreComments) {
                return (MoreComments) mStack.pop().getData();
            } else {
                Comment comment = (Comment) object;
                if (comment.mReplies == null
                        || comment.mReplies.getData() instanceof MoreComments
                        || ((Listing) comment.mReplies.getData()).size() == 0) {
                    return (Comment) mStack.pop().getData();
                } else {
                    mStack.pop();
                    Listing replies = (Listing) comment.mReplies.getData();
                    for (int i = replies.size() - 1; i >= 0; i--) {
                        ResponseRedditWrapper tempComment = replies.getChildren().get(i);
                        if (tempComment.getData() instanceof Comment) {
                            ((Comment) tempComment.getData()).setLevel(comment.getLevel() + 1);
                            mStack.add(tempComment);
                        }
                    }
                    comment.setReplies(null);
                    return comment;
                }
            }

        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mReplies);
        dest.writeString(this.mApprovedBy);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mAuthorFlairCss);
        dest.writeString(this.mAuthorFlairText);
        dest.writeString(this.mBannedBy);
        dest.writeString(this.mBodyHtml);
        dest.writeString(this.mSubreddit);
        dest.writeString(this.mSubredditId);
        dest.writeString(this.mLinkAuthor);
        dest.writeString(this.mLinkId);
        dest.writeString(this.mLinkTitle);
        dest.writeString(this.mLinkUrl);
        dest.writeString(this.mDistinguished);
        dest.writeByte(mSaved ? (byte) 1 : (byte) 0);
        dest.writeValue(this.mVoteStatus);
        dest.writeLong(this.mCreated);
        dest.writeLong(this.mCreatedUtc);
        dest.writeString(this.mUps);
        dest.writeInt(this.mScore);
        dest.writeInt(this.mLevel);
        dest.writeByte(mIsHidden ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsBeingEdited ? (byte) 1 : (byte) 0);
        dest.writeString(this.mReplyText);
        if (mChildren != null) {
            dest.writeInt(HAS_CHILDREN);
            Comment[] children = new Comment[mChildren.size()];
            mChildren.toArray(children);
            dest.writeParcelableArray(children, 0);
        } else {
            dest.writeInt(DOES_NOT_HAVE_CHILDREN);
        }
    }

    @SuppressWarnings("unchecked")
    private Comment(Parcel in) {
        this.mReplies = (ResponseRedditWrapper) in.readSerializable();
        this.mApprovedBy = in.readString();
        this.mAuthor = in.readString();
        this.mAuthorFlairCss = in.readString();
        this.mAuthorFlairText = in.readString();
        this.mBannedBy = in.readString();
        this.mBodyHtml = in.readString();
        this.mSubreddit = in.readString();
        this.mSubredditId = in.readString();
        this.mLinkAuthor = in.readString();
        this.mLinkId = in.readString();
        this.mLinkTitle = in.readString();
        this.mLinkUrl = in.readString();
        this.mDistinguished = in.readString();
        this.mSaved = in.readByte() != 0;
        this.mVoteStatus = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.mCreated = in.readLong();
        this.mCreatedUtc = in.readLong();
        this.mUps = in.readString();
        this.mScore = in.readInt();
        this.mLevel = in.readInt();
        this.mIsHidden = in.readByte() != 0;
        this.mIsBeingEdited = in.readByte() != 0;
        this.mReplyText = in.readString();
        int hasChildren = in.readInt();
        if (hasChildren == HAS_CHILDREN) {
            Comment[] children = (Comment[]) in.readParcelableArray(Comment.class.getClassLoader());
            this.mChildren = new ArrayList<>();
            Collections.addAll(this.mChildren, children);
        }
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
