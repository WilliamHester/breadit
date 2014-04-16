package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

    private final int DEFAULT_CURSOR_SIZE = 4; // Cursor height in px
    private final float DEFAULT_CURSOR_TRANSPARENCY = 0.80f;

    private ArrayList<FrameLayout> mTabLayouts = new ArrayList<FrameLayout>();
    private Context mContext;
    private LinearLayout mLinearLayout;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private List<String> mFragmentTags = new ArrayList<String>();
    private View mCursor;
    private ViewPager mViewPager;
    private float mCursorAlpha;
    private int mCursorHeight;
    private String mSelectedTab;
    private int mSelectedTabPosition;
    private int mViewWidth = 0;
    private Drawable mCursorBackground;
    private TabSwitcher mTabSwitcher;

    public TabView(Context context) {
        super(context);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
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
        try {
            mTabSwitcher = (TabSwitcher) context;
        } catch (ClassCastException e) {
            Log.e("TabView", "ERROR: Activity must implement TabSwitcher.");
        }
        mSelectedTabPosition = -1;
        mSelectedTab = "";
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
                        if (mSelectedTabPosition > -1) {
                            mCursor.setBackgroundColor(getResources().getColor(R.color.auburn));
                            addView(mCursor, params);
                            mCursor.setScaleX(mTabLayouts.get(mSelectedTabPosition).getMeasuredWidth()
                                    / getMeasuredWidth());
                        }
                        int translationX = 0;
                        if (mSelectedTabPosition > 0)
                            translationX += mTabLayouts.get(mSelectedTabPosition - 1).getRight();
                        translationX -= (getMeasuredWidth() - mCursor.getScaleX() * getMeasuredWidth()) / 2;
                        mCursor.setTranslationX(translationX);
                    }
                }
            });
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                Gravity.CENTER));
        mCursor = new View(mContext);
        mLinearLayout = new LinearLayout(mContext);
        addView(mLinearLayout, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, Gravity.CENTER));
        mCursor.setAlpha(mCursorAlpha);
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

        // Create the tab's view
        FrameLayout tabFrame = new FrameLayout(mContext);
        mTabLayouts.add(tabFrame);
        tabFrame.setBackgroundResource(R.drawable.actionbar_item_background);

        final String finalTag = tag;
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
        float scale = getResources().getDisplayMetrics().density;
        tabFrame.setPadding((int) (8 * scale),
                (int) (4 * scale), (int) (8 * scale), (int) (4 * scale));
        tabFrame.addView(innerView, innerParams);
        tabFrame.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedTabPosition = mFragmentTags.indexOf(finalTag);
                if (mSelectedTab.equals(finalTag)) {
                    mTabSwitcher.onTabReselected(finalTag,
                            mFragmentList.get(mSelectedTabPosition));
                } else {
                    mTabSwitcher.onTabSelected(finalTag,
                            mFragmentList.get(mSelectedTabPosition));
                }
                mTabSwitcher.onTabUnSelected(mSelectedTab);
                mSelectedTab = finalTag;
            }
        });
        mLinearLayout.addView(tabFrame, params);
        mFragmentList.add(Fragment.instantiate(mContext, clss.getName(), fragmentArgs));
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
            Log.e("BreaditDebug", "Fragment was requested out of bounds");
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

    public interface TabSwitcher {
        public void onTabSelected(String tag, Fragment fragment);
        public void onTabReselected(String tag, Fragment fragment);
        public void onTabUnSelected(String tag);
    }
}