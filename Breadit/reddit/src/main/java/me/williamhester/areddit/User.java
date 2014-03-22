package me.williamhester.areddit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.utils.Utilities;

/**
 * This class represents a reddit user.
 * 
 * @author <a href="http://www.omrlnr.com">Omer Elnour</a>
 * @author <a hred="https://github.com/jasonsimpson">Jason Simpson</a>
 *
 */
// Todo: implement parcelable so that the user can be passed between Activities
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
        if (mData != null) {
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
     * @return returns a usre object with all of its data
     * @throws IOException if the connection fails
     */
    public static User newUser(String username, String password)
            throws IOException{
        User u = new User();
        u.mUsername = username;
        ArrayList<String> hashCookiePair = hashCookiePair(username, password);
        u.mCookie = hashCookiePair.get(0);
        u.mModhash = hashCookiePair.get(1);
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
	 */
	public void submitLink(String title, String link, String subreddit)
			throws IOException {
		JsonObject object = new JsonParser().parse(submit(title, link, false, subreddit)).getAsJsonObject();
		if (object.toString().contains(".error.USER_REQUIRED")) {
			Log.e("BreaditError", "User not logged in");
		} else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
			Log.e("BreaditError", "User hit ratelimit");
		} else if (object.toString().contains(".error.ALREADY_SUB.field-url")) {
			Log.e("BreaditError", "That link has already been submitted.");
		} else {
//			Log.i("Link submitted to "
//                    + ((JSONArray) ((JSONArray) ((JSONArray) object
//                    .get("jquery")).get(16)).get(3)).get(0));
		}
	}

	/**
	 * This function submits a self post to the specified subreddit.
	 * 
	 * @param title The title of the submission
	 * @param text The text of the submission
	 * @param subreddit The subreddit to submit to
	 * @throws java.io.IOException If connection fails
	 */
	public void submitSelfPost(String title, String text, String subreddit)
			throws IOException {
        JsonElement element = new JsonParser().parse(submit(title, text, true, subreddit));
        JsonObject object;
        if (element.isJsonObject()) {
            object = element.getAsJsonObject();
            if (object.toString().contains(".error.USER_REQUIRED")) {
                Log.e("BreaditError", "User not logged in");
            } else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
                Log.e("BreaditError", "User hit ratelimit");
            } else if (object.toString().contains(".error.ALREADY_SUB.field-url")) {
                Log.e("BreaditError", "That link has already been submitted.");
            } else {
                //			System.out.println("Self post submitted to "
                //					+ ((JSONArray) ((JSONArray) ((JSONArray) object
                //							.get("jquery")).get(10)).get(3)).get(0));
            }
        }
	}

	/**
	 * This functions returns true if this user has unread mail.
	 * 
	 * @return This user has mail or not
	 * @throws java.io.IOException
	 *             If connection fails
	 */
	public boolean hasMail() throws IOException {
		return Boolean.parseBoolean(mData.get("has_mail").toString());
	}

	/**
	 * This function returns the Unix time that the user's account was created.
	 * 
	 * @return Unix time that the user's account was created
	 * @throws NumberFormatException
	 *             If the "created" property isn't a double
	 */
	public double created() throws IOException {
		return Double.parseDouble(mData.get("created").toString());
	}

	/**
	 * This function returns the Unix time (in UTC/Coordinated Universal Time)
	 * that the user's account was created.
	 * 
	 * @return Unix time that the user's account was created in UTC
	 * @throws NumberFormatException If the "created_utc" property isn't a double
	 */
	public double createdUTC() throws IOException {
		return Double.parseDouble(mData.get("created_utc").toString());
	}

	/**
	 * This function returns the amount of link karma this user has. <br />
	 * Returns int because I doubt anyone has more than 2,147,483,647 link
	 * karma.
	 * 
	 * @return Link Karma
	 * @throws NumberFormatException If the "link_karma" property isn't an integer
	 */
	public int linkKarma() throws IOException {
		return Integer.parseInt(mData.get("link_karma").toString());
	}

	/**
	 * This function returns the amount of comment karma this user has. <br />
	 * Returns int because I doubt anyone has more than 2,147,483,647 comment
	 * karma.
	 * 
	 * @return Comment Karma
	 * @throws NumberFormatException If the "link_karma" property isn't an integer
	 */
	public int commentKarma() throws IOException {
		return Integer.parseInt(mData.get("comment_karma").toString());
	}

	/**
	 * This functions returns true if this user has a gold account.
	 * 
	 * @return This user has a gold account or not
	 */
	public boolean isGold() throws IOException {
		return Boolean.parseBoolean(mData.get("is_gold").toString());
	}

	/**
	 * This functions returns true if this user is a reddit moderator
	 * (apparently this means a moderator of any subreddit).
	 * 
	 * @return This user is a moderator or not
	 * @throws java.io.IOException
	 *             If connection fails
	 */
	public boolean isMod() throws IOException {
		return Boolean.parseBoolean(mData.get("is_mod").toString());
	}

	/**
	 * This function returns the user's ID. <br />
	 * The user's ID. This is only used internally, <b>right</b>?
	 * 
	 * @return This user's ID
	 */
	public String id() throws IOException {
		return mData.get("id").toString();
	}

	/**
	 * This functions returns true if this user has unread moderator mail.
	 * 
	 * @return This user has unread moderator mail or not
	 */
	public boolean hasModMail() throws IOException {
		return Boolean.parseBoolean(mData.get("has_mod_mail").toString());
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
	private static ArrayList<String> hashCookiePair(String username, String password)
			throws IOException {
		ArrayList<String> values = new ArrayList<String>();
        ArrayList<NameValuePair> apiParams = new ArrayList<NameValuePair>();
        apiParams.add(new BasicNameValuePair("api_type", "json"));
        apiParams.add(new BasicNameValuePair("user", username));
        apiParams.add(new BasicNameValuePair("passwd", password));
        apiParams.add(new BasicNameValuePair("rem", "true"));

        String data = Utilities.post(apiParams, "http://www.reddit.com/api/login/" + username, null, null);
        JsonObject jsObject = new JsonParser().parse(data).getAsJsonObject();

		JsonElement valuePair = jsObject.get("json");
        if (valuePair.isJsonObject()) {
            JsonObject object = valuePair.getAsJsonObject();
            values.add(object.get("modhash").toString());
            values.add(object.get("cookie").toString());
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

		JsonObject jsonObject = new JsonParser().parse(Utilities.get("", "http://www.reddit.com/api/me.json",
                mCookie, mModhash)).getAsJsonObject();

		return jsonObject.getAsJsonObject("data");
	}

    public void refreshUserData() throws IOException {
        mData = getUserData();
        mDataString = mData.toString();
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
}
