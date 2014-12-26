package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 6/22/14.
 */
public class ImgurAlbum implements Serializable, Parcelable {

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
    private int views;
    private int images_count;
    private ArrayList<ImgurImage> images;

    private int mLastViewePosition = 0;

    public String getTitle() {
        return title;
    }

    public List<ImgurImage> getImages() {
        return images;
    }

    public int getLastViewedPosition() {
        return mLastViewePosition;
    }

    public void setLastViewedPosition(int position) {
        mLastViewePosition = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.cover);
        dest.writeString(this.account_url);
        dest.writeString(this.privacy);
        dest.writeString(this.layout);
        dest.writeString(this.link);
        dest.writeString(this.deletehash);
        dest.writeInt(this.datetime);
        dest.writeInt(this.cover_width);
        dest.writeInt(this.cover_height);
        dest.writeInt(this.views);
        dest.writeInt(this.images_count);
        dest.writeInt(this.mLastViewePosition);
        dest.writeList(this.images);
    }

    private ImgurAlbum(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.cover = in.readString();
        this.account_url = in.readString();
        this.privacy = in.readString();
        this.layout = in.readString();
        this.link = in.readString();
        this.deletehash = in.readString();
        this.datetime = in.readInt();
        this.cover_width = in.readInt();
        this.cover_height = in.readInt();
        this.views = in.readInt();
        this.images_count = in.readInt();
        this.mLastViewePosition = in.readInt();
        this.images = new ArrayList<>();
        in.readList(this.images, ImgurImage.class.getClassLoader());
    }

    public static final Parcelable.Creator<ImgurAlbum> CREATOR = new Creator<ImgurAlbum>() {
        public ImgurAlbum createFromParcel(Parcel source) {
            return new ImgurAlbum(source);
        }

        public ImgurAlbum[] newArray(int size) {
            return new ImgurAlbum[size];
        }
    };
}
