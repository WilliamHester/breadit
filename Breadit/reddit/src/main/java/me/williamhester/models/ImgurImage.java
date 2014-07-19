package me.williamhester.models;

/**
 * Created by william on 6/22/14.
 */
public class ImgurImage {

    private String id;
    private String title;
    private String description;
    private String type;
    private String deletehash;
    private String section;
    private String link;
    private boolean animated;
    private int width;
    private int height;
    private int size;
    private int views;
    private int bandwidth;

    public boolean isAnimated() {
        return animated;
    }

    public String getUrl() {
        return link;
    }

    public String getHugeThumbnail() {
        return link.replace(".", "h.");
    }

}
