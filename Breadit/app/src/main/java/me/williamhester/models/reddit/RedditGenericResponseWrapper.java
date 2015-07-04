package me.williamhester.models.reddit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 12/20/14.
 */
public class RedditGenericResponseWrapper<T> {

    @SerializedName("kind")
    private String mKind;
    @SerializedName("data")
    private T mData;

    public T getData() {
        return mData;
    }

}
