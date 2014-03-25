package me.williamhester.areddit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
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

    public Comment(JsonObject jsonObj) {
        this(jsonObj, null);
    }

    public Comment(JsonObject jsonObj, Comment parent) {
        super(jsonObj);
        mParent = parent;
        if (!getKind().equals("more")) {
            mReplies = getReplies();
        }
    }

    public String getBody() { 
        return mData.get("data").getAsJsonObject().get("body").toString();
    }

    public long getUpVotes() { 
        return mData.get("data").getAsJsonObject().get("ups").getAsLong();
    }

    public long getDownVotes() { 
        return mData.get("data").getAsJsonObject().get("downs").getAsLong();
    }

    public long getScore() {
        return getUpVotes() - getDownVotes();
//        return mData.get("data").getAsJsonObject().get("score").getAsLong();
    }

    public String getAuthor() { 
        return mData.get("data").getAsJsonObject().get("author").toString();
    }

    public boolean hasReplies() {
        return mReplies != null;
    }

    public double getCreated() {
        return mData.get("data").getAsJsonObject().get("created").getAsDouble();
    }

    public double getCreatedUtc() {
        return mData.get("data").getAsJsonObject().get("created_utc").getAsDouble();
    }

    /**
     * Get the replies to this comment.
     */
    public List<Comment> getReplies() {
        List<Comment> ret = new ArrayList<Comment>();
        
        JsonObject data = mData.get("data").getAsJsonObject();
        if (data == null || data.get("replies") == null || !data.get("replies").isJsonObject()) {
            return null;
        }
        JsonObject replies = data.get("replies").getAsJsonObject();
        JsonObject replyData = replies.get("data").getAsJsonObject();
        JsonArray children = replyData.get("children").getAsJsonArray();

        for (int i = 0; i < children.size(); i++) {
            JsonObject jsonData = (JsonObject)children.get(i);
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
     */
    public static List<Comment> getComments(String articleId, User user, String after)
            throws IOException {

        ArrayList<Comment> comments = new ArrayList<Comment>();

        String urlString = "http://www.reddit.com" + articleId + ".json";
        String cookie = user == null ? null : user.getCookie();
        String modhash = user == null ? null : user.getModhash();

        if (after != null)
            urlString += "?after=" + after;

        JsonArray array = new JsonParser().parse(Utilities.get(null, urlString, cookie, modhash))
                .getAsJsonArray();
        if(array != null && array.size() > 0) {
            JsonObject replies = array.get(1).getAsJsonObject();
            JsonArray children = replies.get("data").getAsJsonObject().get("children").getAsJsonArray();

            for (int i = 0; i < children.size(); i++) {
                JsonObject jsonData = (JsonObject)children.get(i);
                comments.add(new Comment(jsonData));
            }
        }

        return comments;
    }
}
