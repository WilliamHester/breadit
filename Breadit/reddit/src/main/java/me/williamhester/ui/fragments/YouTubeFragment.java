package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import me.williamhester.Auth;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.ActionBarPaddedFrameLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by william on 9/15/14.
 */
public class YouTubeFragment extends Fragment implements YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener {

    private static final String VIDEO_ID = "videoId";

    private ActionBarPaddedFrameLayout mLayout;
    private YouTubePlayerFragment mPlayerFragment;
    private YouTubePlayer mPlayer;
    private int mOldOrientation;
    private boolean mIsFullscreen;

    public static YouTubeFragment newInstance(String videoId) {
        Bundle bundle = new Bundle();
        bundle.putString(VIDEO_ID, videoId);
        YouTubeFragment fragment = new YouTubeFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlayerFragment = YouTubePlayerFragment.newInstance();
        mOldOrientation = getResources().getConfiguration().orientation;
        if (savedInstanceState != null) {
            mPlayerFragment.setInitialSavedState((SavedState) savedInstanceState.getParcelable("childState"));
            mOldOrientation = savedInstanceState.getInt("oldOrientation");
            mIsFullscreen = savedInstanceState.getBoolean("isFullscreen");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (ActionBarPaddedFrameLayout) inflater.inflate(R.layout.fragment_youtube, container, false);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.video_container, mPlayerFragment, "youTube")
                .commit();

        mPlayerFragment.initialize(Auth.YOUTUBE_AUTH, this);

        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        return mLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mPlayer.release();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("childState", getChildFragmentManager()
                .saveFragmentInstanceState(mPlayerFragment));
        outState.putInt("oldOrientation", mOldOrientation);
        outState.putBoolean("isFullscreen", mIsFullscreen);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        youTubePlayer.setOnFullscreenListener(this);
//        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
//        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        mPlayer = youTubePlayer;
        if (!wasRestored) {
            youTubePlayer.cueVideo(getArguments().getString(VIDEO_ID));
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    public boolean onBackPressed() {
        if (mIsFullscreen) {
            mPlayer.setFullscreen(false);
        }
        return mIsFullscreen;
    }

    @Override
    public void onFullscreen(boolean fullscreen) {
        mLayout.setEnablePadding(!fullscreen);
        mIsFullscreen = fullscreen;
        ViewGroup.LayoutParams playerParams = mPlayerFragment.getView().getLayoutParams();
        if (fullscreen) {
            playerParams.width = MATCH_PARENT;
            playerParams.height = MATCH_PARENT;
        } else {
            playerParams.width = 0;
            playerParams.height = WRAP_CONTENT;
        }
    }
}
