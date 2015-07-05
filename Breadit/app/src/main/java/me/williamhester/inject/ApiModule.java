package me.williamhester.inject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.williamhester.network.BulletinBoardApi;
import me.williamhester.network.RedditApi;
import me.williamhester.network.VoatApi;
import me.williamhester.reddit.BuildConfig;

@Module(includes = ParentApiModule.class)
public class ApiModule {

    @Provides
    @Singleton
    BulletinBoardApi provideBulletinBoardApi(RedditApi redditApi, VoatApi voatApi) {
        return BuildConfig.BUILD_FLAVOR == BuildConfig.REDDIT ? redditApi : voatApi;
    }

}
