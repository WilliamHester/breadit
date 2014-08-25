package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ImgurImage;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by William on 6/24/14.
 */
public class ImageFragment extends Fragment {

    private static final String IMAGE_URL_KEY = "imageUrl";
    private static final String IMGUR_IMAGE_KEY = "imgurImage";

    private ImgurImage mImgurImage;
    private String mUrl;
    private PhotoViewAttacher mAttacher;

    public static ImageFragment newInstance(String url) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL_KEY, url);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageFragment newInstance(ImgurImage image) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMGUR_IMAGE_KEY, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(IMAGE_URL_KEY);
            mImgurImage = (ImgurImage) getArguments().getSerializable(IMGUR_IMAGE_KEY);
            if (mImgurImage != null) {
                mUrl = mImgurImage.getUrl();
            }
        }
        if (mImgurImage != null && mImgurImage.getDescription() != null) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_imgur_image, root, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.image);
        TextView description = (TextView) v.findViewById(R.id.description);
        if (mImgurImage != null && mImgurImage.getDescription() != null) {
            description.setText(mImgurImage.getDescription());
            description.setVisibility(View.VISIBLE);
        }
        ImgurApi.loadImage(mUrl, imageView, new FutureCallback<ImageView>() {
            @Override
            public void onCompleted(Exception e, ImageView result) {
                mAttacher = new PhotoViewAttacher(result);
                if (getView() != null) {
                    ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_image_pager, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_description) {
            if (getView() != null) {
                int visiblity = getView().findViewById(R.id.description).getVisibility();
                getView().findViewById(R.id.description).setVisibility(visiblity == View.GONE ? View.VISIBLE : View.GONE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mAttacher != null) {
            mAttacher.cleanup();
        }
    }
}