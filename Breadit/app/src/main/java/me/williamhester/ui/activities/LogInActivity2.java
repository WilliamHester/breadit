package me.williamhester.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Set;

import io.realm.Realm;
import me.williamhester.Auth;
import me.williamhester.models.OauthLogin;
import me.williamhester.models.RedditAboutMe;
import me.williamhester.models.RedditAccount;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.OauthLoginCompleteFragment;
import me.williamhester.ui.fragments.OauthLoginFragment;

/**
 *
 */
public class LogInActivity2 extends BaseActivity implements OauthLoginFragment.OnRedirectListener {

    private Realm mRealm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (f == null) {
            f = OauthLoginFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, f, "LoginFragment")
                    .commit();
        }
        mRealm = Realm.getInstance(this);
    }

    @Override
    public void onRedirect(Uri uri) {
        Set<String> params = uri.getQueryParameterNames();
        if (params.contains("error")) {
            Log.d("OauthLoginFragment", uri.getQueryParameter("error"));
        } else {
            getToken(uri.getQueryParameter("code"));
            Fragment f = OauthLoginCompleteFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, f, "LoginCompleteFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    private void getToken(String code) {
        String authString = Auth.REDDIT_CLIENT_ID + ":";
        String encoded = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        Ion.with(this)
                .load("https://www.reddit.com/api/v1/access_token")
                .setHeader("Authorization", "Basic " + encoded)
                .setBodyParameter("grant_type", "authorization_code")
                .setBodyParameter("code", code)
                .setBodyParameter("redirect_uri", Auth.REDDIT_REDIRECT_URL)
                .as(new TypeToken<OauthLogin>() {
                })
                .setCallback(new FutureCallback<OauthLogin>() {
                    @Override
                    public void onCompleted(Exception e, final OauthLogin result) {
                        if (e != null) {
                            Log.e("OauthLoginFragment", "Authorization failed");
                            e.printStackTrace();
                            return;
                        }
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                RedditAccount redditAccount = realm.createObject(RedditAccount.class);
                                redditAccount.setAccessToken(result.getAccessToken());
                                redditAccount.setRefreshToken(result.getRefreshToken());
                            }
                        });
                        getAccountDetails();
                    }
                });
    }

    private void getAccountDetails() {
        final RedditAccount account = mRealm.where(RedditAccount.class).findFirst();
        Ion.with(this)
                .load("https://oauth.reddit.com/api/v1/me")
                .setHeader("Authorization", "bearer " + account.getAccessToken())
                .as(new TypeToken<RedditAboutMe>() {
                })
                .setCallback(new FutureCallback<RedditAboutMe>() {
                    @Override
                    public void onCompleted(Exception e, final RedditAboutMe result) {
                        if (e != null) {
                            // do your thing
                            return;
                        }
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                account.setUsername(result.getUsername());
                            }
                        });
                        finish();
                    }
                });
    }
}
