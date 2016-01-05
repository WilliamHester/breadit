package me.williamhester;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.koushikdutta.ion.Ion;

import me.williamhester.inject.ApiComponent;
import me.williamhester.inject.ApiModule;
import me.williamhester.inject.DaggerApiComponent;
import me.williamhester.models.AccountManager;
import me.williamhester.network.GfycatApi;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.BuildConfig;

/**
 * Created by william on 9/5/14.
 */
public class BreaditApplication extends Application {

    private ApiComponent mApiComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mApiComponent = DaggerApiComponent.builder()
                .apiModule(new ApiModule())
                .build();

        Crittercism.initialize(this, BuildConfig.CRITTERCISM_APP_ID);
        AccountManager.init(this);
        GfycatApi.init(this);
        ImgurApi.init(this);
        Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setSpdyEnabled(false);
        SettingsManager.init(this);
    }

    public ApiComponent getApiComponent() {
        return mApiComponent;
    }
}
