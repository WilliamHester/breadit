package me.williamhester.models.bulletin;

/**
 * Created by william on 7/4/15.
 */
public interface User {
    String getUsername();
    String getCreatedDate();
    int getCommentPoints();
    int getSubmissionPoints();
}
