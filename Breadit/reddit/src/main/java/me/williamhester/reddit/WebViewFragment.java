package me.williamhester.reddit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by William on 3/25/14.
 */
public class WebViewFragment extends Fragment {

    public static String URI = "uri";

    private WebView mWebView;

    private String mUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUri = savedInstanceState.getString(URI);
        } else if (getArguments() != null) {
            mUri = getArguments().getString(URI);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, null);
        mWebView = (WebView) v.findViewById(R.id.content);

        if (savedInstanceState != null)
            mWebView.restoreState(savedInstanceState);
        else if (mUri != null)
            mWebView.loadUrl(mUri);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI, mUri);
        if (mWebView != null)
            mWebView.saveState(outState);
    }

}
