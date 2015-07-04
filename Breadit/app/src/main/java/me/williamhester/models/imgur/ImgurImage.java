package me.williamhester.models.imgur;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by william on 6/22/14.
 */
public class ImgurImage implements Parcelable {

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

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getHugeThumbnail() {
        return "http://i.imgur.com/" + id + "h.png";
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof ImgurImage && ((ImgurImage) o).id.equals(id);
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
        dest.writeString(this.type);
        dest.writeString(this.deletehash);
        dest.writeString(this.section);
        dest.writeString(this.link);
        dest.writeByte(animated ? (byte) 1 : (byte) 0);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeLong(this.size);
        dest.writeLong(this.views);
        dest.writeLong(this.bandwidth);
    }

    public ImgurImage() {
    }

    private ImgurImage(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.type = in.readString();
        this.deletehash = in.readString();
        this.section = in.readString();
        this.link = in.readString();
        this.animated = in.readByte() != 0;
        this.width = in.readInt();
        this.height = in.readInt();
        this.size = in.readLong();
        this.views = in.readLong();
        this.bandwidth = in.readLong();
    }

    public static final Creator<ImgurImage> CREATOR = new Creator<ImgurImage>() {
        public ImgurImage createFromParcel(Parcel source) {
            return new ImgurImage(source);
        }

        public ImgurImage[] newArray(int size) {
            return new ImgurImage[size];
        }
    };
}
