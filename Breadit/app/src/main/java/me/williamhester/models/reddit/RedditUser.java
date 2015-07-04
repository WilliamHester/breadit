package me.williamhester.models.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

import me.williamhester.models.bulletin.User;

/**
 * Created by William on 4/13/14.
 */
public class RedditUser implements User, Parcelable {

    public static final String OVERVIEW = "";
    public static final String COMMENTS = "comments";
    public static final String SUBMITTED = "submitted";
    public static final String GILDED = "gilded";
    public static final String LIKED = "liked";
    public static final String DISLIKED = "disliked";
    public static final String HIDDEN = "hidden";
    public static final String SAVED = "saved";

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

    public RedditUser() { }

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

    @Override
    public String getCreatedDate() {
        return "fixme: created date";
    }

    @Override
    public int getCommentPoints() {
        return mCommentKarma;
    }

    @Override
    public int getSubmissionPoints() {
        return mLinkKarma;
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

    public static class UserNotFoundException extends Exception {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUsername);
        dest.writeString(this.mModhash);
        dest.writeString(this.mId);
        dest.writeInt(this.mCommentKarma);
        dest.writeInt(this.mLinkKarma);
        dest.writeInt(this.mGoldExpiration);
        dest.writeInt(this.mGoldCreddits);
        dest.writeLong(this.mCreatedUtc);
        dest.writeLong(this.mCreated);
        dest.writeByte(mHasVerifiedEmail ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsFriend ? (byte) 1 : (byte) 0);
        dest.writeByte(mHasMail ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsOver18 ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsGold ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsMod ? (byte) 1 : (byte) 0);
        dest.writeByte(mHasModMail ? (byte) 1 : (byte) 0);
    }

    private RedditUser(Parcel in) {
        this.mUsername = in.readString();
        this.mModhash = in.readString();
        this.mId = in.readString();
        this.mCommentKarma = in.readInt();
        this.mLinkKarma = in.readInt();
        this.mGoldExpiration = in.readInt();
        this.mGoldCreddits = in.readInt();
        this.mCreatedUtc = in.readLong();
        this.mCreated = in.readLong();
        this.mHasVerifiedEmail = in.readByte() != 0;
        this.mIsFriend = in.readByte() != 0;
        this.mHasMail = in.readByte() != 0;
        this.mIsOver18 = in.readByte() != 0;
        this.mIsGold = in.readByte() != 0;
        this.mIsMod = in.readByte() != 0;
        this.mHasModMail = in.readByte() != 0;
    }

    public static final Creator<RedditUser> CREATOR = new Creator<RedditUser>() {
        public RedditUser createFromParcel(Parcel source) {
            return new RedditUser(source);
        }

        public RedditUser[] newArray(int size) {
            return new RedditUser[size];
        }
    };
}
