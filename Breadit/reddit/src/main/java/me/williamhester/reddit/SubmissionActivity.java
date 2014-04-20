package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Message;
import me.williamhester.areddit.Submission;

/**
 * Created by William on 2/11/14.
 */
public class SubmissionActivity extends Activity implements TabView.TabSwitcher {

    public static final int COMMENT_TAB = 0;
    public static final int CONTENT_TAB = 1;

    private ActionBar mAction;

    private Submission mSubmission;
    private TabView mTabView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        int selectedTab = 0;
        if (getIntent().getExtras() != null) {
            mSubmission = getIntent().getExtras().getParcelable("submission");
            selectedTab = getIntent().getExtras().getInt("tab");
        }

        mAction = getActionBar();
        if (mAction != null) {
            setUpActionBarTabs(selectedTab);
            mAction.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submission, menu);
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

    public void setUpActionBarTabs(int selectedTab) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_tabview, null);

        TextView commentTab = new TextView(this);
        commentTab.setText(R.string.comments);
        commentTab.setTextColor(getResources().getColor(R.color.mid_gray));
        commentTab.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        commentTab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        TextView content = new TextView(this);
        content.setText(R.string.content);
        content.setTextColor(getResources().getColor(R.color.mid_gray));
        content.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        Bundle args = getIntent().getExtras();

        mTabView = (TabView) v.findViewById(R.id.tabs);
        mTabView.addTab(CommentFragment.class, args, TabView.TAB_TYPE_MAIN, commentTab, "comments");
        if (!mSubmission.isMeta()) {
            mTabView.addTab(WebViewFragment.class, args, TabView.TAB_TYPE_MAIN, content, "content");
        } else {
            args = new Bundle();
            args.putParcelable("account", (Parcelable) getIntent().getExtras().get("account"));
            args.putString("permalink", mSubmission.getUrl());
            mTabView.addTab(CommentFragment.class, args, TabView.TAB_TYPE_MAIN, content, "content");
        }

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }

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

        mTabView.selectTab(tag);
    }

    @Override
    public void onTabSelected(String tag, Fragment fragment) {
        if (getFragmentManager().findFragmentByTag(tag) != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, getFragmentManager().findFragmentByTag(tag))
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
