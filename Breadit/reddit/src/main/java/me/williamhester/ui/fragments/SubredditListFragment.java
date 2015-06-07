package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.SlidingTabLayout;

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

        ViewPager pager = (ViewPager) v.findViewById(R.id.pager);
        pager.setAdapter(new SubredditsPagerAdapter());

        SlidingTabLayout tabs = (SlidingTabLayout) v.findViewById(R.id.sliding_tabs);
        tabs.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.app_highlight);
            }
        });
        tabs.setViewPager(pager);

        RedditApi.getTrendingSubreddits(getActivity(), new FutureCallback<ArrayList<String>>() {
            @Override
            public void onCompleted(Exception e, ArrayList<String> result) {
                if (e != null) {

                } else {
                    for (String s : result) {
                        Log.d("trending", s);
                    }
                }
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

    private ListView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SubredditAdapter adapter = (SubredditAdapter) parent.getAdapter();
            Intent i = new Intent();
            i.putExtra(SELECTED_SUBREDDIT, adapter.getItem(position).getDisplayName());
            getActivity().setResult(Activity.RESULT_OK, i);
            getActivity().finish();
        }
    };

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

    private class SubredditsPagerAdapter extends PagerAdapter {
        private LayoutInflater mLayoutInflater;

        public SubredditsPagerAdapter() {
            mLayoutInflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object; // what?
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getResources().getString(R.string.subscriptions);
                case 1: return getResources().getString(R.string.special);
                case 2: return getResources().getString(R.string.recents);
                case 3: return getResources().getString(R.string.trending);
            }
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            ListView list = (ListView) mLayoutInflater
                    .inflate(R.layout.view_subreddits_list, collection, false);
            list.setAdapter(new SubredditAdapter());
            list.setOnItemClickListener(mItemClickListener);
            collection.addView(list, 0);
            return list;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }
    }
}
