package me.williamhester.models.voat;

import android.os.Parcel;

import me.williamhester.models.bulletin.Comment;

/**
 * Created by william on 7/4/15.
 */
public class VoatComment extends VoatContent implements Comment {
    private int submissionID;
    private int childCount;
    private int level;
    private int voteValue;
    private String parentID;

    public VoatComment() { }

    @Override
    public String getFormattedRelativeTime() {
        return "5 days ago";
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getVoteValue() {
        return voteValue;
    }

    @Override
    public void setVoteValue(int value) {
        voteValue = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.submissionID);
        dest.writeInt(this.childCount);
        dest.writeInt(this.level);
        dest.writeInt(this.voteValue);
        dest.writeString(this.parentID);
    }

    protected VoatComment(Parcel in) {
        super(in);
        this.submissionID = in.readInt();
        this.childCount = in.readInt();
        this.level = in.readInt();
        this.voteValue = in.readInt();
        this.parentID = in.readString();
    }

    public static final Creator<VoatComment> CREATOR = new Creator<VoatComment>() {
        public VoatComment createFromParcel(Parcel source) {
            return new VoatComment(source);
        }

        public VoatComment[] newArray(int size) {
            return new VoatComment[size];
        }
    };
}
