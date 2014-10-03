package me.williamhester.tools;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by william on 7/19/14.
 */
public class Url implements Parcelable {

    public static final int NOT_SPECIAL = 0;
    public static final int IMGUR_IMAGE = 1;
    public static final int IMGUR_ALBUM = 2;
    public static final int IMGUR_GALLERY = 3;
    public static final int YOUTUBE = 4;
    public static final int NORMAL_IMAGE = 6;
    public static final int SUBMISSION = 7;
    public static final int SUBREDDIT = 8;
    public static final int USER = 9;
    public static final int REDDIT_LIVE = 10;
    public static final int GFYCAT_LINK = 11;
    public static final int GIF = 12;

    private Uri mUri;
    private String mUrl;
    private String mId;
    private int mType;

    public Url(String url) {
        mUrl = url;
        if (mUrl.charAt(0) == '/') {
            if (mUrl.charAt(1) == 'u') { // go to a user
                mId = mUrl.substring(mUrl.indexOf("/u/") + 3);
                mType = USER;
            } else if (mUrl.charAt(1) == 'r') { // go to a subreddit
                mId = url.substring(3);
                mType = SUBREDDIT;
            }
        } else {
            mUri = Uri.parse(url);
            if (mUri.getHost().contains("reddit.com")) {
                generateRedditDetails();
            } else if (mUri.getHost().contains("imgur")) {
                generateImgurDetails();
            } else if (mUri.getHost().contains("youtu.be")
                    || mUri.getHost().contains("youtube.com")) {
                generateYoutubeDetails();
            } else if (isDirectImageLink()) {
                mType = NORMAL_IMAGE;
            } else if (isGif()) {
                mType = GIF;
            } else if (mUri.getHost().contains("livememe.com")) {
                mType = NORMAL_IMAGE;
                generateLiveMemeDetails();
            } else if (mUri.getHost().contains("imgflip.com")) {
                mType = NORMAL_IMAGE;
                generateImgFlipDetails();
            } else if (mUrl.contains("gfycat.com")) {
                mType = GFYCAT_LINK;
                generateGfycatDetails();
            } else {
                mType = NOT_SPECIAL;
            }
        }
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
        if (mUri.getHost().equals("youtu.be")) {
            mId = mUri.getPathSegments().get(0);
        } else {
            mId = mUri.getQueryParameter("v");
        }
        mType = YOUTUBE;
        mUrl = "http://img.youtube.com/vi/" + mId + "/maxresdefault.jpg";
    }

    private void generateLiveMemeDetails() {
        mUrl += ".jpg";
    }

    private void generateImgFlipDetails() {
        int start = mUrl.length() - 2;
        while (mUrl.charAt(start - 1) != '/') {
            start--;
        }
        mId = mUrl.substring(start);
        mUrl = "http://i.imgflip.com/" + mId + ".jpg";
    }

    private boolean isDirectImageLink() {
        String lps = mUri.getLastPathSegment();
        String suffix = mUrl.substring(mUrl.indexOf(".", mUrl.indexOf(lps)) + 1);
        return suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg") || suffix.equalsIgnoreCase("bmp");
    }

    private boolean isGif() {
        String lps = mUri.getLastPathSegment();
        String suffix = mUrl.substring(mUrl.indexOf(".", mUrl.indexOf(lps)) + 1);
        return suffix.equalsIgnoreCase("gif");
    }

    private void generateRedditDetails() {
        int i = mUrl.indexOf("/", 17);
        if (i == -1 || i == mUrl.length()) { // definitely a subreddit or the frontpage
            int slashR = mUrl.indexOf("/r/");
            if (slashR != -1) {
                mId = mUrl.substring(slashR + 3, i == -1 ? mUrl.length() : i);
            } else {
                mId = "";
            }
            mType = SUBREDDIT;
        } else if (mUrl.toLowerCase().contains("/live/")) {
            mId = mUrl.substring(mUrl.indexOf("/live/") + 6);
            mType = REDDIT_LIVE;
        } else { // found a link to another post
            mId = mUrl.substring(mUrl.indexOf("/r/"));
            mType = SUBMISSION;
        }
    }

    private void generateGfycatDetails() {
        int start = mUrl.toLowerCase().indexOf("gfycat.com/");
        if (start < 0) {
            mType = NOT_SPECIAL;
            return;
        }
        start += 11;
        int end = mUrl.indexOf(".", start);
        if (end < 0) {
            end = mUrl.length();
        }
        mId = mUrl.substring(start, end);
        mUrl = "http://zippy.gfycat.com/" + mId + ".webm";
    }

    public String getLinkId() {
        return mId;
    }

    public int getType() {
        return mType;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUrl);
        dest.writeString(this.mId);
        dest.writeInt(this.mType);
    }

    private Url(Parcel in) {
        this.mUrl = in.readString();
        this.mId = in.readString();
        this.mType = in.readInt();
        mUri = Uri.parse(mUrl);
    }

    public static final Creator<Url> CREATOR = new Creator<Url>() {
        public Url createFromParcel(Parcel source) {
            return new Url(source);
        }

        public Url[] newArray(int size) {
            return new Url[size];
        }
    };
}
