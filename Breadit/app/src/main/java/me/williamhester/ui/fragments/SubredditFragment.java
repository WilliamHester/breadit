package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.models.reddit.RedditListing;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.models.reddit.RedditResponseWrapper;
import me.williamhester.models.reddit.RedditSubreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SelectSubredditActivity;

public class SubredditFragment extends AbsSubmissionListFragment implements
        Toolbar.OnMenuItemClickListener {

    private static final int SELECT_SUBREDDIT = 1;

    @Save String mSubredditName;
    @Save ArrayList<RedditSubreddit> mRedditSubredditList = new ArrayList<>();
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
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
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

        if (mRedditSubmissionList.size() == 0) {
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
            RedditApi.getSubmissions(getActivity(), mSubredditName, mPrimarySortType,
                    mSecondarySortType, null, null, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            mLoading = false;
                            if (getView() != null) {
                                mProgressBar.setVisibility(View.GONE);
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                            if (e == null) {
                                mNames.clear();
                                RedditResponseWrapper wrapper = new RedditResponseWrapper(result,
                                        new Gson());
                                if (wrapper.getData() instanceof RedditListing) {
                                    ArrayList<RedditSubmission> redditSubmissions = new ArrayList<>();
                                    List<RedditResponseWrapper> children =
                                            ((RedditListing) wrapper.getData()).getChildren();
                                    for (RedditResponseWrapper innerWrapper : children) {
                                        if (innerWrapper.getData() instanceof RedditSubmission) {
                                            mNames.add(((RedditSubmission) innerWrapper.getData())
                                                    .getName());
                                            redditSubmissions.add((RedditSubmission) innerWrapper.getData());
                                        }
                                    }
                                    if (redditSubmissions.size() > 0) {
                                        mRedditSubmissionList.clear();
                                        mRedditSubmissionList.addAll(redditSubmissions);
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
        mRedditSubmissionList.clear();
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
        if (mRedditSubmissionList == null || mRedditSubmissionList.size() == 0) {
            after = null;
        } else {
            after = mRedditSubmissionList.get(mRedditSubmissionList.size() - 1).getName();
        }
//        setFooterLoading();
        FutureCallback<JsonObject> callback = new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                mProgressBar.setVisibility(View.GONE);
                if (e == null) {
                    RedditResponseWrapper wrapper = new RedditResponseWrapper(result, new Gson());
                    if (wrapper.getData() instanceof RedditListing) {
                        ArrayList<RedditSubmission> redditSubmissions = new ArrayList<>();
                        List<RedditResponseWrapper> children = ((RedditListing) wrapper.getData())
                                .getChildren();
                        if (children.size() != 0) {
                            for (RedditResponseWrapper innerWrapper : children) {
                                if (innerWrapper.getData() instanceof RedditSubmission &&
                                        !mNames.contains(((RedditSubmission)innerWrapper.getData())
                                                .getName())) {
                                    redditSubmissions.add((RedditSubmission) innerWrapper.getData());
                                    mNames.add(((RedditSubmission) innerWrapper.getData()).getName());
                                }
                            }
                            mRedditSubmissionList.addAll(redditSubmissions);
                            mSubmissionsAdapter.notifyItemRangeInserted(
                                    mRedditSubmissionList.size() - redditSubmissions.size() + 1,
                                    redditSubmissions.size());
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
