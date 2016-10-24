package me.williamhester.reddit.models.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by william on 7/29/14.
 */
public class RedditLiveResponse implements Parcelable {

  @SerializedName("type")
  private String mType;
  @SerializedName("payload")
  private String mPayload;

  public RedditLiveResponse() {
  }

  public String getType() {
    return mType;
  }

  public String getPayload() {
    return mPayload;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.mType);
    dest.writeString(this.mPayload);
  }

  private RedditLiveResponse(Parcel in) {
    this.mType = in.readString();
    this.mPayload = in.readString();
  }

  public static final Creator<RedditLiveResponse> CREATOR = new Creator<RedditLiveResponse>() {
    public RedditLiveResponse createFromParcel(Parcel source) {
      return new RedditLiveResponse(source);
    }

    public RedditLiveResponse[] newArray(int size) {
      return new RedditLiveResponse[size];
    }
  };
}
