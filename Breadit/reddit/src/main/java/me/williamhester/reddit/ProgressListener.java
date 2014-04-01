package me.williamhester.reddit;

/**
 * Created by William on 3/31/14.
 */
public interface ProgressListener {

    /**
     * The method to be called as the percent is changed.
     * @param percent the percent (from 0 to 1 inclusive) that the item has been either downloaded
     *                or uploaded
     */
    public void onProgressUpdate(float percent);

}
