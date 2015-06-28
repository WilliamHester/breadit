package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.adapters.ImgurAlbumAdapter;
import me.williamhester.ui.adapters.SingleImageAdapter;

/**
 * Created by william on 6/24/14.
 */
public class ImagePagerFragment extends BaseFragment {

    private static final String IMAGE = "image";
    private static final String ALBUM = "album";
    private static final String IMAGE_URL = "imageUrl";
    private static final String IMGUR_ID = "imgurUrl";
    private static final String IMGUR_ALBUM = "imgurAlbum";
    private static final String URL_PARSER = "urlParser";

    private static final int PAGER_INDICATOR_MS = 1000;

    private FragmentPagerAdapter mAdapter;
    private Handler mAnimHandler;
    private ImgurAlbum mAlbum;
    private Runnable mAnimRunnable;
    private String mTitle;
    private int mCurrentPosition;

    public static ImagePagerFragment newInstance(ImgurImage image) {
        Bundle args = new Bundle();
        args.putParcelable(IMAGE, image);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImagePagerFragment newInstance(ImgurAlbum album) {
        Bundle args = new Bundle();
        args.putParcelable(ALBUM, album);
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

    public static ImagePagerFragment newInstance(Url parser) {
        Bundle args = new Bundle();
        args.putParcelable(URL_PARSER, parser);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImagePagerFragment newInstance(Submission submission) {
        if (submission.getImgurData() != null) {
            return newInstance(submission.getImgurData());
        }
        return newInstance(submission.getUrl());
    }

    public static ImagePagerFragment newInstance(Object object) {
        if (object instanceof ImgurImage) {
            return newInstance((ImgurImage) object);
        }
        if (object instanceof ImgurAlbum) {
            return newInstance((ImgurAlbum) object);
        }
        return null;
    }

    public static ImagePagerFragment newInstanceLazyLoaded(String imgurId, boolean imgurAlbum) {
        Bundle args = new Bundle();
        args.putString(IMGUR_ID, imgurId);
        args.putBoolean(IMGUR_ALBUM, imgurAlbum);
        ImagePagerFragment fragment = new ImagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (getArguments().containsKey(IMAGE)) {
                ImgurImage image = getArguments().getParcelable(IMAGE);
                mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), image);
                mTitle = image.getTitle();
            } else if (getArguments().containsKey(ALBUM)) {
                mAlbum = getArguments().getParcelable(ALBUM);
                mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), mAlbum);
                mTitle = mAlbum.getTitle();
            } else if (getArguments().containsKey(IMAGE_URL)) {
                String imageUrl = getArguments().getString(IMAGE_URL);
                mAdapter = new SingleImageAdapter(getChildFragmentManager(), imageUrl);
            } else if (getArguments().containsKey(URL_PARSER)) {
                Url parser = getArguments().getParcelable(URL_PARSER);
                mAdapter = new SingleImageAdapter(getChildFragmentManager(), parser);
            }
        }

        mAnimHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_pager, root, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        TextView title = (TextView) v.findViewById(R.id.album_title);
        title.setText(mTitle);
        if (getArguments().containsKey(IMGUR_ID)) {
            String imgurId = getArguments().getString(IMGUR_ID);
            if (getArguments().getBoolean(IMGUR_ALBUM)) {
                ImgurApi.getAlbumDetails(imgurId, getActivity(), new FutureCallback<ResponseImgurWrapper<ImgurAlbum>>() {
                    @Override
                    public void onCompleted(Exception e, ResponseImgurWrapper<ImgurAlbum> result) {
                        if (e != null) return;
                        mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), result.getData());
                        setUpAdapter(v);
                    }
                });
            } else {
                ImgurApi.getImageDetails(imgurId, getActivity(), new FutureCallback<ResponseImgurWrapper<ImgurImage>>() {
                    @Override
                    public void onCompleted(Exception e, ResponseImgurWrapper<ImgurImage> result) {
                        if (e != null) return;
                        mAdapter = new ImgurAlbumAdapter(getChildFragmentManager(), result.getData());
                        setUpAdapter(v);
                    }
                });
            }
        } else {
            setUpAdapter(v);
        }

        return v;
    }

    private void setUpAdapter(View v) {
        View progressBar = v.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        ViewPager pager = (ViewPager) v.findViewById(R.id.view_pager);
        final TextView indicator = (TextView) v.findViewById(R.id.pager_indicator);
        if (mAdapter.getCount() < 2) {
            indicator.setVisibility(View.INVISIBLE);
        } else {
            indicator.setText((mCurrentPosition + 1) + " of " + mAdapter.getCount());
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
            mAnimHandler.postDelayed(mAnimRunnable, PAGER_INDICATOR_MS);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int pixels) {
                    if (positionOffset > 0.5f) {
                        mCurrentPosition = position + 1;
                    } else if (positionOffset < -0.5f) {
                        mCurrentPosition = position - 1;
                    } else {
                        mCurrentPosition = position;
                    }
                    indicator.setText((mCurrentPosition + 1) + " of " + mAdapter.getCount());
                }

                @Override
                public void onPageSelected(int i) {
                    mCurrentPosition = i;
                    if (mAlbum != null) {
                        mAlbum.setLastViewedPosition(mCurrentPosition);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    switch (i) {
                        case ViewPager.SCROLL_STATE_DRAGGING:
                            indicator.setVisibility(View.VISIBLE);
                            mAnimHandler.removeCallbacks(mAnimRunnable);
                            break;
                        case ViewPager.SCROLL_STATE_IDLE:
                            mAnimHandler.postDelayed(mAnimRunnable, PAGER_INDICATOR_MS);
                            break;
                    }
                }
            });
        }
        pager.setAdapter(mAdapter);
        if (mAlbum != null) {
            pager.setCurrentItem(mAlbum.getLastViewedPosition());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAnimHandler.removeCallbacks(mAnimRunnable);
    }

}
