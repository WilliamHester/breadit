package me.williamhester.areddit;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import me.williamhester.areddit.utils.Utilities;

public class Comment extends Thing implements Parcelable, Votable {

    public static final int BEST = 0;
    public static final int TOP = 1;
    public static final int HOT = 2;
    public static final int CONTROVERSIAL = 3;
    public static final int NEW = 4;
    public static final int OLD = 5;

    private List<Comment> mReplies;
//    private List<String> mMore;

    private String mApprovedBy;
    private String mAuthor;
    private String mAuthorFlairCss;
    private String mAuthorFlairText;
    private String mBannedBy;
    private String mBody;
    private String mBodyHtml;
    private String mSubreddit;
    private String mSubredditId;
    private String mLinkAuthor;
    private String mLinkId;
    private String mLinkTitle;
    private String mLinkUrl;
    private String mDistinguished;
    private boolean mSaved;
    private int mVoteStatus;
    private long mCreated;
    private long mCreatedUtc;
    private long mUps;
    private long mDowns;
    private long mEdited;

    private int mLevel = 0;
    private boolean mIsHidden = false;
    private boolean mIsBeingEdited = false;
    private String mReplyText;

    // For use when replying to comments
    public Comment(Account account, int level) {
        mAuthor = account.getUsername();
        mVoteStatus = 0;
        mUps = 0;
        mDowns = 0;
        mCreatedUtc = System.currentTimeMillis() / 1000;
        mBodyHtml = "";
        mBody = "";
        mLevel = level;
        mIsBeingEdited = true;
    }

    private Comment(JsonObject jsonObj, int level) {
        super(jsonObj);
        mLevel = level;
        if (!getKind().equals("more")) {
            mReplies = generateReplies(jsonObj);
        }
    }

    public static Comment fromJsonString(JsonObject data, int level) {
        Comment comment = new Comment(data, level);
        if (!comment.mKind.equals("more")) {
            if (!data.get("data").getAsJsonObject().get("approved_by").isJsonNull())
                comment.mApprovedBy = data.get("data").getAsJsonObject().get("approved_by").getAsString();
            if (!data.get("data").getAsJsonObject().get("author").isJsonNull())
                comment.mAuthor = data.get("data").getAsJsonObject().get("author").getAsString();
            if (!data.get("data").getAsJsonObject().get("author_flair_css_class").isJsonNull())
                comment.mAuthorFlairCss = data.get("data").getAsJsonObject().get("author_flair_css_class").getAsString();
            if (!data.get("data").getAsJsonObject().get("author_flair_text").isJsonNull())
                comment.mAuthorFlairText = data.get("data").getAsJsonObject().get("author_flair_text").getAsString();
            if (!data.get("data").getAsJsonObject().get("banned_by").isJsonNull())
                comment.mBannedBy = data.get("data").getAsJsonObject().get("banned_by").getAsString();
            comment.mBody = data.get("data").getAsJsonObject().get("body").getAsString();
            comment.mBodyHtml = data.get("data").getAsJsonObject().get("body_html").getAsString();
            if (data.get("data").getAsJsonObject().get("link_author") != null) {
                if (!data.get("data").getAsJsonObject().get("link_author").isJsonNull())
                    comment.mLinkAuthor = data.get("data").getAsJsonObject().get("link_author").getAsString();
                if (!data.get("data").getAsJsonObject().get("link_title").isJsonNull())
                    comment.mLinkTitle = data.get("data").getAsJsonObject().get("link_title").getAsString();
                if (!data.get("data").getAsJsonObject().get("link_id").isJsonNull())
                    comment.mLinkId = data.get("data").getAsJsonObject().get("link_id").getAsString();
                if (!data.get("data").getAsJsonObject().get("link_url").isJsonNull())
                    comment.mLinkUrl = data.get("data").getAsJsonObject().get("link_url").getAsString();
            }
            comment.mSubreddit = data.get("data").getAsJsonObject().get("subreddit").getAsString();
            comment.mSubredditId = data.get("data").getAsJsonObject().get("subreddit_id").getAsString();
            if (!data.get("data").getAsJsonObject().get("distinguished").isJsonNull())
                comment.mDistinguished = data.get("data").getAsJsonObject().get("distinguished").getAsString();
            comment.mSaved = data.get("data").getAsJsonObject().get("saved").getAsBoolean();
            comment.mCreated = data.get("data").getAsJsonObject().get("created").getAsLong();
            comment.mCreatedUtc = data.get("data").getAsJsonObject().get("created_utc").getAsLong();
            comment.mUps = data.get("data").getAsJsonObject().get("ups").getAsLong();
            comment.mDowns = data.get("data").getAsJsonObject().get("downs").getAsLong();
            JsonElement je = data.get("data").getAsJsonObject().get("likes");
            if (je.isJsonNull()) {
                comment.mVoteStatus = NEUTRAL;
            } else if (je.getAsBoolean()) {
                comment.mVoteStatus = UPVOTED;
            } else {
                comment.mVoteStatus = DOWNVOTED;
            }
            je = data.get("data").getAsJsonObject().get("edited");
            if (je.isJsonNull()) {
                comment.mEdited = -1;
            } else {
                try {
                    comment.mEdited = je.getAsLong();
                } catch (NumberFormatException e) {
                    comment.mEdited = -1;
                }
            }
        } else {
//            comment.mMore = new ArrayList<String>();
//            JsonArray array = data.get("data").getAsJsonObject().get("children").getAsJsonArray();
//            for (JsonElement e : array) {
//                comment.mMore.add(e.getAsString());
//            }
        }
        return comment;
    }

