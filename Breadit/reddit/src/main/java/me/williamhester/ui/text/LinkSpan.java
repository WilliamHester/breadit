package me.williamhester.ui.text;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import me.williamhester.reddit.R;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.activities.MainActivity;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.WebViewFragment;

/**
 * Created by William on 6/15/14.
 */
public class LinkSpan extends ClickableSpan {

    private String mLink;

    public LinkSpan(String link) {
        mLink = link;
    }

    @Override
    public void onClick(View view) {
        UrlParser parser = new UrlParser(mLink);
        Bundle args = new Bundle();
        args.putString("link", parser.getUrl());
        Intent i = null;
        Fragment f = null;
        switch (parser.getType()) {
            case UrlParser.SUBMISSION:
                i = new Intent(view.getContext(), SubmissionActivity.class);
                break;
            case UrlParser.SUBREDDIT:
                i = new Intent(view.getContext(), MainActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                args.putString(MainActivity.SUBREDDIT, parser.getLinkId());
                break;
            case UrlParser.USER:
                i = new Intent(view.getContext(), UserActivity.class);
                break;
            case UrlParser.IMGUR_GALLERY: // For now, we're going to go to a WebView because weird things happen with galleries
            case UrlParser.NOT_SPECIAL: // Go to a webview
                f = WebViewFragment.newInstance(mLink);
                break;
            case UrlParser.IMGUR_ALBUM:
                f = ImagePagerFragment.newInstanceLazyLoaded(parser.getLinkId(), true);
                break;
            case UrlParser.IMGUR_IMAGE:
                f = ImagePagerFragment.newInstanceLazyLoaded(parser.getLinkId(), false);
                break;
            case UrlParser.YOUTUBE:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mLink));
                view.getContext().startActivity(browserIntent);
                break;
            case UrlParser.NORMAL_IMAGE:
                f = ImagePagerFragment.newInstance(parser.getUrl());
                break;
        }
        if (i != null) {
            i.putExtras(args);
            view.getContext().startActivity(i);
        } else if (f != null) {
            Activity activity = (Activity) view.getContext();
            activity.getFragmentManager().beginTransaction()
                    .add(R.id.container, f, "Link")
                    .addToBackStack("Link")
                    .commit();
        }
    }

    protected String getLink() {
        return mLink;
    }
}
