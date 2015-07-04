package me.williamhester.network;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.cache.ResponseCacheMiddleware;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.williamhester.models.gfycat.GfycatResponse;
import me.williamhester.models.imgur.ImgurImage;
import me.williamhester.models.gfycat.GfycatUrlCheck;
import me.williamhester.models.gfycat.GfycatUrlUpload;

/**
 * Created by William on 6/14/14.
 */
public class GfycatApi {

    private static final AsyncHttpClient mGfyClient = new AsyncHttpClient(AsyncServer.getDefault());
    private static Ion mGfyIon;
    private static String mVideoFileName;

    private GfycatApi() {}

    public static void init(Context context) {
        mGfyIon = Ion.getInstance(context, "gfy");
        ArrayList<AsyncHttpClientMiddleware> middlewares = mGfyIon.getHttpClient().getMiddleware();
        for (AsyncHttpClientMiddleware middleware : middlewares) { // Set the cache size to 1MB. That's a lot of Imgur data
            if (middleware instanceof ResponseCacheMiddleware) {
                ((ResponseCacheMiddleware) middleware).getFileCache().setMaxSize(1024 * 256);
            }
        }
        try {
            mVideoFileName = context.getCacheDir() + "/gif.video";
            ResponseCacheMiddleware.addCache(mGfyClient,
                    new File(context.getCacheDir(), "gifcache"),
                    1024 * 1024 * 8);
        } catch (IOException e) {
            // Do nothing
        }
    }

    public static void checkIfLinkExists(Context context, String url,
                                         FutureCallback<GfycatUrlCheck> callback) {
        String checkUrl = "http://gfycat.com/cajax/checkUrl/" + encodeUrl(url);
        Ion.with(context)
                .load(checkUrl)
                .as(new TypeToken<GfycatUrlCheck>() {})
                .setCallback(callback);
    }

    public static void uploadOrConvertGif(Context context, String url,
                                          FutureCallback<GfycatUrlUpload> callback) {
        String uploadUrl = "http://upload.gfycat.com/transcode/0?fetchUrl=" + encodeUrl(url);
        Ion.with(context)
                .load(uploadUrl)
                .as(new TypeToken<GfycatUrlUpload>() {})
                .setCallback(callback);
    }

    public static void getGfyDetails(Context context, String gfyname,
                                     FutureCallback<GfycatResponse> callback) {
        String url = "http://gfycat.com/cajax/get/" + gfyname;
        mGfyIon.build(context)
                .load(url)
                .as(new TypeToken<GfycatResponse>() {})
                .setCallback(callback);
    }

    public static void downloadWebmGif(String url,
                                       final ProgressBar progressBar,
                                       final FutureCallback<File> callback) {
        mGfyClient.executeFile(new AsyncHttpGet(url), mVideoFileName, new AsyncHttpClient.FileCallback() {
            @Override
            public void onProgress(AsyncHttpResponse response, final long downloaded, final long total) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) (downloaded * 100 / total));
                    }
                });
            }

            @Override
            public void onCompleted(Exception e, final AsyncHttpResponse response, final File result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                callback.onCompleted(null, result);
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public static void downloadImgurGif(ImgurImage image,
                                       final ProgressBar progressBar,
                                       final FutureCallback<File> callback) {
        String url = "http://i.imgur.com/" + image.getId() + ".mp4";
        mGfyClient.executeFile(new AsyncHttpGet(url), mVideoFileName, new AsyncHttpClient.FileCallback() {
            @Override
            public void onProgress(AsyncHttpResponse response, final long downloaded, final long total) {
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress((int) (downloaded * 100 / total));
                    }
                });
            }

            @Override
            public void onCompleted(Exception e, final AsyncHttpResponse response, final File result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                callback.onCompleted(null, result);
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return url;
        }
    }

}
