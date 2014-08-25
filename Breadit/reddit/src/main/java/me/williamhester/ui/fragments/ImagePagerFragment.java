package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.reddit.R;
import me.williamhester.ui.adapters.ImgurAlbumAdapter;
import me.williamhester.ui.adapters.SingleImageAdapter;

/**
 * Created by william on 6/24/14.
 */
public class ImagePagerFragment extends Fragment {

    private static final String IMAGE = "image";
    private static final String ALBUM = "album";
    private static final String IMAGE_URL = "imageUrl";

    private FragmentPagerAdapter mAdapter;
    private Handler mAnimHandler;
    private Runnable mAnimRunnable;
    private String mTitle;
    private int mCurrentPosition;

    private ImagePagerCallbacks mCallback;

    public static ImagePagerFragment newInstance(ImgurImage image) {
        Bundle args = new Bundle();
        args.putSerializable(IMAGE, image);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImagePagerFragment newInstance(ImgurAlbum album) {
        Bundle args = new Bundle();
        args.putSerializable(ALBUM, album);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImagePagerFragment newInstance(String imageUrl) {
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageUrl);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof ImagePagerCallbacks) {
            mCallback = (ImagePagerCallbacks) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mCallback != null) {
            mCallback.onImagePagerFragmentCreated();
        }

        if (getArguments() != null) {
            if (getArguments().containsKey(IMAGE)) {
                ImgurImage image = (ImgurImage) getArguments().getSerializable(IMAGE);
                mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), image);
                mTitle = image.getTitle();
            } else if (getArguments().containsKey(ALBUM)) {
                ImgurAlbum album = (ImgurAlbum) getArguments().getSerializable(ALBUM);
                mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), album);
                mTitle = album.getTitle();
            } else if (getArguments().containsKey(IMAGE_URL)) {
                String imageUrl = getArguments().getString(IMAGE_URL);
                mAdapter = new SingleImageAdapter(getChildFragmentManager(), imageUrl);
            }
        }
        mAnimHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_pager, root, false);
        final TextView indicator = (TextView) v.findViewById(R.id.pager_indicator);
        TextView title = (TextView) v.findViewById(R.id.album_title);
        title.setText(mTitle);
        if (mAdapter.getCount() < 2) {
            indicator.setVisibility(View.INVISIBLE);
        } else {
            mAnimRunnable = new Runnable() {
                @Override
                public void run() {
                    Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            indicator.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    indicator.startAnimation(fadeOut);
                }
            };
            mAnimHandler.postDelayed(mAnimRunnable, 500);
        }
        ViewPager pager = (ViewPager) v.findViewById(R.id.view_pager);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) { }

            @Override
            public void onPageSelected(int i) {
                indicator.setVisibility(View.VISIBLE);
                mAnimHandler.postDelayed(mAnimRunnable, 1000);
                mCurrentPosition = i;
                indicator.setText((mCurrentPosition + 1) + " of " + mAdapter.getCount());
            }

            @Override
            public void onPageScrollStateChanged(int i) { }
        });
        pager.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAdapter = null;
        mAnimHandler.removeCallbacks(mAnimRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCallback != null) {
            mCallback.onImagePagerFragmentDestroyed();
        }
    }

    public interface ImagePagerCallbacks {
        public void onImagePagerFragmentCreated();
        public void onImagePagerFragmentDestroyed();
    }

}