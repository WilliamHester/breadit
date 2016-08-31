package me.williamhester.network;

import android.util.Log;

import retrofit2.Response;

/**
 * This class is a wrapper for network responses.
 *
 * Created by william on 1/5/16.
 */
public abstract class Callback<T> implements retrofit2.Callback<T> {

    /**
     * This method is always called after the network operation is complete, even if it fails.
     */
    public abstract void onCompleted();

    /**
     * Called if the response is successful.
     *
     * @param data the data from the response
     */
    public abstract void onSuccess(T data);

    /**
     * Called if the response is unsuccessful.
     */
    public void onFailure() {
        Log.d("Callback", "Network operation failed");
    }

    public void onResponse(Response<T> response) {
        onCompleted();
        if (response.isSuccess()) {
            onSuccess(response.body());
        } else {
            onFailure();
        }
    }

    public void onFailure(Throwable t) {
        onCompleted();
        onFailure();
    }


}
