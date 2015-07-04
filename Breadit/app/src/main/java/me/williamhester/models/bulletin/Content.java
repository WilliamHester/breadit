package me.williamhester.models.bulletin;

import android.text.Spannable;

/**
 * Created by william on 7/4/15.
 */
public interface Content {
    String getAuthor();
    String getFlair();
    String getBodyMarkdown();
    String getBodyHtml();
    String getId();
    String getBulletin();
    String getFormattedRelativeTime();
    Spannable getSpannableBody();
    int getScore();
    int getVoteValue();
    void setBodyMarkdown(String markdown);
    void setBodyHtml(String html);
    void setSpannableBody(Spannable body);
    void setVoteValue(int value);
}
