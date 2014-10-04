package me.williamhester.network;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClientMiddleware;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.ResponseCacheMiddleware;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import me.williamhester.models.GfycatResponse;
import me.williamhester.models.ResponseGfycatUrlCheck;
import me.williamhester.models.ResponseGfycatUrlUpload;

/**
 * Created by William on 6/14/14.
 */
public class GfycatApi {

    private static final AsyncHttpClient mGfyClient = new AsyncHttpClient(AsyncServer.getDefault());
    private static Ion mGfyIon;
    private static String mGfyFileName;

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
            mGfyFileName = context.getCacheDir() + "/gfy.webm";
            ResponseCacheMiddleware.addCache(mGfyClient,
                    new File(context.getCacheDir(), "gifcache"),
                    1024 * 1024 * 8);
        } catch (IOException e) {
            // Do nothing
        }
    }

    public static void checkIfLinkExists(Context context, String url,
                                         FutureCallback<ResponseGfycatUrlCheck> callback) {
        String checkUrl = "http://gfycat.com/cajax/checkUrl/" + encodeUrl(url);
        Ion.with(context)
                .load(checkUrl)
                .as(new TypeToken<ResponseGfycatUrlCheck>() {})
                .setCallback(callback);
    }

    public static void uploadOrConvertGif(Context context, String url,
                                          FutureCallback<ResponseGfycatUrlUpload> callback) {
        String uploadUrl = "http://upload.gfycat.com/transcode/0?fetchUrl=" + encodeUrl(url);
        Ion.with(context)
                .load(uploadUrl)
                .as(new TypeToken<ResponseGfycatUrlUpload>() {})
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
                                       final VideoView videoView) {
        mGfyClient.executeFile(new AsyncHttpGet(url), mGfyFileName, new AsyncHttpClient.FileCallback() {
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
                    e.printStackTrace();
                    return;
                }
                videoView.post(new Runnable() {
                    @Override
                    public void run() {
                        videoView.setVideoPath(result.getAbsolutePath());
                        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                videoView.start();
                                result.delete();
                            }
                        });
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                videoView.start();
                            }
                        });
                    }
                });
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
