package me.williamhester.models.voat;

import android.text.Spannable;

import me.williamhester.models.bulletin.Comment;

/**
 * Created by william on 7/4/15.
 */
public class VoatComment implements Comment {
    private int id;
    private int submissionID;
    private int upVotes;
    private int downVotes;
    private int childCount;
    private int level;
    private int voteValue;
    private String parentID;
    private String subverse;
    private String date;
    private String lastEditDate;
    private String userName;
    private String content;
    private String formattedContent;
    private Spannable spannableBody;

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
    public int getLevel() {
        return level;
    }

    @Override
    public int getVoteValue() {
        return voteValue;
    }

    @Override
    public void setBodyMarkdown(String markdown) {
        this.content = markdown;
    }

    @Override
    public void setBodyHtml(String html) {
        this.formattedContent = html;
    }

    @Override
    public void setSpannableBody(Spannable body) {
        spannableBody = body;
    }

    @Override
    public void setVoteValue(int value) {
        voteValue = value;
    }
}
