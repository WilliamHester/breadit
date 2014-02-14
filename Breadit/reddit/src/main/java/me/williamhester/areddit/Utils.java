package me.williamhester.areddit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

import java.lang.Thread;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * This class contains (or will contain) various utilities for jReddit.
 * 
 */
public class Utils {

    private static int SLEEP_TIME = 2000;

    private static final String USER_AGENT =  "William Hester's Reddit Java API Wrapper";

    /**
     * This function is here because I do this same request a hundred times
     * throughout jReddit and I wanted to inline the function somehow.
     * 
     * It basically submits a POST request and returns a JSON object that
     * corresponds to it.
     */
    public static JSONObject post(String apiParams, URL url, String cookie)
            throws IOException, ParseException {

        //
        // Adhere to API rules....
        // (Make this configurable)
        //        
        try {
            Thread.sleep(SLEEP_TIME);
        } catch(InterruptedException ie) {
            ie.printStackTrace();
            return null;
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Content-Length",
                String.valueOf(apiParams.length()));
        connection.setRequestProperty("cookie", "reddit_session=" + cookie);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(apiParams);
        wr.flush();
        wr.close();

        JSONParser parser = new JSONParser();
        Object object = parser.parse(new BufferedReader(new InputStreamReader(
                connection.getInputStream())).readLine());
        JSONObject jsonObject = (JSONObject) object;

        return jsonObject;
    }

    /**
     * This function submits a GET request and returns a JSON object that
     * corresponds to it.
     * @param url URL
     * @param cookie cookie
     */
    public static Object get(URL url, String cookie) throws IOException, ParseException{
        return get("", url, cookie);
    }

    /**
     * This function submits a GET request and returns a JSON object that
     * corresponds to it.
     *
     * @param apiParams HTTP arguments
     * @param url URL
     * @param cookie cookie
     */
    public static Object get(String apiParams, URL url, String cookie)
            throws IOException, ParseException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        // Don't pass cookie if it is null
        if (cookie != null) {
            connection.setRequestProperty("cookie", "reddit_session=" + cookie);
        }
        connection.setRequestProperty("User-Agent", USER_AGENT);


        // Debugging stuff
        // @author Karan Goel
        InputStream is = null;
        Scanner s = null;
        String response = null;
//        try {
//            if (connection.getResponseCode() != 200) {
//                s = new Scanner(connection.getErrorStream());
//            } else {
                is = connection.getInputStream();
                s = new Scanner(is);
//            }
            s.useDelimiter("\\Z");
            response = s.next();
////            For debugging purposes
////            System.out.println("\nResponse: " + response + "\n\n");
            s.close();
//        } catch (IOException e2) {
//            e2.printStackTrace();
//        }
        // Debugging stuff


        JSONParser parser = new JSONParser();
        System.out.println("response = " + response);
        return parser.parse(response);
    }

    /**
     *
     * Get a somewhat more human readable version of the JSON string.
     *
     */
    public static String getJSONDebugString(Object obj) {
        return getJSONDebugString(obj, "");
    }

    /**
     *
     * Get a somewhat more human readable version of the JSON string.
     *
     */
    public static String getJSONDebugString(Object obj, String indent) {

        String ret = "";

        //
        // Handle hashtable
        //
        if(obj instanceof HashMap) {
            ret += indent + "{\n";
            HashMap hash = (HashMap)obj;
            Iterator it = hash.keySet().iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                ret += indent + key + ": ";
                Object val = hash.get(key);
                ret += indent + getJSONDebugString(val, indent + "    ");
            }
            ret += indent + "}\n";
            return ret;
        }

        //
        // Handle array
        //
        if(obj instanceof ArrayList) {
            ret += indent + "[\n";
            ArrayList list = (ArrayList)obj;
            for(int i = 0; i < list.size(); i++) {
                Object val = list.get(i); 
                ret += indent + getJSONDebugString(val, indent + "    ");
            }
            ret += indent + "]\n";
            return ret;
        }

        //
        // No hashtable or array so this should be a primitive...
        //
        return ((obj == null) ? "null" : obj.toString()) + "\n";

    }


}
