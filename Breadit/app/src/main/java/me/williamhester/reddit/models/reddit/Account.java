package me.williamhester.reddit.models.reddit;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

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
