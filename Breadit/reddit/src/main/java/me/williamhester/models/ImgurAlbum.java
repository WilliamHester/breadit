package me.williamhester.models;

import java.util.List;

/**
 * Created by william on 6/22/14.
 */
public class ImgurAlbum {

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
    private List<ResponseImgurImage> images;

}
