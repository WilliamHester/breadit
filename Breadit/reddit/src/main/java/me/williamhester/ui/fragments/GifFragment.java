package me.williamhester.ui.fragments;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;

import java.io.File;

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
public class GifFragment extends Fragment implements TextureView.SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {

    private Url mParser;
    private ImgurImage mImage;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private File mFile;
    private SurfaceTexture mSurface;

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
            mImage = savedInstanceState.getParcelable("image");
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
        return inflater.inflate(R.layout.fragment_gfycat, root, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTextureView = (TextureView) view.findViewById(R.id.gif_view);
        mTextureView.setSurfaceTextureListener(this);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        if (mImage != null) {
            GfycatApi.downloadImgurGif(mImage, progressBar, mGifLoadedCallback);
        } else if (mParser.getType() == Url.GFYCAT_LINK) {
            progressBar.setIndeterminate(true);
            GfycatApi.getGfyDetails(getActivity(), mParser.getLinkId(), new FutureCallback<GfycatResponse>() {
                @Override
                public void onCompleted(Exception e, GfycatResponse result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (result.getGfyUrl() != null) {
                        setUrl(result.getGfyUrl());
                        progressBar.setIndeterminate(false);
                        GfycatApi.downloadWebmGif(result.getGfyUrl(), progressBar, mGifLoadedCallback);
                    }
                }
            });
        } else if (mParser.getType() == Url.DIRECT_GFY) {
            GfycatApi.downloadWebmGif(mParser.getUrl(), progressBar, mGifLoadedCallback);
        } else {
            progressBar.setIndeterminate(true);
            GfycatApi.uploadOrConvertGif(getActivity(), mParser.getUrl(), new FutureCallback<ResponseGfycatUrlUpload>() {
                @Override
                public void onCompleted(Exception e, ResponseGfycatUrlUpload result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    if (result.getWebmUrl() != null) {
                        setUrl(result.getWebmUrl());
                        progressBar.setIndeterminate(false);
                        GfycatApi.downloadWebmGif(result.getWebmUrl(), progressBar, mGifLoadedCallback);
                    }
                }
            });
        }
        view.findViewById(R.id.clickable_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private FutureCallback<File> mGifLoadedCallback = new FutureCallback<File>() {
        @Override
        public void onCompleted(Exception e, File result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            mFile = result;
            try {
                Surface surface = new Surface(mSurface);
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(getActivity(), Uri.fromFile(result));
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mMediaPlayer.setOnPreparedListener(GifFragment.this);
                mMediaPlayer.setOnBufferingUpdateListener(GifFragment.this);
                mMediaPlayer.setOnCompletionListener(GifFragment.this);
                mMediaPlayer.setOnPreparedListener(GifFragment.this);
                mMediaPlayer.setOnVideoSizeChangedListener(GifFragment.this);
                mMediaPlayer.prepareAsync();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    };


    @Override
    public void onStop() {
        super.onStop();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("parser", mParser);
        outState.putParcelable("image", mImage);
    }

    private void setUrl(String url) {
        mParser = new Url(url);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = surface;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        int height = mp.getVideoHeight();
        int width = mp.getVideoWidth();
        float gifRatio = height / (float) width;

        int viewHeight = getView().getHeight();
        int viewWidth = getView().getWidth();
        float viewRatio = viewHeight / (float) viewWidth;

        if (gifRatio < viewRatio) {
            // the gif is shorter than the viewport
            float scaleX = viewWidth / ((float) width);
            float scaleY = (scaleX * height) / viewHeight;
            mTextureView.setScaleY(scaleY);
        } else {
            // the gif is wider than the viewport
            float scaleY = viewHeight / ((float) height);
            float scaleX = (scaleY * width) / viewWidth;
            mTextureView.setScaleX(scaleX);
        }

        mFile.delete();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }
}
