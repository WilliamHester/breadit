package me.williamhester.models.bulletin;

import android.os.Parcelable;

/**
 * Created by william on 7/4/15.
 */
public interface User extends Parcelable {
    String getUsername();
    String getCreatedDate();
    int getCommentPoints();
    int getSubmissionPoints();
}
