package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

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
    private boolean mIsSelf;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        mAction = getActionBar();
        if (mAction != null) {
            mAction.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

        ActionBar.Tab[] tabs = new ActionBar.Tab[2];

        tabs[0] = mAction.newTab().setText(R.string.comments)
                .setTabListener(new TabListener<CommentFragment>(this, "commentsFragment",
                        CommentFragment.class, getIntent().getExtras()));

        mAction.addTab(tabs[0]);

        tabs[1] = mAction.newTab().setText(R.string.content)
                .setTabListener(new TabListener<WebViewFragment>(this, "webViewFragment",
                        WebViewFragment.class, getIntent().getExtras()));

        mAction.addTab(tabs[1]);

        int selectedTab = 0;
        if (getIntent().getExtras() != null) {
            mPermalink = getIntent().getExtras().getString("permalink", null);
            mUrl = getIntent().getExtras().getString("url", null);
            mIsSelf = getIntent().getExtras().getBoolean("isSelf", false);
            mUser = getIntent().getExtras().getParcelable("user");
            selectedTab = getIntent().getExtras().getInt("tab");
        }

        mAction.selectTab(tabs[selectedTab]);
    }

}
