package me.williamhester.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.CommentFragment;
import me.williamhester.ui.fragments.SubredditFragment;
import me.williamhester.ui.fragments.UserFragment;
import me.williamhester.ui.fragments.WebViewFragment;

/**
 * This Activity handles the external link requests then proceeds to open the proper Activity with
 * the proper content displaying.
 */
public class BrowseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
        // If the fragment is already populating the view, we're done.
        if (f != null) {
            return;
        }

        // get the data from the intent and figure out where it goes.
        Uri uri = getIntent().getData();
        Url url = new Url(uri.toString());

        switch (url.getType()) {
            case Url.USER:
                f = UserFragment.newInstance(url.getLinkId());
                break;
            case Url.SUBREDDIT:
                f = SubredditFragment.newInstance(url.getLinkId());
                break;
            case Url.SUBMISSION:
                f = CommentFragment.newInstance(url.getUrl(), false);
                break;
            default:
                f = WebViewFragment.newInstance(url.getUrl());
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, f, "content")
                .commit();
    }
}
