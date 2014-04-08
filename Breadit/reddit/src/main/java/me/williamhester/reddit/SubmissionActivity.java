package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import me.williamhester.areddit.Submission;
import me.williamhester.areddit.User;

/**
 * Created by William on 2/11/14.
 */
public class SubmissionActivity extends Activity {

    public static final int COMMENT_TAB = 0;
    public static final int CONTENT_TAB = 1;

    private ActionBar mAction;

    private String mPermalink;
    private String mUrl;
    private Submission mSubmission;
    private boolean mIsSelf;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        int selectedTab = 0;
        if (getIntent().getExtras() != null) {
            mSubmission = getIntent().getExtras().getParcelable("submission");
            mPermalink = getIntent().getExtras().getString("permalink", null);
            mUrl = getIntent().getExtras().getString("url", null);
            mIsSelf = getIntent().getExtras().getBoolean("isSelf", false);
            mUser = getIntent().getExtras().getParcelable("user");
            selectedTab = getIntent().getExtras().getInt("tab");
        }

        mAction = getActionBar();
        if (mAction != null) {
            mAction.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        ActionBar.Tab[] tabs = new ActionBar.Tab[2];

        tabs[0] = mAction.newTab().setText(R.string.comments)
                .setTabListener(new TabListener<CommentFragment>(this, "commentsFragment",
                        CommentFragment.class, getIntent().getExtras()));
        mAction.addTab(tabs[0]);

        if (!mSubmission.isSelf()) {
            tabs[1] = mAction.newTab().setText(R.string.content)
                    .setTabListener(new TabListener<WebViewFragment>(this, "webViewFragment",
                            WebViewFragment.class, getIntent().getExtras()));
            mAction.addTab(tabs[1]);
        }

        mAction.selectTab(tabs[selectedTab]);

        mAction.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submission, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mSubmission.getUrl());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with)));
            return true;
        } else if (id == R.id.action_open_link_in_browser) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
            startActivity(browserIntent);
            return true;
        } else if (id == R.id.action_open_comments_browser) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com"
                    + mSubmission.getPermalink()));
            startActivity(browserIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
