package me.williamhester.reddit;

import android.content.Context;
import android.content.SharedPreferences;

import me.williamhester.reddit.network.RedditApi;

/**
 * This class contains the settings for the application and should be loaded during the creation of
 * the application.
 *
 * Created by william on 10/26/14.
 */
public class SettingsManager {

  private static boolean mShowThumbnails;
  private static boolean mLowBandwidthMode;

  private static String mCommentSort;
  private static int mNotificationInterval;

  private SettingsManager() {
  }

  public static void init(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    mCommentSort = prefs.getString("pref_default_comment_sort", RedditApi.COMMENT_SORT_BEST);
    mShowThumbnails = prefs.getBoolean("pref_show_thumbnails", true);
    mLowBandwidthMode = prefs.getBoolean("pref_low_bandwidth_mode", false);
    mNotificationInterval = Integer.parseInt(prefs.getString("pref_notification_interval", "60"));
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

  public static int getNotificationInterval() {
    return mNotificationInterval;
  }

}
