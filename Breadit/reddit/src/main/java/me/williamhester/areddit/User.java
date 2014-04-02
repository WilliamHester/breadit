package me.williamhester.areddit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

public class User implements Parcelable {

    public static String USERNAME = "username";
    public static String MODHASH = "modhash";
    public static String COOKIE = "cookie";
    public static String JS_STRING = "jsstring";

	private String mUsername;
	private String mModhash;
    private String mCookie;
    private String mDataString;

    private JsonObject mData;

    private User() { }

    public User(Parcel in) {
        Bundle b = in.readBundle();
        mUsername = b.getString(USERNAME);
        mModhash = b.getString(MODHASH);
        mCookie = b.getString(COOKIE);
        mDataString = b.getString(JS_STRING);
        if (mDataString != null) {
            mData = new JsonParser().parse(mDataString).getAsJsonObject();
        }
    }

    /**
     * This constructor should only be used to reconstruct a User from data saved in
     * SharedPreferences to create a new User, the static method newUser() must be called.
     *
     * @param username
     * @param modhash
     * @param cookie
     */
    public User(String username, String modhash, String cookie) {
        mUsername = username;
        mModhash = modhash;
        mCookie = cookie;
        new UserDataLoader().execute();
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Creates a new User object and creates a cookie that can be used long-term.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return returns a User object with all of its data
     * @throws IOException if the connection fails
     * @return returns a new User object.
     */
    public static User newUser(String username, String password)
            throws IOException{
        User u = new User();
        u.mUsername = username;
        HashMap<String, String> hashCookiePair = hashCookiePair(username, password);
        u.mCookie = hashCookiePair.get("cookie");
        u.mModhash = hashCookiePair.get("modhash");
        try {
            u.mDataString = u.getUserData().toString();
        } catch (NullPointerException e) {
            if (u.mData == null) {
                Log.e("BreaditDebug", "mData is null");
            }
        }
        return u;
    }

	/**
	 * This function submits a link to the specified subreddit.
	 * 
	 * @param title The title of the submission
	 * @param link The link to the submission
	 * @param subreddit The subreddit to submit to
	 * @throws java.io.IOException If connection fails
     *
     * @return returns a boolean value representing the success of the submission
	 */
	public JsonObject submitLink(String title, String link, String subreddit)
			throws IOException {
		JsonObject object = new JsonParser().parse(submit(title, link, false, subreddit)).getAsJsonObject();
		if (object.toString().contains(".error.USER_REQUIRED")) {
//			User not logged in
            return null;
		} else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
//			User hit ratelimit
            return null;
		} else if (object.toString().contains(".error.ALREADY_SUB.field-url")) {
//			That link has already been submitted.
            return null;
		}
        return object;
	}

	/**
	 * This function submits a self post to the specified subreddit.
	 * 
	 * @param title The title of the submission
	 * @param text The text of the submission
	 * @param subreddit The subreddit to submit to
	 * @throws java.io.IOException If connection fails
     * @return returns the JsonObject of the submission that was created.
	 */
	public JsonObject submitSelfPost(String title, String text, String subreddit)
			throws IOException {
        JsonObject object = new JsonParser().parse(submit(title, text, true, subreddit)).getAsJsonObject();
        if (object.toString().contains(".error.USER_REQUIRED")) {
//			User not logged in
            return null;
        } else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
//			User hit ratelimit
            return null;
        } else if (object.toString().contains(".error.ALREADY_SUB.field-url")) {
//			That link has already been submitted.
            return null;
        }
        return object;
	}

	public boolean hasMail() {
		return mData.get("has_mail").getAsBoolean();
	}

	public double created() {
		return mData.get("created").getAsDouble();
	}

	public double createdUTC() {
		return mData.get("created_utc").getAsDouble();
	}

	public long linkKarma() {
		return mData.get("link_karma").getAsLong();
	}

	public long commentKarma() {
		return mData.get("comment_karma").getAsLong();
	}

	public boolean isGold() {
		return mData.get("is_gold").getAsBoolean();
	}

	public boolean isMod() {
		return mData.get("is_mod").getAsBoolean();
	}

	public String id() {
		return mData.get("id").toString();
	}

	public boolean hasModMail() {
		return mData.get("has_mod_mail").getAsBoolean();
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

		if (mCookie == null || mModhash == null) {
			throw new IOException("User not connected. " +
                "Please invoke the \"newUser\" method before attempting " +
                "to call any other User API functions.");
		}
        String s = Utilities.get("", "http://www.reddit.com/api/me.json", mCookie, mModhash);

		JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();

		return jsonObject.getAsJsonObject("data");
	}

    public boolean refreshUserData() {
        try {
            mData = getUserData();
        } catch (IOException e) {
            return false;
        }
        mModhash = mData.get("modhash").getAsString();
        mDataString = mData.toString();
        return true;
    }

	/**
	 * This function submits a link or self post.
	 * 
	 * @param title The title of the submission
	 * @param linkOrText The link of the submission or text
	 * @param selfPost If this submission is a self post
	 * @param subreddit Which subreddit to submit this to
	 * @return a String that can be parsed into a JsonObject
	 * @throws java.io.IOException If connection fails
	 */
	private String submit(String title, String linkOrText,
			boolean selfPost, String subreddit) throws IOException {
        List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
        apiParams.add(new BasicNameValuePair("title", title));
        if (selfPost) {
            apiParams.add(new BasicNameValuePair("text", linkOrText));
            apiParams.add(new BasicNameValuePair("kind", "self"));
        } else {
            apiParams.add(new BasicNameValuePair("url", linkOrText));
            apiParams.add(new BasicNameValuePair("kind", "link"));
        }
        apiParams.add(new BasicNameValuePair("sr", subreddit));
        apiParams.add(new BasicNameValuePair("uh", mModhash));

		return Utilities.post(apiParams, "http://www.reddit.com/api/submit", mCookie, mModhash);
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
                getCookie(), getModhash())).getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray array = data.get("children").getAsJsonArray();

        for (int i = 0; i < array.size(); i++) {
            JsonObject jsonData = array.get(i).getAsJsonObject();
            subreddits.add(new Subreddit(jsonData));
        }

        String after = null;
        JsonElement je = data.get("after");
        if (!je.isJsonNull())
            after = je.getAsString();

        while (after != null) {

            object = new JsonParser().parse(Utilities.get(null,
                    "http://www.reddit.com/subreddits/mine/subscriber.json?after=" + after,
                    getCookie(), getModhash())).getAsJsonObject();
            data = object.get("data").getAsJsonObject();
            array = data.get("children").getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
                JsonObject jsonData = array.get(i).getAsJsonObject();
                subreddits.add(new Subreddit(jsonData));
            }

            je = data.get("after");
            if (!je.isJsonNull())
                after = je.getAsString();
            else
                after = null;
        }

        return subreddits;
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

    public class FailedLoginException extends Exception {

    }
}
