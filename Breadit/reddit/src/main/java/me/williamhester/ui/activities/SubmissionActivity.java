package me.williamhester.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.ui.fragments.ImageFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.RedditLiveFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

public class SubmissionActivity extends Activity implements ImageFragment.ImageTapCallbacks {

    public static final String COMMENT_TAB = "comments";
    public static final String CONTENT_TAB = "content";
    public static final String SUBMISSION = "submission";
    public static final String PERMALINK = "permalink";
    public static final String TAB = "tab";

    private Submission mSubmission;
    private Submission.Media mMedia;
    private String mPermalink;
    private String mCurrentTag;
    private Url mParser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            mCurrentTag = savedInstanceState.getString("currentTag");
            mParser = savedInstanceState.getParcelable("urlParser");
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(PERMALINK)) {
                mPermalink = extras.getString(PERMALINK);
            } else {
                mSubmission = extras.getParcelable(SUBMISSION);
                setTitle("/r/" + mSubmission.getSubredditName());
                mMedia = (Submission.Media) extras.getSerializable("media");
                if (mCurrentTag == null) {
                    mCurrentTag = getIntent().getExtras().getString(TAB);
                }
            }
        }

        setUpContent();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submission, menu);
        if (mSubmission == null || mSubmission.isSelf()) {
            menu.removeItem(R.id.action_view_link);
        } else if (mParser.getType() == Url.IMGUR_ALBUM
                || mParser.getType() == Url.IMGUR_IMAGE
                || mParser.getType() == Url.NORMAL_IMAGE
                || mParser.getType() == Url.GIF
                || mParser.getType() == Url.GFYCAT_LINK) {
            menu.findItem(R.id.action_view_link).setIcon(android.R.drawable.ic_menu_gallery);
        } else if (mParser.getType() == Url.YOUTUBE) {
            menu.findItem(R.id.action_view_link).setIcon(R.drawable.ic_youtube);
        }
        menu.removeItem(R.id.action_open_link_in_browser);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mSubmission.getUrl());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with)));
                return true;
            case R.id.action_view_link:
                Fragment f = getFragmentManager().findFragmentByTag("content");
                if (f != null) {
                    getFragmentManager().popBackStack();
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.container, getContentFragment(), "content")
                            .addToBackStack("content")
                            .commit();
                }
                return true;
            case R.id.action_open_link_in_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
                startActivity(browserIntent);
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentTag", mCurrentTag);
        outState.putParcelable("urlParser", mParser);
    }

    private void setUpContent() {
        Fragment f = getFragmentManager().findFragmentByTag("comments");
        if (f == null) {
            CommentFragment comments;
            if (mSubmission == null) {
                comments = CommentFragment.newInstance(mPermalink);
                comments.setOnSubmissionLoadedListener(new CommentFragment.OnSubmissionLoaded() {
                    @Override
                    public void onSubmissionLoaded(Submission submission) {
                        setTitle("/r/" + mSubmission.getSubredditName());
                        mSubmission = submission;
                        mParser = new Url(mSubmission.getUrl());
                        invalidateOptionsMenu();
                    }
                });
            } else {
                comments = CommentFragment.newInstance(mSubmission);
                mParser = new Url(mSubmission.getUrl());
            }
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, comments, "comments")
                    .commit();
        }
    }

    private Fragment getContentFragment() {
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
                    return CommentFragment.newInstance(mParser.getUrl());
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
        Fragment f = getFragmentManager().findFragmentByTag("content");
        if (f != null && f instanceof WebViewFragment) {
            WebViewFragment web = (WebViewFragment) f;
            if (!web.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onImageTapped() {
        getFragmentManager().popBackStack();
    }
}
