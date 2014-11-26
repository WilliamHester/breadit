package me.williamhester;

import android.content.Context;
import android.content.SharedPreferences;

import me.williamhester.network.RedditApi;

/**
 * This class contains the settings for the application and should be loaded during the creation
 * of the application.
 *
 * Created by william on 10/26/14.
 */
public class SettingsManager {

    private static boolean mShowThumbnails;
    private static boolean mLowBandwidthMode;

    private static String mCommentSort;
    private static SharedPreferences mPrefs;

    private SettingsManager() { }

    public static void init(Context context) {
        mPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        mCommentSort = mPrefs.getString("pref_default_comment_sort", RedditApi.COMMENT_SORT_BEST);
        mShowThumbnails = mPrefs.getBoolean("pref_show_thumbnails", true);
        mLowBandwidthMode = mPrefs.getBoolean("pref_low_bandwidth", false);
    }

    public static void setDefaultCommentSort(String string) {
        mCommentSort = string;
    }

    public static String getDefaultCommentSort() {
        return mCommentSort;
    }

    public static boolean isShowingThumbnails() {
        return mShowThumbnails;
    }

    public static boolean isLowBandwidth() {
        return mLowBandwidthMode;
    }

}
