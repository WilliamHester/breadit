package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import me.williamhester.models.AccountManager;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.reddit.R;

public class WebViewFragment extends ContentFragment implements Toolbar.OnMenuItemClickListener,
        BackableFragment {

    public static final String URI = "uri";

    @Bind(R.id.content) WebView mWebView;

    private String mUri;

    public static WebViewFragment newInstance(String uri) {
        Bundle args = new Bundle();
        args.putString(URI, uri);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WebViewFragment newInstance(RedditSubmission redditSubmission) {
        Bundle args = new Bundle();
        args.putString(URI, redditSubmission.getUrl());
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getString(URI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview, root, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mToolbar.setOnMenuItemClickListener(this);
        final TextView link = (TextView) view.findViewById(R.id.url);
        link.setText(mUri);
        onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                link.setText(url);
            }
        });
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else if (mUri != null) {
            mWebView.loadUrl(mUri, getHeaders());
            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int progress) {
                    if (progress < 100 && progressBar.getVisibility() == View.GONE){
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        progressBar.setVisibility(ProgressBar.GONE);
                    }
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_web_view, menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.go_forward: {
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
                return true;
            }
            case R.id.open_in_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebView.getOriginalUrl()));
                startActivity(browserIntent);
                return true;
            }
            case R.id.share_link: {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mWebView.getUrl());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,
                        getResources().getText(R.string.share_with)));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI, mUri);
        if (mWebView != null)
            mWebView.saveState(outState);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (AccountManager.isLoggedIn()) {
            headers.put("Cookie",
                    "reddit_session=\"" + AccountManager.getAccount().getCookie() + "\"");
            headers.put("X-Modhash", AccountManager.getAccount().getModhash());
        }
        return headers;
    }

    @Override
    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    @Override
    public void goBack() {
        mWebView.goBack();
    }
}
