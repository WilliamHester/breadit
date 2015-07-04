package me.williamhester.models.reddit;

/**
 * Created by William on 4/6/14.
 */
public interface RedditVotable extends RedditThing {

    int UPVOTED = 1;
    int NEUTRAL = 0;
    int DOWNVOTED = -1;

    int getVoteValue();
    void setVoteValue(int status);
    long getCreatedUtc();
    String getBodyMarkdown();
    void setBodyMarkdown(String string);

}
