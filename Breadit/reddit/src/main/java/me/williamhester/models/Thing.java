package me.williamhester.models;

import com.google.gson.JsonObject;

public class Thing {

    protected String id;
    protected String name;
    protected String kind;
    protected Object data;

    public Thing() { }

    protected Thing(JsonObject data) {
        if (data != null) {
            id = data.getAsJsonObject("data").get("id").getAsString();
            name = data.getAsJsonObject("data").get("name").getAsString();
            kind = data.get("kind").getAsString();
        }
    }

    protected JsonObject mData;

    public String getId() {
        return id;
    }

    public String getName() { 
        return name;
    }

    public String getKind() {
        return kind;
    }
}
