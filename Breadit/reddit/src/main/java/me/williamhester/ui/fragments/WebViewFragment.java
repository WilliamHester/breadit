package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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
    public static final String SUBMISSION = "submission";

    private WebView mWebView;

    private String mUri;
    private Submission mSubmission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mUri = savedInstanceState.getString(URI);
            mSubmission = savedInstanceState.getParcelable(SUBMISSION);
        } else if (getArguments() != null) {
            mSubmission = getArguments().getParcelable("submission");
            mUri = mSubmission.getUrl();
        }
    }

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
        mWebView.setBackgroundColor(0x00000000);

        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else if (mSubmission.getUrl() != null) {
            if (isYoutubeLink(mSubmission.getUrl())) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
                startActivity(browserIntent);
            } else {
                mWebView.loadUrl(imgurOptimize(mSubmission.getUrl()));
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
        return v;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SUBMISSION, mSubmission);
        outState.putString(URI, mUri);
        if (mWebView != null)
            mWebView.saveState(outState);
    }

    private String imgurOptimize(String s) {
        if (s.contains("imgur.com")
                && !(s.contains("imgur.com/a/") || s.contains("imgur.com/gallery/"))) {
            s += ".png";
        }
        return s;
    }

    private boolean isYoutubeLink(String s) {
        s = s.toLowerCase();
        return s.contains("youtu.be") || s.contains("youtube.com");
    }

}
