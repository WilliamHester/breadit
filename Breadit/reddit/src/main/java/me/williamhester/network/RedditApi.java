package me.williamhester.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
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
import me.williamhester.models.Thing;
import me.williamhester.models.Votable;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static final String REDDIT_URL = "https://www.reddit.com";

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

    public static String COMMENT_SORT_HOT = "hot";
    public static String COMMENT_SORT_NEW = "new";
    public static String COMMENT_SORT_CONTROVERSIAL = "controversial";
    public static String COMMENT_SORT_OLD = "old";
    public static String COMMENT_SORT_TOP = "top";
    public static String COMMENT_SORT_BEST = "best";

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

    public static void getSubmissionData(Context context, String permalink, String sortType,
                                         final FutureCallback<Submission> submissionCallback,
                                         final FutureCallback<List<AbsComment>> commentCallback) {
        Ion.with(context)
                .load(REDDIT_URL + permalink + ".json")
                .addQuery("sort", sortType)
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
                                       final FutureCallback<ArrayList<Thing>> callback) {
        // Build the list of children separated by commas
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : children) {
            stringBuilder.append(s);
            stringBuilder.append(',');
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("link_id", linkId);
        body.addStringPart("children", stringBuilder.toString());
        body.addStringPart("sort", sortType);

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/morechildren");
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                getVotableDataFromNames(result, baseLevel, context, callback);
            }
        });
    }

    private static void getVotableDataFromNames(JSONObject result, int baseLevel, Context context,
                                                final FutureCallback<ArrayList<Thing>> callback) {
        try {
            JSONArray array = result.getJSONObject("json").getJSONObject("data").getJSONArray("things");
            final ArrayList<String> names = new ArrayList<>();
            final ArrayList<Integer> levels = new ArrayList<>();
            ArrayList<String> parents = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                JSONObject data = o.getJSONObject("data");
                names.add(data.getString("id"));
                if (!o.getString("kind").equals("t3")) {
                    parents.add(data.getString("parent"));

                    int parentIndex = names.indexOf(parents.get(i));
                    if (parentIndex >= 0) { // The parent is contained within the other comments
                        levels.add(levels.get(parentIndex) + 1);
                    } else {
                        levels.add(baseLevel);
                    }
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
                    .addHeaders(generateUserHeaders())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                callback.onCompleted(e, null);
                                return;
                            }

                            Gson gson = new Gson();
                            ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, gson);
                            ArrayList<Thing> comments = new ArrayList<>();
                            if (wrapper.getData() instanceof Listing) {
                                Listing listing = (Listing) wrapper.getData();
                                for (int i = 0; i < names.size(); i++) {
                                    Thing comment = (Thing) listing.getChildren().get(i).getData();
                                    if (comment instanceof Comment) {
                                        ((Comment) comment).setLevel(levels.get(i));
                                    }
                                    comments.add(comment);
                                }
                            }
                            callback.onCompleted(null, comments);
                        }
                    });
        } catch (Exception e2) {
            callback.onCompleted(e2, null);
        }
    }

    public static void hide(Context context, Submission submission,
                            FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/hide")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void unhide(Context context, Submission submission,
                              FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/unhide")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void delete(Context context, Votable votable, FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/del")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", votable.getName())
                .asString()
                .setCallback(callback);
    }

    public static void markNsfw(Context context, Submission submission,
                                FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/marknsfw")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void unmarkNsfw(Context context, Submission submission,
                                  FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/unmarknsfw")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void approve(Context context, Submission submission,
                                  FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/approve")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void remove(Context context, Submission submission, boolean spam,
                              FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/remove")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("id", submission.getName())
                .setBodyParameter("spam", String.valueOf(spam))
                .asString()
                .setCallback(callback);
    }

    public static void setContestMode(Context context, boolean isContest, Submission submission,
                                      FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/set_contest_mode")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("id", submission.getName())
                .setBodyParameter("state", String.valueOf(isContest))
                .asString()
                .setCallback(callback);
    }

    public static void setSubredditSticky(Context context, boolean isSticky, Submission submission,
                                          FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/set_subreddit_sticky")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("id", submission.getName())
                .setBodyParameter("state", String.valueOf(isSticky))
                .asString()
                .setCallback(callback);
    }

    public static void replyToComment(final Context context, final Thing thing, String text,
                             final FutureCallback<ArrayList<Thing>> callback) {
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("parent", thing.getName());
        body.addStringPart("text", text);

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/comment/");
        Account account = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=" + account.getCookie().replace("\"", ""));
        request.addHeader("X-Modhash", account.getModhash().replace("\"", ""));
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                int level = thing instanceof Comment ? ((Comment) thing).getLevel() + 1 : 0;
                getVotableDataFromNames(result, level, context, callback);
            }
        });
    }

    public static void editThing(final Context context, final Votable votable,
                                 final FutureCallback<ArrayList<Thing>> callback) {
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("thing_id", votable.getName());
        body.addStringPart("text", votable.getRawMarkdown());

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/editusertext/");
        Account account = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=" + account.getCookie().replace("\"", ""));
        request.addHeader("X-Modhash", account.getModhash().replace("\"", ""));
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeJSONObject(request, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                int level = votable instanceof Comment ? ((Comment) votable).getLevel() : 0;
                getVotableDataFromNames(result, level, context, callback);
            }
        });
    }

    public static void getUserDetails(Context context, String username, String after,
                                      FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/user/" + username + "/.json")
                .addHeaders(generateUserHeaders())
                .addQuery("after", after)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void needsCaptcha(Context context, FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/needs_captcha/.json")
                .addHeaders(generateUserHeaders())
                .asString()
                .setCallback(callback);
    }

    public static void getCaptcha(Context context, FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/new_captcha")
                .setBodyParameter("api_type", "json")
                .asJsonObject()
                .setCallback(callback);
    }

    public static void compose(Context context, String to, String subject, String body,
                               FutureCallback<JsonObject> callback) {
        compose(context, "", "", to, subject, body, callback);
    }

    public static void compose(Context context, String iden, String captchaRespnse, String to,
                               String subject, String body, FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/compose")
                .addHeaders(generateUserHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("iden", iden)
                .setBodyParameter("captcha", captchaRespnse)
                .setBodyParameter("subject", subject)
                .setBodyParameter("text", body)
                .setBodyParameter("to", to)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void printOutLongString(String string) {
        for (int i = 0; i < string.length(); i += 1000) {
            Log.d("RedditApi", string.substring(i, Math.min(string.length(), i + 1000)));
        }
    }

}
