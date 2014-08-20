package me.williamhester.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by william on 7/30/14.
 */
public class RedditLive implements Serializable {
    private static final long serialVersionUID = 4655722592749339956L;
    
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
}
