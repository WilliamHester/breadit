package me.williamhester.areddit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This class represents a reddit Submission or "Article"
 *
 * @author <a href="https://github.com/jasonsimpson">Jason Simpson</a>
 * 
 */
public class Submission extends Thing {

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

    public static final int FRONTPAGE = 0;

    public Submission(JSONObject data) {
        super(data);
    }

    public String toString() {
        String thing = super.toString();
        return thing +
                "   Submission: "   + getTitle()        + "\n" +
                "       author: "   + getAuthor()       + "\n" +
                "       url:    "   + getUrl()          + "\n" +
                "       score:  "   + getScore()        + "\n" +
                "       up:     "   + getUpVotes()      + "\n" +
                "       down:   "   + getDownVotes()    + "\n";
                // Utils.getJSONDebugString(_data);

    }

    public String getUrl() { 
        return ((JSONObject)_data.get("data")).get("url").toString();
    }

    public long getUpVotes() { 
        return Long.parseLong(((JSONObject)_data.get("data")).get("ups").toString());
    }

    public long getDownVotes() { 
        return Long.parseLong(((JSONObject)_data.get("data")).get("downs").toString());
    }

    public long getScore() { 
        return Long.parseLong(((JSONObject)_data.get("data")).get("score").toString());
    }

    public String getAuthor() { 
        return ((JSONObject)_data.get("data")).get("author").toString();
    }

    public String getTitle() { 
        return ((JSONObject)_data.get("data")).get("title").toString();
    }

    public String getDomain() {
        return ((JSONObject)_data.get("data")).get("domain").toString();
    }

    public String getBannedBy() {
        return ((JSONObject)_data.get("data")).get("banned_by").toString();
    }

    public String getMediaEmbed() {
        return ((JSONObject)_data.get("data")).get("media_embed").toString();
    }

    public String getPermalink() {
        return ((JSONObject) _data.get("data")).get("permalink").toString();
    }

    public String getSubredditName() {
        return ((JSONObject)_data.get("data")).get("subreddit").toString();
    }

    public String getSelfTextHtml() {
        return (String)((JSONObject)_data.get("data")).get("selftext_html");
    }

    public String getSelfText() {
        return (String)((JSONObject)_data.get("data")).get("selftext");
    }

    public String getLikes() {
        return ((JSONObject)_data.get("data")).get("likes").toString();
    }

    public String getSecureMedia() {
        return ((JSONObject)_data.get("data")).get("secure_media").toString();
    }

    public String getLinkFlairText() {
        return (String)((JSONObject)_data.get("data")).get("link_flair_text");
    }

    public String getSecureMediaEmbed() {
        return ((JSONObject)_data.get("data")).get("secure_media_embed").toString();
    }

    public String getMedia() {
        return ((JSONObject)_data.get("data")).get("media").toString();
    }

    public String getApprovedBy() {
        return ((JSONObject)_data.get("data")).get("approved_by").toString();
    }

    public boolean isNsfw() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("over_18").toString());
    }

    public boolean isHidden() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("hidden").toString());
    }

    public String getThumbnailUrl() {
        return ((JSONObject)_data.get("data")).get("thumbnail").toString();
    }

    public String getSubredditId() {
        return ((JSONObject)_data.get("data")).get("subreddit_id").toString();
    }

    public double getEdited() {
        return Double.parseDouble(((JSONObject)_data.get("data")).get("edited").toString());
    }

    public boolean isSaved() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("saved").toString());
    }

    public boolean isSelf() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("is_self").toString());
    }

    public int getNumberOfComments() {
        return Integer.parseInt(((JSONObject)_data.get("data")).get("num_comments").toString());
    }

    public long getCreated() {
//        return Long.parseLong(new Scanner((((JSONObject)_data.get("data")).get("created").toString())).useDelimiter("\\.").next());
        return Long.parseLong(new DecimalFormat("###########").format(Double.parseDouble(((JSONObject)_data.get("data")).get("created").toString())));
    }

    public long getCreatedUtc() {
//        return Long.parseLong(new Scanner((((JSONObject)_data.get("data")).get("created_utc").toString())).useDelimiter("\\.").next());
        return Long.parseLong(new DecimalFormat("###########").format(Double.parseDouble(((JSONObject)_data.get("data")).get("created_utc").toString())));

    }

    /**
     * This function returns a list containing the submissions on a given
     * subreddit and page.
     *
     * @param subredditName     The subreddit's name; null if frontpage
     * @param sortType          A constant for the type of the sorting of the top
     * @param typeArgs          If the sortType is TOP or CONTROVERSIAL, then this must be specified. Specifies how long
     *                              since the current date back the subreddits should go.
     * @param before            The before= argument
     * @param after             The after= argument
     * @param user              The user. If the user is not connected, it will throw an exception.
     *
     * @return The list containing submissions
     *
     * @throws java.io.IOException      If connection fails
     * @throws org.json.simple.parser.ParseException   If JSON parsing fails
     */
    public static List<Submission> getSubmissions(String subredditName,
                                                  int sortType,
                                                  int typeArgs,
                                                  String before,
                                                  String after,
                                                  User user)
            throws IOException, ParseException {

        String append;
        if (subredditName == null) {
            append = "";
        } else if (subredditName.equals("")) {
            append = subredditName;
        } else {
            append = "r/" + subredditName;
        }

        ArrayList<Submission> submissions = new ArrayList<Submission>();
        URL url;
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
                default: break;
            }
        }

        if (after != null) {
            urlString += "after=" + after;
        } else if (before != null) {
            urlString += "before=" + before;
        }

        url = new URL(urlString);

        JSONObject object = (JSONObject) Utils.get("", url, user.getCookie());
        JSONObject data = (JSONObject) object.get("data");
        JSONArray array = (JSONArray)data.get("children");

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonData = (JSONObject)array.get(i);
            submissions.add(new Submission(jsonData));
        }

        return submissions;
    }

}
