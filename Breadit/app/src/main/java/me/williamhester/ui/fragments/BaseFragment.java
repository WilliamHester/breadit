package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import me.williamhester.knapsack.Knapsack;

/**
 * Created by william on 6/20/15.
 */
public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Knapsack.restore(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }
}
