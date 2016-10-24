package me.williamhester.reddit.inject;

import com.google.gson.Gson;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.network.RedditService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

@Module
public class ApiModule {

  @Provides
  @Singleton
  Gson provideGson() {
    return new Gson();
  }

  @Provides
  @Singleton
  OkHttpClient provideOkClient() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    Interceptor headerInterceptor = new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Account account = AccountManager.getAccount();

        // Customize the request
        Request.Builder builder = original.newBuilder()
            .addHeader("User-Agent", RedditApi.USER_AGENT)
            .addHeader("Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8")
            .method(original.method(), original.body());

        if (AccountManager.isLoggedIn()) {
          builder.addHeader("Cookie", "reddit_session=\"" + account.getCookie() + "\"")
              .addHeader("X-Modhash", account.getModhash());
        }

        return chain.proceed(builder.build());
      }
    };

    return new OkHttpClient.Builder()
//                .addInterceptor(logging)
        .addInterceptor(headerInterceptor)
        .build();
  }

  @Provides
  @Singleton
  RedditApi provideRedditApi(OkHttpClient client, Gson gson) {

    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://api.reddit.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build();

    RedditService service = retrofit.create(RedditService.class);

    return new RedditApi(service);
  }

}
