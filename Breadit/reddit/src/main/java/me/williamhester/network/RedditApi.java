package me.williamhester.network;

import android.content.Context;
import android.text.Html;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.tools.HtmlParser;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    private static final String REDDIT_URL = "https://www.reddit.com";

    public static String SORT_TYPE_HOT = "";
    public static String SORT_TYPE_NEW = "new";
    public static String SORT_TYPE_RISING = "rising";
    public static String SORT_TYPE_CONTROVERSIAL = "controversial";
    public static String SORT_TYPE_TOP = "top";

    public static String SECONDARY_SORT_HOUR = "hour";
    public static String SECONDARY_SORT_DAY = "day";
    public static String SECONDARY_SORT_WEEK = "week";
    public static String SECONDARY_SORT_MONTH = "month";
    public static String SECONDARY_SORT_YEAR = "year";
    public static String SECONDARY_SORT_ALL = "all";

    public static void vote(Context context, Votable v) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/vote")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("dir", String.valueOf(v.getVoteStatus()))
                .setBodyParameter("id", v.getName())
                .asString();
    }

    public static void getRedditLiveData(Context context, Submission submission,
                                         final FutureCallback<ResponseRedditWrapper> callback) {
        Ion.with(context)
                .load(submission.getUrl() + "/about.json")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            e.printStackTrace();
                            callback.onCompleted(e, null);
                            return;
                        }
                        Gson gson = new Gson();
                        ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, gson);
                        callback.onCompleted(null, wrapper);
                    }
                });
    }

    public static void getSubmissions(Context context, String subredditName, String sortType,
                                      String secondarySort, String before, String after,
                                      FutureCallback<JsonObject> callback) {
        if (!subredditName.equals("")) {
            subredditName = "/r/" + subredditName;
        }
        Ion.with(context)
                .load(REDDIT_URL + subredditName + "/" + sortType + "/.json")
                .addQueries(generateSubmissionQueries(secondarySort, before, after))
                .addHeaders(generateUserHeaders())
                .addHeader("User-Agent", USER_AGENT)
                .asJsonObject()
                .setCallback(callback);
    }

    private static Map<String, List<String>> generateSubmissionQueries(String secondarySort,
                                                                         String before, String after) {
        Map<String, List<String>> queries = new HashMap<>();
//        if (secondarySort != null) {
//            ArrayList<String> query = new ArrayList<>();
//            query.add(secondarySort);
//            queries.put("t", query);
//        }
        if (before != null) {
            ArrayList<String> query = new ArrayList<>();
            query.add(before);
            queries.put("before", query);
        }
        if (after != null) {
            ArrayList<String> query = new ArrayList<>();
            query.add(after);
            queries.put("after", query);
        }
        return queries;
    }

    private static Map<String, List<String>> generateUserHeaders() {
        Account account = AccountManager.getAccount();
        Map<String, List<String>> headers = new HashMap<>();
        if (account != null) {
            ArrayList<String> list1 = new ArrayList<>();
            list1.add("reddit_session=" + account.getCookie());
            headers.put("Cookie", list1);
            ArrayList<String> list2 = new ArrayList<>();
            list2.add(account.getModhash().replace("\"", ""));
            headers.put("X-Modhash", list2);
        }
        return headers;
    }

    public static void getSubmissionData(Context context, String permalink,
                                         final FutureCallback<Submission> submissionCallback,
                                         final FutureCallback<List<Comment>> commentCallback) {
        Ion.with(context)
                .load(REDDIT_URL + permalink + ".json")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeaders(generateUserHeaders())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                            submissionCallback.onCompleted(e, null);
                            commentCallback.onCompleted(e, null);
                            return;
                        }
                        try {
                            Gson gson = new Gson();
                            JsonArray array = new JsonParser().parse(result).getAsJsonArray();
                            TypeToken<Submission> sub = new TypeToken<Submission>() {
                            };
                            Submission submission = gson.fromJson(array.get(0).getAsJsonObject()
                                            .get("data").getAsJsonObject()
                                            .get("children").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("data"),
                                    sub.getType());

                            ResponseRedditWrapper wrapper = new ResponseRedditWrapper(array.get(1).getAsJsonObject(), gson);
                            Listing listing = null;
                            if (wrapper.getData() instanceof Listing) {
                                listing = (Listing) wrapper.getData();
                            }

                            List<Comment> comments = new ArrayList<>();
                            for (ResponseRedditWrapper wrap : listing.getChildren()) {
                                Comment.CommentIterator iterator = new Comment.CommentIterator(wrap);
                                while (iterator.hasNext()) {
                                    comments.add(iterator.next());
                                }
                            }
                            submissionCallback.onCompleted(null, submission);
                            commentCallback.onCompleted(null, comments);
                        } catch (Exception e2) {
                            submissionCallback.onCompleted(e2, null);
                            commentCallback.onCompleted(e2, null);
                        }
                    }
                });
    }

    public static void editThing(Context context, final Votable thing, final FutureCallback<Votable> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/editusertext/")
                .addHeaders(generateUserHeaders())
                .addHeader("api_type", "json")
//                .setBodyParameter("text", thing.getRawMarkdown())
                .setBodyParameter("thing_id", thing.getName())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            callback.onCompleted(e, null);
                            return;
                        }
                        String escapedHtml = result.substring(result.indexOf("\"contentHTML\": ") + 16, result.indexOf(";\",") + 2);
                        HtmlParser parser = new HtmlParser(Html.fromHtml(escapedHtml).toString());
                        thing.setSpannableBody(parser.getSpannableString());
                        callback.onCompleted(null, thing);
                    }
                });
    }

}
