package me.williamhester.ui.text;

import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

/**
 * Created by william on 7/20/14.
 */
public class SpoilerSpan extends ClickableSpan {

    private boolean mClicked = false;

    @Override
    public void onClick(View view) {
        TextPaint unpaint = new TextPaint();
        mClicked = true;
        updateDrawState(unpaint);
        view.invalidate();
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setUnderlineText(true);
        if (!mClicked) {
            ds.bgColor = ds.getColor();
        } else {
            ds.bgColor = 0;
        }
    }
}
