package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * An abstraction for the fragments that show content on top of the regular top-level fragments. It
 * provides callbacks for when the fragments are opened to their hosting activities.
 */
public abstract class ContentFragment extends BaseFragment {

  private ContentFragmentCallbacks mCallback;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (activity instanceof ContentFragmentCallbacks) {
      mCallback = (ContentFragmentCallbacks) activity;
    }
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (mCallback != null) {
      mCallback.onContentFragmentOpened();
    }
  }

  @Override
  public void onDestroyView() {
    if (mCallback != null) {
      mCallback.onContentFragmentClosed();
    }
    super.onDestroyView();
  }

  public interface ContentFragmentCallbacks {
    /**
     * Called when the content fragment is created, notifying the hosting activity to do things like
     * lock the drawers so that they cannot be opened from the content fragment.
     */
    void onContentFragmentOpened();

    /**
     * Called when the content fragment is destroyed, notifying the hosting activity to do things
     * like unlock the drawers so that they can be opened again.
     */
    void onContentFragmentClosed();
  }
}
