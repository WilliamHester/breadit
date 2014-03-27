package me.williamhester.reddit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class ScrollTabView extends ScrollView {

    private TabView mTabView;
    private int mThreshold = (int) (20 * getContext().getResources().getDisplayMetrics().density);
    private final int mThresholdSize =
            (int) (20 * getContext().getResources().getDisplayMetrics().density);

    public ScrollTabView(Context context) {
        this(context, null, 0);
    }

    public ScrollTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTabView = new TabView(context);
    }

    @Override
    public void onScrollChanged(int currentHorizontal, int currentVertical,
                                int oldHorizontal, int oldVertical) {
        int difference = oldVertical - currentVertical;
        if (difference > 0) {
            // Start hiding the TabView
            if (mThreshold > difference) {
                mThreshold -= difference;
            } else if (mThreshold == 0) {
                // move the TabView by the difference
            } else {
                difference -= mThreshold;
                mThreshold = 0;
                // move the TabView by the difference
            }
        } else if (difference < 0) {
            // Start showing the TabView or reducing the threshold
            if (mThreshold < mThresholdSize - difference) {
                mThreshold += difference;
            } else if (mThreshold < mThresholdSize) {

            }
        }
        // Case for normal scrolling,
        //      Have a threshold for scrolling back up (maybe 20dp or something like that)

        // Case for when the scroll is approaching the top; if there are only the number of pixels
        //      that it would take to scroll back down at a 1:1 ratio, then start scrolling
    }



}
