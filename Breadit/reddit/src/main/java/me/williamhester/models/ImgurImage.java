package me.williamhester.models;

import java.io.Serializable;

/**
 * Created by william on 6/22/14.
 */
public class ImgurImage implements Serializable {

    private static final long serialVersionUID = 5186779065758496400L;

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
    private long size;
    private long views;
    private long bandwidth;

    public String getId() {
        return id;
    }

    public boolean isAnimated() {
        return animated;
    }

    public String getUrl() {
        return link;
    }

    public String getHugeThumbnail() {
        return link.replace(".", "h.");
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof ImgurImage && ((ImgurImage) o).id.equals(id);
    }

}
