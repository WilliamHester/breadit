package me.williamhester.areddit;

import com.google.gson.JsonObject;


/**
 *
 * This class represents a reddit "thing"
 */
public abstract class Thing {

    public Thing(JsonObject data) {
        mData = data;
    }

    /**
     * this item's identifier, e.g. "8xwlg"
     */
    protected String mId;

	/**
	 * The full name of this thing.
	 */
	protected String mName;

	/**
	 * The kind of this thing. (i.e "t2" for users)
	 */
	protected String mKind;

    /**
     * A custom data structure used to hold valuable information. 
     * This object's format will follow the data structure respective 
     * of its kind. 
     */
    protected JsonObject mData;

    public String getId() {
        return mData.getAsJsonObject("data").get("id").getAsString();
    }

    public String getName() { 
        return mData.getAsJsonObject("data").get("name").getAsString();
    }

    public String getKind() {
        return mData.get("kind").getAsString();
        // return (String)((JSONObject)(mData.get("data"))).get("kind");
    }

    /**
     * This class and its subclasses should provide convenience methods for
     * accessing data. But if the underlying
     * json data changes or we do not provide the caller with
     * the required methods, they can obtain all underlying data directly 
     * using this method.
     */
    public JsonObject getData() { return mData; }

    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        return  indent + "Thing: \n" +
                indent + "   id: "   + getId()   + "\n" +
                indent + "   name: " + getName() + "\n" +
                indent + "   kind: " + getKind() + "\n";
    }

}
