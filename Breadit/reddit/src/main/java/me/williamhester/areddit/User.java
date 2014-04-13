package me.williamhester.areddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/13/14.
 */
public class User extends Thing implements Parcelable {

    private int mCommentKarma;
    private int mLinkKarma;
    private long mCreated;
    private long mCreatedUtc;
    private boolean mIsFriend;
    private boolean mIsMod;
    private boolean mHasVerifiedEmail;
    private String mUsername;

    private User(JsonObject data) {
        mCommentKarma = data.get("comment_karma").getAsInt();
        mLinkKarma = data.get("link_karma").getAsInt();
        mCreated = data.get("created").getAsLong();
        mCreatedUtc = data.get("created_utc").getAsLong();
        mIsFriend = data.get("is_friend").getAsBoolean();
        mIsMod = data.get("is_mod").getAsBoolean();
        mHasVerifiedEmail = data.get("has_verified_email").getAsBoolean();
        mUsername = data.get("name").getAsString();
    }

    public static User getUser(String username, Account account) throws IOException,
            UserNotFoundException {
        String s = Utilities.get("", "http://www.reddit.com/user/" + username + "/about.json",
                account.getCookie(), account.getModhash());
        if (s.equals("{\"error\": 404}"))
            throw new UserNotFoundException();
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        return new User(jsonObject.getAsJsonObject("data"));
    }

    public int getCommentKarma() {
        return mCommentKarma;
    }

    public int getLinkKarma() {
        return mLinkKarma;
    }

    public long getCreated() {
        return mCreated;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public boolean isFriend() {
        return mIsFriend;
    }

    public boolean isMod() {
        return mIsMod;
    }

    public boolean hasVerifiedEmail() {
        return mHasVerifiedEmail;
    }

    public String getUsername() {
        return mUsername;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCommentKarma);
        dest.writeInt(this.mLinkKarma);
        dest.writeLong(this.mCreated);
        dest.writeLong(this.mCreatedUtc);
        dest.writeByte(mIsFriend ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsMod ? (byte) 1 : (byte) 0);
        dest.writeByte(mHasVerifiedEmail ? (byte) 1 : (byte) 0);
        dest.writeString(this.mUsername);
        dest.writeString(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mKind);
    }

    private User(Parcel in) {
        this.mCommentKarma = in.readInt();
        this.mLinkKarma = in.readInt();
        this.mCreated = in.readLong();
        this.mCreatedUtc = in.readLong();
        this.mIsFriend = in.readByte() != 0;
        this.mIsMod = in.readByte() != 0;
        this.mHasVerifiedEmail = in.readByte() != 0;
        this.mUsername = in.readString();
        this.mId = in.readString();
        this.mName = in.readString();
        this.mKind = in.readString();
    }

    public static Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static class UserNotFoundException extends Exception {

    }
}
