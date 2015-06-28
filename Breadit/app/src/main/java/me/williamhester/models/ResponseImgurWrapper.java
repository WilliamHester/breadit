package me.williamhester.models;

/**
 * Created by william on 6/21/14.
 *
 */
public class ResponseImgurWrapper<T> {

    private T data;
    private boolean success;
    private int status;

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

}
