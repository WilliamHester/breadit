package me.williamhester.areddit;

import com.google.gson.JsonObject;

public class Thing {

    protected String mId;
    protected String mName;
    protected String mKind;

    protected Thing() { }

    protected Thing(JsonObject data) {
        if (data != null) {
            mId = data.getAsJsonObject("data").get("id").getAsString();
            mName = data.getAsJsonObject("data").get("name").getAsString();
            mKind = data.get("kind").getAsString();
        }
    }

    protected JsonObject mData;

    public String getId() {
        return mId;
    }

    public String getName() { 
        return mName;
    }

    public String getKind() {
        return mKind;
    }
}
