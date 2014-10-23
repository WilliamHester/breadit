package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by william on 9/26/14.
 */
public class AbsComment implements Parcelable {

    protected static final int COMMENT = 1;
    protected static final int MORE_COMMENTS = 2;

    public AbsComment(int level) {
        mLevel = level;
    }

    private int mLevel = 0;

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public String getParentName() {
        return null;
    }

    public static class CommentIterator implements Iterator<AbsComment> {

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
        public AbsComment next() {
            Object object = mStack.peek().getData();
            if (object instanceof MoreComments) {
                return (MoreComments) mStack.pop().getData();
            } else {
                Comment comment = (Comment) object;
                if (comment.getReplies() == null
                        || comment.getReplies().getData() instanceof MoreComments
                        || ((Listing) comment.getReplies().getData()).size() == 0) {
                    return (Comment) mStack.pop().getData();
                } else {
                    mStack.pop();
                    Listing replies = (Listing) comment.getReplies().getData();
                    for (int i = replies.size() - 1; i >= 0; i--) {
                        ResponseRedditWrapper tempComment = replies.getChildren().get(i);
                        if (tempComment.getData() instanceof AbsComment) {
                            ((AbsComment) tempComment.getData()).setLevel(comment.getLevel() + 1);
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
        dest.writeInt(this.mLevel);
    }

    protected AbsComment(Parcel in) {
        this.mLevel = in.readInt();
    }

    public static final Creator<AbsComment> CREATOR = new Creator<AbsComment>() {
        public AbsComment createFromParcel(Parcel source) {
            return new AbsComment(source);
        }

        public AbsComment[] newArray(int size) {
            return new AbsComment[size];
        }
    };
}
