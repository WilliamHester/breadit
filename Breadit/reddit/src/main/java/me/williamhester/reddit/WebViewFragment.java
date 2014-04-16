package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.williamhester.areddit.Submission;

/**
 * Created by William on 3/25/14.
 */
public class WebViewFragment extends Fragment {

    public static String URI = "uri";

    private WebView mWebView;

    private String mUri;
    private Submission mSubmission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mUri = savedInstanceState.getString(URI);
        } else if (getArguments() != null) {
            mUri = getArguments().getString("url", null);
            mSubmission = getArguments().getParcelable("submission");
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
        mWebView.setBackgroundColor(0x00000000);

        if (savedInstanceState != null)
            mWebView.restoreState(savedInstanceState);
        else if (mSubmission.getUrl() != null)
            if (isYoutubeLink(mSubmission.getUrl())) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
                startActivity(browserIntent);
            } else {
                mWebView.loadUrl(imgurOptimize(mSubmission.getUrl()));
            }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
