package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by William on 6/24/14.
 */
public class ImgurImageFragment extends Fragment {

    private static final String IMAGE_URL_KEY = "imageUrl";

    private String mUrl;

    public static ImgurImageFragment newInstance(String url) {
        ImgurImageFragment fragment = new ImgurImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(IMAGE_URL_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_imgur_image, root, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.image);
        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        ImgurApi.loadImage(mUrl, imageView, progressBar, new ImageLoadedCallback());

        return v;
    }

    private class ImageLoadedCallback implements FutureCallback<ImageView> {
        @Override
        public void onCompleted(Exception e, ImageView result) {
            PhotoViewAttacher attacher = new PhotoViewAttacher(result);
            if (getView() != null) {
                ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
