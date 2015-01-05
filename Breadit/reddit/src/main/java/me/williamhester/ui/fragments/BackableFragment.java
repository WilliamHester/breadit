package me.williamhester.ui.fragments;

/**
 * Created by william on 1/4/15.
 */
public interface BackableFragment {
    public boolean canGoBack();
    public void goBack();
}
