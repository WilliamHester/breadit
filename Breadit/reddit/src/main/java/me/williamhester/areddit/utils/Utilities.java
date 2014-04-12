package me.williamhester.areddit.utils;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by William on 1/5/14.
 *
 */
public class Utilities {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static String get(String apiParams, String url, String cookie, String modhash)
            throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet;
        httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", USER_AGENT);
        if (cookie != null)
            httpGet.setHeader("Cookie", "reddit_session=" + cookie);
        if (modhash != null)
            httpGet.addHeader("X-Modhash", modhash);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        return readStream(httpResponse.getEntity().getContent());
    }

    public static String post(List<NameValuePair> apiParams, String url, String cookie,
                              String modhash) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("User-Agent", USER_AGENT);
        if (cookie != null)
            httpPost.addHeader("Cookie", "reddit_session=" + cookie);
        if (modhash != null)
            httpPost.addHeader("X-Modhash", modhash);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(apiParams));
            HttpResponse response = httpClient.execute(httpPost);
            return readStream(response.getEntity().getContent());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("Utilities", "Response returned null");

        return null;
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e("Utilities", e.toString());
            }
        }
        return sb.toString();
    }
}
