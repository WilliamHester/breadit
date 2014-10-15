package me.williamhester.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by william on 10/14/14.
 */
public class ActionBarSearch extends FrameLayout {
    public ActionBarSearch(Context context) {
        super(context);
    }

    public ActionBarSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionBarSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ActionBarSearch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
