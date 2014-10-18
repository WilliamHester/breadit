package me.williamhester.network;

import android.content.Context;
import android.text.Html;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.AsyncHttpRequestBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.williamhester.models.AbsComment;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
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

    public static void getSubredditDetails(Context context, String subredditName,
                                           final FutureCallback<Subreddit> callback) {
        if (subredditName != null && subredditName.length() > 0) {
            subredditName = "/r/" + subredditName;
        }
        Ion.with(context)
                .load(REDDIT_URL + subredditName + "/about.json")
                .addHeaders(generateUserHeaders())
                .addHeader("User-Agent", USER_AGENT)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            callback.onCompleted(e, null);
                            return;
                        }
                        ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, new Gson());
                        callback.onCompleted(null, (Subreddit) wrapper.getData());
                    }
                });
    }

    public static void subscribeSubreddit(Context context, boolean sub, Subreddit subreddit,
                                          FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/subscribe/")
                .addHeaders(generateUserHeaders())
                .addHeader("User-Agent", USER_AGENT)
                .setBodyParameter("action", sub ? "sub" : "unsub")
                .setBodyParameter("sr", subreddit.getName())
                .asString()
                .setCallback(callback);
    }

    private static Map<String, List<String>> generateSubmissionQueries(String secondarySort,
                                                                         String before, String after) {
        Map<String, List<String>> queries = new HashMap<>();
        if (secondarySort != null) {
            ArrayList<String> query = new ArrayList<>();
            query.add(secondarySort);
            queries.put("t", query);
        }
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
                                         final FutureCallback<List<AbsComment>> commentCallback) {
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

                            List<AbsComment> comments = new ArrayList<>();
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

    /**
     * This is not intentionally a monster of a method. The way the Reddit API is designed, the
     * /api/MoreChildren endpoint returns JSON, but it's not really the JSON you're looking for,
     * so in order to get the comment data, a second API call must be made to /api/info.json to
     * request the real comment data after creating the correct flattened-tree structure, then
     * that must be returned to the CommentFragment so that it can be put in place.
     * tl;dr: sorry
     *
     * @param context the context of the method call
     * @param linkId the id of the link
     * @param sortType the sort type of the comments
     * @param children the Strings that contain the names of the morechildren
     * @param baseLevel the level at which the morechildren comment is
     * @param callback the callback that returns the new piece of comments
     */
    public static void getMoreChildren(final Context context, String linkId, String sortType,
                                       List<String> children, final int baseLevel,
                                       final MoreCommentsCallback callback) {
        // Build the list of children separated by commas
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : children) {
            stringBuilder.append(s);
            stringBuilder.append(',');
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/morechildren");
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("link_id", linkId);
        body.addStringPart("children", stringBuilder.toString());
        body.addStringPart("sort", sortType);

        // If the user is logged in, add those headers too
        Account account = AccountManager.getAccount();
        if (account != null) {
            request.addHeader("Cookie", "reddit_session=" + account.getCookie());
            request.addHeader("X-Modhash", account.getModhash().replace("\"", ""));
        }
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if (e != null) {
                    e.printStackTrace();
                    callback.onFailure();
                    return;
                }
                try {
                    JSONArray array = result.getJSONObject("json").getJSONObject("data").getJSONArray("things");
                    ArrayList<String> names = new ArrayList<>();
                    final ArrayList<Integer> levels = new ArrayList<>();
                    ArrayList<String> parents = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i).getJSONObject("data");
                        names.add(o.getString("id"));
                        parents.add(o.getString("parent"));

                        int parentIndex = names.indexOf(parents.get(i));
                        if (parentIndex >= 0) { // The parent is contained within the other comments
                            levels.add(levels.get(parentIndex) + 1);
                        } else {
                            levels.add(baseLevel);
                        }
                    }

                    StringBuilder query = new StringBuilder();
                    for (String name : names) {
                        query.append(name);
                        query.append(',');
                    }
                    Ion.with(context)
                            .load(REDDIT_URL + "/api/info.json")
                            .addQuery("id", query.toString())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        callback.onFailure();
                                        return;
                                    }

                                    Gson gson = new Gson();
                                    ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, gson);
                                    ArrayList<Comment> comments = new ArrayList<>();
                                    if (wrapper.getData() instanceof Listing) {
                                        Listing listing = (Listing) wrapper.getData();
                                        for (int i = 0; i < levels.size(); i++) {
                                            Comment comment = (Comment) listing.getChildren().get(i).getData();
                                            comment.setLevel(levels.get(i));
                                            comments.add(comment);
                                        }
                                    }
                                    callback.onComplete(comments, null);
                                }
                            });
                } catch (Exception e2) {
                    e2.printStackTrace();
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

    public static interface MoreCommentsCallback {
        /**
         * @param comments the new subtree of comments
         * @param beforeId the id of the morecomments comment
         */
        public void onComplete(ArrayList<Comment> comments, String beforeId);
        public void onFailure();
    }

}
