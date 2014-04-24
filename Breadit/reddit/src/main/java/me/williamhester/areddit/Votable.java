package me.williamhester.areddit;

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
    public long getScore();
    public String getAuthor();
    public String getBody();
    public void setBody(String body);
    public void setBeingEdited(boolean b);
    public boolean isBeingEdited();
    public void setBodyHtml(String body);

}
