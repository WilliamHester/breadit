package me.williamhester.network;

import android.content.Context;
import android.util.Log;
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
                .load("https://api.imgur.com/3/image/" + id)
                .addHeader(AUTHORIZATION, CLIENT_ID)
                .as(new TypeToken<ResponseImgurWrapper<ImgurImage>>(){})
                .setCallback(callback);
    }

    public static void getAlbumDetails(String id, Context context,
                                       FutureCallback<ResponseImgurWrapper<ImgurAlbum>> callback) {
        Ion.with(context)
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

    public static class ImgurLinkDetails {

        private String mUrl;
        private String mId;
        private int mType;

        public static final int IMAGE = 0;
        public static final int ALBUM = 1;
        public static final int GALLERY = 2;

        public ImgurLinkDetails(String url) {
            mUrl = url;
            generateDetails();
        }

        private void generateDetails() {
            int end = mUrl.indexOf('.', mUrl.indexOf(".com") + 4);
            end = end != -1 ? end : mUrl.length();
            int start = end - 1;
            while (mUrl.charAt(start) != '/') {
                start--;
            }
            mId = mUrl.substring(start + 1, end);
            while (mUrl.charAt(start) == '/') {
                start--;
            }
            char c = mUrl.charAt(start);
            switch (c) {
                case 'm': // imgur.com
                    mType = IMAGE;
                    break;
                case 'a': // imgur.com/a/
                    mType = ALBUM;
                    break;
                case 'y': // imgur.com/gallery/
                    mType = GALLERY;
                    break;
            }
        }

        public String getLinkId() {
            return mId;
        }

        public int getType() {
            return mType;
        }
    }

}
