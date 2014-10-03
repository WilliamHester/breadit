package me.williamhester.network;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.ResponseCacheMiddleware;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.williamhester.models.ResponseGfycatUrlCheck;
import me.williamhester.models.ResponseGfycatUrlUpload;

/**
 * Created by William on 6/14/14.
 */
public class GfycatApi {

    private static final AsyncHttpClient mGfyClient = new AsyncHttpClient(AsyncServer.getDefault());

    private GfycatApi() {}

    public static void init(Context context) {
        try {
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

    public static void downloadWebmGif(Context context, String url,
                                       final ProgressBar progressBar,
                                       final VideoView videoView) {
        String fileName = context.getCacheDir() + "/gfy.webm";
        mGfyClient.executeFile(new AsyncHttpGet(url), fileName, new AsyncHttpClient.FileCallback() {
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
