package me.williamhester.areddit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: William
 * Date: 1/3/14
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Subreddit extends Thing {

    public Subreddit(JSONObject data) {
        super(data);
    }

//    public HTML getSubmitTextHtml() {
//        return (HTML) ((JSONObject)_data.get("data")).get("submit_text_html").toString();
//    }

    public boolean userIsBanned() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("user_is_banned").toString());
    }

    public String getDisplayName() {
        return  ((JSONObject)_data.get("data")).get("display_name").toString();
    }

    public String getHeaderImgUrl() {
        return  ((JSONObject)_data.get("data")).get("header_img").toString();
    }

//    public HTML getDescripionHtml() {
//        return new HTML()((JSONObject)_data.get("data")).get("description_html").toString();
//    }

    public String getTitle() {
        return  ((JSONObject)_data.get("data")).get("title").toString();
    }

    public boolean isNsfw() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("over18").toString());
    }

    public boolean userIsModerator() {
        return Boolean.parseBoolean(((JSONObject)_data.get("data")).get("user_is_moderator").toString());
    }

    public String getHeaderTitle() {
        return  ((JSONObject)_data.get("data")).get("header_title").toString();
    }

    public String getDescription() {
        return  ((JSONObject)_data.get("data")).get("description").toString();
    }

    public String getSubmitLinkLabel() {
        return  ((JSONObject)_data.get("data")).get("submit_link_label").toString();
    }

    public boolean isPublicTraffic() {
        return Boolean.parseBoolean( ((JSONObject)_data.get("data")).get("public_traffic").toString());
    }

    //Todo add a header_size getter. It is formatted like [160, 64]

    public long getSubscriberCount() {
        return Long.parseLong( ((JSONObject)_data.get("data")).get("subscribers").toString());
    }

    public String getSubmitTextLabel() {
        return  ((JSONObject)_data.get("data")).get("submit_text_label").toString();
    }

    public String getUrl() {
        return  ((JSONObject)_data.get("data")).get("url").toString();
    }

    //Todo probably should not be of type String, but I'm not sure if it's a float or what
    public String getCreated() {
        return  ((JSONObject)_data.get("data")).get("created").toString();
    }

    //Todo probably should not be of type String, but I'm not sure if it's a float or what
    public String getCreatedUtc() {
        return  ((JSONObject)_data.get("data")).get("created_utc").toString();
    }

    public boolean userIsContributor() {
        return Boolean.parseBoolean( ((JSONObject)_data.get("data")).get("user_is_contributor").toString());
    }

    public String getPublicDescription() {
        return  ((JSONObject)_data.get("data")).get("public_description").toString();
    }

    public long getCommentScoreHideMins() {
        return Long.parseLong( ((JSONObject)_data.get("data")).get("comment_score_hide_mins").toString());
    }

    public String getSubredditType() {
        return  ((JSONObject)_data.get("data")).get("subreddit_type").toString();
    }

    public String getSubmissionType() {
        return  ((JSONObject)_data.get("data")).get("submission_type").toString();
    }

    public boolean userIsSubscriber() {
        return Boolean.parseBoolean( ((JSONObject)_data.get("data")).get("user_is_subscriber").toString());
    }

    /**
     * This loads in all of the subreddits for a user, but due to the fact that Reddit limits the number that can be
     * loaded at one time to 25, it must be iterative.
     *
     * @param user              the user whose subreddits will be retrieved
     *
     * @return                  returns a list of Subreddit
     *
     * @throws java.io.IOException      if connection fails
     * @throws org.json.simple.parser.ParseException   if JSON parsing fails
     */

    public static List<Subreddit> getMySubreddits(User user) throws IOException, ParseException,
            UserNotConnectedException {
        if (!user.isConnected()) {
            throw new UserNotConnectedException("The user is not connected to reddit. You must invoke user.connect()"
                    + " before calling this method");
        }

        ArrayList<Subreddit> subreddits = new ArrayList<Subreddit>();

        JSONObject object = (JSONObject) Utils.get("", new URL(
                "http://www.reddit.com/subreddits/mine/subscriber.json"), user.getCookie());
        JSONObject data = (JSONObject) object.get("data");
        JSONArray array = (JSONArray)data.get("children");

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonData = (JSONObject) array.get(i);
            subreddits.add(new Subreddit(jsonData));
        }

        String after = (String) data.get("after");

        while (after != null) {

            object = (JSONObject) Utils.get("", new URL(
                    "http://www.reddit.com/subreddits/mine/subscriber.json?after=" + after), user.getCookie());
            data = (JSONObject) object.get("data");
            array = (JSONArray) data.get("children");

            for (int i = 0; i < array.size(); i++) {
                JSONObject jsonData = (JSONObject) array.get(i);
                subreddits.add(new Subreddit(jsonData));
            }

            after = (String) data.get("after");
            System.out.println("after = " + after);
        }

        return subreddits;
    }

}
