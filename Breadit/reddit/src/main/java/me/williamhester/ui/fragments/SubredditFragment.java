package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
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
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.adapters.SubmissionAdapter;

public class SubredditFragment extends AccountFragment implements SubmissionAdapter.AdapterCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;

    private ListView mListView;
    private String mSubredditName;
    private SubmissionAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;

    private String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    private String mSecondarySortType = RedditApi.SECONDARY_SORT_ALL;

    private View mFooter;

    public static SubredditFragment newInstance(String subredditName) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putString("subreddit", subredditName);
        sf.setArguments(b);
        return sf;
    }

    public static SubredditFragment newInstance(int type) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        sf.setArguments(b);
        return sf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSubredditName = savedInstanceState.getString("subreddit");
            mSubmissionList = savedInstanceState.getParcelableArrayList("submissions");
            String[] array = savedInstanceState.getStringArray("names");
            mNames = new HashSet<>();
            Collections.addAll(mNames, array);
        } else if (getArguments() != null) {
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
            mSubmissionList = new ArrayList<>();
        }
        if (mSubredditName == null) {
            mSubredditName = "";
        }
        mSubmissionsAdapter = new SubmissionAdapter(getActivity(), this, mSubmissionList);
        setHasOptionsMenu(true);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, root, false);

        mFooter = createFooterView(inflater, root);

        mListView = (ListView) v.findViewById(R.id.submissions_list);
