package me.williamhester.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 12/20/14.
 */
public class GenericResponseRedditWrapper <T> {

    @SerializedName("kind")
    private String mKind;
    @SerializedName("data")
    private T mData;

    public T getData() {
        return mData;
    }

}
