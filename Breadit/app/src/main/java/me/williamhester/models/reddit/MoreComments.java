package me.williamhester.models.reddit;

import android.os.Parcel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 8/15/14.
 */
public class MoreComments extends AbsComment implements Thing {

    private int mCount;
    private String mParentName;
    private String mId;
    private List<String> mChildren;
    private String mName;
    private boolean mIsLoading;

    public MoreComments(JsonObject object) {
        super(0);
        mCount = object.get("count").getAsInt();
        mParentName = object.get("parent_id").getAsString();
        mId = object.get("id").getAsString();
        mName = object.get("name").getAsString();
        JsonArray array = object.get("children").getAsJsonArray();
        mChildren = new ArrayList<>();
        for (JsonElement element : array) {
            mChildren.add(element.getAsString());
        }
    }

    @Override
    public String getAuthor() {
        return "";
    }

    public String getId() {
        return mName;
    }

    public String getParentId() {
        return mParentName;
    }

    public List<String> getChildren() {
        return mChildren;
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public void setIsLoading(boolean isLoading) {
        mIsLoading = isLoading;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MoreComments && ((MoreComments) o).mId.equals(mId);
    }

    @Override
    public int describeContents() {
        return MORE_COMMENTS;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mCount);
        dest.writeString(this.mParentName);
        dest.writeString(this.mId);
        dest.writeList(this.mChildren);
        dest.writeString(this.mName);
    }

    private MoreComments(Parcel in) {
        super(in);
        this.mCount = in.readInt();
        this.mParentName = in.readString();
        this.mId = in.readString();
        this.mChildren = new ArrayList<>();
        in.readList(this.mChildren, String.class.getClassLoader());
        this.mName = in.readString();
    }

    public static final Creator<MoreComments> CREATOR = new Creator<MoreComments>() {
        public MoreComments createFromParcel(Parcel source) {
            return new MoreComments(source);
        }

        public MoreComments[] newArray(int size) {
            return new MoreComments[size];
        }
    };
}
