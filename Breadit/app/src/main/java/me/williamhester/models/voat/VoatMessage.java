package me.williamhester.models.voat;

/**
 * Created by william on 7/4/15.
 */
public class VoatMessage {

    public static final int MESSAGE_TYPE_INBOX      = 0b00001;
    public static final int MESSAGE_TYPE_SENT       = 0b00010;
    public static final int MESSAGE_TYPE_COMMENT    = 0b00100;
    public static final int MESSAGE_TYPE_SUBMISSION = 0b01000;
    public static final int MESSAGE_TYPE_MENTION    = 0b10000;
    public static final int MESSAGE_TYPE_ALL        = 0b11111;

    private int id;
    private int type;
    private boolean unread;
    private String commentID;
    private String submissionID;
    private String subverse;
    private String recipient;
    private String sender;
    private String subject;
    private String typeName;
    private String sentDate;
    private String content;
    private String formattedContent;

    public String getId() {
        return String.valueOf(id);
    }

    public int getType() {
        return type;
    }

    public boolean isUnread() {
        return unread;
    }

    public String getCommentId() {
        return commentID;
    }

    public String getSubmissionId() {
        return submissionID;
    }

    public String getBulletin() {
        return subverse;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getSentDate() {
        return sentDate;
    }

    public String getContent() {
        return content;
    }

    public String getFormattedContent() {
        return formattedContent;
    }
}
