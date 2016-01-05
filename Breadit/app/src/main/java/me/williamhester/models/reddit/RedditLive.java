package me.williamhester.models.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/30/14.
 */
public class RedditLive implements Parcelable {

    @SerializedName("name")
    private String mName;
    @SerializedName("description_html")
    private String mDescriptionHtml;
    @SerializedName("created")
    private long mCreated;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("created_utc")
    private long mCreatedUtc;
    @SerializedName("websocket_url")
    private String mWebsocketUrl;
    @SerializedName("state")
    private String mState;
    @SerializedName("viewer_count_fuzzed")
    private boolean mViewerCountFuzzed;
    @SerializedName("id")
    private String mId;
    @SerializedName("viewer_count")
    private int mViewerCount;
    @SerializedName("description")
    private String mDescription;

    public RedditLive() {

    }
    
    public String getName() {
        return mName;
    }

    public String getDescriptionHtml() {
        return mDescriptionHtml;
    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getWebsocketUrl() {
        return mWebsocketUrl;
    }

    public String getState() {
        return mState;
    }

    public int getViewerCount() {
        return mViewerCount;
    }

    public String getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public boolean ismViewerCountFuzzed() {
        return mViewerCountFuzzed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mDescriptionHtml);
        dest.writeLong(this.mCreated);
        dest.writeString(this.mTitle);
        dest.writeLong(this.mCreatedUtc);
        dest.writeString(this.mWebsocketUrl);
        dest.writeString(this.mState);
        dest.writeByte(mViewerCountFuzzed ? (byte) 1 : (byte) 0);
        dest.writeString(this.mId);
        dest.writeInt(this.mViewerCount);
        dest.writeString(this.mDescription);
    }

    private RedditLive(Parcel in) {
        this.mName = in.readString();
        this.mDescriptionHtml = in.readString();
        this.mCreated = in.readLong();
        this.mTitle = in.readString();
        this.mCreatedUtc = in.readLong();
        this.mWebsocketUrl = in.readString();
        this.mState = in.readString();
        this.mViewerCountFuzzed = in.readByte() != 0;
        this.mId = in.readString();
        this.mViewerCount = in.readInt();
        this.mDescription = in.readString();
    }

    public static final Creator<RedditLive> CREATOR = new Creator<RedditLive>() {
        public RedditLive createFromParcel(Parcel source) {
            return new RedditLive(source);
        }

        public RedditLive[] newArray(int size) {
            return new RedditLive[size];
        }
    };
}
