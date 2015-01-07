package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

public class SubredditFragment extends AbsSubmissionListFragment implements
        Toolbar.OnMenuItemClickListener {

    private String mSubredditName;
    private ArrayList<Subreddit> mSubredditList = new ArrayList<>();
    private HashSet<String> mNames;
    private SubredditAdapter mSubredditAdapter;

    private boolean mSubredditExists = true;
    private boolean mHasLoadedOriginal;

    private TopLevelFragmentCallbacks mCallback;

    /**
     * Creates a new instance of SubredditFragment using the specified subreddit name.
     *
     * @param subredditName the name of the subreddit to visit or the empty string if visiting the
     *                      front page.
     * @return a new instance of SubredditFragment.
     */
    public static SubredditFragment newInstance(String subredditName) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putString("subreddit", subredditName);
        sf.setArguments(b);
        return sf;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof TopLevelFragmentCallbacks) {
            mCallback = (TopLevelFragmentCallbacks) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSubredditName = savedInstanceState.getString("subreddit");
            String[] array = savedInstanceState.getStringArray("names");
            mNames = new HashSet<>();
            Collections.addAll(mNames, array);
            mSubredditExists = savedInstanceState.getBoolean("subredditExists", true);
            ArrayList<Subreddit> subs = savedInstanceState.getParcelableArrayList("subreddits");
            mSubredditList.addAll(subs);
        } else if (getArguments() != null) {
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
        }
        if (mSubredditName == null) {
            mSubredditName = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, root, savedInstanceState);

        if (mCallback != null) {
            inflater.inflate(R.layout.toolbar_spinner, mToolbar, true);
            Spinner subs = (Spinner) mToolbar.findViewById(R.id.spinner);
            mSubredditAdapter = new SubredditAdapter();
            subs.setAdapter(mSubredditAdapter);
            if (savedInstanceState == null) {
                loadSubreddits(v);
            }
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onHomeClicked();
                }
            });
        } else {
            if (TextUtils.isEmpty(mSubredditName)) {
                mToolbar.setTitle(R.string.front_page);
            } else {
                mToolbar.setTitle("/r/" + mSubredditName);
            }
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }

        if (!mSubredditExists) {
            v.findViewById(R.id.loading_error).setVisibility(View.VISIBLE);
        }

        if (mSubmissionList.size() == 0) {
            onRefreshList();
        }
        return v;
    }

    @Override
    public void onAccountChanged() {
        onRefreshList();
        loadSubreddits(getView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("subreddit", mSubredditName);
        String[] array = new String[mNames.size()];
        mNames.toArray(array);
        outState.putStringArray("names", array);
        outState.putBoolean("subredditExists", mSubredditExists);
        outState.putParcelableArrayList("subreddits", mSubredditList);
        super.onSaveInstanceState(outState);
    }

    public void showSubredditDoesNotExist() {
        mSubredditExists = false;
        if (getView() != null) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            getView().findViewById(R.id.loading_error).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the current subreddit that the fragment is showing
     *
     * @return the display name of the subreddit that the fragment is showing
     */
    public String getSubreddit() {
        return mSubredditName;
    }

    /**
     * Reloads the data for the fragment. This should be called after the account has changed.
     */
    public void onRefreshList() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            loadAndClear();
        }
    }

    private void loadAndClear() {
        if (getActivity() != null) {
            RedditApi.getSubmissions(getActivity(), mSubredditName, mPrimarySortType,
                    mSecondarySortType, null, null, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            mLoading = false;
                            mProgressBar.setVisibility(View.GONE);
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (e == null) {
                                mNames.clear();
                                ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result,
                                        new Gson());
                                if (wrapper.getData() instanceof Listing) {
                                    ArrayList<Submission> submissions = new ArrayList<>();
                                    List<ResponseRedditWrapper> children =
                                            ((Listing) wrapper.getData()).getChildren();
                                    for (ResponseRedditWrapper innerWrapper : children) {
                                        if (innerWrapper.getData() instanceof Submission) {
                                            mNames.add(((Submission) innerWrapper.getData())
                                                    .getName());
                                            submissions.add((Submission) innerWrapper.getData());
                                        }
                                    }
                                    if (submissions.size() > 0) {
                                        mSubmissionList.clear();
                                        mSubmissionList.addAll(submissions);
                                        mSubmissionsAdapter.notifyDataSetChanged();
                                    }
                                }
                                mScrollListener.resetState();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void loadSubreddits(View view) {
        mSubredditList.clear();
        if (view != null) {
            Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
            if (mAccount != null) {
                mSubredditList.clear();
                AccountDataSource dataSource = new AccountDataSource(getActivity());
                dataSource.open();
                mSubredditList.addAll(dataSource.getCurrentAccountSubreddits());
                dataSource.close();
                HashMap<String, Subreddit> subscriptions = AccountManager.getAccount().getSubscriptions();
                for (Subreddit s : mSubredditList) {
                    subscriptions.put(s.getDisplayName().toLowerCase(), s);
                }
                Collections.sort(mSubredditList);
                RedditApi.getSubscribedSubreddits(new FutureCallback<ArrayList<Subreddit>>() {
                    @Override
                    public void onCompleted(Exception e, ArrayList<Subreddit> result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        AccountDataSource dataSource = new AccountDataSource(getActivity());
                        dataSource.open();
                        ArrayList<Subreddit> allSubs = dataSource.getAllSubreddits();
                        ArrayList<Subreddit> savedSubscriptions = dataSource.getCurrentAccountSubreddits();

                        for (Subreddit s : result) {
                            int index = allSubs.indexOf(s); // Get the subreddit WITH the table id
                            if (index < 0) { // if it doesn't exist, create one with a table id
                                dataSource.addSubreddit(s);
                                dataSource.addSubscriptionToCurrentAccount(s);
                            } else if (!savedSubscriptions.contains(s)) {
                                dataSource.addSubscriptionToCurrentAccount(allSubs.get(index));
                            }
                        }

                        dataSource.close();

                        final boolean isNew = !result.equals(savedSubscriptions);

                        if (isNew) {
                            mSubredditList.clear();
                            mSubredditList.addAll(result);
                        }

                        if (getView() != null) {
                            getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isNew) {
                                        Collections.sort(mSubredditList);
                                        mSubredditAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
                mSubredditAdapter.notifyDataSetChanged();
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (mHasLoadedOriginal) {
                            loadSubreddit(position == 0 ? "" : mSubredditList.get(position - 1).getDisplayName());
                        }
                        mHasLoadedOriginal = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                final String[] subs = getResources().getStringArray(R.array.default_subreddits);
                spinner.setAdapter(new SubredditStringAdapter(subs));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (mHasLoadedOriginal) {
                            loadSubreddit(position == 0 ? "" : subs[position - 1]);
                        }
                        mHasLoadedOriginal = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }
    }

    public void loadSubreddit(String subredditTitle) {
        mSubredditName = subredditTitle;
        mProgressBar.setVisibility(View.VISIBLE);
        mSubmissionList.clear();
        mSubmissionsAdapter.notifyDataSetChanged();
        loadAndClear();
    }

    @Override
    public boolean isFrontPage() {
        return TextUtils.isEmpty(mSubredditName);
    }

    @Override
    public void onLoadMore() {
        mProgressBar.setVisibility(View.VISIBLE);
        String after;
        if (mSubmissionList == null || mSubmissionList.size() == 0) {
            after = null;
        } else {
            after = mSubmissionList.get(mSubmissionList.size() - 1).getName();
        }
//        setFooterLoading();
        FutureCallback<JsonObject> callback = new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                mProgressBar.setVisibility(View.GONE);
                if (e == null) {
                    ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, new Gson());
                    if (wrapper.getData() instanceof Listing) {
                        ArrayList<Submission> submissions = new ArrayList<>();
                        List<ResponseRedditWrapper> children = ((Listing) wrapper.getData())
                                .getChildren();
                        if (children.size() != 0) {
                            for (ResponseRedditWrapper innerWrapper : children) {
                                if (innerWrapper.getData() instanceof Submission &&
                                        !mNames.contains(((Submission)innerWrapper.getData())
                                                .getName())) {
                                    submissions.add((Submission) innerWrapper.getData());
                                    mNames.add(((Submission) innerWrapper.getData()).getName());
                                }
                            }
                            mSubmissionList.addAll(submissions);
                            mSubmissionsAdapter.notifyItemRangeInserted(
                                    mSubmissionList.size() - submissions.size() + 1,
                                    submissions.size());
                        }
                    }
                } else {
                    e.printStackTrace();
//                    mSubmissionsAdapter.setFooterFailedToLoad();
                }
            }
        };
        RedditApi.getSubmissions(getActivity(), mSubredditName, mPrimarySortType,
                mSecondarySortType, null, after, callback);
    }

    private class SubredditAdapter extends ArrayAdapter<Subreddit> {

        public SubredditAdapter() {
            super(getActivity(), R.layout.list_item_subreddit, R.id.subreddit_list_item_title, mSubredditList);
        }

        @Override
        public View getDropDownView (int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_subreddit, parent, false);
            }
            TextView text = (TextView) convertView.findViewById(R.id.subreddit_list_item_title);
            text.setText(getItem(position) == null ? getResources().getString(R.string.front_page)
                    : getItem(position).getDisplayName());
            convertView.findViewById(R.id.mod_indicator).setVisibility(getItem(position) != null
                    && getItem(position).userIsModerator() ? View.VISIBLE : View.GONE);

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        @Override
        public Subreddit getItem(int position) {
            if (position == 0) {
                return null;
            } else {
                return super.getItem(position - 1);
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + 1; // Have to account for the "Front Page" option
        }
    }

    private class SubredditStringAdapter extends ArrayAdapter<String> {

        public SubredditStringAdapter(String[] items) {
            super(getActivity(), R.layout.list_item_subreddit, R.id.subreddit_list_item_title, items);
        }

        @Override
        public String getItem(int position) {
            if (position == 0) {
                return getResources().getString(R.string.front_page).toLowerCase();
            } else {
                return super.getItem(position - 1).toLowerCase();
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + 1; // Have to account for the "Front Page" option
        }
    }
}
