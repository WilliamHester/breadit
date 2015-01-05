package me.williamhester.ui.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.Map;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;

public class SubredditFragment extends AccountFragment implements Toolbar.OnMenuItemClickListener,
        SubmissionViewHolder.SubmissionCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;

    private String mSubredditName;
    private ArrayList<Subreddit> mSubredditList = new ArrayList<>();
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;

    private InfiniteLoadingScrollListener mScrollListener;
    private LinearLayoutManager mLayoutManager;

    private SubmissionAdapter mSubmissionsAdapter;
    private SubredditAdapter mSubredditAdapter;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SubmissionViewHolder mFocusedSubmission;
    private Toolbar mToolbar;
    private View mHeaderBar;

    private boolean mLoading = true;
    private boolean mSubredditExists = true;
    private boolean mHasLoadedOriginal;

    /**
     * The primary sort type is set, by default to hot, Reddit's default sort for subreddits.
     */
    private String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    /**
     * The secondary sort type does not need to be set, as hot has no secondary type.
     */
    private String mSecondarySortType;

    private SubredditFragmentCallbacks mCallback;

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
        if (activity instanceof SubredditFragmentCallbacks) {
            mCallback = (SubredditFragmentCallbacks) activity;
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
            mLoading = savedInstanceState.getBoolean("loading");
            mSubredditExists = savedInstanceState.getBoolean("subredditExists", true);
        } else if (getArguments() != null) {
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
            mSubmissionList = new ArrayList<>();
        }
        if (mSubredditName == null) {
            mSubredditName = "";
        }
        mSubmissionsAdapter = new SubmissionAdapter(this, mSubmissionList);
        setHasOptionsMenu(true);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, root, false);

        mHeaderBar = v.findViewById(R.id.header_bar);
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);

        mToolbar.setOnMenuItemClickListener(this);
        if (mCallback != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onHomeClicked();
                }
            });
        } else {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }

        mScrollListener = new InfiniteLoadingScrollListener();
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView = (RecyclerView) v.findViewById(R.id.content_list);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(
                R.drawable.card_divider)));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mSubmissionsAdapter);
        mRecyclerView.setOnScrollListener(mScrollListener);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mSubredditAdapter = new SubredditAdapter();
        Spinner subreddits = (Spinner) v.findViewById(R.id.user_spinner);
        subreddits.setAdapter(mSubredditAdapter);
        loadSubreddits(v);

        if (!mSubredditExists) {
            v.findViewById(R.id.subreddit_does_not_exist).setVisibility(View.VISIBLE);
        }

        if (!mLoading && mSubmissionList.size() > 0) {
            mProgressBar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orangered);
        if (mSubmissionList.size() == 0) {
            refreshData();
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.subreddit, menu);

        View anchor = mToolbar.findViewById(R.id.action_sort);
        if (anchor != null) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), anchor);
            anchor.setTag(popupMenu);
            anchor.setOnTouchListener(popupMenu.getDragToOpenListener());
            popupMenu.setOnMenuItemClickListener(mSortClickListener);
            popupMenu.inflate(R.menu.primary_sorts);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String title;
        if (getActivity() instanceof ActionBarActivity
                && ((ActionBarActivity) getActivity()).getSupportActionBar() != null) {
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
                for (int i = 0; i < mSubmissionList.size(); i++) {
                    if (mSubmissionList.get(i).getName().equals(name)) {
                        mSubmissionList.get(i).setVoteStatus(status);
                        mSubmissionsAdapter.notifyItemChanged(i);
                        return;
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAccountChanged() {
        super.onAccountChanged();
        refreshData();
        loadSubreddits(getView());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("submissions", mSubmissionList);
        outState.putString("subreddit", mSubredditName);
        String[] array = new String[mNames.size()];
        mNames.toArray(array);
        outState.putStringArray("names", array);
        outState.putBoolean("loading", mLoading);
        outState.putBoolean("subredditExists", mSubredditExists);
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
            mProgressBar.setVisibility(View.VISIBLE);
            loadAndClear();
        }
    }

    public void showSubredditDoesNotExist() {
        mSubredditExists = false;
        if (getView() != null) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            getView().findViewById(R.id.subreddit_does_not_exist).setVisibility(View.VISIBLE);
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
            Spinner spinner = (Spinner) view.findViewById(R.id.user_spinner);
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
    public void onCardClicked(Submission submission) {
        Intent i = new Intent(getActivity(), BrowseActivity.class);
        Bundle args = new Bundle();
        args.putString("type", "comments");
        args.putParcelable("submission", submission);
        args.putParcelable("media", submission.getMedia());
        i.putExtras(args);
        startActivityForResult(i, VOTE_REQUEST_CODE);
    }

    @Override
    public void onCardLongPressed(SubmissionViewHolder holder) {
        holder.expandOptions();
        if (mFocusedSubmission != null) {
            mFocusedSubmission.collapseOptions();
            if (mFocusedSubmission == holder) {
                mFocusedSubmission = null;
                return;
            }
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
                b.putString("type", "user");
                b.putString("username", submission.getAuthor());
                Intent i = new Intent(getActivity(), BrowseActivity.class);
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
                        int i = mSubmissionList.indexOf(submission);
                        mSubmissionList.remove(i);
                        mSubmissionsAdapter.notifyItemRemoved(i);
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
                                    int i = mSubmissionList.indexOf(submission);
                                    mSubmissionList.remove(i);
                                    mSubmissionsAdapter.notifyItemRemoved(i);
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
                                int i = mSubmissionList.indexOf(submission);
                                mSubmissionsAdapter.notifyItemChanged(i);
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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort: {
                View anchor = getActivity().findViewById(R.id.action_sort);
                PopupMenu popupMenu = (PopupMenu) anchor.getTag();
                popupMenu.show();
                return true;
            }
        }
        return false;
    }

    private PopupMenu.OnMenuItemClickListener mSortClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String tempSortType;
            View anchor = mToolbar.findViewById(R.id.action_sort);
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
                    PopupMenu popupMenu = new PopupMenu(getActivity(), anchor);
                    popupMenu.setOnMenuItemClickListener(new SecondarySortClickListener(tempSortType));
                    popupMenu.inflate(R.menu.secondary_sorts);
                    popupMenu.show();
                    return true;
                }
                case R.id.sort_top: {
                    tempSortType = RedditApi.SORT_TYPE_TOP;
                    PopupMenu popupMenu = new PopupMenu(getActivity(), anchor);
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
    };

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

    public class InfiniteLoadingScrollListener extends RecyclerView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int mPreviousTotal = 0;
        private boolean mLoading = true;

        @Override
        public void onScrolled(RecyclerView absListView, int dx, int dy) {
            if (mLoading) {
                if (mSubmissionsAdapter.getItemCount() > mPreviousTotal) {
                    mPreviousTotal = mSubmissionsAdapter.getItemCount();
                    mLoading = false;
                    mProgressBar.setVisibility(View.GONE);
                }
            } else if (mSubmissionList.size() > 0
                    && (mSubmissionsAdapter.getItemCount() - mRecyclerView.getChildCount())
                    <= (mLayoutManager.findFirstVisibleItemPosition() + VISIBLE_THRESHOLD)) {
                loadMoreSubmissions();
                mLoading = true;
                mProgressBar.setVisibility(View.VISIBLE);
            }

            float prevY = mHeaderBar.getTranslationY();
            mHeaderBar.setTranslationY(Math.min(Math.max(-mHeaderBar.getHeight(), prevY - dy), 0));
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (Math.abs(mHeaderBar.getTranslationY()) < mHeaderBar.getHeight() / 2
                            || mLayoutManager.findFirstVisibleItemPosition() == 0) {
                        // Need to move it back to completely visible.
                        ObjectAnimator objectAnimator =
                                ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), 0.0F);
                        objectAnimator.setDuration((int) -mHeaderBar.getTranslationY());
                        objectAnimator.start();
                    } else {
                        // Hide the header bar.
                        ObjectAnimator objectAnimator =
                                ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), -mHeaderBar.getHeight());
                        objectAnimator.setDuration(mHeaderBar.getHeight() - (long) mHeaderBar.getTranslationY());
                        objectAnimator.start();
                    }
                    break;
            }
        }

        public void resetState() {
            mPreviousTotal = mSubmissionsAdapter.getItemCount();
            mLoading = false;
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadMoreSubmissions() {
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

    public class SubmissionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int SUBMISSION = 1;
        private static final int HEADER = 2;

        private List<Submission> mSubmissions;
        private SubmissionViewHolder.SubmissionCallbacks mCallback;

        public SubmissionAdapter(SubmissionViewHolder.SubmissionCallbacks callbacks,
                                 List<Submission> submissions) {
            mSubmissions = submissions;
            mCallback = callbacks;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == HEADER) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mHeaderBar.getHeight());
                View header = new View(getActivity());
                header.setLayoutParams(params);
                return new RecyclerView.ViewHolder(header) { };
            }
            CardView cardView = (CardView) inflater.inflate(R.layout.view_content_card, parent, false);
            inflater.inflate(R.layout.view_submission, cardView, true);
            return new SubmissionViewHolder(cardView, mCallback);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder submissionViewHolder, int position) {
            if (position > 0) {
                ((SubmissionViewHolder) submissionViewHolder).setContent(mSubmissions.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return mSubmissions.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return HEADER;
            } else {
                return SUBMISSION;
            }
        }
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

    public static interface SubredditFragmentCallbacks extends TopLevelFragmentCallbacks {
        public void onSubredditSelected(String subreddit);
    }
}
