package me.williamhester.models.voat;

import android.os.Parcel;

import me.williamhester.models.bulletin.Bulletin;

/**
 * Created by william on 7/4/15.
 */
public class VoatSubverse implements Bulletin {
    private int subscriberCount;
    private boolean ratedAdult;
    private String name;
    private String title;
    private String description;
    private String creationDate;
    private String sidebar;
    private String type;

    public VoatSubverse() { }

    @Override
    public int getSubscriberCount() {
        return subscriberCount;
    }

    @Override
    public boolean isNsfw() {
        return ratedAdult;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getCreatedDate() {
        return creationDate;
    }

    @Override
    public String getSidebar() {
        return sidebar;
    }

    @Override
    public String getType() {
        return type;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.subscriberCount);
        dest.writeByte(ratedAdult ? (byte) 1 : (byte) 0);
        dest.writeString(this.name);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.creationDate);
        dest.writeString(this.sidebar);
        dest.writeString(this.type);
    }

    protected VoatSubverse(Parcel in) {
        this.subscriberCount = in.readInt();
        this.ratedAdult = in.readByte() != 0;
        this.name = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.creationDate = in.readString();
        this.sidebar = in.readString();
        this.type = in.readString();
    }

    public static final Creator<VoatSubverse> CREATOR = new Creator<VoatSubverse>() {
        public VoatSubverse createFromParcel(Parcel source) {
            return new VoatSubverse(source);
        }

        public VoatSubverse[] newArray(int size) {
            return new VoatSubverse[size];
        }
    };
}
