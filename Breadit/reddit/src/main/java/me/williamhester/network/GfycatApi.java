package me.williamhester.network;

import android.content.Context;
import android.provider.MediaStore;
import android.widget.ProgressBar;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.loader.VideoLoader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import me.williamhester.models.ResponseGfycatUrlCheck;
import me.williamhester.models.ResponseGfycatUrlUpload;

/**
 * Created by William on 6/14/14.
 */
public class GfycatApi {

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

    public static void downloadWebmGif(Context context, String url, String gifName,
                                       final ProgressBar progressBar, FutureCallback<File> callback) {
        String fileName = gifName + ".webm";
        Ion.with(context)
                .load(url)
                .progressBar(progressBar)
                .write(new File(context.getCacheDir(), fileName))
                .setCallback(callback);
        AsyncHttpClient.getDefaultInstance().executeFile(new AsyncHttpGet(url), fileName, new AsyncHttpClient.FileCallback() {
                @Override
                public void onProgress(AsyncHttpResponse response, long downloaded, long total) {
                    progressBar.setProgress((int) (downloaded * 100 / total));
                }

                @Override
                public void onCompleted(Exception e, AsyncHttpResponse response, File result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    System.out.println("my file is available at: " + result.getAbsolutePath());
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
