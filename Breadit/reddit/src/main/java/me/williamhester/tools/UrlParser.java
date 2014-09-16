package me.williamhester.tools;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by william on 7/19/14.
 */
public class UrlParser implements Parcelable {

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

    private String mUrl;
    private String mId;
    private int mType;

    public UrlParser(String url) {
        mUrl = url;
        if (mUrl.substring(0, 3).equals("/u/") || mUrl.contains("reddit.com/u/")) { // go to a user
            mId = mUrl.substring(mUrl.indexOf("/u/") + 3);
            mType = USER;
        } else if (mUrl.substring(0, 3).equals("/r/")) { // go to a subreddit
            mId = url.substring(3);
            mType = SUBREDDIT;
        } else if (mUrl.toLowerCase().contains("reddit.com")) {
            generateRedditDetails();
        } else if (mUrl.toLowerCase().contains("imgur")) {
            generateImgurDetails();
        } else if (mUrl.toLowerCase().contains("youtu.be")
                || mUrl.toLowerCase().contains("youtube.com")) {
            generateYoutubeDetails();
        } else if (isDirectImageLink()) {
            mType = NORMAL_IMAGE;
        } else if (mUrl.toLowerCase().contains("livememe.com")) {
            mType = NORMAL_IMAGE;
            generateLiveMemeDetails();
        } else if (mUrl.toLowerCase().contains("imgflip.com")) {
            mType = NORMAL_IMAGE;
            generateImgFlipDetails();
        } else {
            mType = NOT_SPECIAL;
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
        int start = mUrl.length() - 2;
        while (mUrl.charAt(start) != '.') {
            start--;
        }
        String suffix = mUrl.substring(start + 1);
        if (suffix.equalsIgnoreCase("png") || suffix.equalsIgnoreCase("jpg")
                || suffix.equalsIgnoreCase("jpeg") || suffix.equalsIgnoreCase("bmp")) {
            return true;
        }
        return false;
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

    private UrlParser(Parcel in) {
        this.mUrl = in.readString();
        this.mId = in.readString();
        this.mType = in.readInt();
    }

    public static final Creator<UrlParser> CREATOR = new Creator<UrlParser>() {
        public UrlParser createFromParcel(Parcel source) {
            return new UrlParser(source);
        }

        public UrlParser[] newArray(int size) {
            return new UrlParser[size];
        }
    };
}
