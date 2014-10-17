package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import me.williamhester.models.Submission;
import me.williamhester.reddit.R;

public class WebViewFragment extends Fragment {

    public static final String URI = "uri";

    private WebView mWebView;

    private String mUri;

    public static WebViewFragment newInstance(String uri) {
        Bundle args = new Bundle();
        args.putString(URI, uri);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WebViewFragment newInstance(Submission submission) {
        Bundle args = new Bundle();
        args.putString(URI, submission.getUrl());
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getString(URI);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, null);
        mWebView = (WebView) v.findViewById(R.id.content);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else if (mUri != null) {
            mWebView.loadUrl(imgurOptimize(mUri));
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
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            private boolean mHandle;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mHandle = true;
                        // Disallow Drawer to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                if (!mHandle) {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                }

                // Handle seekbar touch events.
                if (!view.onTouchEvent(event)) {
                    mHandle = false;
                }
                return true;
            }
        });
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI, mUri);
        if (mWebView != null)
            mWebView.saveState(outState);
    }

    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    private String imgurOptimize(String s) {
        if (s.contains("imgur.com")
                && !(s.contains("imgur.com/a/") || s.contains("imgur.com/gallery/"))) {
            s += ".png";
        }
        return s;
    }

}
