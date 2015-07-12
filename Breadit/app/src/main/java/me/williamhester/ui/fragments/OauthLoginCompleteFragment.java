package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.williamhester.reddit.R;

/**
 * Created by william on 7/11/15.
 */
public class OauthLoginCompleteFragment extends BaseFragment {

    public static OauthLoginCompleteFragment newInstance() {

        Bundle args = new Bundle();

        OauthLoginCompleteFragment fragment = new OauthLoginCompleteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oauth_login_complete, container, false);
    }
}
