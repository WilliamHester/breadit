package me.williamhester.models.voat;

import android.os.Parcel;
import android.text.Spannable;

import me.williamhester.models.bulletin.Message;

/**
 * Created by william on 7/4/15.
 */
public class VoatMessage implements Message {

    public static final int MESSAGE_TYPE_INBOX      = 0b00001;
    public static final int MESSAGE_TYPE_SENT       = 0b00010;
    public static final int MESSAGE_TYPE_COMMENT    = 0b00100;
    public static final int MESSAGE_TYPE_SUBMISSION = 0b01000;
    public static final int MESSAGE_TYPE_MENTION    = 0b10000;
    public static final int MESSAGE_TYPE_ALL        = 0b11111;

    private Integer voteValue = 0;
    private Integer id;
    private Integer type;
    private Boolean unread;
    private Integer commentID;
    private Integer submissionID;
    private String subverse;
    private String recipient;
    private String sender;
    private String subject;
    private String typeName;
    private String sentDate;
    private String content;
    private String formattedContent;
    private Spannable body;

    @Override
    public String getId() {
        return String.valueOf(id);
    }

    @Override
    public boolean isUnread() {
        return unread != null && unread;
    }

    @Override
    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    @Override
    public boolean isComment() {
        return commentID != null;
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
    public String getCommentId() {
        return String.valueOf(commentID);
    }

    @Override
    public String getSubmissionId() {
        return String.valueOf(submissionID);
    }

    @Override
    public String getBulletin() {
        return subverse;
    }

    @Override
    public String getRecipient() {
        return recipient;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getSentDate() {
        return sentDate;
    }

    @Override
    public String getMarkdownBody() {
        return content;
    }

    @Override
    public String getHtmlBody() {
        return formattedContent;
    }

    @Override
    public Spannable getSpannableBody() {
        return body;
    }

    @Override
    public void setSpannableBody(Spannable body) {
        this.body = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.voteValue);
        dest.writeValue(this.id);
        dest.writeValue(this.type);
        dest.writeValue(this.unread);
        dest.writeValue(this.commentID);
        dest.writeValue(this.submissionID);
        dest.writeString(this.subverse);
        dest.writeString(this.recipient);
        dest.writeString(this.sender);
        dest.writeString(this.subject);
        dest.writeString(this.typeName);
        dest.writeString(this.sentDate);
        dest.writeString(this.content);
        dest.writeString(this.formattedContent);
    }

    public VoatMessage() {
    }

    protected VoatMessage(Parcel in) {
        this.voteValue = (Integer) in.readValue(Integer.class.getClassLoader());
        this.id = (Integer) in.readValue(Integer.class.getClassLoader());
        this.type = (Integer) in.readValue(Integer.class.getClassLoader());
        this.unread = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.commentID = (Integer) in.readValue(Integer.class.getClassLoader());
        this.submissionID = (Integer) in.readValue(Integer.class.getClassLoader());
        this.subverse = in.readString();
        this.recipient = in.readString();
        this.sender = in.readString();
        this.subject = in.readString();
        this.typeName = in.readString();
        this.sentDate = in.readString();
        this.content = in.readString();
        this.formattedContent = in.readString();
    }

    public static final Creator<VoatMessage> CREATOR = new Creator<VoatMessage>() {
        public VoatMessage createFromParcel(Parcel source) {
            return new VoatMessage(source);
        }

        public VoatMessage[] newArray(int size) {
            return new VoatMessage[size];
        }
    };
}
