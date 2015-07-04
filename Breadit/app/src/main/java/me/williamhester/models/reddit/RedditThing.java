package me.williamhester.models.reddit;

import android.os.Parcelable;

/**
 * Created by william on 10/19/14.
 */
public interface RedditThing extends Parcelable {
    String getAuthor();
    String getId();
}
