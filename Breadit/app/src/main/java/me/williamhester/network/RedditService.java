package me.williamhester.network;

import com.google.gson.JsonObject;

import java.util.List;

import me.williamhester.models.reddit.RedditGenericListing;
import me.williamhester.models.reddit.RedditGenericResponseWrapper;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.models.reddit.RedditSubreddit;
import me.williamhester.models.reddit.RedditSuccess;
import me.williamhester.models.reddit.RedditUser;
import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * This service is used by Retrofit to wrap Reddit's API. It can only wrap most of the endpoints
 * because of the nature of Reddit's terribly thought out API.
 *
 * @author William Hester
 */
public interface RedditService {

    @FormUrlEncoded
    @POST("/api/vote/.json")
    void postVote(@Field("id") String id,
                  @Field("dir") int voteDirection,
                  Callback<RedditSuccess> callback);

    @GET("/{subredditName}/{sortType}/.json")
    void getSubmissions(@Path("subredditName") String subredditName,
                        @Path("sortType") String sortType,
                        @Query("t") String secondarySort,
                        @Query("after") String after,
                        // Who doesn't love generics?
                        Callback<RedditGenericResponseWrapper<RedditGenericListing<RedditSubmission>>> submissionCallback);

    @FormUrlEncoded
    @POST("/api/subscribe/.json")
    void postSubscribeToSubreddit(@Field("action") String sub,
                                  @Field("sr") String sr,
                                  Callback<String> stringCallback);

    @GET("/{subredditName}/about.json")
    void getSubreddit(@Path("subredditName") String subredditName,
                      Callback<RedditGenericResponseWrapper<RedditSubreddit>> subredditCallback);

    @FormUrlEncoded
    @POST("/api/hide/.json")
    void postHide(@Field("id") String id, Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/unhide/.json")
    void postUnhide(@Field("id") String id, Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/marknsfw/.json")
    void postMarkNsfw(@Field("id") String id, Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/approve/.json")
    void postApprove(@Field("id") String id, Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/remove/.json")
    void postRemove(@Field("id") String id, Callback<String> callback);

    @GET("/user/{username}/{type}/.json")
    void getUserContent(@Path("username") String username,
                        @Path("type") String type,
                        @Query("after") String after,
                        Callback<List<RedditSubmission>> callback);

    @GET("/user/{username}/about.json")
    void getUserAbout(@Path("username") String username,
                      Callback<RedditGenericResponseWrapper<RedditUser>> callback);

    @POST("/api/needs_captcha/.json")
    void postNeedsCaptcha(Callback<Boolean> booleanCallback);

    @POST("/api/new_captcha/.json")
    void getNewCaptcha(Callback<JsonObject> captchaCallback);

    @POST("/api/compose/.json")
    void postCompose(@Field("iden") String iden,
                     @Field("captcha") String captchaRespnse,
                     @Field("subject") String subject,
                     @Field("text") String body,
                     @Field("to") String to,
                     Callback<JsonObject> callback);
}
