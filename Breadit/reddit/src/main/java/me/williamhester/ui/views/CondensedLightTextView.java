package me.williamhester.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by William on 9/17/14.
 */
public class CondensedLightTextView extends TextView {

    private static Typeface mTypeface;

    public CondensedLightTextView(Context context) {
        super(context);
        setTypeface();
    }

    public CondensedLightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface();
    }

    public CondensedLightTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface();
    }

    @TargetApi(21)
    public CondensedLightTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setTypeface();
    }

    private void setTypeface() {
        if (mTypeface == null) {
            mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoCondensed-Light.ttf");
        }
        setTypeface(mTypeface);
    }
}
