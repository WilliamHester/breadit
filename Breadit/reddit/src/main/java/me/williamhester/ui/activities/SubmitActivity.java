package me.williamhester.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.HashMap;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.SubmitFragment;
import me.williamhester.ui.fragments.SubmitLinkFragment;
import me.williamhester.ui.fragments.SubmitSelfTextFragment;
import me.williamhester.ui.views.SlidingTabLayout;

/**
 * This Activity houses a ViewPager and a button that sends the form data collected by calling
 * SubmitFragment.getSubmitBody() on the currently selected Fragment.
 *
 * Created by william on 10/31/14.
 */
public class SubmitActivity extends ActionBarActivity {

    private HashMap<Integer, SubmitFragment> mFragments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.submit);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        ReplyFragmentPagerAdapter adapter =
                new ReplyFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.auburn_orange);
            }
        });
        tabs.setViewPager(viewPager);

        final EditText subreddit = (EditText) findViewById(R.id.submit_subreddit);
        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(SubmitActivity.this);
                dialog.setCancelable(false);
                dialog.setMessage("Submitting");
                dialog.show();
                RedditApi.submit(v.getContext(),
                        mFragments.get(viewPager.getCurrentItem()).getSubmitBody(),
                        subreddit.getText().toString(), new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                dialog.cancel();
                                if (e != null) {
                                    Toast.makeText(SubmitActivity.this, "Failed to submit. " +
                                            "Please try again.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                JsonObject json = result.get("json").getAsJsonObject();

                                if (json.has("data")) {
                                    // Consider submission successful
                                    JsonObject data = json.get("data").getAsJsonObject();
                                    String permalink = data.get("url").getAsString();
                                    Bundle extras = new Bundle();
                                    extras.putString("permalink", permalink);
                                    Intent i = new Intent(SubmitActivity.this, SubmissionActivity.class);
                                    i.putExtras(extras);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(SubmitActivity.this, "Failed to submit. " +
                                            "Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private class ReplyFragmentPagerAdapter extends FragmentPagerAdapter {

        public ReplyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new HashMap<>();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mFragments.put(0, SubmitLinkFragment.newInstance());
                    return mFragments.get(0);
                case 1:
                    mFragments.put(1, SubmitSelfTextFragment.newInstance());
                    return mFragments.get(1);
                case 2:
                    // Imgur submit
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.link);
                case 1:
                    return getResources().getString(R.string.self_post);
                case 2:
                    return getResources().getString(R.string.imgur);
            }
            return null;
        }
    }
}
