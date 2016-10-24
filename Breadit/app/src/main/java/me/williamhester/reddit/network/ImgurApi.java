package me.williamhester.reddit.network;

import android.content.Context;
import android.widget.ImageView;

import com.google.gson.reflect.TypeToken;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.cache.ResponseCacheMiddleware;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import me.williamhester.reddit.models.imgur.ImgurAlbum;
import me.williamhester.reddit.models.imgur.ImgurImage;
import me.williamhester.reddit.models.imgur.ImgurResponseWrapper;
import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.BuildConfig;

/**
 * Created by William on 6/14/14.
 *
 * This class is a class that contains methods to fetch details about an Imgur image or album.
 */
public class ImgurApi {

  private static final String AUTHORIZATION = "Authorization";
  private static final String CLIENT_ID = "Client-ID " + BuildConfig.IMGUR_CLIENT_ID;

  private static Ion mImgurClient;

  private ImgurApi() {
  }

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
        .as(new TypeToken<ImgurResponseWrapper<ImgurImage>>() {
        })
        .setCallback(new FutureCallback<ImgurResponseWrapper<ImgurImage>>() {
          @Override
          public void onCompleted(Exception e, ImgurResponseWrapper<ImgurImage> result) {
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
                                     final FutureCallback<ImgurResponseWrapper<ImgurImage>> callback) {
    mImgurClient.build(context)
        .load("https://api.imgur.com/3/image/" + id)
        .addHeader(AUTHORIZATION, CLIENT_ID)
        .as(new TypeToken<ImgurResponseWrapper<ImgurImage>>() {
        })
        .setCallback(callback);
  }

  public static void getAlbumDetails(String id, Context context, final Submission submission,
                                     final FutureCallback<Submission> callback) {
    mImgurClient.build(context)
        .load("https://api.imgur.com/3/album/" + id)
        .addHeader(AUTHORIZATION, CLIENT_ID)
        .as(new TypeToken<ImgurResponseWrapper<ImgurAlbum>>() {
        })
        .setCallback(new FutureCallback<ImgurResponseWrapper<ImgurAlbum>>() {
          @Override
          public void onCompleted(Exception e, ImgurResponseWrapper<ImgurAlbum> result) {
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
                                     final FutureCallback<ImgurResponseWrapper<ImgurAlbum>> callback) {
    mImgurClient.build(context)
        .load("https://api.imgur.com/3/album/" + id)
        .addHeader(AUTHORIZATION, CLIENT_ID)
        .as(new TypeToken<ImgurResponseWrapper<ImgurAlbum>>() {
        })
        .setCallback(callback);
  }

  public static void loadImage(String url, ImageView imageView, FutureCallback<ImageView> callback) {
    Ion.with(imageView)
        .animateIn(android.R.anim.fade_in)
        .load(url)
        .setCallback(callback);
  }

}
