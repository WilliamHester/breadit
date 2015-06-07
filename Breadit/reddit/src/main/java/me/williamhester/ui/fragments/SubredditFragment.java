package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SelectSubredditActivity;

public class SubredditFragment extends AbsSubmissionListFragment implements
        Toolbar.OnMenuItemClickListener {

    private static final int SELECT_SUBREDDIT = 1;

    private String mSubredditName;
    private ArrayList<Subreddit> mSubredditList = new ArrayList<>();
    private HashSet<String> mNames;
    private TextView mTitle;

    private boolean mSubredditExists = true;

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
            mTitle = (TextView) v.findViewById(R.id.current_subreddit);
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

    public void loadSubreddit(String subredditTitle) {
        mSubredditName = subredditTitle;
        mSubmissionList.clear();
        if (getView() == null) {
            return;
        }
        if (mTitle != null) {
            mTitle.setText(subredditTitle);
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
}
