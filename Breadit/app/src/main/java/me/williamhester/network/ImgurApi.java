package me.williamhester.network;

import android.content.Context;
import android.widget.ImageView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.cache.ResponseCacheMiddleware;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import me.williamhester.Auth;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.models.Submission;

/**
 * Created by William on 6/14/14.
 *
 * This class is a class that contains methods to fetch details about an Imgur image or album.
 */
public class ImgurApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String CLIENT_ID = "Client-ID " + Auth.IMGUR_CLIENT_ID;

    private static Ion mImgurClient;

    private ImgurApi() {}

    public static void init(Context context) {
        mImgurClient = Ion.getInstance(context, "imgur");
        mImgurClient.getHttpClient().getSSLSocketMiddleware().setSpdyEnabled(false);
        ArrayList<AsyncHttpClientMiddleware> middlewares = mImgurClient.getHttpClient().getMiddleware();
        for (AsyncHttpClientMiddleware middleware : middlewares) { // Set the cache size to 1MB. That's a lot of Imgur data
            if (middleware instanceof ResponseCacheMiddleware) {
                ((ResponseCacheMiddleware) middleware).getFileCache().setMaxSize(1024 * 1024);
            }
        }
    }

    public static void getImageDetails(String id, Context context, final Submission submission,
                                       final FutureCallback<Submission> callback) {
        mImgurClient.build(context)
                .load("https://api.imgur.com/3/image/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurImage>>(){})
                .setCallback(new FutureCallback<ResponseImgurWrapper<ImgurImage>>() {
                    @Override
                    public void onCompleted(Exception e, ResponseImgurWrapper<ImgurImage> result) {
                        if (e == null) {
                            submission.setImgurData(result.getData());
                            callback.onCompleted(null, submission);
                        } else {
                            callback.onCompleted(e, null);
                        }
                    }
                });
    }

    public static void getImageDetails(String id, Context context,
                                       final FutureCallback<ResponseImgurWrapper<ImgurImage>> callback) {
        mImgurClient.build(context)
                .load("https://api.imgur.com/3/image/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurImage>>(){})
                .setCallback(callback);
    }

    public static void getAlbumDetails(String id, Context context, final Submission submission,
                                       final FutureCallback<Submission> callback) {
        mImgurClient.build(context)
                .load("https://api.imgur.com/3/album/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurAlbum>>(){})
                .setCallback(new FutureCallback<ResponseImgurWrapper<ImgurAlbum>>() {
                    @Override
                    public void onCompleted(Exception e, ResponseImgurWrapper<ImgurAlbum> result) {
                        if (e == null) {
                            submission.setImgurData(result.getData());
                            callback.onCompleted(null, submission);
                        } else {
                            callback.onCompleted(e, null);
                        }
                    }
                });
    }

    public static void getAlbumDetails(String id, Context context,
                                       final FutureCallback<ResponseImgurWrapper<ImgurAlbum>> callback) {
        mImgurClient.build(context)
                .load("https://api.imgur.com/3/album/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurAlbum>>(){})
                .setCallback(callback);
    }

    public static void loadImage(String url, ImageView imageView, FutureCallback<ImageView> callback) {
        Ion.with(imageView)
                .animateIn(android.R.anim.fade_in)
                .load(url)
                .setCallback(callback);
    }

}
