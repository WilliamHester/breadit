package me.williamhester.reddit.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.AbsComment;
import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.models.reddit.Comment;
import me.williamhester.reddit.models.reddit.Friend;
import me.williamhester.reddit.models.reddit.GenericListing;
import me.williamhester.reddit.models.reddit.GenericResponseWrapper;
import me.williamhester.reddit.models.reddit.Listing;
import me.williamhester.reddit.models.reddit.ResponseWrapper;
import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.models.reddit.Subreddit;
import me.williamhester.reddit.models.reddit.Success;
import me.williamhester.reddit.models.reddit.Thing;
import me.williamhester.reddit.models.reddit.User;
import me.williamhester.reddit.models.reddit.Votable;
import retrofit2.Call;

/**
 * Created by William on 6/14/14.
 */
public class RedditApi {

  public static final String USER_AGENT = "Breadit_Android_App";

  public static final String REDDIT_URL = "https://api.reddit.com";
  public static final String PUBLIC_REDDIT_URL = "https://www.reddit.com";

  public static String SORT_TYPE_HOT = "hot";
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

  private RedditService mRedditService;

  public RedditApi(RedditService redditService) {
    mRedditService = redditService;
  }

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

  public void vote(Votable v) {
    Call<Success> call = mRedditService.postVote(v.getId(), v.getVoteValue());
    call.enqueue(NO_OP_CALLBACK);
  }

