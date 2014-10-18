package me.williamhester.ui.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.GfycatResponse;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseGfycatUrlUpload;
import me.williamhester.network.GfycatApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;

/**
 * Created by william on 6/22/14.
 *
 */
public class GifFragment extends Fragment {

    private Url mParser;
    private ImgurImage mImage;

    public static GifFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("url", url);
        GifFragment fragment = new GifFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static GifFragment newInstance(ImgurImage image) {
        Bundle args = new Bundle();
        args.putParcelable("image", image);
        GifFragment fragment = new GifFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mParser = savedInstanceState.getParcelable("parser");
        } else {
            if (getArguments().containsKey("url")) {
                mParser = new Url(getArguments().getString("url"));
            }
            if (getArguments().containsKey("image")) {
                mImage = getArguments().getParcelable("image");
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gfycat, root, false);

        final VideoView gif = (VideoView) v.findViewById(R.id.gif_view);
        final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (mImage != null) {
            GfycatApi.downloadImgurGif(mImage, progressBar, gif);
        } else if (mParser.getType() == Url.GFYCAT_LINK) {
            GfycatApi.getGfyDetails(getActivity(), mParser.getLinkId(), new FutureCallback<GfycatResponse>() {
                @Override
                public void onCompleted(Exception e, GfycatResponse result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (result.getGfyUrl() != null) {
                        setUrl(result.getGfyUrl());
                        GfycatApi.downloadWebmGif(result.getGfyUrl(), progressBar, gif);
                    }
                }
            });
        } else if (mParser.getType() == Url.DIRECT_GFY) {
            GfycatApi.downloadWebmGif(mParser.getUrl(), progressBar, gif);
        } else {
            GfycatApi.uploadOrConvertGif(getActivity(), mParser.getUrl(), new FutureCallback<ResponseGfycatUrlUpload>() {
                @Override
                public void onCompleted(Exception e, ResponseGfycatUrlUpload result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (result.getWebmUrl() != null) {
                        setUrl(result.getWebmUrl());
                        GfycatApi.downloadWebmGif(result.getWebmUrl(), progressBar, gif);
                    }
                }
            });
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("parser", mParser);
    }

    private void setUrl(String url) {
        mParser = new Url(url);
    }

}