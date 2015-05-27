package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import me.williamhester.models.Subreddit;
import me.williamhester.reddit.R;

/**
 * Created by william on 5/26/15.
 */
public class SubredditListFragment extends AccountFragment {

    public static final String SELECTED_SUBREDDIT = "selectedSubreddit";

    private String mSelectedSubreddit;
    private View mHeader;

    /**
     * Creates a new SubredditListFragment in an empty state.
     *
     * @param selected the currently selected subreddit.
     * @return a new SubredditListFragment.
     */
    public static SubredditListFragment newInstance(String selected) {
        Bundle args = new Bundle();
        args.putString("selected", selected);
        SubredditListFragment fragment = new SubredditListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSelectedSubreddit = savedInstanceState.getString(SELECTED_SUBREDDIT);
        } else {
            mSelectedSubreddit = getArguments().getString(SELECTED_SUBREDDIT);
        }
    }

    @Override
    public void onAccountChanged() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subreddit_list, container, false);

        ListView listView = (ListView) v.findViewById(R.id.list);
        mHeader = inflater.inflate(R.layout.header_subreddit_list, null, false);
        setUpHeader();
        listView.addHeaderView(mHeader);
        final SubredditAdapter adapter = new SubredditAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectSubreddit(adapter.getItem(position).getDisplayName());
            }
        });

        return v;
    }

    private void setUpHeader() {
        final EditText subreddit = (EditText) mHeader.findViewById(R.id.go_to_subreddit);
        subreddit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    selectSubreddit(subreddit.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void selectSubreddit(String subreddit) {
        Intent i = new Intent();
        i.putExtra(SELECTED_SUBREDDIT, subreddit);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
    }

    private class SubredditAdapter extends ArrayAdapter<Subreddit> {
        public SubredditAdapter() {
            super(getActivity(), R.layout.list_item_subreddit);

            ArrayList<Subreddit> subs = new ArrayList<>();
            for (String key : mAccount.getSubscriptions().keySet()) {
                subs.add(mAccount.getSubscriptions().get(key));
            }
            addAll(subs);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_subreddit, parent, false);
            }
            TextView subreddit = (TextView) convertView.findViewById(R.id.subreddit_list_item_title);
            subreddit.setText(getItem(position).getDisplayName());
            return convertView;
        }
    }
}

