package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Calendar;

import me.williamhester.models.utils.Utilities;

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
        String s;
        s = Utilities.get("", "http://www.reddit.com/user/" + username + "/about.json", account);
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

    public String calculateCakeDay() {
        Calendar cakeday = Calendar.getInstance();
        cakeday.setTimeInMillis(mCreatedUtc * 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("Cake Day on ");
        switch (cakeday.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                sb.append("January ");
                break;
            case Calendar.FEBRUARY:
                sb.append("February ");
                break;
            case Calendar.MARCH:
                sb.append("March ");
                break;
            case Calendar.APRIL:
                sb.append("April ");
                break;
            case Calendar.MAY:
                sb.append("May ");
                break;
            case Calendar.JUNE:
                sb.append("June ");
                break;
            case Calendar.JULY:
                sb.append("July ");
                break;
            case Calendar.AUGUST:
                sb.append("August ");
                break;
            case Calendar.SEPTEMBER:
                sb.append("September ");
                break;
            case Calendar.OCTOBER:
                sb.append("October ");
                break;
            case Calendar.NOVEMBER:
                sb.append("November ");
                break;
            case Calendar.DECEMBER:
                sb.append("December ");
                break;
        }
        sb.append(cakeday.get(Calendar.DAY_OF_MONTH));
        int day = cakeday.get(Calendar.DAY_OF_MONTH);
        if (day == 1 || day == 21 || day == 31) {
            sb.append("st");
        } else if (day == 2 || day == 22) {
            sb.append("nd");
        } else if (day == 3 || day == 23) {
            sb.append("rd");
        } else {
            sb.append("th");
        }
        return sb.toString();
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
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.kind);
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
        this.id = in.readString();
        this.name = in.readString();
        this.kind = in.readString();
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