    public static Comment fromJsonString(JsonObject data) {
        Comment comment = new Comment(data, 0);
        if (!data.get("data").getAsJsonObject().get("approved_by").isJsonNull())
            comment.mApprovedBy = data.get("data").getAsJsonObject().get("approved_by").getAsString();
        if (!data.get("data").getAsJsonObject().get("author").isJsonNull())
            comment.mAuthor = data.get("data").getAsJsonObject().get("author").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_css_class").isJsonNull())
            comment.mAuthorFlairCss = data.get("data").getAsJsonObject().get("author_flair_css_class").getAsString();
        if (!data.get("data").getAsJsonObject().get("author_flair_text").isJsonNull())
            comment.mAuthorFlairText = data.get("data").getAsJsonObject().get("author_flair_text").getAsString();
        if (!data.get("data").getAsJsonObject().get("banned_by").isJsonNull())
            comment.mBannedBy = data.get("data").getAsJsonObject().get("banned_by").getAsString();
        comment.mBody = data.get("data").getAsJsonObject().get("body").getAsString();
        comment.mBodyHtml = data.get("data").getAsJsonObject().get("body_html").getAsString();
        if (data.get("data").getAsJsonObject().get("link_author") != null) {
            if (!data.get("data").getAsJsonObject().get("link_author").isJsonNull())
                comment.mLinkAuthor = data.get("data").getAsJsonObject().get("link_author").getAsString();
            if (!data.get("data").getAsJsonObject().get("link_title").isJsonNull())
                comment.mLinkTitle = data.get("data").getAsJsonObject().get("link_title").getAsString();
            if (!data.get("data").getAsJsonObject().get("link_id").isJsonNull())
                comment.mLinkId = data.get("data").getAsJsonObject().get("link_id").getAsString();
            if (!data.get("data").getAsJsonObject().get("link_url").isJsonNull())
                comment.mLinkUrl = data.get("data").getAsJsonObject().get("link_url").getAsString();
        }
        comment.mSubreddit = data.get("data").getAsJsonObject().get("subreddit").getAsString();
        comment.mSubredditId = data.get("data").getAsJsonObject().get("subreddit_id").getAsString();
        if (!data.get("data").getAsJsonObject().get("distinguished").isJsonNull())
            comment.mDistinguished = data.get("data").getAsJsonObject().get("distinguished").getAsString();
        comment.mSaved = data.get("data").getAsJsonObject().get("saved").getAsBoolean();
        comment.mCreated = data.get("data").getAsJsonObject().get("created").getAsLong();
        comment.mCreatedUtc = data.get("data").getAsJsonObject().get("created_utc").getAsLong();
        comment.mUps = data.get("data").getAsJsonObject().get("ups").getAsLong();
        comment.mDowns = data.get("data").getAsJsonObject().get("downs").getAsLong();
        JsonElement je = data.get("data").getAsJsonObject().get("likes");
        if (je.isJsonNull()) {
            comment.mVoteStatus = NEUTRAL;
        } else if (je.getAsBoolean()) {
            comment.mVoteStatus = UPVOTED;
        } else {
            comment.mVoteStatus = DOWNVOTED;
        }
        je = data.get("data").getAsJsonObject().get("edited");
        if (je.isJsonNull()) {
            comment.mEdited = -1;
        } else {
            try {
                comment.mEdited = je.getAsLong();
            } catch (NumberFormatException e) {
                comment.mEdited = -1;
            }
        }
        return comment;
    }

