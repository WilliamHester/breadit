package me.williamhester.network;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Future;

import me.williamhester.models.ResponseGfycatUrlCheck;

/**
 * Created by William on 6/14/14.
 */
public class GfycatApi {

    public static void checkIfLinkExists(Context context, String url,
                                         FutureCallback<ResponseGfycatUrlCheck> callback) {
        try {
            String checkUrl = "http://gfycat.com/cajax/checkUrl/" + URLEncoder.encode(url, "utf-8");
            Ion.with(context)
                    .load(checkUrl)
                    .as(new TypeToken<ResponseGfycatUrlCheck>() {})
                    .setCallback(callback);
        } catch (UnsupportedEncodingException e) {
            Log.d("GfycatApi", "unsupported encoding exception");
        }
    }

    public static void uploadOrConvertGif(Context context, String url,
                                          FutureCallback<Object> callback) {

    }

}
