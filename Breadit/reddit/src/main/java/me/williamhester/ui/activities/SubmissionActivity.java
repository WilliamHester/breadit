package me.williamhester.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.RedditLiveFragment;
import me.williamhester.ui.fragments.WebViewFragment;

public class SubmissionActivity extends Activity {

    public static final String COMMENT_TAB = "comments";
    public static final String CONTENT_TAB = "content";
    public static final String SUBMISSION = "submission";
    public static final String PERMALINK = "permalink";
    public static final String TAB = "tab";

    private DrawerLayout mDrawerLayout;
    private Submission mSubmission;
    private Submission.Media mMedia;
    private String mPermalink;
    private String mCurrentTag;

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
        if (mSubmission != null && mSubmission.isSelf()) { // We don't want people leaving the app for a self post
            menu.removeItem(R.id.action_open_link_in_browser);
        }
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
    public void onSaveInstanceState(Bundle outState) {
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
        Fragment contentFragment = null;
        if (mSubmission.getMedia() != null
                && mSubmission.getMedia().getType().equals(Submission.LIVE_UPDATE)) {
            contentFragment = RedditLiveFragment.newInstance(mSubmission);
        } else if (!mSubmission.isMeta() && !mSubmission.isSelf()) {
            UrlParser parser = new UrlParser(mSubmission.getUrl());
            switch (parser.getType()) {
                case UrlParser.IMGUR_ALBUM:
                    contentFragment = ImagePagerFragment.newInstance((ImgurAlbum) mSubmission.getImgurData());
                    break;
                case UrlParser.IMGUR_IMAGE:
                    contentFragment = ImagePagerFragment.newInstance((ImgurImage) mSubmission.getImgurData());
                    break;
                case UrlParser.NORMAL_IMAGE:
                    contentFragment = ImagePagerFragment.newInstance(mSubmission.getUrl());
                    break;
                default:
                    contentFragment = WebViewFragment.newInstance(mSubmission);
            }
        } else if (!mSubmission.isSelf()) {
            contentFragment = CommentFragment.newInstance(mSubmission.getUrl());
        }

        if (contentFragment != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_container, contentFragment, "content")
                    .commit();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
            mDrawerLayout.closeDrawer(Gravity.END);
        } else {
            super.onBackPressed();
        }
    }
}
