package me.williamhester.models.utils;

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

import me.williamhester.models.Account;

public class Utilities {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static String get(String apiParams, String url, Account account) throws IOException {
        if (account != null) {
            return get(apiParams, url, account.getCookie(), account.getModhash());
        } else {
            return get(apiParams, url, null, null);
        }
    }

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

    public static String post(List<NameValuePair> apiParams, String url, Account account) {
        if (account != null) {
            return post(apiParams, url, account.getCookie(), removeEndQuotes(account.getModhash()));
        } else {
            return post(apiParams, url, null, null);
        }
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

    public static String calculateTimeShort(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            time = difference / 31536000 + "y";
        } else if (difference / 2592000 > 0) {
            time = difference / 2592000 + "mo";
        } else if (difference / 604800 > 0) {
            time = difference / 604800 + "w";
        } else if (difference / 86400 > 0) {
            time = difference / 86400 + "d";
        } else if (difference / 3600 > 0) {
            time = difference / 3600 + "h";
        } else if (difference / 60 > 0) {
            time = difference / 60 + "m";
        } else {
            time = difference + "s";
        }
        return time;
    }

    public static String removeEndQuotes(String s) {
        if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

}
