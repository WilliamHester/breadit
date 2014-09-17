package me.williamhester.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.adapters.SubmissionsRecyclerAdapter;

public class SubredditFragment extends AccountFragment implements SubmissionsRecyclerAdapter.AdapterCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;

    private Context mContext;
    private String mSubredditName;
    private SubmissionsRecyclerAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;

    private RecyclerView mSubmissionsView;
    private LinearLayoutManager mLayoutManager;

    private boolean mHideNsfw = true;
    private boolean mHideViewed = false;
    private String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    private String mSecondarySortType = RedditApi.SECONDARY_SORT_ALL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        if (savedInstanceState != null) {
            mSubredditName = savedInstanceState.getString("subreddit");
            mSubmissionList = (ArrayList<Submission>) savedInstanceState.getSerializable("submissions");
            String[] array = savedInstanceState.getStringArray("names");
            mNames = new HashSet<>();
            for (String name : array) {
                mNames.add(name);
            }
        } else if (getArguments() != null) {
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
            mSubmissionList = new ArrayList<>();
        }
        if (mSubredditName == null) {
            mSubredditName = "";
        }
        mSubmissionsAdapter = new SubmissionsRecyclerAdapter(mSubmissionList, this);
        loadPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, null);

        mSubmissionsView = (RecyclerView) v.findViewById(R.id.Submissions_recycler);
        mSubmissionsView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mSubmissionsView.setLayoutManager(mLayoutManager);
        mSubmissionsView.setAdapter(mSubmissionsAdapter);
        mSubmissionsView.setOnScrollListener(new InfiniteLoadingScrollListener());
        mSubmissionsView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
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
    public void onResume() {
        super.onResume();
        String title = "";
        if (getActivity() != null && getActivity().getActionBar() != null) {
            if (!TextUtils.isEmpty(mSubredditName)) {
                title = "/r/" + mSubredditName;
            } else {
                title = "Front Page";
            }
            getActivity().getActionBar().setTitle(title);
        }
        loadPrefs();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOTE_REQUEST_CODE) {
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
    protected void onAccountChanged() {
        refreshData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("submissions", mSubmissionList);
        outState.putString("subreddit", mSubredditName);
        String[] array = new String[mNames.size()];
        mNames.toArray(array);
        outState.putStringArray("names", array);
        super.onSaveInstanceState(outState);
    }

    public void setPrimarySort(int sortType) {
        String tempSortType;
        switch (sortType) {
            case 0:
                tempSortType = RedditApi.SORT_TYPE_HOT;
                break;
            case 1:
                tempSortType = RedditApi.SORT_TYPE_NEW;
                break;
            case 2:
                tempSortType = RedditApi.SORT_TYPE_RISING;
                break;
            case 3:
                tempSortType = RedditApi.SORT_TYPE_CONTROVERSIAL;
                break;
            case 4:
                tempSortType = RedditApi.SORT_TYPE_TOP;
                break;
            default:
                tempSortType = RedditApi.SORT_TYPE_HOT;
        }
        if (!mPrimarySortType.equals(tempSortType)) {
            mPrimarySortType = tempSortType;
            refreshData();
        }
    }

    public void setSecondarySort(int sortType) {
        String tempSortType;
        switch (sortType) {
            case 0:
                tempSortType = RedditApi.SECONDARY_SORT_HOUR;
                break;
            case 1:
                tempSortType = RedditApi.SECONDARY_SORT_DAY;
                break;
            case 2:
                tempSortType = RedditApi.SECONDARY_SORT_WEEK;
                break;
            case 3:
                tempSortType = RedditApi.SECONDARY_SORT_MONTH;
                break;
            case 4:
                tempSortType = RedditApi.SECONDARY_SORT_YEAR;
                break;
            case 5:
                tempSortType = RedditApi.SECONDARY_SORT_ALL;
                break;
            default:
                tempSortType = RedditApi.SECONDARY_SORT_ALL;
        }
        if (!mSecondarySortType.equals(tempSortType)) {
            mSecondarySortType = tempSortType;
            refreshData();
        }
    }

    public String getPrimarySortType() {
        return mPrimarySortType;
    }

    public String getSecondarySortType() {
        return mSecondarySortType;
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

    private void loadPrefs() {
        SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean oldHideViewed = mHideViewed;
        if (mSubredditName != null) {
            mHideViewed = prefs.getBoolean("pref_remove_viewed_sub", false);
        } else {
            mHideViewed = prefs.getBoolean("pref_remove_viewed_front", false);
        }
        mHideNsfw = prefs.getBoolean("pref_hide_nsfw", true);
        if (getActivity() != null)
            getActivity().invalidateOptionsMenu();
        if (oldHideViewed != mHideViewed) {
            refreshData();
        }
    }

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        refreshData();
    }

    private View createFooterView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.footer_subreddit_fragment, null);
        return v;
    }

    @Override
    public void onImageViewClicked(ImgurImage image) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(image), "ImagePagerFragment")
                .addToBackStack("ImagePagerFragment")
                .commit();
    }

    @Override
    public void onImageViewClicked(ImgurAlbum album) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(album), "ImagePagerFragment")
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
        args.putSerializable(SubmissionActivity.SUBMISSION, submission);
        args.putSerializable("media", submission.getMedia());
        args.putString(SubmissionActivity.TAB, SubmissionActivity.COMMENT_TAB);
        i.putExtras(args);
        startActivityForResult(i, VOTE_REQUEST_CODE);
    }

    public class InfiniteLoadingScrollListener implements RecyclerView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int previousTotal = 0;
        private boolean loading = true;

        @Override
        public void onScrollStateChanged(int i) {
            // Don't care
        }

        @Override
        public void onScrolled(int i, int i2) {
            if (loading) {
                if (mSubmissionsAdapter.getItemCount() > previousTotal) {
                    previousTotal = mSubmissionsAdapter.getItemCount();
                    loading = false;
                }
            } else if (mSubmissionList.size() > 0
                    && (mSubmissionsAdapter.getItemCount() - mLayoutManager.getChildCount()) // 25 - 4 = 21
                    <= (mLayoutManager.findFirstVisibleItemPosition() + VISIBLE_THRESHOLD)) { // 20 + 5 = 25
                String after;
                if (mSubmissionList == null || mSubmissionList.size() == 0) {
                    after = null;
                } else {
                    after = mSubmissionList.get(mSubmissionList.size() - 1).getName();
                }
                mSwipeRefreshLayout.setRefreshing(true);
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
                                        mSwipeRefreshLayout.setRefreshing(false);
                                    }
                                } else {
                                    e.printStackTrace();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                loading = true;
            }
        }
    }
}
