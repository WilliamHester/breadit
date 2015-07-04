package me.williamhester.models.voat;

import android.os.Parcel;
import android.text.Spannable;

import me.williamhester.models.bulletin.Content;

/**
 * Created by william on 7/4/15.
 */
public abstract class VoatContent implements Content {
    private int id;
    private int upVotes;
    private int downVotes;
    private int voteValue;
    private String date;
    private String lastEditDate;
    private String userName;
    private String subverse;
    private String content;
    private String formattedContent;
    private Spannable spannableBody;

    public VoatContent() {
    }

    @Override
    public String getAuthor() {
        return userName;
    }

    @Override
    public String getFlair() {
        return null;
    }

    @Override
    public String getBodyMarkdown() {
        return content;
    }

    @Override
    public String getBodyHtml() {
        return formattedContent;
    }

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public String getBulletin() {
        return subverse;
    }

    @Override
    public String getFormattedRelativeTime() {
        return "5 days ago";
    }

    @Override
    public Spannable getSpannableBody() {
        return spannableBody;
    }

    @Override
    public int getScore() {
        return upVotes - downVotes;
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
    public void setMarkdownBody(String markdown) {
        content = markdown;
    }

    @Override
    public void setBodyHtml(String html) {
        formattedContent = html;
    }

    @Override
    public void setSpannableBody(Spannable body) {
        spannableBody = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.upVotes);
        dest.writeInt(this.downVotes);
        dest.writeInt(this.voteValue);
        dest.writeString(this.date);
        dest.writeString(this.lastEditDate);
        dest.writeString(this.userName);
        dest.writeString(this.subverse);
        dest.writeString(this.content);
        dest.writeString(this.formattedContent);
    }

    protected VoatContent(Parcel in) {
        this.id = in.readInt();
        this.upVotes = in.readInt();
        this.downVotes = in.readInt();
        this.voteValue = in.readInt();
        this.date = in.readString();
        this.lastEditDate = in.readString();
        this.userName = in.readString();
        this.subverse = in.readString();
        this.content = in.readString();
        this.formattedContent = in.readString();
    }

    public static final Creator<VoatContent> CREATOR = new Creator<VoatContent>() {
        public VoatContent createFromParcel(Parcel source) {
            return null;
        }

        public VoatContent[] newArray(int size) {
            return null;
        }
    };
}
