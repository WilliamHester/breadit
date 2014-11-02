package me.williamhester.ui.fragments;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by william on 10/31/14.
 */
public abstract class SubmitFragment extends Fragment {

    public abstract Map<String, List<String>> getSubmitBody();

}
