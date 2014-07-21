package me.williamhester.ui.text;

import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by william on 7/20/14.
 */
public class SpoilerSpan extends ClickableSpan {

    @Override
    public void onClick(View view) {
        TextPaint unpaint = new TextPaint();
        unpaint.bgColor = view.getContext().getResources().getColor(android.R.color.transparent);
        updateDrawState(new TextPaint());
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.bgColor = ds.getColor();
    }
}
