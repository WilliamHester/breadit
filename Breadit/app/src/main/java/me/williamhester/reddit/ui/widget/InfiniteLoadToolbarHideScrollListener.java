package me.williamhester.reddit.ui.widget;

import android.animation.ObjectAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

/**
 * This scroll listener checks for two things. 1.) It listens for when it is close to the end of the
 * list of items, calling back, notifying that there should be more items loaded into the adapter.
 * 2.) It hides the Toolbar at the top of the fragment when scrolling down and shows it again when
 * scrolling up.
 */
public class InfiniteLoadToolbarHideScrollListener extends RecyclerView.OnScrollListener {

  private static final int VISIBLE_THRESHOLD = 5;
  private int mPreviousTotal = 0;
  private boolean mLoading = true;
  private LinearLayoutManager mLayoutManager;
  private List<?> mList;
  private OnLoadMoreListener mCallback;
  private RecyclerView.Adapter<?> mAdapter;
  private RecyclerView mRecyclerView;
  private View mHeaderBar;

  public InfiniteLoadToolbarHideScrollListener(RecyclerView.Adapter<?> adapter, View headerBar,
                                               RecyclerView recyclerView, List<?> list,
                                               LinearLayoutManager layoutManager,
                                               OnLoadMoreListener callback) {
    mAdapter = adapter;
    mHeaderBar = headerBar;
    mRecyclerView = recyclerView;
    mLayoutManager = layoutManager;
    mList = list;
    mCallback = callback;
  }

  @Override
  public void onScrolled(RecyclerView absListView, int dx, int dy) {
    if (mLoading) {
      if (mAdapter.getItemCount() > mPreviousTotal) {
        mPreviousTotal = mAdapter.getItemCount();
        mLoading = false;
      }
    } else if (mList.size() > 0
        && (mAdapter.getItemCount() - mRecyclerView.getChildCount())
        <= (mLayoutManager.findFirstVisibleItemPosition() + VISIBLE_THRESHOLD)) {
      mLoading = true;
      if (mCallback != null) {
        mCallback.onLoadMore();
      }
    }

    float prevY = mHeaderBar.getTranslationY();
    mHeaderBar.setTranslationY(Math.min(Math.max(-mHeaderBar.getHeight(), prevY - dy), 0));
  }

  @Override
  public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
    switch (newState) {
      case RecyclerView.SCROLL_STATE_IDLE:
        if (Math.abs(mHeaderBar.getTranslationY()) < mHeaderBar.getHeight() / 2
            || mLayoutManager.findFirstVisibleItemPosition() == 0) {
          // Need to move it back to completely visible.
          ObjectAnimator objectAnimator =
              ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), 0.0F);
          objectAnimator.setDuration((int) -mHeaderBar.getTranslationY());
          objectAnimator.start();
        } else {
          // Hide the header bar.
          ObjectAnimator objectAnimator =
              ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), -mHeaderBar.getHeight());
          objectAnimator.setDuration(mHeaderBar.getHeight() - (long) mHeaderBar.getTranslationY());
          objectAnimator.start();
        }
        break;
    }
  }

  public void resetState() {
    mPreviousTotal = mAdapter.getItemCount();
    mLoading = false;
  }

  public interface OnLoadMoreListener {
    public void onLoadMore();
  }
}
