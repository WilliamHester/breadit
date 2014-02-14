package me.williamhester.areddit.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by William on 1/5/14.
 */
public class Utilities {

    private static final String USER_AGENT = "William's Reddit API wrapper";

    public static Object get(String apiParams, String url, String cookie)
            throws IOException, ParseException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet;
        httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent", USER_AGENT);
        if (cookie != null)
            httpGet.setHeader("cookie", "reddit_session=" + cookie);

        HttpResponse httpResponse = httpClient.execute(httpGet);
        String data;
        data = readStream(httpResponse.getEntity().getContent());

        JSONParser parser = new JSONParser();
        return parser.parse(data);
    }

    public static String readStream(InputStream in)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        try
        {
            while((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        } catch(Exception ex) {

        }
        finally
        {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        return sb.toString();
    }

}
