package me.williamhester;

import android.app.Application;
import android.content.SharedPreferences;

import com.crittercism.app.Crittercism;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;

/**
 * Created by william on 9/5/14.
 */
public class BreaditApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AccountManager.init(this);
        Crittercism.initialize(getApplicationContext(), Auth.CRITTERCISM_APP_ID);
    }
}
