package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;
import me.williamhester.ui.widget.InfiniteLoadToolbarHideScrollListener;

/**
 * Created by william on 1/6/15.
 */
public abstract class AbsSubmissionListFragment extends AccountFragment implements
        Toolbar.OnMenuItemClickListener, InfiniteLoadToolbarHideScrollListener.OnLoadMoreListener,
        SubmissionViewHolder.SubmissionCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;

    protected ArrayList<Submission> mSubmissionList;

    protected InfiniteLoadToolbarHideScrollListener mScrollListener;

    protected SubmissionAdapter mSubmissionsAdapter;

    protected ProgressBar mProgressBar;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    private SubmissionViewHolder mFocusedSubmission;
    protected Toolbar mToolbar;

    /**
     * The primary sort type is set, by default to hot, Reddit's default sort for subreddits.
     */
    protected String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    /**
     * The secondary sort type does not need to be set, as hot has no secondary type.
     */
    protected String mSecondarySortType;

    private View mHeaderBar;
    protected boolean mLoading = true;

    protected abstract void refreshData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mSubmissionList = savedInstanceState.getParcelableArrayList("submissions");
            mLoading = savedInstanceState.getBoolean("loading");
        } else if (getArguments() != null) {
            mSubmissionList = new ArrayList<>();
        }
        mSubmissionsAdapter = new SubmissionAdapter(this, mSubmissionList);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subreddit, container, false);

        mHeaderBar = v.findViewById(R.id.header_bar);
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);

        mToolbar.setOnMenuItemClickListener(this);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orangered);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
        mHeaderBar.post(new Runnable() {
            @Override
            public void run() {
                float density = getResources().getDisplayMetrics().density;
                int startAt = mHeaderBar.getHeight() - (int) (40 * density);
                int endAt = startAt + (int) (64 * density);
                mSwipeRefreshLayout.setProgressViewOffset(false, startAt, endAt);
            }
        });

        if (!mLoading && mSubmissionList.size() > 0) {
            mProgressBar.setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.content_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(
                R.drawable.card_divider)));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mSubmissionsAdapter);

        mScrollListener = new InfiniteLoadToolbarHideScrollListener(mSubmissionsAdapter, mHeaderBar,
                recyclerView, mSubmissionList, layoutManager, this);
        recyclerView.setOnScrollListener(mScrollListener);

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("submissions", mSubmissionList);
        outState.putBoolean("loading", mLoading);
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
            case R.id.option_go_to_subreddit: {
                Intent i = new Intent(getActivity(), BrowseActivity.class);
                // TODO: Change this so that it opens a BrowseActivity that's hosting a
                // SubredditFragment
                break;
            }
            case R.id.option_view_user: {
                Bundle b = new Bundle();
                b.putString("type", "user");
                b.putString("username", submission.getAuthor());
                Intent i = new Intent(getActivity(), BrowseActivity.class);
                i.putExtras(b);
                getActivity().startActivity(i);
                break;
            }
            case R.id.option_share: {
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
            }
            case R.id.option_save: {
                // TODO: actually save it
                break;
            }
            case R.id.option_overflow: {
                inflateOverflowPopupMenu(view, submission);
                break;
            }
        }
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
            refreshData();
        }
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
                    case R.id.overflow_report:
                        break; // TODO: Make a form for this
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
}
