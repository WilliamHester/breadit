package me.williamhester.models.bulletin;

/**
 * Created by william on 7/4/15.
 */
public interface Bulletin {
    int getSubscriberCount();
    boolean isNsfw();
    String getName();
    String getTitle();
    String getDescription();
    String getCreatedDate();
    String getSidebar();
    String getType();
}
