package me.williamhester.models.reddit;

import android.os.Parcel;

import java.util.Iterator;
import java.util.Stack;

/**
 * This class provides an abstraction of a Reddit comment, allowing for a MoreComments class and a
 * standard Comment class.
 *
 * Created by William on 9/26/14.
 */
public abstract class AbsComment implements Thing {

    protected static final int COMMENT = 1;
    protected static final int MORE_COMMENTS = 2;

    public AbsComment(int level) {
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

    public static class CommentIterator implements Iterator<AbsComment> {

        private Stack<ResponseWrapper> mStack;

        public CommentIterator(ResponseWrapper root) {
            mStack = new Stack<>();
            mStack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !mStack.isEmpty();
        }

        @Override
        public AbsComment next() {
            Object object = mStack.peek().getData();
            if (object instanceof MoreComments) {
                return (MoreComments) mStack.pop().getData();
            } else {
                Comment redditComment = (Comment) object;
                if (redditComment.getReplies() == null
                        || redditComment.getReplies().getData() instanceof MoreComments
                        || ((Listing) redditComment.getReplies().getData()).size() == 0) {
                    return (Comment) mStack.pop().getData();
                } else {
                    mStack.pop();
                    Listing replies = (Listing) redditComment.getReplies().getData();
                    for (int i = replies.size() - 1; i >= 0; i--) {
                        ResponseWrapper tempComment = replies.getChildren().get(i);
                        if (tempComment.getData() instanceof AbsComment) {
                            ((AbsComment) tempComment.getData()).setLevel(redditComment.getLevel() + 1);
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

    protected AbsComment(Parcel in) {
        this.mLevel = in.readInt();
    }
}
