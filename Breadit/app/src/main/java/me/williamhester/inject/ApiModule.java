package me.williamhester.inject;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.williamhester.network.BulletinBoardApi;
import me.williamhester.network.RedditApi;
import me.williamhester.network.VoatApi;
import me.williamhester.reddit.BuildConfig;
import retrofit.client.OkClient;

@Module
public class ApiModule {

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    OkClient provideOkClient() {
        return new OkClient();
    }

    @Provides
    @Singleton
    RedditApi provideRedditApi() {
        return new RedditApi();
    }

    @Provides
    @Singleton
    VoatApi provideVoatApi() {
        return new VoatApi();
    }

    @Provides
    @Singleton
    BulletinBoardApi provideBulletinBoardApi(RedditApi redditApi, VoatApi voatApi) {
        return BuildConfig.BUILD_FLAVOR == BuildConfig.REDDIT ? redditApi : voatApi;
    }

}
