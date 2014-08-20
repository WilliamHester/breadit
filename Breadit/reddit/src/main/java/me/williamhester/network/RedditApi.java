package me.williamhester.network;

import android.content.Context;
import android.text.Spannable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.williamhester.models.Account;
import me.williamhester.models.Comment;
import me.williamhester.models.Listing;
import me.williamhester.models.RedditLive;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.tools.HtmlParser;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

    private static final String USER_AGENT = "Breadit_Android_App";

    private static final String REDDIT_URL = "http://www.reddit.com";

    public static void vote(Context context, Votable v, Account account) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/vote")
                .addHeader("Cookie", "reddit_session=" + account.getCookie())
                .addHeader("X-Modhash", account.getModhash().replace("\"", ""))
                .setBodyParameter("dir", String.valueOf(v.getVoteStatus()))
                .setBodyParameter("id", v.getName());
    }

    public static void getRedditLiveData(Context context, Submission submission,
                                         FutureCallback<ResponseRedditWrapper> callback) {
        Ion.with(context)
                .load(submission.getUrl() + "/about.json")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .as(new TypeToken<ResponseRedditWrapper>() {})
                .setCallback(callback);
    }

    public static void getSubmissionData(Context context, String permalink,
                                         final FutureCallback<Submission> submissionCallback,
                                         final FutureCallback<List<Comment>> commentCallback) {
        Ion.with(context)
                .load(REDDIT_URL + permalink + ".json")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
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
                        Gson gson = new Gson();
                        JsonArray array = new JsonParser().parse(result).getAsJsonArray();
                        TypeToken<Submission> sub = new TypeToken<Submission>() {};
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
                    }
                });
    }

    public static void editThing(Context context, final Votable thing, Account account, final FutureCallback<Votable> callback) {
        Ion.with(context)
                .load(REDDIT_URL + "/api/editusertext/")
                .addHeader("Cookie", "reddit_session=" + account.getCookie())
                .addHeader("X-Modhash", account.getModhash().replace("\"", ""))
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
                        HtmlParser parser = new HtmlParser(StringEscapeUtils.unescapeHtml4(escapedHtml));
                        thing.setSpannableBody(parser.getSpannableString());
                        callback.onCompleted(null, thing);
                    }
                });
    }


}
