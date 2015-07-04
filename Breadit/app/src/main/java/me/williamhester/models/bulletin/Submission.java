package me.williamhester.models.bulletin;

/**
 * This interface is supposed to contain the common elements between Voat and Reddit's Submission
 * data type.
 *
 * @author William Hester
 */
public interface Submission extends Content {
    String getTitle();
    String getUrl();
    String getThumbnail();
    int getCommentCount();
    boolean isSelf();
}