  public static void getRedditLiveData(Context context, Submission submission,
                                       final FutureCallback<ResponseWrapper> callback) {
    Ion.with(context)
        .load(submission.getUrl() + "/about.json")
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
            ResponseWrapper wrapper = new ResponseWrapper(result, gson);
            callback.onCompleted(null, wrapper);
          }
        });
  }

  public void getSubmissions(String subredditName, String sortType,
                             String secondarySort, String after,
                             Callback<List<Submission>> callback) {
    Call<GenericResponseWrapper<GenericListing<Submission>>> call;
    if (!TextUtils.isEmpty(subredditName)) {
      call = mRedditService.getSubredditSubmissions(subredditName, sortType, secondarySort, after);
    } else {
      call = mRedditService.getFrontpageSubmissions(sortType, secondarySort, after);
    }
    call.enqueue(new ListingCallbackWrapper<>(callback));
  }

  public void getSubredditDetails(String subredditName, Callback<Subreddit> callback) {
    if (subredditName != null && subredditName.length() > 0) {
      Call<GenericResponseWrapper<Subreddit>> call
          = mRedditService.getSubreddit(subredditName);
      call.enqueue(new GenericCallbackWrapper<>(callback));
    }
  }

  public void getDefaultSubreddits(Callback<List<Subreddit>> callback) {
    getSubreddits(callback, "default", null, new ArrayList<Subreddit>());
  }

  public void getSubscribedSubreddits(Callback<List<Subreddit>> callback) {
    getSubreddits(callback, "mine", "", new ArrayList<Subreddit>());
  }

  private void getSubreddits(final Callback<List<Subreddit>> callback,
                             final String type, final String after,
                             final ArrayList<Subreddit> subreddits) {
    final Call<GenericResponseWrapper<GenericListing<Subreddit>>> call
        = mRedditService.getSubreddits(type, after);

    call.enqueue(new ListingCallbackWrapper<>(new Callback<List<Subreddit>>() {
      @Override
      public void onCompleted() {
      }

      @Override
      public void onSuccess(List<Subreddit> data) {
        if (data.isEmpty()) {
          callback.onCompleted();
          callback.onSuccess(subreddits);
          return;
        }
        subreddits.addAll(data);
        getSubreddits(callback, type, subreddits.get(subreddits.size() - 1).getName(),
            subreddits);
      }

      @Override
      public void onFailure() {
        callback.onFailure();
      }
    }));
  }

  public void subscribeSubreddit(boolean sub, Subreddit subreddit,
                                 FutureCallback<String> callback) {
    Call<Success> call = mRedditService.postSubscribeToSubreddit(sub ? "sub" : "unsub",
        subreddit.getName());
    call.enqueue(NO_OP_CALLBACK);
  }

  private static Map<String, List<String>> getStandardHeaders() {
    Account account = AccountManager.getAccount();
    Map<String, List<String>> headers = new HashMap<>();
    ArrayList<String> userAgent = new ArrayList<>(1);
    userAgent.add(USER_AGENT);
    headers.put("User-Agent", userAgent);
    ArrayList<String> contentType = new ArrayList<>(1);
    userAgent.add("application/x-www-form-urlencoded; charset=UTF-8");
    headers.put("Content-Type", contentType);
    if (account != null) {
      ArrayList<String> list1 = new ArrayList<>(1);
      list1.add("reddit_session=\"" + account.getCookie() + "\"");
      headers.put("Cookie", list1);
      ArrayList<String> list2 = new ArrayList<>(1);
      list2.add(account.getModhash());
      headers.put("X-Modhash", list2);
    }
    return headers;
  }

  public static void getSubmissionData(Context context, String permalink, String sortType,
                                       final FutureCallback<Submission> submissionCallback,
                                       final FutureCallback<List<AbsComment>> commentCallback) {
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
              TypeToken<Submission> sub = new TypeToken<Submission>() {
              };
              Submission submission = gson.fromJson(array.get(0).getAsJsonObject()
                      .get("data").getAsJsonObject()
                      .get("children").getAsJsonArray().get(0).getAsJsonObject()
                      .get("data"),
                  sub.getType());

              ResponseWrapper wrapper = new ResponseWrapper(array.get(1).getAsJsonObject(), gson);
              Listing listing = null;
              if (wrapper.getData() instanceof Listing) {
                listing = (Listing) wrapper.getData();
              }

              List<AbsComment> comments = new ArrayList<>();
              for (ResponseWrapper wrap : listing.getChildren()) {
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
   * /api/MoreChildren endpoint returns JSON, but it's not really the JSON you're looking for, so in
   * order to get the comment data, a second API call must be made to /api/info.json to request the
   * real comment data after creating the correct flattened-tree structure, then that must be
   * returned to the CommentFragment so that it can be put in place. tl;dr: sorry
   *
   * @param linkId    the id of the link
   * @param sortType  the sort type of the comments
   * @param children  the Strings that contain the names of the morechildren
   * @param baseLevel the level at which the morechildren comment is
   * @param callback  the callback that returns the new piece of comments
   */
  public static void getMoreChildren(String linkId, String sortType,
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
                                              final FutureCallback<ArrayList<Thing>> callback) {
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
          .get("things").getAsJsonArray();
      ArrayList<Thing> things = new ArrayList<>();
      Gson gson = new Gson();
      for (int i = 0; i < array.size(); i++) {
        ResponseWrapper wrapper =
            new ResponseWrapper(array.get(i).getAsJsonObject(), gson);
        Thing data = (Thing) wrapper.getData();

        // if it's a comment, we need to get its relative depth in the tree
        if (data instanceof AbsComment) {
          boolean foundParent = false;
          for (Thing thing : things) {
            if (!(thing instanceof Comment)) {
              continue;
            }
            Comment c = (Comment) thing;
            if (c.getId().equals(((AbsComment) data).getParentId())) {
              ((AbsComment) data).setLevel(c.getLevel() + 1);
              foundParent = true;
            }
          }
          if (!foundParent) {
            ((AbsComment) data).setLevel(baseLevel);
          }
        }
        things.add(data);
      }

      callback.onCompleted(null, things);
    } catch (Exception e2) {
      callback.onCompleted(e2, null);
    }
  }

  public static void hide(Context context, Submission submission,
                          FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/hide")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .asString()
        .setCallback(callback);
  }

  public static void unhide(Context context, Submission submission,
                            FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/unhide")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .asString()
        .setCallback(callback);
  }

  public static void delete(Context context, Votable redditVotable, FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/del")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", redditVotable.getId())
        .asString()
        .setCallback(callback);
  }

  public static void markNsfw(Context context, Submission submission,
                              FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/marknsfw")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .asString()
        .setCallback(callback);
  }

  public static void unmarkNsfw(Context context, Submission submission,
                                FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/unmarknsfw")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .asString()
        .setCallback(callback);
  }

  public static void approve(Context context, Submission submission,
                             FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/approve")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .asString()
        .setCallback(callback);
  }

  public static void remove(Context context, Submission submission, boolean spam,
                            FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/remove")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("id", submission.getId())
        .setBodyParameter("spam", String.valueOf(spam))
        .asString()
        .setCallback(callback);
  }

  public static void setContestMode(Context context, boolean isContest, Submission submission,
                                    FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/set_contest_mode")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("api_type", "json")
        .setBodyParameter("id", submission.getId())
        .setBodyParameter("state", String.valueOf(isContest))
        .asString()
        .setCallback(callback);
  }

  public static void setSubredditSticky(Context context, boolean isSticky, Submission submission,
                                        FutureCallback<String> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/api/set_subreddit_sticky")
        .addHeaders(getStandardHeaders())
        .setBodyParameter("api_type", "json")
        .setBodyParameter("id", submission.getId())
        .setBodyParameter("state", String.valueOf(isSticky))
        .asString()
        .setCallback(callback);
  }

  public static void replyToComment(final Context context, final Thing thing, String text,
                                    final FutureCallback<ArrayList<Thing>> callback) {
    MultipartFormDataBody body = new MultipartFormDataBody();
    body.addStringPart("api_type", "json");
    body.addStringPart("parent", thing.getId());
    body.addStringPart("text", text);

    AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/comment/");
    Account account = AccountManager.getAccount();
    request.addHeader("Cookie", "reddit_session=\"" + account.getCookie() + "\"");
    request.addHeader("X-Modhash", account.getModhash());
    request.setBody(body);
    AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
      @Override
      public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
        if (e != null) {
          callback.onCompleted(e, null);
          return;
        }
        int level = thing instanceof Comment ? ((Comment) thing).getLevel() + 1 : 0;
        getVotableDataFromNames(result, level, callback);
      }
    });
  }

  public static void editThing(final Votable redditVotable,
                               final FutureCallback<ArrayList<Thing>> callback) {
    MultipartFormDataBody body = new MultipartFormDataBody();
    body.addStringPart("api_type", "json");
    body.addStringPart("thing_id", redditVotable.getId());
    body.addStringPart("text", redditVotable.getBodyMarkdown());

    AsyncHttpRequest request = new AsyncHttpPost(REDDIT_URL + "/api/editusertext/");
    Account account = AccountManager.getAccount();
    request.addHeader("Cookie", "reddit_session=\"" + account.getCookie() + "\"");
    request.addHeader("X-Modhash", account.getModhash());
    request.setBody(body);
    AsyncHttpClient.getDefaultInstance().executeString(request, new AsyncHttpClient.StringCallback() {
      @Override
      public void onCompleted(Exception e, AsyncHttpResponse source, String result) {
        if (e != null) {
          callback.onCompleted(e, null);
          return;
        }
        int level = redditVotable instanceof Comment ? ((Comment) redditVotable).getLevel() : 0;
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
                                  FutureCallback<GenericResponseWrapper<User>> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/user/" + username + "/about.json")
        .addHeaders(getStandardHeaders())
        .as(new TypeToken<GenericResponseWrapper<User>>() {
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
    Account account = AccountManager.getAccount();
    request.addHeader("Cookie", "reddit_session=\"" + account.getCookie() + "\"");
    request.addHeader("X-Modhash", account.getModhash());
    request.addHeader("User-Agent", USER_AGENT);
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

  public static void getFriends(final FutureCallback<ArrayList<Friend>> callback) {
    AsyncHttpRequest request = new AsyncHttpGet(REDDIT_URL + "/api/v1/me/friends");
    Account account = AccountManager.getAccount();
    request.addHeader("Cookie", "reddit_session=\"" + account.getCookie() + "\"");
    request.addHeader("X-Modhash", account.getModhash());
    request.addHeader("User-Agent", USER_AGENT);
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

        final ArrayList<Friend> friends = new ArrayList<>();
        for (JsonElement element : children) {
          TypeToken<Friend> token = new TypeToken<Friend>() {
          };
          Friend f = gson.fromJson(element, token.getType());
          friends.add(f);
        }

        handler.post(new Runnable() {
          @Override
          public void run() {
            callback.onCompleted(null, friends);
          }
        });
      }
    });
  }

  public static void getTrendingSubreddits(Context context,
                                           final FutureCallback<ArrayList<String>> callback) {
    Ion.with(context)
        .load(REDDIT_URL + "/r/trendingsubreddits/?limit=1")
        .as(new TypeToken<GenericResponseWrapper<GenericListing<Submission>>>() {
        })
        .setCallback(new FutureCallback<GenericResponseWrapper<GenericListing<Submission>>>() {
          @Override
          public void onCompleted(Exception e, GenericResponseWrapper<GenericListing<Submission>> result) {
            if (e != null) {
              callback.onCompleted(e, null);
            } else {
              ArrayList<String> trendingSubs = new ArrayList<>();
              Submission trending = result.getData().getChildren().get(0).getData();
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
      super("Submission is archived. Commenting is disallowed.");
    }
  }

  private static final Callback<Success> NO_OP_CALLBACK = new Callback<Success>() {
    @Override
    public void onCompleted() {
    }

    @Override
    public void onSuccess(Success data) {
    }
  };

  static class ListingCallbackWrapper<T>
      extends Callback<GenericResponseWrapper<GenericListing<T>>> {

    private Callback<List<T>> mCallback;

    ListingCallbackWrapper(Callback<List<T>> callback) {
      mCallback = callback;
    }

    @Override
    public void onCompleted() {
      mCallback.onCompleted();
    }

    @Override
    public void onSuccess(GenericResponseWrapper<GenericListing<T>> data) {
      List<GenericResponseWrapper<T>> listing = data.getData().getChildren();
      List<T> unwrapped = new ArrayList<>(listing.size());
      for (GenericResponseWrapper<T> item : listing) {
        unwrapped.add(item.getData());
      }
      mCallback.onSuccess(unwrapped);
    }

    @Override
    public void onFailure() {
      mCallback.onFailure();
    }
  }

  static class GenericCallbackWrapper<T> extends Callback<GenericResponseWrapper<T>> {
    private Callback<T> mCallback;

    GenericCallbackWrapper(Callback<T> callback) {
      mCallback = callback;
    }

    @Override
    public void onCompleted() {
      mCallback.onCompleted();
    }

    @Override
    public void onSuccess(GenericResponseWrapper<T> data) {
      mCallback.onSuccess(data.getData());
    }

    @Override
    public void onFailure() {
      mCallback.onFailure();
    }
  }

}
