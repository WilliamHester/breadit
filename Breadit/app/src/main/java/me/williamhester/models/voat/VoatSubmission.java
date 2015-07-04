package me.williamhester.models.voat;

import me.williamhester.models.bulletin.Submission;

/**
 * Created by william on 7/4/15.
 */
public class VoatSubmission extends VoatContent implements Submission {
    public static final int SELF_POST = 1;
    public static final int LINK_POST = 2;

    private int commentCount;
    private int views;
    private int type;
    private String thumbnail;
    private String title;
    private String url;

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public int getCommentCount() {
        return commentCount;
    }

    @Override
    public boolean isSelf() {
        return type == SELF_POST;
    }

    public int getViews() {
        return views;
    }
}
