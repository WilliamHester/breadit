package me.williamhester;

import android.app.Application;

import com.crittercism.app.Crittercism;

import me.williamhester.models.AccountManager;
import me.williamhester.network.GfycatApi;

/**
 * Created by william on 9/5/14.
 */
public class BreaditApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AccountManager.init(this);
        GfycatApi.init(this);
        Crittercism.initialize(getApplicationContext(), Auth.CRITTERCISM_APP_ID);
    }
}
