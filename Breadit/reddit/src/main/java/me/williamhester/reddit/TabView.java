package me.williamhester.reddit;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by William on 1/23/14.
 */
public class TabView extends FrameLayout {

    public static final int TAB_TYPE_MAIN = 0;
    public static final int TAB_TYPE_MINOR = 1;

    private final int DEFAULT_CURSOR_SIZE = 16; // Cursor height in px
    private final float DEFAULT_CURSOR_TRANSPARENCY = 0.80f;

    private ArrayList<FrameLayout> mTabLayouts = new ArrayList<FrameLayout>();
    private Context mContext;
    private LinearLayout mLinearLayout;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private List<String> mFragmentTags = new ArrayList<String>();
    private TabAdapter mTabAdapter;
    private View mCursor;
    private ViewPager mViewPager;
    private float mCursorAlpha;
    private int mCursorHeight;
    private int mSelectedTab;
    private int mViewWidth = 0;
    private Drawable mCursorBackground;

    public TabView(Context context) {
        super(context);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        // Todo: get attributes and set them for the View below the selected item
        if (attrs != null) {
            String packageName = "http://www.williamhester.me/";
            mCursorHeight = attrs.getAttributeIntValue(packageName, "cursorHeight",
                    DEFAULT_CURSOR_SIZE);
            mCursorAlpha = attrs.getAttributeFloatValue(packageName, "cursorAlpha",
                    DEFAULT_CURSOR_TRANSPARENCY);
        } else {
            mCursorHeight = DEFAULT_CURSOR_SIZE;
            mCursorAlpha = DEFAULT_CURSOR_TRANSPARENCY;
        }
        mTabAdapter = new TabAdapter(((Activity)context).getFragmentManager());
        if (getViewTreeObserver() != null)
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mViewWidth != mLinearLayout.getMeasuredWidth()) {
                        mViewWidth = mLinearLayout.getMeasuredWidth();
                        removeView(mCursor);
                        FrameLayout.LayoutParams params;
                        params = new FrameLayout.LayoutParams(mViewWidth, mCursorHeight);
                        params.gravity = Gravity.BOTTOM;
                        mCursor.setBackgroundColor(getResources().getColor(R.color.ghostwhite));
                        addView(mCursor, params);
                        mCursor.setScaleX(mTabLayouts.get(mSelectedTab).getMeasuredWidth()
                                / getMeasuredWidth());
                        int translationX = 0;
                        if (mSelectedTab > 0)
                            translationX += mTabLayouts.get(mSelectedTab - 1).getRight();
                        translationX -= (getMeasuredWidth() - mCursor.getScaleX() * getMeasuredWidth()) / 2;
                        mCursor.setTranslationX(translationX);
                    }
                }
            });
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mCursor = new View(mContext);
        mLinearLayout = new LinearLayout(mContext);
        addView(mLinearLayout, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mCursor.setAlpha(mCursorAlpha);
        // Todo: set the view parameters that can be specified by the user.
    }

    public void attachViewPager(ViewPager v) {
        mViewPager = v;
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOnPageChangeListener(mTabAdapter);
    }

    /**
     * This method adds a tab to the right of the previously added tabs. If no tabs exist, it adds
     * a tab to the left side of the view.
     *
     * @param clss the class of the fragment to be added
     * @param fragmentArgs the bundle that should be passed to the fragment upon creation
     * @param tabType the type that specifies whether the tab should be a main tab or minor tab.
     * @param tag the tag of the Fragment so that it can be found later
     */
    public void addTab(Class<?> clss, Bundle fragmentArgs, int tabType, View innerView, String tag) {
        mFragmentTags.add(tag);

        // Add the fragment data to the TabAdapter
        mTabAdapter.addTab(clss, fragmentArgs);

        // Create the tab's view
        FrameLayout tabFrame = new FrameLayout(mContext);
        mTabLayouts.add(tabFrame);

        // Specify the parameters
        LinearLayout.LayoutParams params;
        switch (tabType) {
            case TAB_TYPE_MAIN:
                params = new LinearLayout.LayoutParams(0,
                        LayoutParams.MATCH_PARENT);
                params.weight = 1;
                params.gravity = Gravity.CENTER_VERTICAL;
                break;
            case TAB_TYPE_MINOR:
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                params.weight = 0;
                params.gravity = Gravity.CENTER_VERTICAL;
                break;
            default:
                params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.MATCH_PARENT);
                params.weight = 0;
                params.gravity = Gravity.CENTER_VERTICAL;
                break;
        }
        FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        innerParams.gravity = Gravity.CENTER_VERTICAL;
        tabFrame.addView(innerView, innerParams);
        mLinearLayout.addView(tabFrame, params);
    }

    /**
     * This method adds a tab to the right of the previously added tabs. If no tabs exist, it adds
     * a tab to the left side of the view.
     *
     * @param clss the class of the fragment to be added
     * @param fragmentArgs the bundle that should be passed to the fragment upon creation
     * @param tabType the type that specifies whether the tab should be a main tab or minor tab.
     */
    public void addTab(Class<?> clss, Bundle fragmentArgs, int tabType, View innerView) {
        addTab(clss, fragmentArgs, tabType, innerView, null);
    }

    public Fragment getFragment(int position) {
        if (position >= mFragmentList.size()) {
            Log.e("QuantumDebug", "Fragment was requested out of bounds");
            return null;
        } else {
            return mFragmentList.get(position);
        }
    }

    public Fragment getFragment(String tag) {
        if (mFragmentTags.contains(tag)) {
            return mFragmentList.get(mFragmentTags.indexOf(tag));
        } else {
            Log.e("QuantumDebug", "Fragment with requested tag does not exist.");
            return null;
        }
    }

    private class TabAdapter extends FragmentPagerAdapter
            implements ViewPager.OnPageChangeListener {

        private int mRightOrLeft = 0;
        private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addTab(Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            mFragmentList.add(Fragment.instantiate(mContext, info.clss.getName(), info.args));
            return mFragmentList.get(mFragmentList.size() - 1);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            if (offset > 0.0) {
                // First, we need to check and see what direction this is moving if it's not set
                if (mRightOrLeft == 0 && offset > 0.5)
                    mRightOrLeft = -1;
                else if (mRightOrLeft == 0)
                    mRightOrLeft = 1;
                // Now, let's scale the cursor to the correct size
                int movingPosition = mSelectedTab;
                float scaleOffset = offset;
                if (mRightOrLeft == -1) {
                    movingPosition = mSelectedTab - 1;
                    scaleOffset = 1 - offset;
                }
                mCursor.setScaleX(((float) mTabLayouts.get(mSelectedTab).getMeasuredWidth()
                        + ((mTabLayouts.get(mSelectedTab + mRightOrLeft).getMeasuredWidth()
                        - mTabLayouts.get(mSelectedTab).getMeasuredWidth()) * scaleOffset))
                        / getMeasuredWidth());
                // Now, figure out how far we need to move the cursor
                int translationX = 0;
                translationX += mTabLayouts.get(movingPosition).getLeft();                            // Gets the left side of the current view
                translationX += mTabLayouts.get(movingPosition).getMeasuredWidth() * offset;          // Gets how far across the view to move the cursor
                translationX -= (getMeasuredWidth() - mCursor.getScaleX() * getMeasuredWidth()) / 2;  // Offsets the cursor because of scaling
                mCursor.setTranslationX(translationX);
            } else {
                mSelectedTab = position;
                mRightOrLeft = 0;
                mCursor.setScaleX(((float) mTabLayouts.get(mSelectedTab).getMeasuredWidth())
                        / getMeasuredWidth());
                int translationX = 0;
                translationX += mTabLayouts.get(mSelectedTab).getLeft();
                translationX -= (getMeasuredWidth() - mCursor.getScaleX() * getMeasuredWidth()) / 2;
                mCursor.setTranslationX(translationX);

                Log.i("QuantumDebug", position + " " + offset + " " + offsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE:
                    mSelectedTab = mViewPager.getCurrentItem();
                    mCursor.setScaleX(((float) mTabLayouts.get(mSelectedTab).getMeasuredWidth())
                            / getMeasuredWidth());
                    int translationX = 0;
                    if (mSelectedTab > 0)
                        translationX += mTabLayouts.get(mSelectedTab - 1).getRight();
                    translationX -= (getMeasuredWidth() - mCursor.getScaleX() * getMeasuredWidth()) / 2;
                    mCursor.setTranslationX(translationX);
                    mRightOrLeft = 0;
                    break;
                case ViewPager.SCROLL_STATE_DRAGGING:
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                    break;
            }
        }

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            public TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }
    }
}