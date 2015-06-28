package me.williamhester.models;

/**
 * Created by William on 4/6/14.
 */
public interface Votable extends Thing {

    public static final int UPVOTED = 1;
    public static final int NEUTRAL = 0;
    public static final int DOWNVOTED = -1;

    public int getVoteStatus();
    public void setVoteStatus(int status);
    public long getCreatedUtc();
    public String getRawMarkdown();
    public void setRawMarkdown(String string);

}
