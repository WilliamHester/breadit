package me.williamhester.reddit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by William on 1/6/14.
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        if (getMeasuredWidth() != 0) {
            setMeasuredDimension(height, height);
        }
    }

}
