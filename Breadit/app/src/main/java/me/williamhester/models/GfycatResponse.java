package me.williamhester.models;

/**
 * Created by william on 10/3/14.
 */
public class GfycatResponse {

    private GfyItem gfyItem;

    public String getGfyUrl() {
        return gfyItem.webmUrl;
    }

    public static class GfyItem {
        private long gfyNumber;
        private long gifSize;
        private long createDate;
        private long mp4Size;
        private long webmSize;
        private int width;
        private int height;
        private int frameRate;
        private int numFrames;
        private int views;
        private String gfyId;
        private String gfyName;
        private String userName;
        private String mp4Url;
        private String webmUrl;
        private String gifUrl;
        private String title;
        private String extraLemmas;
        private String md5;
        private String tags;
        private String nsfw;
        private String sar;
        private String url;
        private String source;
        private String dynamo;
        private String subreddit;
        private String redditId;
        private String redditIdText;
        private String uploadGifName;
    }
}
