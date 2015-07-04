package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import me.williamhester.Auth;
import me.williamhester.reddit.R;

/**
 * Created by william on 9/15/14.
 */
public class YouTubeFragment extends BaseFragment implements YouTubePlayer.OnInitializedListener,
        BackableFragment {

    private static final String VIDEO_ID = "videoId";

    private YouTubePlayerSupportFragment mPlayerFragment;
    private YouTubePlayer mPlayer;
    private boolean mIsFullscreen;

    public static YouTubeFragment newInstance(String videoId) {
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_ID, videoId);
        YouTubeFragment fragment = new YouTubeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Fragment f = getChildFragmentManager().findFragmentByTag("YouTubePlayerFragment");
        if (f == null) {
            mPlayerFragment = YouTubePlayerSupportFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .add(R.id.video_container, mPlayerFragment, "YouTubePlayerFragment")
                    .commit();
        } else {
            mPlayerFragment = (YouTubePlayerSupportFragment) f;
        }
        mPlayerFragment.initialize(Auth.YOUTUBE_AUTH, this);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setShowFullscreenButton(true);
        youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                mIsFullscreen = b;
            }
        });
        mPlayer = youTubePlayer;
        if (!wasRestored) {
            youTubePlayer.cueVideo(getArguments().getString(VIDEO_ID));
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    @Override
    public boolean canGoBack() {
        return mIsFullscreen;
    }

    @Override
    public void goBack() {
        mPlayer.setFullscreen(false);
    }
}
