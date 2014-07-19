package me.williamhester;

import android.app.Application;

import com.koushikdutta.ion.Ion;

import javax.net.ssl.SSLContext;

/**
 * Created by william on 7/19/14.
 */
public class BreaditApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        Ion.getDefault(this).getHttpClient().getSSLSocketMiddleware().setSSLContext();
    }

}
