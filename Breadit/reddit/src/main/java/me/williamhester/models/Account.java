package me.williamhester.models;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.williamhester.models.utils.Utilities;

public class Account implements Parcelable {

	private String mUsername;
	private String mModhash;
    private String mCookie;
    private HashMap<String, Subreddit> mSubscriptions = new HashMap<>();
    private long mId;

    public Account(String username, String modhash, String cookie) {
        mUsername = username;
        mModhash = modhash;
        mCookie = cookie;
    }

    public Account(Cursor c) {
        mId = c.getLong(0);
        mUsername = c.getString(1);
        mCookie = c.getString(2);
        mModhash = c.getString(3);
    }

	public String getUsername() {
		return mUsername;
	}

	public String getModhash() {
		return mModhash;
	}

	public String getCookie() {
		return mCookie;
	}

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setSubscriptions(HashMap<String, Subreddit> subs) {
        mSubscriptions = subs;
    }

    public HashMap<String, Subreddit> getSubscriptions() {
        return mSubscriptions;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Account && ((Account) o).getUsername().equals(mUsername);
    }

    /**
     * This loads in all of the subreddits for a user, but due to the fact that Reddit limits the number that can be
     * loaded at one time to 25, it must be iterative.
     *
     * @return returns a List of type Subreddit
     *
     * @throws java.io.IOException if connection fails
     */

    public ArrayList<Subreddit> getSubscribedSubreddits() throws IOException {

        ArrayList<Subreddit> subreddits = new ArrayList<Subreddit>();

        JsonObject object = new JsonParser().parse(Utilities.get(null,
                "http://www.reddit.com/subreddits/mine/subscriber.json",
                this)).getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray array = data.get("children").getAsJsonArray();

        Gson gson = new Gson();
        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonData = array.get(i).getAsJsonObject();
            subreddits.add((Subreddit) new ResponseRedditWrapper(jsonData, gson).getData());
        }

        String after = null;
        JsonElement je = data.get("after");
        if (!je.isJsonNull())
            after = je.getAsString();

        while (after != null) {
            try {
                String json = Utilities.get(null,
                        "http://www.reddit.com/subreddits/mine/subscriber.json?after=" + after,
                        this);
                object = new JsonParser().parse(json).getAsJsonObject();
            } catch (IllegalStateException e) {
                break;
            } catch (JsonSyntaxException e) {
                break;
            }
            data = object.get("data").getAsJsonObject();
            array = data.get("children").getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject jsonData = array.get(i).getAsJsonObject();
                subreddits.add((Subreddit) new ResponseRedditWrapper(jsonData, gson).getData());
            }

            je = data.get("after");
            if (!je.isJsonNull())
                after = je.getAsString();
            else
                after = null;
        }
        if (subreddits.size() > 0)
            return subreddits;
        else
            return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUsername);
        dest.writeString(this.mModhash);
        dest.writeString(this.mCookie);
        dest.writeLong(this.mId);
    }

    private Account(Parcel in) {
        this.mUsername = in.readString();
        this.mModhash = in.readString();
        this.mCookie = in.readString();
        this.mId = in.readLong();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}
