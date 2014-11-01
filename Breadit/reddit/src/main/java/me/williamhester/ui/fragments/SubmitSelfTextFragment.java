package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.williamhester.reddit.R;

/**
 * Created by william on 10/31/14.
 */
public class SubmitSelfTextFragment extends SubmitFragment {

    private MarkdownBodyFragment mBodyFragment;

    public static SubmitSelfTextFragment newInstance() {
        return new SubmitSelfTextFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_submit_self_text, container, false);
        mBodyFragment = MarkdownBodyFragment.newInstance(
                getResources().getString(R.string.self_text));
        getChildFragmentManager().beginTransaction()
                .replace(R.id.body_container, mBodyFragment, "bodyFragment")
                .commit();
        return v;
    }

    public Map<String, List<String>> getSubmitBody() {
        Map<String, List<String>> body = new HashMap<>();
        body.put("kind", new ArrayList<String>(1));
        body.get("kind").add("self");
        body.put("text", new ArrayList<String>(1));
        body.get("text").add(mBodyFragment.getMarkdownBody());
        return body;
    }
}
