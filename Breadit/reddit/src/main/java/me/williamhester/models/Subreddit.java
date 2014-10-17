package me.williamhester.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Subreddit implements Parcelable, Comparable<Subreddit> {

    protected String id;
    protected String name;

    @SerializedName("accounts_active")
    private int mAccountsActive;
    @SerializedName("comment_score_hide_mins")
    private int mCommentScoreHideMins;
    @SerializedName("created")
    private long mCreated;
    @SerializedName("created_utc")
    private long mCreatedUtc;
    @SerializedName("subscribers")
    private long mSubscribers;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("description_html")
    private String mDescriptionHtml;
    @SerializedName("display_name")
    private String mDisplayName;
    @SerializedName("header_img")
    private String mHeaderImg;
    @SerializedName("header_title")
    private String mHeaderTitle;
    @SerializedName("public_description")
    private String mPublicDescription;
    @SerializedName("submission_type")
    private String mSubmissionType;
    @SerializedName("submit_link_label")
    private String mSubmitLinkLabel;
    @SerializedName("submit_text_label")
    private String mSubmitTextLabel;
    @SerializedName("subreddit_type")
    private String mSubredditType;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("url")
    private String mUrl;
    @SerializedName("over18")
    private boolean mOver18;
    @SerializedName("public_traffic")
    private boolean mPublicTraffic;
    @SerializedName("user_is_banned")
    private boolean mUserIsBanned;
    @SerializedName("user_is_contributor")
    private boolean mUserIsContributor;
    @SerializedName("user_is_moderator")
    private boolean mUserIsModerator;
    @SerializedName("user_is_subscriber")
    private boolean mUserIsSubscriber;

    private long mTableId;

    public Subreddit(Cursor cursor) {
        mTableId = cursor.getLong(0);
        mDisplayName = cursor.getString(1);
        mOver18 = cursor.getInt(2) == 1;
        mPublicTraffic = cursor.getInt(3) == 1;
        name = cursor.getString(4);
        mCreated = cursor.getLong(5);
        mSubmissionType = cursor.getString(6);
        if (cursor.getColumnCount() > 7) { // In case the cursor does not have the account-related info
            mUserIsModerator = cursor.getInt(7) == 1;
            mUserIsBanned = cursor.getInt(8) == 1;
        }
    }

    public boolean userIsBanned() {
        return mUserIsBanned;
    }

    public String getDisplayName() {
        return  mDisplayName;
    }

    public String getHeaderImgUrl() {
        return  mHeaderImg;
    }

    public String getTitle() {
        return  mTitle;
    }

    public boolean isNsfw() {
        return mOver18;
    }

    public boolean userIsModerator() {
        return mUserIsModerator;
    }

    public String getHeaderTitle() {
        return mHeaderTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getSubmitLinkLabel() {
        return mSubmitLinkLabel;
    }

    public boolean isPublicTraffic() {
        return mPublicTraffic;
    }

    public long getSubscriberCount() {
        return mSubscribers;
    }

    public String getSubmitTextLabel() {
        return  mSubmitTextLabel;
    }

    public String getUrl() {
        return  mUrl;
    }

    public long getCreated() {
        return  mCreated;
    }

    public long getCreatedUtc() {
        return  mCreatedUtc;
    }

    public boolean userIsContributor() {
        return mUserIsContributor;
    }

    public String getPublicDescription() {
        return  mPublicDescription;
    }

    public long getCommentScoreHideMins() {
        return mCommentScoreHideMins;
    }

    public String getSubredditType() {
        return  mSubredditType;
    }

    public String getSubmissionType() {
        return  mSubmissionType;
    }

    public boolean userIsSubscriber() {
        return mUserIsSubscriber;
    }

    public long getTableId() {
        return mTableId;
    }

    public void setTableId(long id) {
        mTableId = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Subreddit && ((Subreddit) o).name == null) {
            Log.d("Subreddit", "breakpoint");
        }
        return o instanceof Subreddit && name != null && (((Subreddit) o).name).equals(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAccountsActive);
        dest.writeInt(this.mCommentScoreHideMins);
        dest.writeLong(this.mCreated);
        dest.writeLong(this.mCreatedUtc);
        dest.writeLong(this.mSubscribers);
        dest.writeString(this.mDescription);
        dest.writeString(this.mDescriptionHtml);
        dest.writeString(this.mDisplayName);
        dest.writeString(this.mHeaderImg);
        dest.writeString(this.mHeaderTitle);
        dest.writeString(this.mPublicDescription);
        dest.writeString(this.mSubmissionType);
        dest.writeString(this.mSubmitLinkLabel);
        dest.writeString(this.mSubmitTextLabel);
        dest.writeString(this.mSubredditType);
        dest.writeString(this.mTitle);
        dest.writeString(this.mUrl);
        dest.writeByte(mOver18 ? (byte) 1 : (byte) 0);
        dest.writeByte(mPublicTraffic ? (byte) 1 : (byte) 0);
        dest.writeByte(mUserIsBanned ? (byte) 1 : (byte) 0);
        dest.writeByte(mUserIsContributor ? (byte) 1 : (byte) 0);
        dest.writeByte(mUserIsModerator ? (byte) 1 : (byte) 0);
        dest.writeByte(mUserIsSubscriber ? (byte) 1 : (byte) 0);
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    private Subreddit(Parcel in) {
        this.mAccountsActive = in.readInt();
        this.mCommentScoreHideMins = in.readInt();
        this.mCreated = in.readLong();
        this.mCreatedUtc = in.readLong();
        this.mSubscribers = in.readLong();
        this.mDescription = in.readString();
        this.mDescriptionHtml = in.readString();
        this.mDisplayName = in.readString();
        this.mHeaderImg = in.readString();
        this.mHeaderTitle = in.readString();
        this.mPublicDescription = in.readString();
        this.mSubmissionType = in.readString();
        this.mSubmitLinkLabel = in.readString();
        this.mSubmitTextLabel = in.readString();
        this.mSubredditType = in.readString();
        this.mTitle = in.readString();
        this.mUrl = in.readString();
        this.mOver18 = in.readByte() != 0;
        this.mPublicTraffic = in.readByte() != 0;
        this.mUserIsBanned = in.readByte() != 0;
        this.mUserIsContributor = in.readByte() != 0;
        this.mUserIsModerator = in.readByte() != 0;
        this.mUserIsSubscriber = in.readByte() != 0;
        this.id = in.readString();
        this.name = in.readString();
    }

    public static Creator<Subreddit> CREATOR = new Creator<Subreddit>() {
        public Subreddit createFromParcel(Parcel source) {
            return new Subreddit(source);
        }

        public Subreddit[] newArray(int size) {
            return new Subreddit[size];
        }
    };

    @Override
    public int compareTo(Subreddit subreddit) {
        if (mDisplayName == null || subreddit == null || subreddit.mDisplayName == null)
            return -1;
        return mDisplayName.compareTo(subreddit.mDisplayName);
    }
}
