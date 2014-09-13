package me.williamhester.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.RedditLiveFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.WebViewFragment;

public class SubmissionActivity extends Activity {

    public static final String COMMENT_TAB = "comments";
    public static final String CONTENT_TAB = "content";
    public static final String SUBMISSION = "submission";
    public static final String PERMALINK = "permalink";
    public static final String TAB = "tab";

    private DrawerLayout mDrawerLayout;
    private Fragment mContentFragment;
    private Submission mSubmission;
    private Submission.Media mMedia;
    private String mPermalink;
    private String mCurrentTag;
    private UrlParser mParser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            mCurrentTag = savedInstanceState.getString("currentTag");
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(PERMALINK)) {
                mPermalink = extras.getString(PERMALINK);
            } else {
                mSubmission = (Submission) extras.getSerializable(SUBMISSION);
                mMedia = (Submission.Media) extras.getSerializable("media");
                if (mCurrentTag == null) {
                    mCurrentTag = getIntent().getExtras().getString(TAB);
                }
            }
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.submission_drawer);
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
        if (mDrawerLayout.getDrawerLockMode(Gravity.END) != DrawerLayout.LOCK_MODE_UNLOCKED
                && mParser.getType() != UrlParser.YOUTUBE) {
            menu.removeItem(R.id.action_view_link);
        } else if (mParser.getType() == UrlParser.IMGUR_ALBUM
                || mParser.getType() == UrlParser.IMGUR_IMAGE
                || mParser.getType() == UrlParser.NORMAL_IMAGE) {
            menu.findItem(R.id.action_view_link).setIcon(android.R.drawable.ic_menu_gallery);
        }
//        if (mSubmission != null && mSubmission.isSelf()) { // We don't want people leaving the app for a self post
            menu.removeItem(R.id.action_open_link_in_browser);
//        }
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
                if (mParser.getType() != UrlParser.YOUTUBE) {
                    if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                        mDrawerLayout.closeDrawer(Gravity.END);
                    } else {
                        mDrawerLayout.openDrawer(Gravity.END);
                    }
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
                    startActivity(browserIntent);
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
    }

    private void setUpContent() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        CommentFragment comments;
        if (mSubmission == null) {
            comments = CommentFragment.newInstance(mPermalink);
            comments.setOnSubmissionLoadedListener(new CommentFragment.OnSubmissionLoaded() {
                @Override
                public void onSubmissionLoaded(Submission submission) {
                    mSubmission = submission;
                    setUpDrawer();
                }
            });
        } else {
            comments = CommentFragment.newInstance(mSubmission);
            setUpDrawer();
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.comments_container, comments, "comments")
                .commit();
    }

    private void setUpDrawer() {
        mParser = new UrlParser(mSubmission.getUrl());
        mContentFragment = null;
        View contentFrame = findViewById(R.id.content_container);
        if (!mSubmission.isSelf()) {
            switch (mParser.getType()) {
                case UrlParser.NOT_SPECIAL:
                    mContentFragment = WebViewFragment.newInstance(mParser.getUrl());
                    break;
                case UrlParser.IMGUR_IMAGE:
                    if (mSubmission != null && mSubmission.getImgurData() != null)
                        mContentFragment = ImagePagerFragment
                                .newInstance((ImgurImage) mSubmission.getImgurData());
                    else
                        mContentFragment = ImagePagerFragment
                                .newInstanceLazyLoaded(mParser.getLinkId(), false);
                    break;
                case UrlParser.IMGUR_ALBUM:
                    if (mSubmission != null && mSubmission.getImgurData() != null)
                        mContentFragment = ImagePagerFragment
                                .newInstance((ImgurAlbum) mSubmission.getImgurData());
                    else
                        mContentFragment = ImagePagerFragment
                                .newInstanceLazyLoaded(mParser.getLinkId(), true);
                    break;
                case UrlParser.IMGUR_GALLERY:
                    break;
                case UrlParser.YOUTUBE:
                    break;
                case UrlParser.NORMAL_IMAGE:
                    mContentFragment = ImagePagerFragment.newInstance(mParser.getUrl());
                    break;
                case UrlParser.SUBMISSION:
                    mContentFragment = CommentFragment.newInstance(mParser.getUrl());
                    contentFrame.setBackgroundColor(getResources().getColor(R.color.blackish));
                    break;
                case UrlParser.SUBREDDIT:
                    mContentFragment = SubredditFragment.newInstance(mParser.getLinkId());
                    contentFrame.setBackgroundColor(getResources().getColor(R.color.blackish));
                    break;
                case UrlParser.USER:
                    break;
                case UrlParser.REDDIT_LIVE:
                    mContentFragment = RedditLiveFragment.newInstance(mSubmission);
                    contentFrame.setBackgroundColor(getResources().getColor(R.color.blackish));
                    break;
            }
        }

        if (mContentFragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_container, mContentFragment, "content")
                    .commit();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
            if (mContentFragment instanceof WebViewFragment) {
                WebViewFragment web = (WebViewFragment) mContentFragment;
                if (!web.onBackPressed()) {
                    mDrawerLayout.closeDrawer(Gravity.END);
                }
            } else {
                mDrawerLayout.closeDrawer(Gravity.END);
            }
        } else {
            super.onBackPressed();
        }
    }
}
