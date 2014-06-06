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

import me.williamhester.areddit.Account;

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

//    public static String imgurPostImage() {
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        HttpPost httpPost = new HttpPost("https://api.imgur.com/3/image");
//        httpPost.addHeader("User-Agent", USER_AGENT);
//        httpPost.addHeader("Authorization", "CLIENT-ID 2bdd3ec7a3fa918");
//
//        BufferedImage image = ImageIO.read(file);
//        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//        ImageIO.write(image, "png", byteArray);
//        byte[] byteImage = byteArray.toByteArray();
//        String imageString = new Base64().encodeAsString(byteImage);
//        return null;
//    }

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

    public static String formatHtml(String s) {
        int offset;
        int end = 0;
        int beginning = s.indexOf("a href=\"http://www.reddit.com/r/", end);
        int temp = s.indexOf("a href=\"http://reddit.com/r/", end);
        if ((beginning < temp && beginning == -1) || (temp < beginning && temp != -1)) {
            beginning = temp;
            offset = 26;
        } else {
            offset = 30;
        }
        StringBuilder sb = new StringBuilder();
        while (beginning != -1) {
            beginning = beginning == -1 ? s.length() : beginning;
            int i = s.indexOf("/", beginning + offset + 2);
            int j = s.indexOf("/\"", beginning + offset + 2);
            int k = s.indexOf("\"", beginning + offset + 2);

            sb.append(s.substring(end, beginning)); // what's in-between the link and the last link
            sb.append("a href=\"me.williamhester.breadit://"); // to start the link

            end = k + 1;
            if (i == -1 || i == j) { // definitely a subreddit
                sb.append("subreddit/");
                sb.append(s.substring(beginning + offset + 2, end));
            } else { // found a link to another post
                sb.append(s.substring(beginning + offset, end));
            }
            beginning = s.indexOf("a href=\"http://www.reddit.com/r/", end);
            temp = s.indexOf("a href=\"http://reddit.com/r/", end);
            if ((beginning < temp && beginning == -1) || (temp < beginning && temp != -1)) {
                beginning = temp;
                offset = 26;
            } else {
                offset = 30;
            }
        }
        if (end != s.length() - 1) {
            sb.append(s.substring(end, s.length()));
        }
        return sb.toString();
    }

}
