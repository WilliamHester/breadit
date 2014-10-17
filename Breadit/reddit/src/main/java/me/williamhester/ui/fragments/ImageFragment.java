package me.williamhester.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
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
    private ImageTapCallbacks mCallback;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof ImageTapCallbacks) {
            mCallback = (ImageTapCallbacks) getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(IMAGE_URL_KEY);
            mImgurImage = (ImgurImage) getArguments().getSerializable(IMGUR_IMAGE_KEY);
            if (mImgurImage != null) {
                mUrl = mImgurImage.getHugeThumbnail();
            }
        }
        if (mImgurImage != null && mImgurImage.getDescription() != null) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_imgur_image, root, false);
        final ImageView imageView = (ImageView) v.findViewById(R.id.image);
        TextView description = (TextView) v.findViewById(R.id.description);
        if (mImgurImage != null && mImgurImage.getDescription() != null) {
            description.setText(mImgurImage.getDescription());
            description.setVisibility(View.VISIBLE);
        }
        ImgurApi.loadImage(mUrl, imageView, new FutureCallback<ImageView>() {
            @Override
            public void onCompleted(Exception e, ImageView result) {
                mAttacher = new PhotoViewAttacher(result);
                ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
                imageView.setOnClickListener(null);
                mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float v, float v2) {
                        if (mCallback != null) {
                            mCallback.onImageTapped();
                        }
                    }
                });
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onImageTapped();
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

    public interface ImageTapCallbacks {
        public void onImageTapped();
    }
}
