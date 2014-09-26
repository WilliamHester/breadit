package me.williamhester.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 8/15/14.
 */
public class MoreComments extends AbsComment implements Parcelable {

    private int mCount;
    private String mParentId;
    private String mId;
    private List<String> mChildren;
    private String mName;

    public MoreComments(JsonObject object) {
        super(0);
        mCount = object.get("count").getAsInt();
        mParentId = object.get("parent_id").getAsString();
        mId = object.get("id").getAsString();
        mName = object.get("name").getAsString();
        JsonArray array = object.get("children").getAsJsonArray();
        mChildren = new ArrayList<>();
        for (JsonElement element : array) {
            mChildren.add(element.getAsString());
        }
    }

    @Override
    public int describeContents() {
        return AbsComment.MORE_COMMENTS;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mCount);
        dest.writeString(this.mParentId);
        dest.writeString(this.mId);
        dest.writeList(this.mChildren);
        dest.writeString(this.mName);
    }

    private MoreComments(Parcel in) {
        super(in);
        this.mCount = in.readInt();
        this.mParentId = in.readString();
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
