package me.williamhester.ui.fragments;

import android.app.Activity;
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

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;
import me.williamhester.ui.widget.InfiniteLoadToolbarHideScrollListener;

/**
 * This Fragment contains a RecyclerView and a Toolbar and fetches Submissions by its subclasses.
 */
public abstract class AbsSubmissionListFragment extends AccountFragment implements
        Toolbar.OnMenuItemClickListener,
        InfiniteLoadToolbarHideScrollListener.OnLoadMoreListener,
        SubmissionViewHolder.SubmissionCallbacks {

    public static final int VOTE_REQUEST_CODE = 1;
    public static final int REMOVE_RESULT_CODE = 2;


    protected InfiniteLoadToolbarHideScrollListener mScrollListener;

    protected SubmissionAdapter mSubmissionsAdapter;

    /**
     * The primary sort type is set, by default to hot, Reddit's default sort for subreddits.
     */
    protected String mPrimarySortType = RedditApi.SORT_TYPE_HOT;
    /**
     * The secondary sort type does not need to be set, as hot has no secondary type.
     */
    protected String mSecondarySortType;

    @Bind(R.id.header_bar) View mHeaderBar;
    @Bind(R.id.content_list) RecyclerView mRecyclerView;
    @Bind(R.id.progress_bar) protected ProgressBar mProgressBar;
    @Bind(R.id.swipe_refresh) protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Save protected boolean mLoading = true;
    @Save protected ArrayList<RedditSubmission> mRedditSubmissionList;

    private SubmissionViewHolder mFocusedSubmission;

    protected abstract void onRefreshList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null && getArguments() != null) {
            mRedditSubmissionList = new ArrayList<>();
        }
        mSubmissionsAdapter = new SubmissionAdapter(this, mRedditSubmissionList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subreddit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar.setOnMenuItemClickListener(this);

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshList();
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

        if (!mLoading && mRedditSubmissionList.size() > 0) {
            mProgressBar.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(
                R.drawable.card_divider)));
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mSubmissionsAdapter);

        mScrollListener = new InfiniteLoadToolbarHideScrollListener(mSubmissionsAdapter, mHeaderBar,
                mRecyclerView, mRedditSubmissionList, layoutManager, this);
        mRecyclerView.addOnScrollListener(mScrollListener);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != VOTE_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (resultCode) {
            case Activity.RESULT_OK: {
                if (data == null)
                    return;
                Bundle b = data.getExtras();
                String name = b.getString("name");
                int status = b.getInt("status");
                if (name != null) {
                    for (int i = 0; i < mRedditSubmissionList.size(); i++) {
                        if (mRedditSubmissionList.get(i).getId().equals(name)) {
                            mRedditSubmissionList.get(i).setVoteValue(status);
                            mSubmissionsAdapter.notifyItemChanged(i + 1);
                            return;
                        }
                    }
                }
                return;
            }
            case REMOVE_RESULT_CODE: {
                if (data == null) {
                    return;
                }
                Bundle b = data.getExtras();
                String name = b.getString("name");
                if (name != null) {
                    int position = -1;
                    for (int i = 0; i < mRedditSubmissionList.size(); i++) {
                        if (mRedditSubmissionList.get(i).getId().equals(name)) {
                            position = i;
                            break;
                        }
                    }
                    if (position > -1) {
                        mRedditSubmissionList.remove(position);
                        mSubmissionsAdapter.notifyItemRemoved(position + 1);
                    }
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAccountChanged() {

    }

    @Override
    public void onCardClicked(RedditSubmission redditSubmission) {
        Intent i = new Intent(getActivity(), BrowseActivity.class);
        Bundle args = new Bundle();
        args.putString("type", "comments");
        args.putParcelable("redditSubmission", redditSubmission);
        args.putParcelable("media", redditSubmission.getMedia());
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
    public boolean onOptionsRowItemSelected(int itemId, final RedditSubmission redditSubmission) {
        FutureCallback<String> removeCallback = new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                int i = mRedditSubmissionList.indexOf(redditSubmission);
                mRedditSubmissionList.remove(i);
                mSubmissionsAdapter.notifyItemRemoved(i);
            }
        };
        switch (itemId) {
            case R.id.overflow_hide: {
                FutureCallback<String> callback = new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        if (!redditSubmission.isHidden()) {
                            int i = mRedditSubmissionList.indexOf(redditSubmission);
                            mRedditSubmissionList.remove(i);
                            mSubmissionsAdapter.notifyItemRemoved(i);
                        }
                    }
                };
                if (redditSubmission.isHidden()) {
                    RedditApi.unhide(getActivity(), redditSubmission, callback);
                } else {
                    RedditApi.hide(getActivity(), redditSubmission, callback);
                }
                break;
            }
            case R.id.overflow_delete: {
                RedditApi.delete(getActivity(), redditSubmission, removeCallback);
                break;
            }
            case R.id.overflow_remove: {
                RedditApi.remove(getActivity(), redditSubmission, false, removeCallback);
                break;
            }
            case R.id.overflow_spam: {
                RedditApi.remove(getActivity(), redditSubmission, true, removeCallback);
                break;
            }
        }
        return false;
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
            onRefreshList();
        }
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
        private static final int FOOTER = 3;

        private List<RedditSubmission> mRedditSubmissions;
        private SubmissionViewHolder.SubmissionCallbacks mCallback;

        public SubmissionAdapter(SubmissionViewHolder.SubmissionCallbacks callbacks,
                                 List<RedditSubmission> redditSubmissions) {
            mRedditSubmissions = redditSubmissions;
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
            } else if (viewType == FOOTER) {
                return new RecyclerView.ViewHolder(inflater.inflate(R.layout.footer_spacer, parent,
                        false)) {};
            }
            CardView cardView = (CardView) inflater.inflate(R.layout.view_content_card, parent, false);
            inflater.inflate(R.layout.view_submission, cardView, true);
            return new SubmissionViewHolder(cardView, mCallback);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder submissionViewHolder, int position) {
            if (getItemViewType(position) == SUBMISSION) {
                ((SubmissionViewHolder) submissionViewHolder).setContent(mRedditSubmissions.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return mRedditSubmissions.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return FOOTER;
            } else if (position == 0) {
                return HEADER;
            } else {
                return SUBMISSION;
            }
        }
    }
}
