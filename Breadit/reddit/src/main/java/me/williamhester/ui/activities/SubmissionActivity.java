package me.williamhester.ui.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import me.williamhester.models.Account;
import me.williamhester.models.Submission;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.databases.AccountDataSource;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.TabView;

public class SubmissionActivity extends Activity implements TabView.TabSwitcher {

    public static final String ACCOUNT = "account";
    public static final String COMMENT_TAB = "comments";
    public static final String CONTENT_TAB = "content";
    public static final String SUBMISSION = "submission";
    public static final String TAB = "tab";

    private Context mContext = this;
    private Submission mSubmission;
    private TabView mTabView;
    private String mPermalink;
    private Account mAccount;
    private String mCurrentTag;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            mCurrentTag = savedInstanceState.getString("currentTag");
        }
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            mPermalink = getIntent().getDataString();
            mPermalink = "http://www.reddit.com" + mPermalink.substring(mPermalink.indexOf("/r/"));
            Log.i("SubmissionActivity", "Viewing " + mPermalink);
            SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
            long id = prefs.getLong("accountId", -1);
            if (id != -1) {
                try {
                    AccountDataSource dataSource = new AccountDataSource(this);
                    dataSource.open();
                    mAccount = dataSource.getAccount(id);
                    dataSource.close();
                } catch (NullPointerException e) {
                    Log.e("Breadit", "error accessing database");
                }
            }
            mCurrentTag = COMMENT_TAB;
        } else if (getIntent().getExtras() != null) {
            mSubmission = getIntent().getExtras().getParcelable(SUBMISSION);
            mAccount = getIntent().getExtras().getParcelable(ACCOUNT);
            if (mCurrentTag == null) {
                mCurrentTag = getIntent().getExtras().getString(TAB);
            }
        }

        setUpActionBarTabs();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
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

    private void setUpActionBarTabs() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        TextView commentTab = new TextView(this);
        commentTab.setText(R.string.comments);
        commentTab.setTextColor(getResources().getColor(R.color.ghostwhite));
        commentTab.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        commentTab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextView contentTab = new TextView(this);
        contentTab.setText(R.string.content);
        contentTab.setTextColor(getResources().getColor(R.color.ghostwhite));
        contentTab.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        contentTab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        Bundle args = new Bundle();
        args.putString("permalink", mPermalink);
        args.putParcelable("submission", mSubmission);
        args.putParcelable("account", mAccount);

        CommentFragment comments;
        Fragment content;
        
        mTabView = (TabView) v.findViewById(R.id.tabs);
        comments = new CommentFragment();
        comments.setArguments(args);
        mTabView.addTab(comments, TabView.TAB_TYPE_MAIN, commentTab, COMMENT_TAB);
        if (mSubmission == null) {
            mTabView.selectTab(COMMENT_TAB);
            comments.setOnSubmissionLoadedListener(new CommentFragment.OnSubmissionLoaded() {
                @Override
                public void onSubmissionLoaded(Submission submission) {
                    mSubmission = submission;

                    TextView contentTab = new TextView(mContext);
                    contentTab.setText(R.string.content);
                    contentTab.setTextColor(getResources().getColor(R.color.ghostwhite));
                    contentTab.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                    contentTab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                    if (!mSubmission.isMeta() && !mSubmission.isSelf()) {
                        Bundle args = new Bundle();
                        args.putParcelable("submission", mSubmission);
                        Fragment content = new WebViewFragment();
                        content.setArguments(args);
                        mTabView.addTab(content, TabView.TAB_TYPE_MAIN, contentTab, CONTENT_TAB);
                    } else if (!mSubmission.isSelf()) {
                        Bundle args = new Bundle();
                        args.putParcelable("account", mAccount);
                        args.putString("permalink", mPermalink);
                        Fragment content = new CommentFragment();
                        content.setArguments(args);
                        mTabView.addTab(content, TabView.TAB_TYPE_MAIN, contentTab, CONTENT_TAB);
                    }
                }
            });
        } else if (!mSubmission.isMeta() && !mSubmission.isSelf()) {
            content = new WebViewFragment();
            content.setArguments(args);
            mTabView.addTab(content, TabView.TAB_TYPE_MAIN, contentTab, CONTENT_TAB);

            mTabView.selectTab(mCurrentTag);
        } else if (!mSubmission.isSelf()) {
            args = new Bundle();
            args.putParcelable("account", (Parcelable) getIntent().getExtras().get("account"));
            args.putString("permalink", mSubmission.getUrl());
            content = new CommentFragment();
            content.setArguments(args);
            mTabView.addTab(content, TabView.TAB_TYPE_MAIN, contentTab, CONTENT_TAB);
            mTabView.selectTab(mCurrentTag);
        } else {
            mTabView.selectTab(COMMENT_TAB);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }
    }

    @Override
    public void onTabSelected(String tag, Fragment fragment) {
        getFragmentManager().executePendingTransactions();
        Fragment f = getFragmentManager().findFragmentByTag(tag);
        if (f != null) {
            getFragmentManager().beginTransaction()
                    .attach(f)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        }
        mCurrentTag = tag;
    }

    @Override
    public void onTabReselected(String tag, Fragment fragment) {
        if (fragment instanceof CommentFragment) {
            ((CommentFragment) fragment).scrollToTop();
        }
    }

    @Override
    public void onTabUnSelected(String tag) {
        if (tag != null) {
            Fragment f = getFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                getFragmentManager().beginTransaction()
                        .detach(f)
                        .commit();
            }
        }
    }
}
