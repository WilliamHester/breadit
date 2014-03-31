package me.williamhester.areddit;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

public class Comment extends Thing implements Parcelable {

    public static int UPVOTED = 1;
    public static int NEUTRAL = 0;
    public static int DOWNVOTED = -1;

    private List<Comment> mReplies;

    public Comment(JsonObject jsonObj) {
        super(jsonObj);
        if (!getKind().equals("more")) {
            mReplies = generateReplies();
        }
    }

    public Comment(Parcel in) {
        this(new JsonParser().parse(in.readBundle().getString("jsonData")).getAsJsonObject());
    }

    public int getVotedStatus() {
        JsonElement o = mData.get("data").getAsJsonObject().get("likes");
        if (o == null) {
            return NEUTRAL;
        } else if (o.getAsBoolean()) {
            return UPVOTED;
        } else {
            return DOWNVOTED;
        }
    }

    public List<Comment> getReplies() {
        return mReplies;
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
    }

    public String getAuthor() { 
        return mData.get("data").getAsJsonObject().get("author").toString();
    }

    public boolean hasReplies() {
        return mReplies != null;
    }

    public long getCreated() {
        return mData.get("data").getAsJsonObject().get("created").getAsLong();
    }

    public long getCreatedUtc() {
        return mData.get("data").getAsJsonObject().get("created_utc").getAsLong();
    }

    /**
     * Get the replies to this comment.
     */
    public List<Comment> generateReplies() {
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
                ret.add(new Comment(jsonData));
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle b = new Bundle();
        b.putString("jsonData", mData.toString());
        parcel.writeBundle(b);
    }

    public static final Parcelable.Creator<Comment> CREATOR
            = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
