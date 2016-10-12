package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.models.reddit.Submission;
import me.williamhester.models.reddit.Subreddit;
import me.williamhester.network.Callback;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SelectSubredditActivity;

public class SubredditFragment extends AbsSubmissionListFragment implements
        Toolbar.OnMenuItemClickListener {

    private static final int SELECT_SUBREDDIT = 1;

    @Save String mSubredditName;
    @Save ArrayList<Subreddit> mSubredditList = new ArrayList<>();
    @Save HashSet<String> mNames;
    @Save boolean mSubredditExists = true;

    @Bind(R.id.current_subreddit) TextView mTitle;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_SUBREDDIT) {
            if (resultCode == Activity.RESULT_OK) {
                String subreddit = data.getStringExtra(SubredditListFragment.SELECTED_SUBREDDIT);
                loadSubreddit(subreddit);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

        if (savedInstanceState == null && getArguments() != null) {
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
        }
        if (mSubredditName == null) {
            mSubredditName = "";
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mCallback != null) {
            if (TextUtils.isEmpty(mSubredditName)) {
                mTitle.setText(R.string.front_page);
            } else {
                mTitle.setText("/r/" + mSubredditName);
            }
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), SelectSubredditActivity.class);
                    Bundle args = new Bundle();
                    args.putString(SubredditListFragment.SELECTED_SUBREDDIT, mSubredditName);
                    i.putExtras(args);
                    startActivityForResult(i, SELECT_SUBREDDIT);
                }
            });
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
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }

        if (!mSubredditExists) {
            view.findViewById(R.id.loading_error).setVisibility(View.VISIBLE);
        }

        if (mSubmissionList.size() == 0) {
            onRefreshList();
        }
    }

    @Override
    public void onAccountChanged() {
        onRefreshList();
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
            mApi.getSubmissions(mSubredditName, mPrimarySortType,
                    mSecondarySortType, null,
                    new Callback<List<Submission>>() {
                        @Override
                        public void onCompleted() {
                            mLoading = false;
                            if (getView() != null) {
                                mProgressBar.setVisibility(View.GONE);
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }

                        @Override
                        public void onSuccess(List<Submission> data) {
                            mNames.clear();
                            mSubmissionList.clear();
                            mSubmissionList.addAll(data);
                            mSubmissionsAdapter.notifyDataSetChanged();
                            for (Submission s : mSubmissionList) {
                                mNames.add(s.getId());
                            }
                            mScrollListener.resetState();
                        }
                    }
            );
        }
    }

    public void loadSubreddit(String subredditTitle) {
        mSubredditName = subredditTitle;
        mSubmissionList.clear();
        if (getView() == null) {
            return;
        }
        if (mTitle != null) {
            if (TextUtils.isEmpty(mSubredditName)) {
                mTitle.setText(R.string.front_page);
            } else {
                mTitle.setText("/r/" + mSubredditName);
            }
        }
        mSubmissionsAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.VISIBLE);
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
            after = mSubmissionList.get(mSubmissionList.size() - 1).getId();
        }
        Callback<List<Submission>> callback = new Callback<List<Submission>>() {
            @Override
            public void onCompleted() {
                mLoading = false;
                if (getView() != null) {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onSuccess(List<Submission> data) {
                mSubmissionList.addAll(data);
                mSubmissionsAdapter.notifyDataSetChanged();
                for (Submission s : mSubmissionList) {
                    mNames.add(s.getId());
                }
                mScrollListener.resetState();
            }
        };
        mApi.getSubmissions(mSubredditName, mPrimarySortType, mSecondarySortType, after, callback);
    }
}
