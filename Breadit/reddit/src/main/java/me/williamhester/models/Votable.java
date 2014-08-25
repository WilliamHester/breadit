package me.williamhester.models;

import android.text.Spannable;

/**
 * Created by William on 4/6/14.
 */
public interface Votable {

    public static final int UPVOTED = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTED = -1;

    public String getName();
    public int getVoteStatus();
    public void setVoteStatus(int status);
    public int getScore();
    public String getAuthor();
    public void setSpannableBody(Spannable body);
    public void setBeingEdited(boolean b);
    public boolean isBeingEdited();
    public void setBodyHtml(String body);
    public long getCreatedUtc();

}