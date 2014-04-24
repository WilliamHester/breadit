package me.williamhester.areddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Subreddit extends Thing implements Parcelable {

    private int mAccountsActive;
    private int mCommentScoreHideMins;
    private long mCreated;
    private long mCreatedUtc;
    private long mSubscribers;
    private String mDescription;
    private String mDescriptionHtml;
    private String mDisplayName;
    private String mHeaderImg;
    private String mHeaderTitle;
    private String mPublicDescription;
    private String mSubmissionType;
    private String mSubmitLinkLabel;
    private String mSubmitTextLabel;
    private String mSubredditType;
    private String mTitle;
    private String mUrl;
    private boolean mOver18;
    private boolean mPublicTraffic;
    private boolean mUserIsBanned;
    private boolean mUserIsContributor;
    private boolean mUserIsModerator;
    private boolean mUserIsSubscriber;

    public Subreddit(JsonObject data) {
        super(data);
        if (data != null) {
            data = data.get("data").getAsJsonObject();
//        mAccountsActive = data.get("accounts_active").getAsInt();
//        mCommentScoreHideMins = data.get("comment_score_hide_mins").getAsInt();
            mCreated = data.get("created").getAsLong();
            mCreatedUtc = data.get("created_utc").getAsLong();
            mSubscribers = data.get("subscribers").getAsLong();
//        mDescription = data.get("description").getAsString();
//        mDescriptionHtml = data.get("description_html").getAsString();
            mDisplayName = data.get("display_name").getAsString();
//        mHeaderImg = data.get("header_img").getAsString();
//        mHeaderTitle = data.get("header_title").getAsString();
//        mPublicDescription = data.get("public_description").getAsString();
//        mSubmissionType = data.get("submission_type").getAsString();
//        mSubmitLinkLabel = data.get("submit_link_label").getAsString();
//        mSubmitTextLabel = data.get("submit_text_label").getAsString();
//        mSubredditType = data.get("subreddit_type").getAsString();
            mTitle = data.get("title").getAsString();
            mUrl = data.get("url").getAsString();
            mOver18 = data.get("over18").getAsBoolean();
            mPublicTraffic = data.get("public_traffic").getAsBoolean();
            if (!data.get("user_is_banned").isJsonNull()) {
                mUserIsBanned = data.get("user_is_banned").getAsBoolean();
                mUserIsContributor = data.get("user_is_contributor").getAsBoolean();
                mUserIsModerator = data.get("user_is_moderator").getAsBoolean();
                mUserIsSubscriber = data.get("user_is_subscriber").getAsBoolean();
            }
        }
    }

    public static Subreddit fromString(String dataString) {
        JsonParser parser = new JsonParser();
        JsonObject data = parser.parse(dataString).getAsJsonObject();
        return new Subreddit(data);
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
        dest.writeString(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mKind);
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
        this.mId = in.readString();
        this.mName = in.readString();
        this.mKind = in.readString();
    }

    public static Creator<Subreddit> CREATOR = new Creator<Subreddit>() {
        public Subreddit createFromParcel(Parcel source) {
            return new Subreddit(source);
        }

        public Subreddit[] newArray(int size) {
            return new Subreddit[size];
        }
    };
}
