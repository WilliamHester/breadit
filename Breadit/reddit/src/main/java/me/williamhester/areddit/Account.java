package me.williamhester.areddit;

import android.database.Cursor;
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
import java.util.Scanner;
import java.util.TreeSet;

import me.williamhester.areddit.utils.Utilities;

public class Account implements Parcelable {

    public static final String USERNAME = "username";
    public static final String MODHASH = "modhash";
    public static final String COOKIE = "cookie";
    public static final String JS_STRING = "jsstring";
    public static final String TABLE_ID = "table_id";
    public static final String SUBSCRIBED_SUBREDDITS = "subscribed_subreddits";
    public static final String SAVED_SUBMISSIONS = "saved_submissions";
    public static final String HISTORY = "history";

	private String mUsername;
	private String mModhash;
    private String mCookie;
    private String mDataString;
    private String mSavedSubmissions;
    private String mSubredditsString;
    private String mHistory;
    private TreeSet<String> mHistoryTree;
    private long mId;
    private List<String> mSubreddits;

    private JsonObject mData;

    private Account() { }

    public Account(Parcel in) {
        Bundle b = in.readBundle();
        mUsername = b.getString(USERNAME);
        mModhash = b.getString(MODHASH);
        mCookie = b.getString(COOKIE);
        mDataString = b.getString(JS_STRING);
        mId = b.getLong(TABLE_ID);
        mSubredditsString = b.getString(SUBSCRIBED_SUBREDDITS);
        mSavedSubmissions = b.getString(SAVED_SUBMISSIONS);
        mHistory = b.getString(HISTORY);

        if (mDataString != null) {
            mData = new JsonParser().parse(mDataString).getAsJsonObject();
        }
        if (mSubredditsString != null) {
            Scanner scan = new Scanner(mSubredditsString).useDelimiter(",");
            mSubreddits = new ArrayList<String>();
            while (scan.hasNext()) {
                mSubreddits.add(scan.next());
            }
        } else {
            mSubreddits = new ArrayList<String>();
        }
        if (mHistory != null) {
            Scanner scan = new Scanner(mHistory).useDelimiter(",");
            mHistoryTree = new TreeSet<String>();
            while (scan.hasNext()) {
                mHistoryTree.add(scan.next());
            }
        } else {
            mHistory = "";
            mHistoryTree = new TreeSet<String>();
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
        c.moveToFirst();
        mId = c.getLong(0);
        mUsername = c.getString(1);
        mCookie = c.getString(2);
        mModhash = c.getString(3);
        String subs = c.getString(4);
        mSavedSubmissions = c.getString(5);
        mHistory = c.getString(6);
        Scanner scan = new Scanner(subs).useDelimiter(",");
        mSubreddits = new ArrayList<String>();
        while (scan.hasNext()) {
            mSubreddits.add(scan.next());
        }
        if (mHistory != null) {
            scan = new Scanner(mHistory).useDelimiter(",");
            mHistoryTree = new TreeSet<String>();
            while (scan.hasNext()) {
                mHistoryTree.add(scan.next());
            }
        } else {
            mHistory = "";
            mHistoryTree = new TreeSet<String>();
        }
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
    public static Account newAccount(String username, String password) throws IOException{
        Account a = new Account();
        a.mUsername = username;
        HashMap<String, String> hashCookiePair = hashCookiePair(username, password);
        a.mCookie = hashCookiePair.get("cookie");
        a.mModhash = hashCookiePair.get("modhash");
        a.mHistory = "";
        a.mSavedSubmissions = "";
        List<Subreddit> subs = a.getSubscribedSubreddits();
        a.mSubreddits = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (Subreddit s : subs) {
            a.mSubreddits.add(s.getDisplayName());
            sb.append(s.getDisplayName());
            sb.append(',');
        }
        a.mSubredditsString = sb.toString();

        try {
            a.mDataString = a.getUserData().toString();
        } catch (NullPointerException e) {
            if (a.mData == null) {
                Log.e("BreaditDebug", "mData is null");
            }
        }
        return a;
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
//			Account not logged in
            return null;
		} else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
//			Account hit ratelimit
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
//			Account not logged in
            return null;
        } else if (object.toString().contains(".error.RATELIMIT.field-ratelimit")) {
//			Account hit ratelimit
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

    public String getHistory() {
        return mHistory;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public String getCommaSepSubs() {
        return mSubredditsString;
    }

    public List<String> getSubreddits() {
        return mSubreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        mSubreddits = subreddits;
        StringBuilder sb = new StringBuilder();
        for (String s : subreddits) {
            sb.append(s);
            sb.append(',');
        }
        mSubredditsString = sb.toString();
    }

    public void visit(String fullname) {
        mHistory = fullname + "," + mHistory;
        mHistoryTree.add(fullname);
    }

    public boolean hasVisited(String fullname) {
        return mHistoryTree.contains(fullname);
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
     *
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

    public List<Subreddit> getSubscribedSubreddits() throws IOException {

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
        b.putLong(TABLE_ID, mId);
        b.putString(SAVED_SUBMISSIONS, mSavedSubmissions);
        b.putString(SUBSCRIBED_SUBREDDITS, mSubredditsString);
        b.putString(HISTORY, mHistory);
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
