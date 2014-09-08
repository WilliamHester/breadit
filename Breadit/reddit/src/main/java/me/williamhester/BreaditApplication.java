package me.williamhester;

import android.app.Application;
import android.content.SharedPreferences;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.ResponseCacheMiddleware;

import java.io.IOException;

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
        try {
            ResponseCacheMiddleware.addCache(AsyncHttpClient.getDefaultInstance(),
                    getCacheDir(),
                    1024 * 1024 * 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
