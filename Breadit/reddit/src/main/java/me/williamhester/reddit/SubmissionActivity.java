package me.williamhester.reddit;

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

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Submission;

public class SubmissionActivity extends Activity implements TabView.TabSwitcher {

    public static final int COMMENT_TAB = 0;
    public static final int CONTENT_TAB = 1;

    private ActionBar mAction;

    private Context mContext = this;
    private Submission mSubmission;
    private TabView mTabView;
    private Fragment mComments;
    private Fragment mContent;
    private String mPermalink;
    private Account mAccount;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        int selectedTab = 0;
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            mPermalink = getIntent().getDataString();
            mPermalink = "http://www.reddit.com" + mPermalink.substring(mPermalink.indexOf("/r/"));
            SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
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
        } else if (getIntent().getExtras() != null) {
            mSubmission = getIntent().getExtras().getParcelable("submission");
            selectedTab = getIntent().getExtras().getInt("tab");
            setUpActionBarTabs(selectedTab);
        }

        mAction = getActionBar();
        if (mAction != null) {
            mAction.setDisplayHomeAsUpEnabled(true);
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

    private void setUpActionBarTabs(int selectedTab) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        TextView commentTab = new TextView(this);
        commentTab.setText(R.string.comments);
        commentTab.setTextColor(getResources().getColor(R.color.mid_gray));
        commentTab.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        commentTab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        TextView content = new TextView(this);
        content.setText(R.string.content);
        content.setTextColor(getResources().getColor(R.color.mid_gray));
        content.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        Bundle args = getIntent().getExtras();

        String tag;
        switch (selectedTab) {
            case COMMENT_TAB:
                tag = "comments";
                break;
            case CONTENT_TAB:
                tag = "content";
                break;
            default:
                tag = "comments";
        }

        mTabView = (TabView) v.findViewById(R.id.tabs);
        mComments = new CommentFragment();
        mComments.setArguments(args);
        mTabView.addTab(mComments, TabView.TAB_TYPE_MAIN, commentTab, "comments");
        if (mSubmission == null) {
            mTabView.selectTab("comments");
            ((CommentFragment) mComments).setOnSubmissionLoadedListener(new CommentFragment.OnSubmissionLoaded() {
                @Override
                public void onSubmissionLoaded(Submission submission) {
                    mSubmission = submission;

                    TextView content = new TextView(mContext);
                    content.setText(R.string.content);
                    content.setTextColor(getResources().getColor(R.color.mid_gray));
                    content.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                    content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                    if (!mSubmission.isMeta() && !mSubmission.isSelf()) {
                        Bundle args = new Bundle();
                        args.putParcelable("submission", mSubmission);
                        mContent = new WebViewFragment();
                        mContent.setArguments(args);
                        mTabView.addTab(mContent, TabView.TAB_TYPE_MAIN, content, "content");
                    } else if (!mSubmission.isSelf()) {
                        Bundle args = new Bundle();
                        args.putParcelable("account", mAccount);
                        args.putString("permalink", mPermalink);
                        mContent = new CommentFragment();
                        mContent.setArguments(args);
                        mTabView.addTab(mContent, TabView.TAB_TYPE_MAIN, content, "content");
                    }
                }
            });
        } else if (!mSubmission.isMeta() && !mSubmission.isSelf()) {
            mContent = new WebViewFragment();
            mContent.setArguments(args);
            mTabView.addTab(mContent, TabView.TAB_TYPE_MAIN, content, "content");
            if (!tag.equals("comments")) // Hotfix for the weird tab problem
                mTabView.selectTab("comments");
            mTabView.selectTab(tag);
        } else if (!mSubmission.isSelf()) {
            args = new Bundle();
            args.putParcelable("account", (Parcelable) getIntent().getExtras().get("account"));
            args.putString("permalink", mSubmission.getUrl());
            mContent = new CommentFragment();
            mContent.setArguments(args);
            mTabView.addTab(mContent, TabView.TAB_TYPE_MAIN, content, "content");
            if (!tag.equals("comments")) // Hotfix for the weird tab problem
                mTabView.selectTab("comments");
            mTabView.selectTab(tag);
        } else {
            mTabView.selectTab("comments");
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
        if (getFragmentManager().findFragmentByTag(tag) != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, getFragmentManager().findFragmentByTag(tag), tag)
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, fragment, tag)
                    .commit();
        }
    }

    @Override
    public void onTabReselected(String tag, Fragment fragment) {
        if (fragment instanceof CommentFragment) {
            ((CommentFragment) fragment).scrollToTop();
        }
    }

    @Override
    public void onTabUnSelected(String tag) {

    }
}
