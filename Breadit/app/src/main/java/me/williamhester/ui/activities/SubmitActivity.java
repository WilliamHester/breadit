package me.williamhester.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.CaptchaDialogFragment;
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
public class SubmitActivity extends ActionBarActivity implements
        CaptchaDialogFragment.OnCaptchaAttemptListener {

    private CaptchaDialogFragment mCaptchaDialog;
    private EditText mSubreddit;
    private ProgressDialog mProgressDialog;
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.submit);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        ReplyFragmentPagerAdapter adapter =
                new ReplyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);

        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.app_highlight);
            }
        });
        tabs.setViewPager(mViewPager);

        mSubreddit = (EditText) findViewById(R.id.submit_subreddit);
        if (savedInstanceState == null) {
            String subName = getIntent().getStringExtra("subredditName");
            if (subName == null) {
                subName = "";
            }
            mSubreddit.setText(subName);
            mSubreddit.setSelection(subName.length());
        }

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitFragment fragment = (SubmitFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.view_pager + ":"
                                + mViewPager.getCurrentItem());
                boolean valid = fragment.isValid();
                // If the subreddit's length is at least 3 and the fragment says that it's valid
                if (subredditNameIsValid() && valid) {
                    submit();
                }
            }
        });

        mProgressDialog = new ProgressDialog(SubmitActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getResources().getString(R.string.submitting));

        mCaptchaDialog = (CaptchaDialogFragment) getSupportFragmentManager()
                .findFragmentByTag("captcha");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean subredditNameIsValid() {
        String name = mSubreddit.getText().toString().toLowerCase().trim();
        String regexedName = name.replaceAll("[abcdefghijklmnopqrstuvwxyz]", "");
        return regexedName.length() == 0 &&  name.length() > 2;
    }

    public void submit() {
        mProgressDialog.show();
        final FutureCallback<JsonObject> submitCallback = new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                mProgressDialog.cancel();
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
                    extras.putString("type", "comments");
                    extras.putString("permalink", permalink);
                    Intent i = new Intent(SubmitActivity.this, BrowseActivity.class);
                    i.putExtras(extras);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(SubmitActivity.this, "Failed to submit. " +
                            "Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        FutureCallback<String> needsCaptchaCallback = new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                boolean needsCaptcha = Boolean.parseBoolean(result);
                if (needsCaptcha) {
                    mProgressDialog.dismiss();
                    showCaptchaDialog();
                } else {
                    SubmitFragment fragment = (SubmitFragment) getSupportFragmentManager()
                            .findFragmentByTag("android:switcher:" + R.id.view_pager + ":"
                                    + mViewPager.getCurrentItem());
                    RedditApi.submit(SubmitActivity.this, fragment.getSubmitBody(),
                            mSubreddit.getText().toString(), submitCallback);
                }
            }
        };

        mProgressDialog.show();
        RedditApi.needsCaptcha(this, needsCaptchaCallback);
    }

    private void showCaptchaDialog() {
        mCaptchaDialog = CaptchaDialogFragment.newInstance();
        mCaptchaDialog.show(getSupportFragmentManager(), "captcha");
    }

    @Override
    public void onCaptchaAttempt(String iden, String attempt) {
        SubmitFragment fragment = (SubmitFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.view_pager + ":"
                        + mViewPager.getCurrentItem());
        RedditApi.submit(this, fragment.getSubmitBody(),
                mSubreddit.getText().toString(), iden, attempt, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        mProgressDialog.dismiss();
                        if (e != null) {
                            Toast.makeText(SubmitActivity.this, "Failed to submit. " +
                                    "Please try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JsonObject json = result.get("json").getAsJsonObject();
                        JsonArray errors = json.get("errors").getAsJsonArray();

                        if (errors.size() == 0 && json.has("data")) {
                            // Consider submission successful
                            JsonObject data = json.get("data").getAsJsonObject();
                            String permalink = data.get("url").getAsString();
                            Bundle extras = new Bundle();
                            extras.putString("type", "comments");
                            extras.putString("permalink", permalink);
                            Intent i = new Intent(SubmitActivity.this, BrowseActivity.class);
                            i.putExtras(extras);
                            startActivity(i);
                            finish();
                        } else if (errors.size() > 0) {
                            // Likely means that the user failed the captcha
                            StringBuilder sb = new StringBuilder();
                            for (JsonElement element : errors) {
                                JsonArray array = element.getAsJsonArray();
                                String errorName = array.get(0).getAsString();
                                switch (errorName) {
                                    case "RATELIMIT":
                                        // You're ratelimited
                                        long minutes = Math.round(json
                                                .get("ratelimit").getAsDouble()) / 60;
                                        sb.append(getResources().getString(R.string.ratelimited))
                                                .append(' ')
                                                .append(minutes)
                                                .append(' ')
                                                .append(getResources().getString(R.string.minutes))
                                                .append('\n');
                                        break;
                                    case "BAD_CAPTCHA":
                                        // The captcha response was wrong
                                        mCaptchaDialog.newCaptcha(json.get("captcha").getAsString());
                                        sb.append(getResources().getString(R.string.failed_captcha))
                                                .append('\n');
                                        break;
                                    case "SUBREDDIT_NOEXIST":
                                        // The subreddit doesn't exist.
                                        sb.append(getResources().getString(
                                                R.string.sub_doesnt_exist))
                                                .append('\n');
                                        break;
                                    case "QUOTA_FILLED":
                                        // You're really ratelimited
                                        sb.append(array.get(1).getAsString())
                                                .append('\n');
                                        break;
                                    default:
                                        Log.e("SubmitActivity", "Unhandled error: \"" + errorName
                                                + "\"");
                                        RedditApi.printOutLongString(json.toString());
                                }
                            }
                            if (sb.length() > 0) {
                                sb.deleteCharAt(sb.length() - 1);
                            }
                            Toast.makeText(SubmitActivity.this, sb, Toast.LENGTH_LONG).show();
                        } else {
                            // Likely means that the user entered a URL incorrectly
                            Toast.makeText(SubmitActivity.this, "Failed to submit. " +
                                    "Please try again.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private class ReplyFragmentPagerAdapter extends FragmentPagerAdapter {

        public ReplyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                   return SubmitLinkFragment.newInstance();
                case 1:
                    return SubmitSelfTextFragment.newInstance();
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
