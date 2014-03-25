package me.williamhester.areddit;

import android.os.Parcelable;

import com.google.gson.JsonObject;

public abstract class Thing implements Parcelable {

    public Thing(JsonObject data) {
        mData = data;
    }

    protected JsonObject mData;

    public String getId() {
        return mData.getAsJsonObject("data").get("id").getAsString();
    }

    public String getName() { 
        return mData.getAsJsonObject("data").get("name").getAsString();
    }

    public String getKind() {
        return mData.get("kind").getAsString();
    }
    public JsonObject getData() {
        return mData;
    }
}
