package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;

import java.io.File;

import me.williamhester.reddit.R;

/**
 * Created by william on 6/22/14.
 *
 */
public class GfycatFragment extends Fragment {

    public static GfycatFragment newInstance(String url) {
        return new GfycatFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gfycat, root, false);

        VideoView gif = (VideoView) v.findViewById(R.id.gif_view);
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        return v;
    }

    private FutureCallback<File> mFutureCallback = new FutureCallback<File>() {
        @Override
        public void onCompleted(Exception e, File result) {
            VideoView gif = (VideoView) getView().findViewById(R.id.gif_view);
            gif.setVideoPath(result.getPath());
            gif.start();
        }
    };

}
