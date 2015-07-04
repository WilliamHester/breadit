package me.williamhester.models.voat;

import java.util.List;

import me.williamhester.models.bulletin.User;

/**
 * Created by william on 7/4/15.
 */
public class VoatUser implements User {

    private String userName;
    private String registrationDate;
    private String bio;
    private String profilePicture;
    private Points commentPoints;
    private Points submissionPoints;
    private Points commentVoting;
    private Points submissionVoting;
    private List<Badge> badges;

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getCreatedDate() {
        return registrationDate;
    }

    @Override
    public int getCommentPoints() {
        return commentPoints.sum;
    }

    @Override
    public int getSubmissionPoints() {
        return submissionPoints.sum;
    }


    public static class Points {
        private int upCount;
        private int downCount;
        private int sum;
    }

    public static class Badge {
        private String name;
        private String awardedDate;
        private String title;
        private String badgeGraphic;
    }
}
