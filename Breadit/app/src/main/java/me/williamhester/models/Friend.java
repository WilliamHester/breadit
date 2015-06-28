package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 1/8/15.
 */
public class Friend implements Parcelable {

    @SerializedName("date")
    private long mDate;
    @SerializedName("name")
    private String mName;
    @SerializedName("id")
    private String mId;

    public Friend() { }

    public long getDate() {
        return mDate;
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mDate);
        dest.writeString(this.mName);
        dest.writeString(this.mId);
    }

    private Friend(Parcel in) {
        this.mDate = in.readLong();
        this.mName = in.readString();
        this.mId = in.readString();
    }

    public static final Parcelable.Creator<Friend> CREATOR = new Parcelable.Creator<Friend>() {
        public Friend createFromParcel(Parcel source) {
            return new Friend(source);
        }

        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
