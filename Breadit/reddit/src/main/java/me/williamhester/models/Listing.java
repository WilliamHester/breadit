package me.williamhester.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 8/3/14.
 */
public class Listing implements Serializable {
    private static final long serialVersionUID = 4321840693498849062L;

    private String mModhash;
    private ArrayList<ResponseRedditWrapper> mChildren;
    private String mAfter;
    private String mBefore;

    public Listing(JsonObject object, Gson gson) {
        mModhash = object.get("modhash").getAsString();
        if (!object.get("before").isJsonNull()) {
            mBefore = object.get("before").getAsString();
        }
        if (!object.get("after").isJsonNull()) {
            mAfter = object.get("after").getAsString();
        }
        mChildren = new ArrayList<>();
        JsonArray children = object.get("children").getAsJsonArray();
        for (JsonElement element : children) {
            mChildren.add(new ResponseRedditWrapper(element.getAsJsonObject(), gson));
        }
    }

    public String getModhash() {
        return mModhash;
    }

    public String getAfter() {
        return mAfter;
    }

    public String getBefore() {
        return mBefore;
    }

    public int size() {
        if (mChildren == null) {
            return 0;
        }
        return mChildren.size();
    }

    public List<ResponseRedditWrapper> getChildren() {
        return mChildren;
    }
}
