package me.williamhester.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 8/15/14.
 */
public class MoreComments extends Comment implements Serializable {
    private static final long serialVersionUID = 8781777510019675672L;

    private int mCount;
    private String mParentId;
    private String mId;
    private List<String> mChildren;
    private String mName;

    public MoreComments(JsonObject object) {
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

}
