package me.williamhester;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.koushikdutta.ion.Ion;

import me.williamhester.models.AccountManager;
import me.williamhester.network.GfycatApi;
import me.williamhester.network.ImgurApi;

/**
 * Created by william on 9/5/14.
 */
public class BreaditApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Crittercism.initialize(this, Auth.CRITTERCISM_APP_ID);
        AccountManager.init(this);
        GfycatApi.init(this);
        ImgurApi.init(this);
        Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setSpdyEnabled(false);
        SettingsManager.init(this);
    }
}
