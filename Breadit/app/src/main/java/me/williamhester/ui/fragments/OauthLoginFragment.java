package me.williamhester.ui.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.UUID;

import me.williamhester.Auth;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 3/27/15.
 */
public class OauthLoginFragment extends BaseFragment {

    private static final String OAUTH_URL = "https://www.reddit.com/api/v1/authorize.compact";

    private OnRedirectListener mCallback;

    public static OauthLoginFragment newInstance() {
        Bundle args = new Bundle();
        args.putString("state", UUID.randomUUID().toString());
        OauthLoginFragment fragment = new OauthLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = (OnRedirectListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OLF", getArguments().getString("state"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oauth_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= 21) {
            cookieManager.removeAllCookies(null);
        } else {
            cookieManager.removeAllCookie();
        }
        cookieManager.setAcceptCookie(true);

        WebView webView = (WebView) view.findViewById(R.id.webview);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setSaveFormData(false);
        if (Build.VERSION.SDK_INT < 18) {
            webView.getSettings().setSavePassword(false);
        }
        webView.setWebViewClient(new RedditWebViewClient());
        String url = buildUri().toString();
        RedditApi.printOutLongString(url);
        webView.loadUrl(url);
    }

    public Uri buildUri() {
        Uri.Builder builder = Uri.parse(OAUTH_URL).buildUpon();
        builder.appendQueryParameter("client_id", Auth.REDDIT_CLIENT_ID)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("state", getArguments().getString("state"))
                .appendQueryParameter("duration", "permanent")
                .appendQueryParameter("redirect_uri", Auth.REDDIT_REDIRECT_URL)
                .appendQueryParameter("scope", "identity,edit,flair,history,modconfig,modflair," +
                        "modlog,modposts,modwiki,mysubreddits,privatemessages,read,report,save," +
                        "submit,subscribe,vote,wikiedit,wikiread");
        return builder.build();
    }

    public class RedditWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            interceptUrl(url);
        }

        private boolean interceptUrl(String url) {
            RedditApi.printOutLongString(url);
            if (url.startsWith(Auth.REDDIT_REDIRECT_URL)) {
                mCallback.onRedirect(Uri.parse(url));
                return true;
            }
            return false;
        }
    }

    public interface OnRedirectListener {
        void onRedirect(Uri uri);
    }
}
