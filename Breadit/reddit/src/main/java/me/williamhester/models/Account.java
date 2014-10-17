package me.williamhester.models;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.williamhester.models.utils.Utilities;

public class Account implements Parcelable {

    public static final String USERNAME = "username";
    public static final String MODHASH = "modhash";
    public static final String COOKIE = "cookie";
    public static final String JS_STRING = "jsstring";
    public static final String TABLE_ID = "table_id";

	private String mUsername;
	private String mModhash;
    private String mCookie;
    private String mDataString;
    private long mId;

    private JsonObject mData;

    private Account() { }

    public Account(Parcel in) {
        Bundle b = in.readBundle();
        mUsername = b.getString(USERNAME);
        mModhash = b.getString(MODHASH);
        mCookie = b.getString(COOKIE);
        mDataString = b.getString(JS_STRING);
        mId = b.getLong(TABLE_ID);

        if (mDataString != null) {
            mData = new JsonParser().parse(mDataString).getAsJsonObject();
        }
    }

    /**
     * This constructor should only be used to reconstruct a Account from data saved in
     * SharedPreferences to create a new Account, the static method newAccount() must be called.
     *
     * @param username
     * @param modhash
     * @param cookie
     */
    public Account(String username, String modhash, String cookie) {
        mUsername = username;
        mModhash = modhash;
        mCookie = cookie;
        new UserDataLoader().execute();
    }

    public Account(Cursor c) {
        mId = c.getLong(0);
        mUsername = c.getString(1);
        mCookie = c.getString(2);
        mModhash = c.getString(3);
    }

    public static final Parcelable.Creator<Account> CREATOR
            = new Parcelable.Creator<Account>() {
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    /**
     * Creates a new Account object and creates a cookie that can be used long-term.
     *
     * @param username the username of the user
     * @param password the password of the user
     *
     * @return returns a Account object with all of its data
     *
     * @throws IOException if the connection fails
     *
     * @return returns a new Account object.
     */
    public static Account newAccount(String username, String password, Context context)
            throws IOException{
        Account a = new Account();
        a.mUsername = username;
        HashMap<String, String> hashCookiePair = hashCookiePair(username, password);
        a.mCookie = hashCookiePair.get("cookie");
        a.mModhash = hashCookiePair.get("modhash");
        return a;
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Account && ((Account) o).getUsername().equals(mUsername);
    }

	/**
	 * This function logs in to reddit and returns an ArrayList containing a
	 * modhash and cookie.
	 * 
	 * @param username The username
	 * @param password The password
	 * @return An array containing a modhash and cookie
	 * @throws java.io.IOException If connection fails
	 */
    private static HashMap<String, String> hashCookiePair(String username, String password)
            throws IOException {
        HashMap<String, String> values = new HashMap<String, String>();
        ArrayList<NameValuePair> apiParams = new ArrayList<NameValuePair>();
        apiParams.add(new BasicNameValuePair("api_type", "json"));
        apiParams.add(new BasicNameValuePair("user", username));
        apiParams.add(new BasicNameValuePair("passwd", password));
        apiParams.add(new BasicNameValuePair("rem", "True"));

        String data = Utilities.post(apiParams, "http://www.reddit.com/api/login/" + username, null, null);
        JsonObject jsObject = new JsonParser().parse(data).getAsJsonObject();

        JsonElement valuePair = jsObject.get("json");
        if (valuePair.isJsonObject()) {
            JsonObject object = valuePair.getAsJsonObject().get("data").getAsJsonObject();
            values.put("modhash", object.get("modhash").toString());
            values.put("cookie", object.get("cookie").toString());
        }
        return values;
    }

	/**
	 * This function returns a "response" (me.json) JSON data containing getUserData
	 * about the user. <br />
	 * 
	 * @return JSON data containing getUserData about the user
	 */
	private JsonObject getUserData() throws IOException {
        String s = Utilities.get("", "http://www.reddit.com/api/me.json", mCookie, mModhash);
		JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();

		return jsonObject.getAsJsonObject("data");
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
        Bundle b = new Bundle();
        b.putString(USERNAME, mUsername);
        b.putString(MODHASH, mModhash);
        b.putString(COOKIE, mCookie);
        b.putString(JS_STRING, mDataString);
        b.putLong(TABLE_ID, mId);
        dest.writeBundle(b);
    }

    private class UserDataLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mData = getUserData();
                if (mData != null)
                    mDataString = mData.toString();
            } catch (IOException e) {
                Log.e("BreaditDebug", "IOException was thrown when getting getUserData");
            }
            return null;
        }
    }
}
