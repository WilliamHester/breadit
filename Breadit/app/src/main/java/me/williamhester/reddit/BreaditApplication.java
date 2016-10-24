package me.williamhester.reddit;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.koushikdutta.ion.Ion;

import me.williamhester.reddit.inject.ApiComponent;
import me.williamhester.reddit.inject.ApiModule;
import me.williamhester.reddit.inject.DaggerApiComponent;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.network.GfycatApi;
import me.williamhester.reddit.network.ImgurApi;

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
