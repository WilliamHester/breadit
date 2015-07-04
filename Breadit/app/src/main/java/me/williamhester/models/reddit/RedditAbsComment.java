package me.williamhester.models.reddit;

import android.os.Parcel;

import java.util.Iterator;
import java.util.Stack;

/**
 * This class provides an abstraction of a Reddit comment, allowing for a RedditMoreComments class and a
 * standard RedditComment class.
 *
 * Created by William on 9/26/14.
 */
public abstract class RedditAbsComment implements RedditThing {

    protected static final int COMMENT = 1;
    protected static final int MORE_COMMENTS = 2;

    public RedditAbsComment(int level) {
        mLevel = level;
    }

    protected int mLevel = 0;

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public abstract String getParentId();

    public static class CommentIterator implements Iterator<RedditAbsComment> {

        private Stack<RedditResponseWrapper> mStack;

        public CommentIterator(RedditResponseWrapper root) {
            mStack = new Stack<>();
            mStack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !mStack.isEmpty();
        }

        @Override
        public RedditAbsComment next() {
            Object object = mStack.peek().getData();
            if (object instanceof RedditMoreComments) {
                return (RedditMoreComments) mStack.pop().getData();
            } else {
                RedditComment redditComment = (RedditComment) object;
                if (redditComment.getReplies() == null
                        || redditComment.getReplies().getData() instanceof RedditMoreComments
                        || ((RedditListing) redditComment.getReplies().getData()).size() == 0) {
                    return (RedditComment) mStack.pop().getData();
                } else {
                    mStack.pop();
                    RedditListing replies = (RedditListing) redditComment.getReplies().getData();
                    for (int i = replies.size() - 1; i >= 0; i--) {
                        RedditResponseWrapper tempComment = replies.getChildren().get(i);
                        if (tempComment.getData() instanceof RedditAbsComment) {
                            ((RedditAbsComment) tempComment.getData()).setLevel(redditComment.getLevel() + 1);
                            mStack.add(tempComment);
                        }
                    }
                    redditComment.setReplies(null);
                    return redditComment;
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
        dest.writeInt(this.mLevel);
    }

    protected RedditAbsComment(Parcel in) {
        this.mLevel = in.readInt();
    }
}
