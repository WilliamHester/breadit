package me.williamhester.models;

import android.util.Log;
import android.widget.ListView;

/**
 * Created by William on 1/4/14.
 */
public class SubmissionsListViewHelper {

    public static final int HOT = 0;
    public static final int NEW = 1;
    public static final int RISING = 2;
    public static final int CONTROVERSIAL = 3;
    public static final int TOP = 4;

    public static final int HOUR = 0;
    public static final int DAY = 1;
    public static final int WEEK = 2;
    public static final int MONTH = 3;
    public static final int YEAR = 4;
    public static final int ALL = 5;

    private String mUrl;
    private Account mAccount;

    /**
     * Creates a list of submissions with the specified attributes. This should be used with
     * RetrieveSubmissionsTask in order to retrieve the submissions.
     *
     * @param subredditName The subreddit's name; null if frontpage
     * @param sortType A constant for the type of the sorting of the top
     * @param typeArgs If the sortType is TOP or CONTROVERSIAL, then this must be specified. Specifies how long
     *                              since the current date back the subreddits should go.
     * @param before the before= argument
     * @param after the after= argument
     * @param account the account. If the account is not connected, it will throw an exception.
     *
     * @return The list containing submissions
     */
    public SubmissionsListViewHelper(String subredditName, int sortType, int typeArgs, String before,
                                     String after, Account account) {

        String append;
        if (subredditName == null) {
            append = "";
        } else if (subredditName.equals("")) {
            append = subredditName;
        } else {
            append = "r/" + subredditName + "/";
        }

        String urlString = "http://www.reddit.com/" + append;

        switch (sortType) {
            case HOT:
                urlString += ".json?";
                break;
            case NEW:
                urlString += "new/.json?";
                break;
            case RISING:
                urlString += "rising/.json?";
                break;
            case CONTROVERSIAL:
                urlString += "controversial/.json?";
                break;
            case TOP:
                urlString += "top/.json?";
                break;
        }

        if (sortType == CONTROVERSIAL || sortType == TOP) {
            switch (typeArgs) {
                case HOUR:
                    urlString += "t=hour&";
                    break;
                case DAY:
                    urlString += "t=day&";
                    break;
                case WEEK:
                    urlString += "t=week&";
                    break;
                case MONTH:
                    urlString += "t=month&";
                    break;
                case YEAR:
                    urlString += "t=year&";
                    break;
                case ALL:
                    urlString += "t=all&";
                    break;
                default:
                    break;
            }
        }

        if (after != null) {
            urlString += "after=" + after;
        } else if (before != null) {
            urlString += "before=" + before;
        }

        Log.i("Submission", urlString);

        mUrl = urlString;
        mAccount = account;
    }

    public String getUrl() {
        return mUrl;
    }

    public Account getAccount() {
        return mAccount;
    }
}