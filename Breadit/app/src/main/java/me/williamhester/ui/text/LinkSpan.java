package me.williamhester.ui.text;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.google.android.youtube.player.YouTubeApiServiceUtil;

import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.activities.OverlayContentActivity;
import me.williamhester.ui.fragments.ImagePagerFragment;
import me.williamhester.ui.fragments.WebViewFragment;
import me.williamhester.ui.fragments.YouTubeFragment;

/**
 * Created by William on 6/15/14.
 */
public class LinkSpan extends ClickableSpan {

    private static final int ORANGE = 0xfff68026;
    private static final int RED = 0xffb31217;
    private static final int GREEN = 0xff85c025;

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
            case Url.MESSAGES:
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
        Intent i;
        switch (mUrl.getType()) {
            case Url.SUBMISSION:
                args.putString("type", "comments");
                i = new Intent(view.getContext(), BrowseActivity.class);
                break;
            case Url.SUBREDDIT:
                args.putString("type", "subreddit");
                i = new Intent(view.getContext(), BrowseActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                args.putString("subreddit", mUrl.getLinkId());
                break;
            case Url.USER:
                args.putString("type", "user");
                i = new Intent(view.getContext(), BrowseActivity.class);
                break;
            case Url.MESSAGES:
                args.putString("type", "messages");
                args.putString("filterType", mUrl.getLinkId());
                i = new Intent(view.getContext(), BrowseActivity.class);
                break;
            default:
                i = new Intent(view.getContext(), OverlayContentActivity.class);
                args.putInt("type", OverlayContentActivity.TYPE_LINK);
                args.putParcelable("url", mUrl);
                break;

        }
        i.putExtras(args);
        Bundle anim = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fade_in,
                R.anim.fade_out).toBundle();
        view.getContext().startActivity(i, anim);
    }

    protected String getLink() {
        return mLink;
    }
}
