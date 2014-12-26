package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/14.
 */
public class LiveResponse implements Parcelable {
    @SerializedName("type")
    private String mType;
    @SerializedName("payload")
    private String mPayload;

    public LiveResponse() { }

    public String getType() {
        return mType;
    }

    public String getPayload() {
        return mPayload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mType);
        dest.writeString(this.mPayload);
    }

    private LiveResponse(Parcel in) {
        this.mType = in.readString();
        this.mPayload = in.readString();
    }

    public static final Creator<LiveResponse> CREATOR = new Creator<LiveResponse>() {
        public LiveResponse createFromParcel(Parcel source) {
            return new LiveResponse(source);
        }

        public LiveResponse[] newArray(int size) {
            return new LiveResponse[size];
        }
    };
}
