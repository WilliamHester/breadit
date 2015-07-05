package me.williamhester.inject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.williamhester.network.RedditApi;
import me.williamhester.network.VoatApi;

/**
 * Created by william on 7/4/15.
 */
@Module
public class ParentApiModule {

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

}
