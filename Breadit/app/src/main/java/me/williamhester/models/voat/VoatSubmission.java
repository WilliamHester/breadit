package me.williamhester.models.voat;

import android.os.Parcel;

import me.williamhester.models.bulletin.Submission;

/**
 * Created by william on 7/4/15.
 */
public class VoatSubmission extends VoatContent implements Submission {
    public static final int SELF_POST = 1;
    public static final int LINK_POST = 2;

    private int commentCount;
    private int views;
    private int type;
    private String thumbnail;
    private String title;
    private String url;

    public VoatSubmission() {
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public int getCommentCount() {
        return commentCount;
    }

    @Override
    public boolean isSelf() {
        return type == SELF_POST;
    }

    public int getViews() {
        return views;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.commentCount);
        dest.writeInt(this.views);
        dest.writeInt(this.type);
        dest.writeString(this.thumbnail);
        dest.writeString(this.title);
        dest.writeString(this.url);
    }

    protected VoatSubmission(Parcel in) {
        super(in);
        this.commentCount = in.readInt();
        this.views = in.readInt();
        this.type = in.readInt();
        this.thumbnail = in.readString();
        this.title = in.readString();
        this.url = in.readString();
    }

    public static final Creator<VoatSubmission> CREATOR = new Creator<VoatSubmission>() {
        public VoatSubmission createFromParcel(Parcel source) {
            return new VoatSubmission(source);
        }

        public VoatSubmission[] newArray(int size) {
            return new VoatSubmission[size];
        }
    };
}
