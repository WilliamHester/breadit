package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;

import me.williamhester.areddit.User;

/**
 * Created by William on 2/11/14.
 */
public class SubmissionActivity extends Activity {

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

        ActionBar.Tab tab = mAction.newTab().setText(R.string.comments)
                .setTabListener(new TabListener<CommentsFragment>(this, "commentsFragment",
                        CommentsFragment.class, getIntent().getExtras()));

        mAction.addTab(tab);

        tab = mAction.newTab().setText(R.string.content)
                .setTabListener(new TabListener<WebViewFragment>(this, "webViewFragment",
                        WebViewFragment.class, getIntent().getExtras()));

        mAction.addTab(tab);

//        CommentsFragment cf = new CommentsFragment();
        if (getIntent().getExtras() != null) {
            mPermalink = getIntent().getExtras().getString("permalink", null);
            mUrl = getIntent().getExtras().getString("url", null);
            mIsSelf = getIntent().getExtras().getBoolean("isSelf", false);
            mUser = getIntent().getExtras().getParcelable("user");
//            cf.setArguments(getIntent().getExtras());
        }
//        getFragmentManager().beginTransaction()
//                .replace(R.id.container, cf)
//                .commit();
    }

}
