package me.williamhester.ui.text;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import me.williamhester.reddit.R;
import me.williamhester.ui.activities.MainActivity;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.fragments.WebViewFragment;

/**
 * Created by William on 6/15/14.
 */
public class LinkSpan extends ClickableSpan {

    public static final int SUBMISSION = 0;
    public static final int SUBREDDIT = 1;
    public static final int USER = 2;
    public static final int EXTERNAL = 3;

    private String mLink;

    public LinkSpan(String link) {
        mLink = link;
    }

    @Override
    public void onClick(View view) {
        Bundle args = new Bundle();
        args.putString("link", mLink);
        Intent i = null;
        switch (getLinkType()) {
            case SUBMISSION:
                i = new Intent(view.getContext(), SubmissionActivity.class);
                break;
            case SUBREDDIT:
                i = new Intent(view.getContext(), MainActivity.class);
                break;
            case USER:
                i = new Intent(view.getContext(), UserActivity.class);
                break;
            case EXTERNAL:
                try {
                    Activity activity = (Activity) view.getContext();
                    activity.getFragmentManager().beginTransaction()
                            .add(R.id.container, WebViewFragment.newInstance(mLink), "Link")
                            .addToBackStack("WebView")
                            .commit();
                } catch (ClassCastException e) {
                    Log.d("LinkSpan", "Can't do that");
                    e.printStackTrace();
                }
                break;
        }
        if (i != null) {
            i.putExtras(args);
            view.getContext().startActivity(i);
        }
    }

    protected String getLink() {
        return mLink;
    }

    private int getLinkType() {
        if (mLink.substring(0, 3).equals("/u/") || mLink.contains("reddit.com/u/")) { // go to a user
            return USER;
        } else if (mLink.substring(0, 3).equals("/r/")) { // go to a subreddit
            return SUBREDDIT;
        } else if (mLink.contains("reddit.com")) {
            int i = mLink.indexOf("/", 17);
            if (i == -1 || i == mLink.length()) { // definitely a subreddit or the frontpage
                return SUBREDDIT;
            } else { // found a link to another post
                return SUBMISSION;
            }
        } else {
            return EXTERNAL;
        }
    }
}
