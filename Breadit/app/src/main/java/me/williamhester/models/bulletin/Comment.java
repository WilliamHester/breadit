package me.williamhester.models.bulletin;

/**
 * This interface is supposed to contain the common elements between Voat and Reddit's Comment
 * data type.
 *
 * @author William Hester
 */
public interface Comment extends Content {
    int getLevel();
}
