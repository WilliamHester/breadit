package me.williamhester.inject;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.williamhester.network.RedditApi;
import me.williamhester.network.RedditService;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

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
        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
//
                Log.d("ApiModule", original.urlString());

                // Customize the request
                Request request = original.newBuilder()
                        .header("Authorization", "auth-token")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        });
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.interceptors().add(interceptor);
        return client;
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
