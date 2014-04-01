package me.williamhester.areddit;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import me.williamhester.areddit.utils.Utilities;

public class Comment extends Thing implements Parcelable {

    public static final int UPVOTED = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTED = -1;

    private List<Comment> mReplies;
    private List<String> mMore;

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

    private Comment(JsonObject jsonObj, int level) {
        super(jsonObj);
        mLevel = level;
        if (!getKind().equals("more")) {
            mReplies = generateReplies(jsonObj);
        }
    }

    public Comment(Parcel in) {
        this(new JsonParser().parse(in.readBundle().getString("jsonData")).getAsJsonObject(),
                in.readBundle().getInt("level"));
    }

    public static Comment fromJsonString(JsonObject data, int level) {
        Comment comment = new Comment(data, level);
        comment.mLevel = level;
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
            comment.mMore = new ArrayList<String>();
            JsonArray array = data.get("data").getAsJsonObject().get("children").getAsJsonArray();
            for (JsonElement e : array) {
                comment.mMore.add(e.getAsString());
            }
        }
        return comment;
    }

    public int getVotedStatus() {
        JsonElement o = mData.get("data").getAsJsonObject().get("likes");
        if (o.isJsonNull()) {
            return NEUTRAL;
        } else if (o.getAsBoolean()) {
            return UPVOTED;
        } else {
            return DOWNVOTED;
        }
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

    public CommentIterator getCommentIterator() {
        return new CommentIterator(this);
    }

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
                Log.i("Comment", comment.getBody());
            }
        }
        return ret;
    }

    /**
     * This function returns a list of comments
     *
     * @param articleId         The id of the link/article/submission
     * @param user              The user
     *
     * @return A list containing Comments
     *
     * @throws java.io.IOException      If connection fails
     */
    public static List<Comment> getComments(String articleId, User user, String after)
            throws IOException {

        ArrayList<Comment> comments = new ArrayList<Comment>();

        String urlString = "http://www.reddit.com" + articleId + ".json";
        String cookie = user == null ? null : user.getCookie();
        String modhash = user == null ? null : user.getModhash();

        if (after != null)
            urlString += "?after=" + after;

        JsonArray array = new JsonParser().parse(Utilities.get(null, urlString, cookie, modhash))
                .getAsJsonArray();
        if(array != null && array.size() > 0) {
            JsonObject replies = array.get(1).getAsJsonObject();
            JsonArray children = replies.get("data").getAsJsonObject().get("children").getAsJsonArray();

            for (int i = 0; i < children.size(); i++) {
                JsonObject jsonData = (JsonObject)children.get(i);
                comments.add(fromJsonString(jsonData, 0));
            }
        }

        return comments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle b = new Bundle();
        b.putString("jsonData", mData.toString());
        b.putInt("level", mLevel);
        parcel.writeBundle(b);
    }

    public static final Parcelable.Creator<Comment> CREATOR
            = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

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
}
