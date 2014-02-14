package me.williamhester.areddit;

import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

/**
 *
 * This class represents a reddit comment
 *
 * @author <a href="https://github.com/jasonsimpson">Jason Simpson</a>
 * 
 */
public class Comment extends Thing {

    Comment mParent;
    List<Comment> mReplies;

    public Comment(JSONObject jsonObj) {
        this(jsonObj, null);
    }

    public Comment(JSONObject jsonObj, Comment parent) {
        super(jsonObj);
        mParent = parent;
        if (!getKind().equals("more")) {
            mReplies = getReplies();
        }
    }

    public String getBody() { 
        return ((JSONObject)_data.get("data")).get("body").toString();
    }

    public long getUpVotes() { 
        return Long.parseLong(((JSONObject)_data.get("data")).get("ups").toString());
    }

    public long getDownVotes() { 
        return Long.parseLong(((JSONObject) _data.get("data")).get("downs").toString());
    }

    public long getScore() { 
        return Long.parseLong((((JSONObject)_data.get("data")).get("score")).toString());
    }

    public String getAuthor() { 
        return ((JSONObject)_data.get("data")).get("author").toString();
    }

    public boolean hasReplies() {
        return mReplies != null;
    }

    public long getCreated() {
//        return Long.parseLong(new Scanner((((JSONObject)_data.get("data")).get("created").toString())).useDelimiter("\\.").next());
        return Long.parseLong(new DecimalFormat("###########").format(Double.parseDouble(((JSONObject) _data.get("data")).get("created").toString())));
    }

    public long getCreatedUtc() {
//        return Long.parseLong(new Scanner((((JSONObject)_data.get("data")).get("created_utc").toString())).useDelimiter("\\.").next());
        return Long.parseLong(new DecimalFormat("###########").format(Double.parseDouble(((JSONObject)_data.get("data")).get("created_utc").toString())));

    }

    /**
     * Get the replies to this comment.
     */
    public List<Comment> getReplies() {
        List<Comment> ret = new ArrayList<>();
        
        JSONObject data = (JSONObject)_data.get("data");
        JSONObject replies = (JSONObject)data.get("replies");
        JSONObject replyData = (JSONObject)replies.get("data");
        JSONArray children = (JSONArray)replyData.get("children");

        for (int i = 0; i < children.size(); i++) {
            JSONObject jsonData = (JSONObject)children.get(i);
            Comment comment = new Comment(jsonData);

            if(!comment.getKind().equals("more")) {
                ret.add(new Comment(jsonData, this));
            }
        }
        return ret;
    }

    /**
     * This function returns a list of comments
     *
     * @param articleId         The id of the link/article/submission
     * @param user              The user
     *
     * @return A list containing Comments
     *
     * @throws java.io.IOException      If connection fails
     * @throws org.json.simple.parser.ParseException   If JSON parsing fails
     */
    public static List<Comment> getComments(String articleId, User user, String after)
            throws IOException, ParseException {

        ArrayList<Comment> comments = new ArrayList<Comment>();

        String urlString = "http://www.reddit.com/comments/" + articleId + "/.json";
        String cookie = user == null ? null : user.getCookie();

        JSONObject rootObject = (JSONObject) Utilities.get("", urlString, cookie);
        if (rootObject == null) {
            Log.e("BreaditDebug", "rootObject == null");
        }
        JSONObject data = (JSONObject) rootObject.get("data");
        if (data == null) {
            Log.e("BreaditDebug", "data == null");
        }
        JSONArray array = (JSONArray) data.get("children");
        if (array == null) {
            Log.e("BreaditDebug", "array == null");
        }

        if(array.size() > 0) {
            JSONObject replies = (JSONObject)array.get(1);
            JSONArray children = (JSONArray)((JSONObject)replies.get("data")).get("children");

            for (int i = 0; i < children.size(); i++) {
                JSONObject jsonData = (JSONObject)children.get(i);
                comments.add(new Comment(jsonData));
            }
        }

        return comments;
    }
}
