package me.williamhester.models;

import android.os.Parcelable;

/**
 * Created by william on 10/19/14.
 */
public interface Thing extends Parcelable {

    public String getAuthor();
    public String getName();
    public String getId();

}
