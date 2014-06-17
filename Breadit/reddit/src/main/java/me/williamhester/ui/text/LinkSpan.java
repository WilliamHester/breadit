package me.williamhester.ui.text;

import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

/**
 * Created by William on 6/15/14.
 */
public class LinkSpan extends ClickableSpan {

    private String mLink;

    protected LinkSpan(String link) {
        mLink = link;
    }

    @Override
    public void onClick(View view) {
        Log.d("LinkSpan", "Link clicked : " + mLink);
    }

    protected String getLink() {
        return mLink;
    }

}