//        mListView.addFooterView(mFooter);
        mListView.setAdapter(mSubmissionsAdapter);
        mListView.setOnScrollListener(new InfiniteLoadingScrollListener());

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orangered, R.color.periwinkle,
                R.color.orangered, R.color.periwinkle);
        if (mSubmissionList.size() == 0) {
            populateSubmissions();
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.subreddit, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        String title;
        if (getActivity() != null && ((ActionBarActivity) getActivity()).getSupportActionBar() != null) {
            if (!TextUtils.isEmpty(mSubredditName)) {
                title = "/r/" + mSubredditName;
            } else {
                title = getResources().getString(R.string.front_page);
            }
            ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOTE_REQUEST_CODE) {
            if (data == null)
                return;
            Bundle b = data.getExtras();
            String name = b.getString("name");
            int status = b.getInt("status");
            if (name != null) {
                for (Submission submission : mSubmissionList) {
                    if (submission.getName().equals(name)) {
                        submission.setVoteStatus(status);
                        mSubmissionsAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            View anchor = getActivity().findViewById(R.id.action_sort);
            PopupMenu popupMenu = new PopupMenu(getActivity(), getActivity().findViewById(R.id.action_sort));
            popupMenu.setOnMenuItemClickListener(new PrimarySortClickListener(anchor));
            popupMenu.inflate(R.menu.primary_sorts);
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountChanged() {
        super.onAccountChanged();
        refreshData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("submissions", mSubmissionList);
        outState.putString("subreddit", mSubredditName);
        String[] array = new String[mNames.size()];
        mNames.toArray(array);
        outState.putStringArray("names", array);
        super.onSaveInstanceState(outState);
    }

    private void setSort(String primary, String secondary) {
        if (!primary.equals(mPrimarySortType)
                || secondary == null && mSecondarySortType != null
                || secondary != null && mSecondarySortType == null
                || (secondary != null && !secondary.equals(mSecondarySortType))) {
            mPrimarySortType = primary;
            mSecondarySortType = secondary;
            refreshData();
        }
    }

    public String getSubreddit() {
        return mSubredditName;
    }

    public void refreshData() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            if (getActivity() != null) {
                RedditApi.getSubmissions(getActivity(), mSubredditName, mPrimarySortType, mSecondarySortType,
                        null, null, new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e == null) {
                                    ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, new Gson());
                                    if (wrapper.getData() instanceof Listing) {
                                        ArrayList<Submission> submissions = new ArrayList<>();
                                        List<ResponseRedditWrapper> children = ((Listing) wrapper.getData()).getChildren();
                                        for (ResponseRedditWrapper innerWrapper : children) {
                                            if (innerWrapper.getData() instanceof Submission) {
                                                submissions.add((Submission) innerWrapper.getData());
                                            }
                                        }
                                        if (submissions.size() > 0) {
                                            mSubmissionList.clear();
                                            mSubmissionList.addAll(submissions);
                                            mSubmissionsAdapter.notifyDataSetChanged();
                                            mSwipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                } else {
                                    e.printStackTrace();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
            }
        }
    }

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        refreshData();
    }

    private View createFooterView(LayoutInflater inflater, ViewGroup root) {
        return inflater.inflate(R.layout.footer_subreddit_fragment, root, false);
    }

    private void setFooterFailedToLoad() {
        TextView text = (TextView) mFooter.findViewById(R.id.footer_text);
        text.setText(R.string.failed_to_load);
        mFooter.findViewById(R.id.progress_bar).setVisibility(View.GONE);
        mFooter.findViewById(R.id.inner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreSubmissions();
            }
        });
    }

    private void setFooterLoading() {
        TextView text = (TextView) mFooter.findViewById(R.id.footer_text);
        text.setText(R.string.loading_submissions);
        mFooter.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        mFooter.findViewById(R.id.inner).setOnClickListener(null);
    }

    @Override
    public void onImageViewClicked(Object imgurData) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(imgurData), "ImagePagerFragment")
                .addToBackStack("ImagePagerFragment")
                .commit();
    }

    @Override
    public void onImageViewClicked(String imageUrl) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(imageUrl), "ImagePagerFragment")
                .addToBackStack("ImagePagerFragment")
                .commit();
    }

    @Override
    public void onYouTubeVideoClicked(String videoId) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, YouTubeFragment.newInstance(videoId), "YouTubeFragment")
                .addToBackStack("YouTubeFragment")
                .commit();
    }

    @Override
    public void onCardClicked(Submission submission) {
        Intent i = new Intent(getActivity(), SubmissionActivity.class);
        Bundle args = new Bundle();
        args.putParcelable(SubmissionActivity.SUBMISSION, submission);
        args.putSerializable("media", submission.getMedia());
        args.putString(SubmissionActivity.TAB, SubmissionActivity.COMMENT_TAB);
        i.putExtras(args);
        startActivityForResult(i, VOTE_REQUEST_CODE);
    }

    private class PrimarySortClickListener implements PopupMenu.OnMenuItemClickListener {

        private View mAnchor;

        public PrimarySortClickListener(View anchor) {
            mAnchor = anchor;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String tempSortType;
            switch (item.getItemId()) {
                case R.id.sort_hot:
                    tempSortType = RedditApi.SORT_TYPE_HOT;
                    break;
                case R.id.sort_new:
                    tempSortType = RedditApi.SORT_TYPE_NEW;
                    break;
                case R.id.sort_rising:
                    tempSortType = RedditApi.SORT_TYPE_RISING;
                    break;
                case R.id.sort_controversial: {
                    tempSortType = RedditApi.SORT_TYPE_CONTROVERSIAL;
                    PopupMenu popupMenu = new PopupMenu(getActivity(), mAnchor);
                    popupMenu.setOnMenuItemClickListener(new SecondarySortClickListener(tempSortType));
                    popupMenu.inflate(R.menu.secondary_sorts);
                    popupMenu.show();
                    return true;
                }
                case R.id.sort_top: {
                    tempSortType = RedditApi.SORT_TYPE_TOP;
                    PopupMenu popupMenu = new PopupMenu(getActivity(), mAnchor);
                    popupMenu.setOnMenuItemClickListener(new SecondarySortClickListener(tempSortType));
                    popupMenu.inflate(R.menu.secondary_sorts);
                    popupMenu.show();
                    return true;
                }
                default:
                    tempSortType = RedditApi.SORT_TYPE_HOT;
            }
            setSort(tempSortType, null);
            return true;
        }
    }

    private class SecondarySortClickListener implements PopupMenu.OnMenuItemClickListener {

        private String mPrimary;

        public SecondarySortClickListener(String primary) {
            mPrimary = primary;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String tempSortType;
            switch (item.getItemId()) {
                case R.id.sort_hour:
                    tempSortType = RedditApi.SECONDARY_SORT_HOUR;
                    break;
                case R.id.sort_today:
                    tempSortType = RedditApi.SECONDARY_SORT_DAY;
                    break;
                case R.id.sort_week:
                    tempSortType = RedditApi.SECONDARY_SORT_WEEK;
                    break;
                case R.id.sort_month:
                    tempSortType = RedditApi.SECONDARY_SORT_MONTH;
                    break;
                case R.id.sort_year:
                    tempSortType = RedditApi.SECONDARY_SORT_YEAR;
                    break;
                case R.id.sort_all:
                    tempSortType = RedditApi.SECONDARY_SORT_ALL;
                    break;
                default:
                    tempSortType = RedditApi.SECONDARY_SORT_ALL;
            }
            setSort(mPrimary, tempSortType);
            return true;
        }
    }

    public class InfiniteLoadingScrollListener implements AbsListView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int previousTotal = 0;
        private boolean loading = true;

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            if (loading) {
                if (mSubmissionsAdapter.getCount() > previousTotal) {
                    previousTotal = mSubmissionsAdapter.getCount();
                    loading = false;
                }
            } else if (mSubmissionList.size() > 0
                    && (mSubmissionsAdapter.getCount() - mListView.getChildCount()) // 25 - 4 = 21
                    <= (mListView.getFirstVisiblePosition() + VISIBLE_THRESHOLD)) { // 20 + 5 = 25
                loadMoreSubmissions();
                loading = true;
            }
        }
    }

    private void loadMoreSubmissions() {
        String after;
        if (mSubmissionList == null || mSubmissionList.size() == 0) {
            after = null;
        } else {
            after = mSubmissionList.get(mSubmissionList.size() - 1).getName();
        }
        setFooterLoading();
        RedditApi.getSubmissions(getActivity(), mSubredditName, mPrimarySortType,
                mSecondarySortType, null, after, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, new Gson());
                            if (wrapper.getData() instanceof Listing) {
                                ArrayList<Submission> submissions = new ArrayList<>();
                                List<ResponseRedditWrapper> children = ((Listing) wrapper.getData()).getChildren();
                                for (ResponseRedditWrapper innerWrapper : children) {
                                    if (innerWrapper.getData() instanceof Submission) {
                                        submissions.add((Submission) innerWrapper.getData());
                                    }
                                }
                                mSubmissionList.addAll(submissions);
                                mSubmissionsAdapter.notifyDataSetChanged();
                            }
                        } else {
                            e.printStackTrace();
                            setFooterFailedToLoad();
                        }
                    }
                });
    }
}
