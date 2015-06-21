package me.williamhester.ui.fragments;

import java.util.List;
import java.util.Map;

/**
 * Created by william on 10/31/14.
 */
public abstract class SubmitFragment extends BaseFragment {

    public abstract Map<String, List<String>> getSubmitBody();

    public abstract boolean isValid();

}
