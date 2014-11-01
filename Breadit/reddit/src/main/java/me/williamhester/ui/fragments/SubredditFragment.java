package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.adapters.SubmissionAdapter;
import me.williamhester.ui.views.SubmissionViewHolder;

public class SubredditFragment extends AccountFragment implements SubmissionViewHolder.SubmissionCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;

    private ListView mListView;
    private String mSubredditName;
    private SubmissionAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;
    private SubmissionViewHolder mFocusedSubmission;

    /**
     * The primary sort type is set, by default to hot, Reddit's default sort for subreddits.
     */
    private String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    /**
     * The secondary sort type does not need to be set, as hot has no secondary type.
     */
    private String mSecondarySortType;

    private View mFooter;
    private OnSubredditSelectedListener mCallback;

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
        if (activity instanceof OnSubredditSelectedListener) {
            mCallback = (OnSubredditSelectedListener) activity;
        }
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

        mFooter = createFooterView(inflater);

        mListView = (ListView) v.findViewById(R.id.submissions_list);
        mListView.addFooterView(mFooter);
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

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orangered);
        if (mSubmissionList.size() == 0) {
            refreshData();
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
            PopupMenu popupMenu = new PopupMenu(getActivity(), anchor, Gravity.TOP);
            anchor.setOnTouchListener(popupMenu.getDragToOpenListener());
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

    /**
     * Sets the sort type of the fragment to the specified sort type(s)
     *
     * @param primary the primary sort type defined in the RedditApi class
     * @param secondary the secondary sort type defined in the RedditApi class
     */
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

    @SuppressLint("InflateParams")
    private View createFooterView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.footer_subreddit_fragment, null);
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
        // TODO: fix this when YouTube updates their Android API
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, YouTubeFragment.newInstance(videoId), "YouTubeFragment")
                    .addToBackStack("YouTubeFragment")
                    .commit();
        } else {
            Intent i = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + videoId));
            getActivity().startActivity(i);
        }
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

    @Override
    public void onCardLongPressed(SubmissionViewHolder holder) {
        if (mFocusedSubmission != null) {
            mFocusedSubmission.collapseOptions();
        }
        mFocusedSubmission = holder;
    }

    @Override
    public void onOptionsRowItemSelected(View view, final Submission submission) {
        switch (view.getId()) {
            case R.id.option_go_to_subreddit:
                mCallback.onSubredditSelected(submission.getSubredditName());
                break;
            case R.id.option_view_user:
                Bundle b = new Bundle();
                b.putString("username", submission.getAuthor());
                Intent i = new Intent(getActivity(), UserActivity.class);
                i.putExtras(b);
                getActivity().startActivity(i);
                break;
            case R.id.option_share:
                PopupMenu menu = new PopupMenu(getActivity(), view);
                menu.inflate(R.menu.share);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        if (item.getItemId() == R.id.share_link) {
                            sendIntent.putExtra(Intent.EXTRA_TEXT, submission.getUrl());
                        } else {
                            String link = RedditApi.PUBLIC_REDDIT_URL + submission.getPermalink();
                            sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                        }
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent,
                                getResources().getText(R.string.share_with)));
                        return false;
                    }
                });
                menu.show();
                break;
            case R.id.option_save:
                // TODO: actually save it
                break;
            case R.id.option_overflow:
                inflateOverflowPopupMenu(view, submission);
                break;
        }
    }

    @Override
    public void onVoted(Submission submission) {
        // Don't do anything
    }

    @Override
    public boolean isFrontPage() {
        return TextUtils.isEmpty(mSubredditName);
    }

    private void inflateOverflowPopupMenu(View view, final Submission submission) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);

        Account account = AccountManager.getAccount();
        Map<String, Subreddit> subscriptions = account.getSubscriptions();
        boolean isMod = subscriptions.containsKey(submission.getSubredditName().toLowerCase())
                && subscriptions.get(submission.getSubredditName().toLowerCase()).userIsModerator();
        boolean isOp = submission.getAuthor().equalsIgnoreCase(account.getUsername());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                FutureCallback<String> removeCallback = new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        mSubmissionList.remove(submission);
                        mSubmissionsAdapter.notifyDataSetChanged();
                    }
                };
                switch (item.getItemId()) {
                    case R.id.overflow_hide: {
                        FutureCallback<String> callback = new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null) {
                                    e.printStackTrace();
                                    return;
                                }
                                if (!submission.isHidden()) {
                                    mSubmissionList.remove(submission);
                                    mSubmissionsAdapter.notifyDataSetChanged();
                                }
                            }
                        };
                        if (submission.isHidden()) {
                            RedditApi.unhide(getActivity(), submission, callback);
                        } else {
                            RedditApi.hide(getActivity(), submission, callback);
                        }
                        break;
                    }
                    case R.id.overflow_mark_nsfw: {
                        FutureCallback<String> callback = new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null) {
                                    e.printStackTrace();
                                }
                                submission.setIsNsfw(!submission.isNsfw());
                                mSubmissionsAdapter.notifyDataSetChanged();
                            }
                        };
                        if (submission.isNsfw()) {
                            RedditApi.unmarkNsfw(getActivity(), submission, callback);
                        } else {
                            RedditApi.markNsfw(getActivity(), submission, callback);
                        }
                        break;
                    }
                    case R.id.overflow_report: break; // TODO: Make a form for this
                    case R.id.overflow_delete: {
                        RedditApi.delete(getActivity(), submission, removeCallback);
                        break;
                    }
                    case R.id.overflow_approve: {
                        FutureCallback<String> callback = new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e != null) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        RedditApi.approve(getActivity(), submission, callback);
                        break;
                    }
                    case R.id.overflow_remove: {
                        RedditApi.remove(getActivity(), submission, false, removeCallback);
                        break;
                    }
                    case R.id.overflow_spam: {
                        RedditApi.remove(getActivity(), submission, true, removeCallback);
                        break;
                    }
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.submission_overflow);

        Menu menu = popupMenu.getMenu();
        if ((isOp || isMod) && submission.isNsfw()) {
            menu.findItem(R.id.overflow_mark_nsfw).setTitle(R.string.unmark_nsfw);
        }
        if (submission.isHidden()) {
            menu.findItem(R.id.overflow_hide).setTitle(R.string.unhide);
        }
        menu.findItem(R.id.overflow_report).setVisible(!isMod);
        menu.findItem(R.id.overflow_mark_nsfw).setVisible(isOp || isMod);
        menu.findItem(R.id.overflow_delete).setVisible(isOp);
        menu.findItem(R.id.overflow_approve).setVisible(isMod);
        menu.findItem(R.id.overflow_remove).setVisible(isMod);
        menu.findItem(R.id.overflow_spam).setVisible(isMod);

        popupMenu.show();
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

    public static interface OnSubredditSelectedListener {
        public void onSubredditSelected(String subreddit);
    }
}
