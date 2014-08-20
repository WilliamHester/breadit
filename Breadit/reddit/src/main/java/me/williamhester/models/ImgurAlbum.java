package me.williamhester.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by william on 6/22/14.
 */
public class ImgurAlbum implements Serializable {

    private static final long serialVersionUID = 2086261516664607998L;

    private String id;
    private String title;
    private String description;
    private String cover;
    private String account_url;
    private String privacy;
    private String layout;
    private String link;
    private String deletehash;
    private int datetime;
    private int cover_width;
    private int cover_height;
    private int ivews;
    private int images_count;
    private List<ImgurImage> images;

    public String getTitle() {
        return title;
    }

    public List<ImgurImage> getImages() {
        return images;
    }

}
