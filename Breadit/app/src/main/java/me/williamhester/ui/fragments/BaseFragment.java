package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.williamhester.knapsack.Knapsack;
import me.williamhester.reddit.R;

/**
 * Created by william on 6/20/15.
 */
public class BaseFragment extends Fragment {

    @Bind(R.id.toolbar_actionbar)
    @Nullable
    protected Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Knapsack.restore(this, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            ButterKnife.bind(this, view);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.d("BaseFragment", "caught a RuntimeException");
            // The class has no views to bind
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            ButterKnife.unbind(this);
        } catch (RuntimeException e) {
            // The class has no views to unbind.
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

Knapsack.save(this, outState);
    }
}
