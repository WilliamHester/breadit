package me.williamhester.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.RedditLiveFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

public class SubmissionActivity extends ActionBarActivity {

    public static final String SUBMISSION = "submission";
    public static final String PERMALINK = "permalink";

    private Submission mSubmission;
    private String mPermalink;
    private Url mParser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            mParser = savedInstanceState.getParcelable("urlParser");
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(PERMALINK)) {
                mPermalink = extras.getString(PERMALINK);
            } else {
                mSubmission = extras.getParcelable(SUBMISSION);
            }
        }

        setUpContent();
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("urlParser", mParser);
    }

    private void setUpContent() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("comments");
        if (f == null) {
            CommentFragment comments;
            if (mSubmission == null) {
                comments = CommentFragment.newInstance(mPermalink,
                        getIntent().getExtras() != null &&
                                getIntent().getExtras().getBoolean("isSingleThread", false));
                comments.setOnSubmissionLoadedListener(new CommentFragment.OnSubmissionLoaded() {
                    @Override
                    public void onSubmissionLoaded(Submission submission) {
                        mSubmission = submission;
                        mParser = new Url(mSubmission.getUrl());
                        invalidateOptionsMenu();
                    }
                });
            } else {
                comments = CommentFragment.newInstance(mSubmission);
                mParser = new Url(mSubmission.getUrl());
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, comments, "comments")
                    .commit();
        }
    }

    public Fragment getContentFragment() {
        if (!mSubmission.isSelf()) {
            switch (mParser.getType()) {
                case Url.NOT_SPECIAL:
                    return WebViewFragment.newInstance(mParser.getUrl());
                case Url.IMGUR_IMAGE:
                    if (mSubmission.getImgurData() != null)
                        return ImagePagerFragment
                                .newInstance((ImgurImage) mSubmission.getImgurData());
                    else
                        return ImagePagerFragment
                                .newInstanceLazyLoaded(mParser.getLinkId(), false);
                case Url.IMGUR_ALBUM:
                    if (mSubmission.getImgurData() != null)
                        return ImagePagerFragment
                                .newInstance((ImgurAlbum) mSubmission.getImgurData());
                    else
                        return ImagePagerFragment
                                .newInstanceLazyLoaded(mParser.getLinkId(), true);
                case Url.IMGUR_GALLERY:
                    return WebViewFragment.newInstance(mParser.getUrl());
                case Url.YOUTUBE:
                    return YouTubeFragment.newInstance(mParser.getLinkId());
                case Url.GFYCAT_LINK:
                case Url.GIF:
                case Url.NORMAL_IMAGE:
                    return ImagePagerFragment.newInstance(mParser);
                case Url.SUBMISSION:
                    return CommentFragment.newInstance(mParser.getUrl(),
                            mParser.getUrl().contains("?context="));
                case Url.SUBREDDIT:
                    return SubredditFragment.newInstance(mParser.getLinkId());
                case Url.USER:
                    break;
                case Url.REDDIT_LIVE:
                    return RedditLiveFragment.newInstance(mSubmission);
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        // Hide the keyboard
        InputMethodManager imm =  (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.main_container).getWindowToken(), 0);

        // If the content fragment is a WebViewFragment, try to go back.
        Fragment f = getSupportFragmentManager().findFragmentByTag("content");
        if (f != null && f instanceof WebViewFragment) {
            WebViewFragment web = (WebViewFragment) f;
            if (!web.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
