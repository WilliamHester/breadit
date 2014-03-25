package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

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

        CommentsFragment cf = new CommentsFragment();
        if (getIntent().getExtras() != null) {
            mPermalink = getIntent().getExtras().getString("permalink", null);
            mUrl = getIntent().getExtras().getString("url", null);
            mIsSelf = getIntent().getExtras().getBoolean("isSelf", false);
//            mUser = getIntent().getExtras().getParcelable("user");
            cf.setArguments(getIntent().getExtras());
        }
        getFragmentManager().beginTransaction()
                .replace(R.id.container, cf)
                .commit();
    }

}
