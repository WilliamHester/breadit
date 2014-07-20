package me.williamhester.tools;

/**
 * Created by william on 7/19/14.
 */
public class UrlParser {

    public static final int NOT_SPECIAL = 0;
    public static final int IMGUR_IMAGE = 1;
    public static final int IMGUR_ALBUM = 2;
    public static final int IMGUR_GALLERY = 3;
    public static final int YOUTUBE = 4;

    private String mUrl;
    private String mId;
    private int mType;
    private boolean mIsYoutube;
    private boolean mIsImgur;

    public UrlParser(String url) {
        mUrl = url;
        mIsImgur = mUrl.toLowerCase().contains("imgur");
        mIsYoutube = mUrl.toLowerCase().contains("youtu.be") || mUrl.toLowerCase().contains("youtube.com");
        if (mIsImgur) {
            generateImgurDetails();
        } else if (mIsYoutube) {
            generateYoutubeDetails();
        } else {
            mType = NOT_SPECIAL;
        }
    }

    public boolean isYoutubeLink() {
        return mIsYoutube;
    }

    public boolean isImgurLink() {
        return mIsImgur;
    }

    private void generateImgurDetails() {
        int end = mUrl.indexOf('.', mUrl.indexOf(".com") + 4);
        end = end != -1 ? end : mUrl.length();
        int start = end - 1;
        while (mUrl.charAt(start) != '/') {
            start--;
        }
        mId = mUrl.substring(start + 1, end);
        while (mUrl.charAt(start) == '/') {
            start--;
        }
        char c = mUrl.charAt(start);
        switch (c) {
            case 'm': // imgur.com
                mType = IMGUR_IMAGE;
                break;
            case 'a': // imgur.com/a/
                mType = IMGUR_ALBUM;
                break;
            case 'y': // imgur.com/gallery/
                mType = IMGUR_GALLERY;
                break;
        }
    }

    private void generateYoutubeDetails() {
        int amp = mUrl.indexOf('&');
        int slash = mUrl.indexOf('/');
        int end = mUrl.length();
        if (amp != -1) {
            end = amp - 1;
        } else if (slash + 1 == end) {
            end = slash;
        }
        int start = end - 1;
        char c = mUrl.charAt(start);
        while (c != '=' && c != '/') {
            start--;
            c = mUrl.charAt(start);
        }
        start += 1;
        mId = mUrl.substring(start, end);
        mType = YOUTUBE;
    }

    public String getLinkId() {
        return mId;
    }

    public int getType() {
        return mType;
    }
}
