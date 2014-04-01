package me.williamhester.areddit;

import com.google.gson.JsonObject;

public class Thing {

    protected String mId;
    protected String mName;
    protected String mKind;

    protected Thing() { }

    protected Thing(JsonObject data) {
        mData = data;
    }

    protected JsonObject mData;

    public static Thing fromJsonString(JsonObject data) {
        Thing thing = new Thing();
        thing.mId = data.getAsJsonObject("data").get("id").getAsString();
        thing.mName = data.getAsJsonObject("data").get("name").getAsString();
        thing.mKind = data.get("kind").getAsString();
        return thing;
    }

    public String getId() {
        return mId;
    }

    public String getName() { 
        return mName;
    }

    public String getKind() {
        return mKind;
    }

    public JsonObject getData() {
        return mData;
    }
}
