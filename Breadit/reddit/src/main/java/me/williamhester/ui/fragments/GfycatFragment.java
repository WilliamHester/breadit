package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ResponseGfycatUrlUpload;
import me.williamhester.network.GfycatApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.UrlParser;

/**
 * Created by william on 6/22/14.
 *
 */
public class GfycatFragment extends Fragment {

    private UrlParser mParser;

    public static GfycatFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("url", url);
        GfycatFragment fragment = new GfycatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mParser = savedInstanceState.getParcelable("parser");
        } else {
            mParser = new UrlParser(getArguments().getString("url"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gfycat, root, false);

        final VideoView gif = (VideoView) v.findViewById(R.id.gif_view);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (mParser.getType() != UrlParser.GFYCAT_LINK) {
            GfycatApi.uploadOrConvertGif(getActivity(), mParser.getUrl(), new FutureCallback<ResponseGfycatUrlUpload>() {
                @Override
                public void onCompleted(Exception e, ResponseGfycatUrlUpload result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (result.getWebmUrl() != null) {
                        setUrl(result.getWebmUrl());
                        GfycatApi.downloadWebmGif(getActivity(), result.getWebmUrl(), progressBar, gif);
                    }
                }
            });
        } else {
            GfycatApi.downloadWebmGif(getActivity(), mParser.getUrl(), progressBar, gif);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("parser", mParser);
    }

    private void setUrl(String url) {
        mParser = new UrlParser(url);
    }

}
