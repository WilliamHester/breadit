package me.williamhester.reddit.models.reddit;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by william on 8/3/14.
 */
public class Listing {

  private String mModhash;
  private ArrayList<ResponseWrapper> mChildren;
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
      mChildren.add(new ResponseWrapper(element.getAsJsonObject(), gson));
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

  public List<ResponseWrapper> getChildren() {
    return mChildren;
  }
}
