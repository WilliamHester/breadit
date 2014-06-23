package me.williamhester.network;

import android.content.Context;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;

/**
 * Created by William on 6/14/14.
 */
public class ImgurApi {

    private static final String CLIENT_ID = "2bdd3ec7a3fa918";

    public static void getImageDetails(String id, Context context,
                                       FutureCallback<ResponseImgurWrapper<ImgurImage>> callback) {
        Ion.with(context)
                .load("https://api.imgur.com/3/image/" + id)
                .addHeader("Client-ID", CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurImage>>() {
                })
                .setCallback(callback);
    }

    public static void getAlbumDetails(String id, Context context,
                                       FutureCallback<ResponseImgurWrapper<ImgurAlbum>> callback) {
        Ion.with(context)
                .load("https://api.imgur.com/3/album/" + id)
                .addHeader("Client-ID", CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurAlbum>>(){})
                .setCallback(callback);
    }

    public static void loadImage(String url, ImageView imageView) {
        Ion.with(imageView)
                .animateIn(android.R.anim.fade_in)
                .load(url);
    }

}
