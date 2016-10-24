package me.williamhester.reddit.models.reddit;

/**
 * Created by William on 4/6/14.
 */
public interface Votable extends Thing {

  int UPVOTED = 1;
  int NEUTRAL = 0;
  int DOWNVOTED = -1;

  int getVoteValue();

  void setVoteValue(int status);

  long getCreatedUtc();

  String getBodyMarkdown();

  void setMarkdownBody(String string);

}