    public int getVoteStatus() {
        return mVoteStatus;
    }

    public void setVoteStatus(int status) {
        mVoteStatus = status;
    }

    public List<Comment> getReplies() {
        return mReplies;
    }

    public String getBodyHtml() {
        return mBodyHtml;
    }

    public String getBody() { 
        return mBody;
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

    public long getUpVotes() { 
        return mUps;
    }

    public long getDownVotes() { 
        return mDowns;
    }

    public long getScore() {
        return mUps - mDowns;
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

    public CommentIterator getCommentIterator() {
        return new CommentIterator(this);
    }

    public void setBeingEdited(boolean b) {
        mIsBeingEdited = b;
    }

    public boolean isBeingEdited() {
        return mIsBeingEdited;
    }

//    public void setReplyText(String mReplyText) {
//
//    }

    /**
     * Get the replies to this comment.
     */
    public List<Comment> generateReplies(JsonObject data) {
        List<Comment> ret = new ArrayList<Comment>();
        
        data = data.get("data").getAsJsonObject();
        if (data == null || data.get("replies") == null || !data.get("replies").isJsonObject()) {
            return null;
        }
        JsonObject replies = data.get("replies").getAsJsonObject();
        JsonObject replyData = replies.get("data").getAsJsonObject();
        JsonArray children = replyData.get("children").getAsJsonArray();

        for (int i = 0; i < children.size(); i++) {
            JsonObject jsonData = (JsonObject)children.get(i);
            Comment comment = fromJsonString(jsonData, mLevel + 1);

            if(comment != null && !comment.getKind().equals("more")) {
                ret.add(comment);
            }
        }
        return ret;
    }

    /**
     * This function returns a list of Comments and their parent Submission
     *
     * @param url The url of the comments thread
     * @param account The account
     * @param after the last listing that is loaded
     * @param sortType the type of sorting
     *
     * @return A list containing Comments
     *
     * @throws java.io.IOException      If connection fails
     */
    public static List<Thing> getComments(String url, Account account, String after, int sortType)
            throws IOException {

        ArrayList<Thing> things = new ArrayList<Thing>();
        if (url == null) {
            return things;
        }

        String urlString;
        if (url.contains("?")) {
            String firstHalf = url.substring(0, url.indexOf('?') - 1);
            String secondHalf = url.substring(url.indexOf('?') + 1);
            urlString = firstHalf + ".json?" + secondHalf;
        } else {

            urlString = url + ".json?";

            switch (sortType) {
                case BEST:
                    urlString += "sort=confidence&";
                    break;
                case TOP:
                    urlString += "sort=top&";
                    break;
                case HOT:
                    urlString += "sort=hot&";
                    break;
                case CONTROVERSIAL:
                    urlString += "sort=controversial&";
                    break;
                case NEW:
                    urlString += "sort=new&";
                    break;
                case OLD:
                    urlString += "sort=old&";
                    break;
            }

            if (after != null)
                urlString += "after=" + after;
        }

        String parceable = Utilities.get(null, urlString, account);
        JsonArray array = new JsonParser().parse(parceable)
                .getAsJsonArray();
        if (array != null && array.size() > 0) {
            JsonObject submission = array.get(0).getAsJsonObject();
            things.add(Submission.fromJsonString(submission.get("data").getAsJsonObject()
                    .get("children").getAsJsonArray().get(0).getAsJsonObject()));
            JsonObject replies = array.get(1).getAsJsonObject();
            JsonArray children = replies.get("data").getAsJsonObject().get("children").getAsJsonArray();

            for (int i = 0; i < children.size(); i++) {
                JsonObject jsonData = (JsonObject) children.get(i);
                things.add(fromJsonString(jsonData, 0));
            }
        }
        return things;
    }

    public class CommentIterator implements Iterator<Comment> {

        private Stack<Comment> mStack;

        public CommentIterator(Comment root) {
            mStack = new Stack<Comment>();
            mStack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !mStack.isEmpty();
        }

        @Override
        public Comment next() {
            if (mStack.peek().mReplies == null || mStack.peek().mReplies.size() == 0) {
                return mStack.pop();
            } else {
                Comment c = mStack.pop();
                for (int i = c.mReplies.size() - 1; i >= 0; i--) {
                    mStack.add(c.mReplies.get(i));
                }
                return c;
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
        dest.writeTypedList(mReplies);
//        dest.writeList(this.mMore);
        dest.writeString(this.mApprovedBy);
        dest.writeString(this.mAuthor);
        dest.writeString(this.mAuthorFlairCss);
        dest.writeString(this.mAuthorFlairText);
        dest.writeString(this.mBannedBy);
        dest.writeString(this.mBody);
        dest.writeString(this.mBodyHtml);
        dest.writeString(this.mSubreddit);
        dest.writeString(this.mSubredditId);
        dest.writeString(this.mLinkAuthor);
        dest.writeString(this.mLinkId);
        dest.writeString(this.mLinkTitle);
        dest.writeString(this.mLinkUrl);
        dest.writeString(this.mDistinguished);
        dest.writeByte(mSaved ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mVoteStatus);
        dest.writeLong(this.mCreated);
        dest.writeLong(this.mCreatedUtc);
        dest.writeLong(this.mUps);
        dest.writeLong(this.mDowns);
        dest.writeLong(this.mEdited);
        dest.writeInt(this.mLevel);
        dest.writeByte(mIsHidden ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsBeingEdited ? (byte) 1 : (byte) 0);
        dest.writeString(this.mReplyText);
        dest.writeString(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mKind);
    }

    private Comment(Parcel in) {
        in.readTypedList(mReplies, Comment.CREATOR);
//        this.mMore = new ArrayList<List<String>>();
//        in.readList(this.mMore, List<String>.class.getClassLoader());
        this.mApprovedBy = in.readString();
        this.mAuthor = in.readString();
        this.mAuthorFlairCss = in.readString();
        this.mAuthorFlairText = in.readString();
        this.mBannedBy = in.readString();
        this.mBody = in.readString();
        this.mBodyHtml = in.readString();
        this.mSubreddit = in.readString();
        this.mSubredditId = in.readString();
        this.mLinkAuthor = in.readString();
        this.mLinkId = in.readString();
        this.mLinkTitle = in.readString();
        this.mLinkUrl = in.readString();
        this.mDistinguished = in.readString();
        this.mSaved = in.readByte() != 0;
        this.mVoteStatus = in.readInt();
        this.mCreated = in.readLong();
        this.mCreatedUtc = in.readLong();
        this.mUps = in.readLong();
        this.mDowns = in.readLong();
        this.mEdited = in.readLong();
        this.mLevel = in.readInt();
        this.mIsHidden = in.readByte() != 0;
        this.mIsBeingEdited = in.readByte() != 0;
        this.mReplyText = in.readString();
        this.mId = in.readString();
        this.mName = in.readString();
        this.mKind = in.readString();
    }

    public static Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
