package me.williamhester.ui.text;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.activities.MainActivity;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

/**
 * Created by William on 6/15/14.
 */
public class LinkSpan extends ClickableSpan {

    private static final int ORANGE = -622554;
    private static final int RED = -5041641;
    private static final int GREEN = -8011739;

    private String mLink;
    private Url mUrl;

    public LinkSpan(String link) {
        mLink = link;
        mUrl = new Url(mLink);
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        switch (mUrl.getType()) {
            case Url.IMGUR_IMAGE:
            case Url.IMGUR_ALBUM:
            case Url.IMGUR_GALLERY:
            case Url.NORMAL_IMAGE:
            case Url.GFYCAT_LINK:
            case Url.GIF:
            case Url.DIRECT_GFY:
                ds.setColor(GREEN);
                break;
            case Url.YOUTUBE:
                ds.setColor(RED);
                break;
            case Url.SUBMISSION:
            case Url.SUBREDDIT:
            case Url.USER:
            case Url.REDDIT_LIVE:
                ds.setColor(ORANGE);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        Bundle args = new Bundle();
        args.putString("permalink", mUrl.getUrl());
        Intent i = null;
        Fragment f = null;
        switch (mUrl.getType()) {
            case Url.SUBMISSION:
                i = new Intent(view.getContext(), SubmissionActivity.class);
                break;
            case Url.SUBREDDIT:
                i = new Intent(view.getContext(), MainActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                args.putString(MainActivity.SUBREDDIT, mUrl.getLinkId());
                break;
            case Url.USER:
                i = new Intent(view.getContext(), UserActivity.class);
                break;
            case Url.IMGUR_GALLERY: // For now, we're going to go to a WebView because weird things happen with galleries
            case Url.NOT_SPECIAL: // Go to a webview
                f = WebViewFragment.newInstance(mLink);
                break;
            case Url.IMGUR_ALBUM:
                f = ImagePagerFragment.newInstanceLazyLoaded(mUrl.getLinkId(), true);
                break;
            case Url.IMGUR_IMAGE:
                f = ImagePagerFragment.newInstanceLazyLoaded(mUrl.getLinkId(), false);
                break;
            case Url.YOUTUBE:
                f = YouTubeFragment.newInstance(mUrl.getLinkId());
                break;
            case Url.DIRECT_GFY:
            case Url.GFYCAT_LINK:
            case Url.GIF:
            case Url.NORMAL_IMAGE:
                f = ImagePagerFragment.newInstance(mUrl);
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
