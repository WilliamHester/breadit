package me.williamhester.ui.fragments;

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

import me.williamhester.models.imgur.ImgurImage;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by William on 6/24/14.
 */
public class ImageFragment extends BaseFragment {

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
        args.putParcelable(IMGUR_IMAGE_KEY, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(IMAGE_URL_KEY);
            mImgurImage = getArguments().getParcelable(IMGUR_IMAGE_KEY);
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
        return inflater.inflate(R.layout.fragment_imgur_image, root, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView description = (TextView) view.findViewById(R.id.description);
        View loadHq = view.findViewById(R.id.load_high_quality);
        if (mImgurImage != null) {
            if (mImgurImage.getDescription() != null) {
                description.setText(mImgurImage.getDescription());
                description.setVisibility(View.VISIBLE);
            }
            loadHq.setVisibility(View.VISIBLE);
            loadHq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    mUrl = mImgurImage.getUrl();
                    ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.VISIBLE);
                    loadImage(view, imageView);
                }
            });
        } else {
            loadHq.setVisibility(View.GONE);
        }
        loadImage(view, imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    public void loadImage(final View v, final ImageView imageView) {
        ImgurApi.loadImage(mUrl, imageView, new FutureCallback<ImageView>() {
            @Override
            public void onCompleted(Exception e, ImageView result) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                mAttacher = new PhotoViewAttacher(result);
                ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
                imageView.setOnClickListener(null);
                mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float v, float v2) {
                        getActivity().onBackPressed();
                    }
                });
            }
        });
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
