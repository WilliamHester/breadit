package me.williamhester.models.voat;

/**
 * Created by william on 7/3/15.
 */
public class VoatResponseWrapper<T> {

    private boolean success;
    private T data;

    public boolean getSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

}
