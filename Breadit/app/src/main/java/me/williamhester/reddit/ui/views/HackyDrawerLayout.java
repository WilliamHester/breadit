package me.williamhester.reddit.ui.views;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by william on 8/24/14.
 */
public class HackyDrawerLayout extends DrawerLayout {
  public HackyDrawerLayout(Context context) {
    super(context);
  }

  public HackyDrawerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HackyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    try {
      return super.onInterceptTouchEvent(ev);
    } catch (IllegalArgumentException e) {
      return false;
    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
  }
}
