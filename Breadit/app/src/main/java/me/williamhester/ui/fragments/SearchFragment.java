package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by william on 1/6/15.
 */
public class SearchFragment extends AbsSubmissionListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        return v;
    }

    @Override
    protected void onRefreshList() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public boolean isFrontPage() {
        return false;
    }
}
