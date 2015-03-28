package me.williamhester.ui.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.Set;
import java.util.UUID;

import me.williamhester.Auth;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 3/27/15.
 */
public class OauthLoginFragment extends Fragment {

    private static final String OAUTH_URL = "https://www.reddit.com/api/v1/authorize.compact";

    public static OauthLoginFragment newInstance() {
        Bundle args = new Bundle();
        args.putString("state", UUID.randomUUID().toString());
        OauthLoginFragment fragment = new OauthLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OLF", getArguments().getString("state"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_oauth_login, container, false);

//        CookieSyncManager.createInstance(getActivity());
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.removeAllCookie();
//        cookieManager.setAcceptCookie(false);

        WebView webView = (WebView) v.findViewById(R.id.webview);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheEnabled(false);
//        webView.getSettings().setSaveFormData(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                RedditApi.printOutLongString(url);
                if (url.startsWith(Auth.REDDIT_REDIRECT_URL)) {
                    Uri auth = Uri.parse(url);
                    Set<String> params = auth.getQueryParameterNames();
                    if (params.contains("error")) {

                    } else {
                        getToken(auth.getQueryParameter("code"));
                    }
                }
                super.onLoadResource(view, url);
            }
        });
        String url = buildUri().toString();
        webView.loadUrl(url);

        return v;
    }

    private void getToken(String code) {
        String deviceId = UUID.randomUUID().toString();
        String authString = Auth.REDDIT_CLIENT_ID + ":";
        String encoded = Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        Ion.with(getActivity())
                .load("https://www.reddit.com/api/v1/access_token")
                .setHeader("Authorization", "Basic " + encoded)
                .setBodyParameter("grant_type", "https://oauth.reddit.com/grants/installed_client")
                .setBodyParameter("device_id", deviceId)
                .setBodyParameter("code", code)
                .setBodyParameter("state", getArguments().getString("state"))
                .setBodyParameter("redirect_uri", Auth.REDDIT_REDIRECT_URL)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Log.e("OauthLoginFragment", "Authorization failed");
                            e.printStackTrace();
                        }
                        Log.d("OauthLoginFragment", result.toString());
                    }
                });
    }

    public Uri buildUri() {
        Uri.Builder builder = Uri.parse(OAUTH_URL).buildUpon();
        builder.appendQueryParameter("client_id", Auth.REDDIT_CLIENT_ID)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("state", getArguments().getString("state"))
                .appendQueryParameter("redirect_uri", Auth.REDDIT_REDIRECT_URL)
                .appendQueryParameter("duration", "permanent")
                .appendQueryParameter("scope", "identity,edit,flair,history,modconfig,modflair," +
                        "modlog,modposts,modwiki,mysubreddits,privatemessages,read,report,save," +
                        "submit,subscribe,vote,wikiedit,wikiread");
        return builder.build();
    }
}
