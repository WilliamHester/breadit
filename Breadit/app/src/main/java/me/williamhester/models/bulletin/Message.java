package me.williamhester.models.bulletin;

import android.os.Parcelable;
import android.text.Spannable;

/**
 * This interface is supposed to contain the common elements between Voat and Reddit's Message
 * data type.
 *
 * @author William Hester
 */
public interface Message extends Parcelable {
    boolean isComment();
    boolean isUnread();
    void setUnread(boolean unread);
    int getVoteValue();
    void setVoteValue(int value);
    String getId();
    String getCommentId();
    String getSubmissionId();
    String getBulletin();
    String getRecipient();
    String getSender();
    String getSubject();
    String getSentDate();
    String getMarkdownBody();
    String getHtmlBody();
    Spannable getSpannableBody();
    void setSpannableBody(Spannable body);
}
