package me.williamhester.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by william on 12/20/14.
 */
public class GenericListing <T> {

    @SerializedName("modhash")
    private String mModhash;
    @SerializedName("children")
    private ArrayList<GenericResponseRedditWrapper<T>> mChildren;
    @SerializedName("after")
    private String mAfter;
    @SerializedName("before")
    private String mBefore;

    public ArrayList<GenericResponseRedditWrapper<T>> getChildren() {
        return mChildren;
    }
}
