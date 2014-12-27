package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Calendar;

import me.williamhester.models.utils.Utilities;

/**
 * Created by William on 4/13/14.
 */
public class User implements Parcelable {

    @SerializedName("name")
    private String mUsername;
    @SerializedName("modhash")
    private String mModhash;
    @SerializedName("id")
    private String mId;
    @SerializedName("comment_karma")
    private int mCommentKarma;
    @SerializedName("link_karma")
    private int mLinkKarma;
    @SerializedName("gold_expiration")
    private int mGoldExpiration;
    @SerializedName("gold_creddits")
    private int mGoldCreddits;
    @SerializedName("created_utc")
    private long mCreatedUtc;
    @SerializedName("created")
    private long mCreated;
    @SerializedName("has_verified_email")
    private boolean mHasVerifiedEmail;
    @SerializedName("is_friend")
    private boolean mIsFriend;
    @SerializedName("has_mail")
    private boolean mHasMail;
    @SerializedName("over_18")
    private boolean mIsOver18;
    @SerializedName("is_gold")
    private boolean mIsGold;
    @SerializedName("is_mod")
    private boolean mIsMod;
    @SerializedName("has_mod_mail")
    private boolean mHasModMail;


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

    public boolean isLoggedInAccount() {
        return mModhash != null;
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
