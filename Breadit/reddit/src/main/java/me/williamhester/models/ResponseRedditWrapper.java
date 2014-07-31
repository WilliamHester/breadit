package me.williamhester.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by william on 7/30/14.
 */
public class ResponseRedditWrapper <T> implements Serializable {

    private static final long serialVersionUID = 5856868935847897955L;
    @SerializedName("kind")
    private String mKind;
    @SerializedName("data")
    private T mData;

    public String getKind() {
        return mKind;
    }

    public T getData() {
        return mData;
    }

}
