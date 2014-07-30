package me.williamhester.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by william on 7/29/14.
 */
public class LiveResponse implements Serializable {
    private static final long serialVersionUID = -5445387342462255976L;

    @SerializedName("type")
    private String mType;
    @SerializedName("payload")
    private String mPayload;

    public String getType() {
        return mType;
    }

    public String getPayload() {
        return mPayload;
    }
}
