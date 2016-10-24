package me.williamhester.reddit.ui.fragments;

/**
 * Created by william on 1/4/15.
 */
public interface BackableFragment {
  boolean canGoBack();

  void goBack();
}
