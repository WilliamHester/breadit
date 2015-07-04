package me.williamhester.models.voat;

import me.williamhester.models.bulletin.Bulletin;

/**
 * Created by william on 7/4/15.
 */
public class VoatSubverse implements Bulletin {
    private int subscriberCount;
    private boolean ratedAdult;
    private String name;
    private String title;
    private String description;
    private String creationDate;
    private String sidebar;
    private String type;

    @Override
    public int getSubscriberCount() {
        return subscriberCount;
    }

    @Override
    public boolean isNsfw() {
        return ratedAdult;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCreatedDate() {
        return creationDate;
    }

    @Override
    public String getSidebar() {
        return sidebar;
    }

    @Override
    public String getType() {
        return type;
    }
}
