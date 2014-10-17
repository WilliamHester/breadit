package me.williamhester.ui.views;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by william on 9/15/14.
 */
public class ActionBarPaddedFrameLayout extends FrameLayout {

    private ActionBar actionBar;
    private boolean paddingEnabled;

    public ActionBarPaddedFrameLayout(Context context) {
        this(context, null);
    }

    public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paddingEnabled = true;
    }

    public void setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        requestLayout();
    }

    public void setEnablePadding(boolean enable) {
        paddingEnabled = enable;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int topPadding =
                paddingEnabled && actionBar != null && actionBar.isShowing() ? actionBar.getHeight() : 0;
        setPadding(0, topPadding, 0, 0);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
