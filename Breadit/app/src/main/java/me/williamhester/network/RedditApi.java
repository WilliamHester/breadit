package me.williamhester.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.williamhester.models.reddit.RedditAbsComment;
import me.williamhester.models.reddit.RedditAccount;
import me.williamhester.models.AccountManager;
import me.williamhester.models.reddit.RedditComment;
import me.williamhester.models.reddit.RedditFriend;
import me.williamhester.models.reddit.RedditGenericListing;
import me.williamhester.models.reddit.RedditGenericResponseWrapper;
import me.williamhester.models.reddit.RedditListing;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.models.reddit.RedditResponseWrapper;
import me.williamhester.models.reddit.RedditSubreddit;
import me.williamhester.models.reddit.RedditThing;
import me.williamhester.models.reddit.RedditUser;
import me.williamhester.models.reddit.RedditVotable;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    public static final String REDDIT_URL = "https://api.reddit.com";
    public static final String PUBLIC_REDDIT_URL = "https://www.reddit.com";

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

    public static void logIn(Context context, String username, String password,
                             FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/login/" + username)
                .setBodyParameter("api_type", "json")
                .setBodyParameter("user", username)
                .setBodyParameter("passwd", password)
                .setBodyParameter("rem", "true")
                .asJsonObject()
                .setCallback(callback);
    }

    public static void vote(Context context, RedditVotable v) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/vote")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("dir", String.valueOf(v.getVoteValue()))
                .setBodyParameter("id", v.getName())
                .asString();
    }

    public static void getRedditLiveData(Context context, RedditSubmission redditSubmission,
                                         final FutureCallback<RedditResponseWrapper> callback) {
        Ion.with(context)
                .load(redditSubmission.getUrl() + "/about.json")
                .addHeaders(getStandardHeaders())
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
                        RedditResponseWrapper wrapper = new RedditResponseWrapper(result, gson);
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
                .addHeaders(getStandardHeaders())
                .asJsonObject()
                .setCallback(callback);
    }

    public static void getSubredditDetails(Context context, String subredditName,
                                           final FutureCallback<JsonObject> callback) {
        if (subredditName != null && subredditName.length() > 0) {
            subredditName = "/r/" + subredditName;
        }
        Ion.with(context)
                .load(REDDIT_URL + subredditName + "/about.json")
                .addHeaders(getStandardHeaders())
                .asJsonObject()
                .setCallback(callback);
    }

    public static void getDefaultSubreddits(FutureCallback<ArrayList<RedditSubreddit>> callback) {
        getSubreddits(callback, "default", null, new ArrayList<RedditSubreddit>(), new Gson());
    }

    public static void getSubscribedSubreddits(FutureCallback<ArrayList<RedditSubreddit>> callback) {
        getSubreddits(callback, "mine", "", new ArrayList<RedditSubreddit>(), new Gson());
    }

    private static void getSubreddits(final FutureCallback<ArrayList<RedditSubreddit>> callback,
                                      final String type, String after,
                                      final ArrayList<RedditSubreddit> redditSubreddits, final Gson gson) {
        String requestUrl = String.format(REDDIT_URL + "/redditSubreddits/%s/?after=%s", type, after);
        AsyncHttpRequest request = new AsyncHttpGet(requestUrl);
        if (AccountManager.isLoggedIn()) {
            RedditAccount redditAccount = AccountManager.getAccount();
            request.addHeader("Cookie", "reddit_session=\"" + redditAccount.getCookie() + "\"");
            request.addHeader("X-Modhash", redditAccount.getModhash());
        }
        request.addHeader("RedditUser-Agent", USER_AGENT);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                try {
                    JsonObject object = new JsonParser().parse(result).getAsJsonObject();
                    RedditResponseWrapper wrapper = new RedditResponseWrapper(object, gson);
                    if (wrapper.getData() instanceof RedditListing) {
                        RedditListing redditListing = (RedditListing) wrapper.getData();
                        for (RedditResponseWrapper wrapper1 : redditListing.getChildren()) {
                            if (wrapper1.getData() instanceof RedditSubreddit) {
                                redditSubreddits.add((RedditSubreddit) wrapper1.getData());
                            }
                        }
                        if (redditListing.getAfter() != null) {
                            getSubreddits(callback, type, redditListing.getAfter(), redditSubreddits, gson);
                        } else {
                            callback.onCompleted(null, redditSubreddits);
                        }
                    }
                } catch (Exception e2) {
                    callback.onCompleted(e2, null);
                }
            }
        });
    }

    public static void subscribeSubreddit(Context context, boolean sub, RedditSubreddit redditSubreddit,
                                          FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/subscribe/")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("action", sub ? "sub" : "unsub")
                .setBodyParameter("sr", redditSubreddit.getName())
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

    private static Map<String, List<String>> getStandardHeaders() {
        RedditAccount redditAccount = AccountManager.getAccount();
        Map<String, List<String>> headers = new HashMap<>();
        ArrayList<String> userAgent = new ArrayList<>(1);
        userAgent.add(USER_AGENT);
        headers.put("RedditUser-Agent", userAgent);
        ArrayList<String> contentType = new ArrayList<>(1);
        userAgent.add("application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Content-Type", contentType);
        if (redditAccount != null) {
            ArrayList<String> list1 = new ArrayList<>(1);
            list1.add("reddit_session=\"" + redditAccount.getCookie() + "\"");
            headers.put("Cookie", list1);
            ArrayList<String> list2 = new ArrayList<>(1);
            list2.add(redditAccount.getModhash());
            headers.put("X-Modhash", list2);
        }
        return headers;
    }

    public static void getSubmissionData(Context context, String permalink, String sortType,
                                         final FutureCallback<RedditSubmission> submissionCallback,
                                         final FutureCallback<List<RedditAbsComment>> commentCallback) {
        Ion.with(context)
                .load(REDDIT_URL + permalink)
                .addQuery("sort", sortType)
                .addHeaders(getStandardHeaders())
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
                            TypeToken<RedditSubmission> sub = new TypeToken<RedditSubmission>() {
                            };
                            RedditSubmission redditSubmission = gson.fromJson(array.get(0).getAsJsonObject()
                                            .get("data").getAsJsonObject()
                                            .get("children").getAsJsonArray().get(0).getAsJsonObject()
                                            .get("data"),
                                    sub.getType());

                            RedditResponseWrapper wrapper = new RedditResponseWrapper(array.get(1).getAsJsonObject(), gson);
                            RedditListing redditListing = null;
                            if (wrapper.getData() instanceof RedditListing) {
                                redditListing = (RedditListing) wrapper.getData();
                            }

                            List<RedditAbsComment> comments = new ArrayList<>();
                            for (RedditResponseWrapper wrap : redditListing.getChildren()) {
                                RedditComment.CommentIterator iterator = new RedditComment.CommentIterator(wrap);
                                while (iterator.hasNext()) {
                                    comments.add(iterator.next());
                                }
                            }
                            submissionCallback.onCompleted(null, redditSubmission);
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
     * @param linkId the id of the link
     * @param sortType the sort type of the comments
     * @param children the Strings that contain the names of the morechildren
     * @param baseLevel the level at which the morechildren comment is
     * @param callback the callback that returns the new piece of comments
     */
    public static void getMoreChildren(String linkId, String sortType,
                                       List<String> children, final int baseLevel,
                                       final FutureCallback<ArrayList<RedditThing>> callback) {
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
        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                getVotableDataFromNames(result, baseLevel, callback);
            }
        });
    }

    private static void getVotableDataFromNames(String result, int baseLevel,
                                                final FutureCallback<ArrayList<RedditThing>> callback) {
        // Just in case Reddit gives us a weird response, this will be wrapped in a try-catch
        try {
            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            JsonObject json = object.get("json").getAsJsonObject();

            if (json.has("errors")) {
                JsonArray errors = json.get("errors").getAsJsonArray();
                if (errors.size() > 0) {
                    // There was an error, probably because the submission was archived.
                    if (errors.toString().contains("TOO_OLD")) {
                        // it was archived
                        callback.onCompleted(new ArchivedSubmissionException(), null);
                    }
                    return;
                }
            }

            JsonArray array = json.get("data").getAsJsonObject()
                    .get("redditThings").getAsJsonArray();
            ArrayList<RedditThing> redditThings = new ArrayList<>();
            Gson gson = new Gson();
            for (int i = 0; i < array.size(); i++) {
                RedditResponseWrapper wrapper =
                        new RedditResponseWrapper(array.get(i).getAsJsonObject(), gson);
                RedditThing data = (RedditThing) wrapper.getData();

                // if it's a comment, we need to get its relative depth in the tree
                if (data instanceof RedditAbsComment) {
                    boolean foundParent = false;
                    for (RedditThing redditThing : redditThings) {
                        if (!(redditThing instanceof RedditComment)) {
                            continue;
                        }
                        RedditComment c = (RedditComment) redditThing;
                        if (c.getName().equals(((RedditAbsComment) data).getParentId())) {
                            ((RedditAbsComment) data).setLevel(c.getLevel() + 1);
                            foundParent = true;
                        }
                    }
                    if (!foundParent) {
                        ((RedditAbsComment) data).setLevel(baseLevel);
                    }
                }
                redditThings.add(data);
            }

            callback.onCompleted(null, redditThings);
        } catch (Exception e2) {
            printOutLongString(result.toString());
            callback.onCompleted(e2, null);
        }
    }

    public static void hide(Context context, RedditSubmission redditSubmission,
                            FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/hide")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void unhide(Context context, RedditSubmission redditSubmission,
                              FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/unhide")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void delete(Context context, RedditVotable redditVotable, FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/del")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditVotable.getName())
                .asString()
                .setCallback(callback);
    }

    public static void markNsfw(Context context, RedditSubmission redditSubmission,
                                FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/marknsfw")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void unmarkNsfw(Context context, RedditSubmission redditSubmission,
                                  FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/unmarknsfw")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void approve(Context context, RedditSubmission redditSubmission,
                                  FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/approve")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .asString()
                .setCallback(callback);
    }

    public static void remove(Context context, RedditSubmission redditSubmission, boolean spam,
                              FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/remove")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", redditSubmission.getName())
                .setBodyParameter("spam", String.valueOf(spam))
                .asString()
                .setCallback(callback);
    }

    public static void setContestMode(Context context, boolean isContest, RedditSubmission redditSubmission,
                                      FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/set_contest_mode")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("id", redditSubmission.getName())
                .setBodyParameter("state", String.valueOf(isContest))
                .asString()
                .setCallback(callback);
    }

    public static void setSubredditSticky(Context context, boolean isSticky, RedditSubmission redditSubmission,
                                          FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/set_subreddit_sticky")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("id", redditSubmission.getName())
                .setBodyParameter("state", String.valueOf(isSticky))
                .asString()
                .setCallback(callback);
    }

    public static void replyToComment(final Context context, final RedditThing redditThing, String text,
                             final FutureCallback<ArrayList<RedditThing>> callback) {
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("parent", redditThing.getName());
        body.addStringPart("text", text);

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/comment/");
        RedditAccount redditAccount = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=\"" + redditAccount.getCookie() + "\"");
        request.addHeader("X-Modhash", redditAccount.getModhash());
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                int level = redditThing instanceof RedditComment ? ((RedditComment) redditThing).getLevel() + 1 : 0;
                getVotableDataFromNames(result, level, callback);
            }
        });
    }

    public static void editThing(final RedditVotable redditVotable,
                                 final FutureCallback<ArrayList<RedditThing>> callback) {
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addStringPart("api_type", "json");
        body.addStringPart("thing_id", redditVotable.getName());
        body.addStringPart("text", redditVotable.getBodyMarkdown());

        AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/editusertext/");
        RedditAccount redditAccount = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=\"" + redditAccount.getCookie() + "\"");
        request.addHeader("X-Modhash", redditAccount.getModhash());
        request.setBody(body);
        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
                if (e != null) {
                    callback.onCompleted(e, null);
                    return;
                }
                int level = redditVotable instanceof RedditComment ? ((RedditComment) redditVotable).getLevel() : 0;
                getVotableDataFromNames(result, level, callback);
            }
        });
    }

    public static void getUserContent(Context context, String username, String after, String type,
                                      FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/user/" + username + "/" + type)
                .addHeaders(getStandardHeaders())
                .addQuery("after", after)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void getUserAbout(Context context, String username,
                                    FutureCallback<RedditGenericResponseWrapper<RedditUser>> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/user/" + username + "/about.json")
                .addHeaders(getStandardHeaders())
                .as(new TypeToken<RedditGenericResponseWrapper<RedditUser>>() {
                })
                .setCallback(callback);
    }

    public static void needsCaptcha(Context context, FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/needs_captcha/.json")
                .addHeaders(getStandardHeaders())
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
                .addHeaders(getStandardHeaders())
                .setBodyParameter("api_type", "json")
                .setBodyParameter("iden", iden)
                .setBodyParameter("captcha", captchaRespnse)
                .setBodyParameter("subject", subject)
                .setBodyParameter("text", body)
                .setBodyParameter("to", to)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void getSuggestedTitle(Context context, String url,
                                         FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/fetch_title")
                .addHeaders(getStandardHeaders())
                .setBodyParameter("url", url)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void submit(Context context, Map<String, List<String>> params, String subreddit,
                              FutureCallback<JsonObject> callback) {
        submit(context, params, subreddit, "", "", callback);
    }

    public static void submit(Context context, Map<String, List<String>> params, String subreddit,
                              String iden, String response, FutureCallback<JsonObject> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/submit")
                .addHeaders(getStandardHeaders())
                .setBodyParameters(params) // Sets the body parameters generated by the SubmitFragment
                .setBodyParameter("api_type", "json")
                .setBodyParameter("extension", "json")
                .setBodyParameter("sr", subreddit)
                .setBodyParameter("then", "comments")
                .setBodyParameter("resubmit", "false")
                .setBodyParameter("iden", iden)
                .setBodyParameter("captcha", response)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void markMessageRead(Context context, boolean read, String messageFullname,
                                       FutureCallback<String> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/" + (read ? "read_message" : "unread_message"))
                .addHeaders(getStandardHeaders())
                .setBodyParameter("id", messageFullname)
                .asString()
                .setCallback(callback);
    }

    public static void getMessages(Context context, String type, String after,
                                   FutureCallback<JsonObject> callback) {
        Map<String, List<String>> queries = new HashMap<>();
        if (after != null) {
            queries.put("after", new ArrayList<String>());
            queries.get("after").add(after);
        }
        Ion.with(context)
                .load(REDDIT_URL + "/message/" + type + "/")
                .addHeaders(getStandardHeaders())
                .addQueries(queries)
                .asJsonObject()
                .setCallback(callback);
    }

    public static void getMe(final FutureCallback<JsonObject> callback) {
        AsyncHttpRequest request = new AsyncHttpGet(REDDIT_URL + "/api/me.json");
        RedditAccount redditAccount = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=\"" + redditAccount.getCookie() + "\"");
        request.addHeader("X-Modhash", redditAccount.getModhash());
        request.addHeader("RedditUser-Agent", USER_AGENT);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(final Exception e, AsyncHttpResponse source, final String result) {
                AsyncHttpClient.getDefaultInstance().getServer().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(e, new JsonParser().parse(result).getAsJsonObject());
                    }
                });
            }
        });
    }

    public static void getFriends(final FutureCallback<ArrayList<RedditFriend>> callback) {
        AsyncHttpRequest request = new AsyncHttpGet(REDDIT_URL + "/api/v1/me/friends");
        RedditAccount redditAccount = AccountManager.getAccount();
        request.addHeader("Cookie", "reddit_session=\"" + redditAccount.getCookie() + "\"");
        request.addHeader("X-Modhash", redditAccount.getModhash());
        request.addHeader("RedditUser-Agent", USER_AGENT);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
            @Override
            public void onCompleted(final Exception e, AsyncHttpResponse source, String result) {
                Handler handler = new Handler(Looper.getMainLooper());
                if (e != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCompleted(e, null);
                        }
                    });
                    return;
                }

                Gson gson = new Gson();
                JsonObject object = new JsonParser().parse(result).getAsJsonObject();
                JsonArray children = object.get("data")
                        .getAsJsonObject()
                        .get("children")
                        .getAsJsonArray();

                final ArrayList<RedditFriend> redditFriends = new ArrayList<>();
                for (JsonElement element : children) {
                    TypeToken<RedditFriend> token = new TypeToken<RedditFriend>() {
                    };
                    RedditFriend f = gson.fromJson(element, token.getType());
                    redditFriends.add(f);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted(null, redditFriends);
                    }
                });
            }
        });
    }

    public static void getTrendingSubreddits(Context context,
                                             final FutureCallback<ArrayList<String>> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/r/trendingsubreddits/?limit=1")
                .as(new TypeToken<RedditGenericResponseWrapper<RedditGenericListing<RedditSubmission>>>() { })
                .setCallback(new FutureCallback<RedditGenericResponseWrapper<RedditGenericListing<RedditSubmission>>>() {
                    @Override
                    public void onCompleted(Exception e, RedditGenericResponseWrapper<RedditGenericListing<RedditSubmission>> result) {
                        if (e != null) {
                            callback.onCompleted(e, null);
                        } else {
                            ArrayList<String> trendingSubs = new ArrayList<>();
                            RedditSubmission trending = result.getData().getChildren().get(0).getData();
                            String title = trending.getTitle();
                            Pattern subreddit = Pattern.compile("/r/\\w+");
                            Matcher m = subreddit.matcher(title);
                            while (m.find()) {
                                trendingSubs.add(m.group().substring(3));
                            }
                            callback.onCompleted(null, trendingSubs);
                        }
                    }
                });
    }

    public static void printOutLongString(String string) {
        for (int i = 0; i < string.length(); i += 1000) {
            Log.d("RedditApi", string.substring(i, Math.min(string.length(), i + 1000)));
        }
    }

    public static class ArchivedSubmissionException extends Exception {
        public ArchivedSubmissionException() {
            super("RedditSubmission is archived. Commenting is disallowed.");
        }
    }

}
