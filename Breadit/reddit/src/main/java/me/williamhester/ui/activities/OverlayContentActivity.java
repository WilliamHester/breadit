package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.RedditLiveFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

/**
 * Created by william on 3/25/15.
 */
public class OverlayContentActivity extends FragmentActivity {

    public static final int TYPE_SUBMISSION = 0;
    public static final int TYPE_LINK = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_overlay_content);

        Bundle extras = getIntent().getExtras();
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (f != null) {
            return;
        }

        switch (extras.getInt("type")) {
            case TYPE_SUBMISSION:
                f = getContentFragment((Submission) extras.getParcelable("submission"));
                break;
            case TYPE_LINK:
                f = getContentFragment((Url) extras.getParcelable("url"));
                break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, f, "Content")
                .commit();
    }

    /**
     * Gets the proper fragment to display the content that the submission is showing.
     *
     * @return returns the fragment needed to display the content
     */
    private Fragment getContentFragment(Submission submission) {
        Url parser = submission.getLinkDetails();
        if (!submission.isSelf()) {
            switch (parser.getType()) {
                case Url.NOT_SPECIAL:
                    return WebViewFragment.newInstance(parser.getUrl());
                case Url.IMGUR_IMAGE:
                    if (submission.getImgurData() != null)
                        return ImagePagerFragment
                                .newInstance((ImgurImage) submission.getImgurData());
                    else
                        return ImagePagerFragment
                                .newInstanceLazyLoaded(parser.getLinkId(), false);
                case Url.IMGUR_ALBUM:
                    if (submission.getImgurData() != null)
                        return ImagePagerFragment
                                .newInstance((ImgurAlbum) submission.getImgurData());
                    else
                        return ImagePagerFragment
                                .newInstanceLazyLoaded(parser.getLinkId(), true);
                case Url.IMGUR_GALLERY:
                    return WebViewFragment.newInstance(parser.getUrl());
                case Url.YOUTUBE:
                    return YouTubeFragment.newInstance(parser.getLinkId());
                case Url.GFYCAT_LINK:
                case Url.GIF:
                case Url.NORMAL_IMAGE:
                    return ImagePagerFragment.newInstance(parser);
//                case Url.SUBMISSION:
//                    return CommentFragment.newInstance(parser.getUrl(),
//                            parser.getUrl().contains("?context="));
                case Url.SUBREDDIT:
                    return SubredditFragment.newInstance(parser.getLinkId());
                case Url.USER:
                    break;
                case Url.REDDIT_LIVE:
                    return RedditLiveFragment.newInstance(submission);
            }
        }
        return null;
    }

    private Fragment getContentFragment(Url url) {
        Fragment f = null;
        switch (url.getType()) {
            case Url.IMGUR_GALLERY: // For now, we're going to go to a WebView because weird things happen with galleries
            case Url.NOT_SPECIAL: // Go to a webview
                f = WebViewFragment.newInstance(url.getUrl());
                break;
            case Url.IMGUR_ALBUM:
                f = ImagePagerFragment.newInstanceLazyLoaded(url.getLinkId(), true);
                break;
            case Url.IMGUR_IMAGE:
                f = ImagePagerFragment.newInstanceLazyLoaded(url.getLinkId(), false);
                break;
            case Url.YOUTUBE:
                f = YouTubeFragment.newInstance(url.getLinkId());
                break;
            case Url.DIRECT_GFY:
            case Url.GFYCAT_LINK:
            case Url.GIF:
            case Url.NORMAL_IMAGE:
                f = ImagePagerFragment.newInstance(url);
                break;
        }
        return f;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
