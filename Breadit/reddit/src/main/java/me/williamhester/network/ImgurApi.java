package me.williamhester.network;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;

/**
 * Created by William on 6/14/14.
 *
 * This class is a class that contains methods to fetch details about an Imgur image or album.
 */
public class ImgurApi {

    private static final String AUTHORIZATION = "Authorization";
    private static final String CLIENT_ID = "Client-ID 2bdd3ec7a3fa918";

    private ImgurApi() {}

    public static void getImageDetails(String id, Context context,
                                       FutureCallback<ResponseImgurWrapper<ImgurImage>> callback) {
        Ion.with(context)
                .load("http://api.imgur.com/3/image/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurImage>>(){})
                .setCallback(callback);
    }

    public static void getAlbumDetails(String id, Context context,
                                       FutureCallback<ResponseImgurWrapper<ImgurAlbum>> callback) {
        Ion.with(context)
                .load("http://api.imgur.com/3/album/" + id)
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

    public static String getImageIdFromUrl(String url) {
        int end = url.indexOf('.', url.indexOf(".com") + 4);
        end = end != -1 ? end : url.length();
        int start = end;
        while (url.charAt(start) != '/') {
            start--;
        }
        start++;
        return url.substring(start, end);
    }

}
