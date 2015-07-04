package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ProgressBar;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import me.williamhester.SettingsManager;
import me.williamhester.knapsack.Knapsack;
import me.williamhester.knapsack.Save;
import me.williamhester.models.reddit.RedditAbsComment;
import me.williamhester.models.reddit.RedditComment;
import me.williamhester.models.reddit.RedditMoreComments;
import me.williamhester.models.reddit.RedditSubmission;
import me.williamhester.models.reddit.RedditThing;
import me.williamhester.models.reddit.RedditVotable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.activities.OverlayContentActivity;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;

import static android.view.View.*;

public class CommentFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener,
        SubmissionViewHolder.OnVotedListener{

    private static final String PERMALINK = "permalink";
    private static final int REPLY_REQUEST = 1;
    private static final int EDIT_REQUEST = 2;

    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;

    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout mRefreshLayout;
    @Bind(R.id.comments) RecyclerView mRecyclerView;

    @Save ArrayList<RedditAbsComment> mCommentsList = new ArrayList<>();
    @Save
    RedditSubmission mRedditSubmission;
    @Save String mPermalink;
    @Save String mSortType;
    @Save boolean mLoading = true;
    @Save boolean mRefreshing = false;
    @Save boolean mIsSingleThread = false;

    public static CommentFragment newInstance(String permalink, boolean isSingleThread) {
        Bundle args = new Bundle();
        args.putString(PERMALINK, permalink);
        args.putBoolean("isSingleThread", isSingleThread);
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommentFragment newInstance(RedditSubmission redditSubmission) {
        Bundle args = new Bundle();
        args.putParcelable("redditSubmission", redditSubmission);
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContext = getActivity();
        if (savedInstanceState == null && args != null) {
            mSortType = SettingsManager.getDefaultCommentSort();
            mRedditSubmission = args.getParcelable("submission");
            if (mRedditSubmission != null) {
                mPermalink = mRedditSubmission.getPermalink();
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            } else {
                mIsSingleThread = args.getBoolean("isSingleThread", false);
                mPermalink = args.getString(PERMALINK);
                if (mPermalink.contains("reddit.com")) {
                    mPermalink = mPermalink.substring(mPermalink.indexOf("reddit.com") + 10);
                }
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
        }
        mCommentAdapter = new CommentArrayAdapter();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment, root, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mToolbar.setTitle(R.string.comments);
        mToolbar.setOnMenuItemClickListener(this);

        mRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        mRefreshLayout.setColorSchemeResources(R.color.white);
        mRefreshLayout.setRefreshing(mRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshing = true;
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
        });
        mRecyclerView.setAdapter(mCommentAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.comments_divider)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mProgressBar.setVisibility(mLoading ? VISIBLE : GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submission_fragment, menu);

        if (mRedditSubmission != null) {
            Url url = new Url(mRedditSubmission.getUrl());
            if (mRedditSubmission.isSelf()) {
                menu.removeItem(R.id.action_view_link);
            } else if (url.getType() == Url.IMGUR_ALBUM
                    || url.getType() == Url.IMGUR_IMAGE
                    || url.getType() == Url.NORMAL_IMAGE
                    || url.getType() == Url.GIF
                    || url.getType() == Url.GFYCAT_LINK) {
                menu.findItem(R.id.action_view_link).setIcon(android.R.drawable.ic_menu_gallery);
            } else if (url.getType() == Url.YOUTUBE) {
                menu.findItem(R.id.action_view_link).setIcon(R.drawable.ic_youtube);
            }
        }
        menu.removeItem(R.id.action_open_link_in_browser);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_comments: {
                View anchor = getActivity().findViewById(R.id.action_sort_comments);
                PopupMenu popupMenu = new PopupMenu(mContext, anchor);
                anchor.setOnTouchListener(popupMenu.getDragToOpenListener());
                popupMenu.setOnMenuItemClickListener(mSortClickListener);
                popupMenu.inflate(R.menu.comment_sorts);
                popupMenu.show();
                return true;
            }
            case R.id.action_view_link:
                Fragment f = getFragmentManager().findFragmentByTag("contentPreview");
                if (f != null) {
                    getActivity().onBackPressed();
                } else {
                    Intent i = new Intent(getActivity(), OverlayContentActivity.class);
                    Bundle args = new Bundle();
                    args.putInt("type", OverlayContentActivity.TYPE_SUBMISSION);
                    args.putParcelable("submission", mRedditSubmission);
                    i.putExtras(args);
                    startActivity(i);
                }
                break;
            case R.id.action_open_link_in_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mRedditSubmission.getUrl()));
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REPLY_REQUEST) {
            Bundle args = data.getExtras();
            RedditThing parent = args.getParcelable("parentThing");
            RedditComment newRedditComment = args.getParcelable("newRedditComment");
            int index = 0;
            if (parent instanceof RedditComment) {
                index = mCommentsList.indexOf(parent) + 1;
            }
            mCommentsList.add(index, newRedditComment);
            if (mCommentAdapter != null) {
                mCommentAdapter.notifyItemInserted(index + mCommentAdapter.getHeaderViewCount());
            }
            return;
        } else if (requestCode == EDIT_REQUEST) {
            Bundle args = data.getExtras();
            RedditThing parent = args.getParcelable("parentThing");
            RedditVotable newThing = args.getParcelable("newComment");
            RedditVotable oldThing = args.getParcelable("oldThing");
            if (parent == null) {
                mSubmissionCallback.onCompleted(null, (RedditSubmission) newThing);
            } else {
                int index = mCommentsList.indexOf(oldThing);
                mCommentsList.set(index, (RedditComment) newThing);
                if (mCommentAdapter != null) {
                    mCommentAdapter.notifyItemChanged(index
                            + mCommentAdapter.getHeaderViewCount());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }

    /**
     * Hides the comment at the specified position.
     *
     * @param position the position of the clicked comment
     * @return the number of comments that were hidden
     */
    private int hideComment(int position) {
        RedditAbsComment comment = mCommentsList.get(position);
        int level = comment.getLevel();
        position++;
        ArrayList<RedditAbsComment> children = new ArrayList<>();
        while (position < mCommentsList.size() && mCommentsList.get(position).getLevel() > level) {
            children.add(mCommentsList.remove(position));
        }
        ((RedditComment) comment).hide(children);
        return children.size();
    }

    /**
     * Shows the comment at the specified position.
     *
     * @param position the position of the clicked comment
     * @return the number of comments that were shown
     */
    private int showComment(int position) {
        RedditAbsComment comment = mCommentsList.get(position);
        ArrayList<RedditAbsComment> children = ((RedditComment) comment).unhideComment();
        for (RedditAbsComment c : children) {
            mCommentsList.add(++position, c);
        }
        return children.size();
    }

    @Override
    public void onVoted(RedditSubmission redditSubmission) {
        Bundle data = new Bundle();
        Intent i = new Intent();
        data.putString("name", redditSubmission.getId());
        data.putInt("status", redditSubmission.getVoteValue());
        i.putExtras(data);
        getActivity().setResult(Activity.RESULT_OK, i);
    }

    private PopupMenu.OnMenuItemClickListener mSortClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            String tempSort;
            switch (item.getItemId()) {
                case R.id.comment_sort_best:
                    tempSort = RedditApi.COMMENT_SORT_BEST;
                    break;
                case R.id.comment_sort_top:
                    tempSort = RedditApi.COMMENT_SORT_TOP;
                    break;
                case R.id.comment_sort_new:
                    tempSort = RedditApi.COMMENT_SORT_NEW;
                    break;
                case R.id.comment_sort_hot:
                    tempSort = RedditApi.COMMENT_SORT_HOT;
                    break;
                case R.id.comment_sort_controversial:
                    tempSort = RedditApi.COMMENT_SORT_CONTROVERSIAL;
                    break;
                case R.id.comment_sort_old:
                    tempSort = RedditApi.COMMENT_SORT_OLD;
                    break;
                default:
                    tempSort = mSortType;
            }
            if (!tempSort.equals(mSortType)) {
                mSortType = tempSort;
                mProgressBar.setVisibility(VISIBLE);
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
            return true;
        }
    };

    private CommentViewHolder.CommentCallbacks mCommentCallbacks = new CommentViewHolder.CommentCallbacks() {

        private CommentViewHolder mFocusedViewHolder;

        @Override
        public void onBodyClick(CommentViewHolder viewHolder, RedditComment redditComment) {
            redditComment.setHidden(!redditComment.isHidden());
            final int position = mCommentsList.indexOf(redditComment);
            mCommentAdapter.notifyItemChanged(position + mCommentAdapter.getHeaderViewCount());
            if (redditComment.isHidden()) {
                mCommentAdapter.notifyItemRangeRemoved(position + 1
                        + mCommentAdapter.getHeaderViewCount(), hideComment(position));
            } else {
                mCommentAdapter.notifyItemRangeInserted(position + 1
                        + mCommentAdapter.getHeaderViewCount(), showComment(position));
            }
        }

        @Override
        public void onCommentLongPressed(CommentViewHolder holder) {
            if (mFocusedViewHolder != null) {
                mFocusedViewHolder.collapseOptions();
            }
            mFocusedViewHolder = holder;
        }

        @Override
        public void onOptionsRowItemSelected(View view, RedditComment redditComment) {
            switch (view.getId()) {
                case R.id.option_view_user:
                    Bundle b = new Bundle();
                    b.putString("type", "user");
                    b.putString("username", redditComment.getAuthor());
                    Intent i = new Intent(getActivity(), BrowseActivity.class);
                    i.putExtras(b);
                    getActivity().startActivity(i);
                    break;
                case R.id.option_reply:
                    Fragment reply = ReplyFragment.newInstance(redditComment);
                    reply.setTargetFragment(CommentFragment.this, REPLY_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_container, reply, "ReplyFragment")
                            .addToBackStack("ReplyFragment")
                            .commit();
                    break;
                case R.id.option_edit: {
                    RedditThing parent;
                    if (redditComment.getLevel() == 0) {
                        parent = mRedditSubmission;
                    } else {
                        int commentIndex = mCommentsList.indexOf(redditComment) - 1;
                        while (mCommentsList.get(commentIndex).getLevel() >= redditComment.getLevel()) {
                            commentIndex--;
                        }
                        parent = mCommentsList.get(commentIndex);
                    }
                    ReplyFragment fragment = ReplyFragment.newInstance(parent, redditComment);
                    fragment.setTargetFragment(CommentFragment.this, EDIT_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_container, fragment, "Edit")
                            .addToBackStack("Edit")
                            .commit();
                    break;
                }
                case R.id.option_share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String link = RedditApi.PUBLIC_REDDIT_URL + mRedditSubmission.getPermalink()
                            + redditComment.getId();
                    sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent,
                            getResources().getText(R.string.share_with)));
                    break;
            }
        }

        @Override
        public String getSubmissionAuthor() {
            return mRedditSubmission.getAuthor();
        }
    };

    private class CommentArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int SUBMISSION = 1;
        private static final int COMMENT = 2;
        private static final int MORE_COMMENTS = 3;
        private static final int SINGLE_THREAD_HEADER = 4;
        private static final int FOOTER = 5;

        @SuppressLint("InflateParams")
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case SUBMISSION:
                    SubmissionViewHolder holder = new SubmissionViewHolder(
                            inflater.inflate(R.layout.header_comments,
                            parent, false), mSubmissionCallbacks);
                    holder.setOnVotedListener(CommentFragment.this);
                    return holder;
                case COMMENT:
                    return new CommentViewHolder(inflater.inflate(R.layout.list_item_comment,
                            parent, false), mCommentCallbacks);
                case MORE_COMMENTS:
                    return new MoreCommentsViewHolder(
                            inflater.inflate(R.layout.list_item_more_comments, parent, false));
                case SINGLE_THREAD_HEADER:
                    return new SingleThreadHeader(inflater, parent);
                case FOOTER:
                    return new RecyclerView.ViewHolder(inflater.inflate(R.layout.footer_spacer,
                            parent, false)) {};
            }
            return null; // Should never hit this case
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
            switch (getItemViewType(position)) {
                case SUBMISSION:
                    ((SubmissionViewHolder) vh).setContent(mRedditSubmission);
                    ((SubmissionViewHolder) vh).expandOptionsForComments();
                    ((SubmissionViewHolder) vh).disableClicks();
                    break;
                case COMMENT:
                    ((CommentViewHolder) vh).setContent(
                            mCommentsList.get(position - getHeaderViewCount()));
                    break;
                case MORE_COMMENTS:
                    ((MoreCommentsViewHolder) vh).setContent(
                            (RedditMoreComments) mCommentsList.get(position - getHeaderViewCount()));
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return FOOTER;
            } else if (position == 0) {
                if (mRedditSubmission != null) {
                    return SUBMISSION;
                } else if (mIsSingleThread) {
                    return SINGLE_THREAD_HEADER;
                }
            } else if (position == 1 && mIsSingleThread) {
                return SINGLE_THREAD_HEADER;
            } else if (mCommentsList.get(position - getHeaderViewCount()) instanceof RedditComment) {
                return COMMENT;
            } else {
                return MORE_COMMENTS;
            }
            return -1;
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(mCommentsList.get(position - getHeaderViewCount()).getId(), 36);
        }

        @Override
        public int getItemCount() {
            return mCommentsList.size() + getHeaderViewCount() + 1;
        }

        public int getHeaderViewCount() {
            return (mRedditSubmission != null ? 1 : 0) + (mIsSingleThread ? 1 : 0);
        }
    }

    private class SingleThreadHeader extends RecyclerView.ViewHolder {

        public SingleThreadHeader(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.header_single_thread, parent, false));

            itemView.findViewById(R.id.view_all_comments).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPermalink = mPermalink.substring(0, mPermalink.lastIndexOf('/'));
                            mRefreshing = true;
                            mIsSingleThread = false;
                            RedditApi.getSubmissionData(mContext, mPermalink, mSortType,
                                    mSubmissionCallback, mCommentCallback);
                            mProgressBar.setVisibility(VISIBLE);
                        }
                    });
            itemView.findViewById(R.id.view_full_context).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPermalink = mPermalink.replace("context=3", "context=10000");
                            mRefreshing = true;
                            RedditApi.getSubmissionData(mContext, mPermalink, mSortType,
                                    mSubmissionCallback, mCommentCallback);
                            mProgressBar.setVisibility(VISIBLE);
                        }
                    });
        }

    }

    private SubmissionViewHolder.SubmissionCallbacks mSubmissionCallbacks = new SubmissionViewHolder.SubmissionCallbacks() {

        @Override
        public FragmentManager getFragmentManager() {
            return CommentFragment.this.getFragmentManager();
        }

        @Override
        public Activity getActivity() {
            return CommentFragment.this.getActivity();
        }

        @Override
        public void onCardClicked(RedditSubmission redditSubmission) {
            // Do nothing
        }

        @Override
        public void onCardLongPressed(SubmissionViewHolder holder) {
            // Do nothing
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
                    Bundle data = new Bundle();
                    Intent i = new Intent();
                    data.putString("name", redditSubmission.getId());
                    i.putExtras(data);
                    getActivity().setResult(AbsSubmissionListFragment.REMOVE_RESULT_CODE, i);
                    getActivity().finish();
                }
            };
            switch (itemId) {
                case R.id.option_reply: {
                    Fragment reply = ReplyFragment.newInstance(redditSubmission);
                    reply.setTargetFragment(CommentFragment.this, REPLY_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_container, reply, "ReplyFragment")
                            .addToBackStack("ReplyFragment")
                            .commit();
                    break;
                }
                case R.id.overflow_hide: {
                    FutureCallback<String> callback = new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            if (!redditSubmission.isHidden()) {
                                Bundle data = new Bundle();
                                Intent i = new Intent();
                                data.putString("name", redditSubmission.getId());
                                i.putExtras(data);
                                getActivity().setResult(AbsSubmissionListFragment.REMOVE_RESULT_CODE, i);
                                getActivity().finish();
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

        @Override
        public boolean isFrontPage() {
            return false;
        }
    };

    private FutureCallback<RedditSubmission> mSubmissionCallback = new FutureCallback<RedditSubmission>() {
        @Override
        public void onCompleted(Exception e, RedditSubmission result) {
            if (e != null) {
                return;
            }
            if (mRedditSubmission != null) {
                mRedditSubmission.update(result);
            } else {
                mRedditSubmission = result;
            }
            getActivity().invalidateOptionsMenu();
            mCommentAdapter.notifyDataSetChanged();
        }
    };

    private FutureCallback<List<RedditAbsComment>> mCommentCallback = new FutureCallback<List<RedditAbsComment>>() {
        @Override
        public void onCompleted(Exception e, List<RedditAbsComment> result) {
            if (e != null) {
                return;
            }
            mCommentsList.clear();
            mCommentsList.addAll(result);
            mCommentAdapter.notifyItemRangeInserted(1, mCommentsList.size());
            mRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(GONE);
            mRefreshing = mLoading = false;
        }
    };

    private class MoreCommentsViewHolder extends RecyclerView.ViewHolder {

        private RedditMoreComments mComment;
        private View mProgressBar;
        private View mLevelIndicator;

        public MoreCommentsViewHolder(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.progress_bar);
            mLevelIndicator = itemView.findViewById(R.id.level_indicator);
            itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mComment.isLoading()) {
                        mProgressBar.setVisibility(VISIBLE);
                        mComment.setIsLoading(true);
                        RedditApi.getMoreChildren(mRedditSubmission.getId(),
                                mSortType, mComment.getChildren(), mComment.getLevel(),
                                new FutureCallback<ArrayList<RedditThing>>() {
                                    @Override
                                    public void onCompleted(Exception e,
                                                            final ArrayList<RedditThing> comments) {
                                        mComment.setIsLoading(false);
                                        if (e != null) {
                                            e.printStackTrace();
                                            return;
                                        }
                                        int insert = mCommentsList.indexOf(mComment);
                                        final int pos = insert + 1;
                                        mCommentsList.remove(insert);
                                        for (RedditThing redditThing : comments) {
                                            if (redditThing instanceof RedditAbsComment) {
                                                mCommentsList.add(insert++, (RedditAbsComment) redditThing);
                                            }
                                        }
                                        if (getView() != null) {
                                            getView().post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mCommentAdapter.notifyItemChanged(pos);
                                                    mCommentAdapter.notifyItemRangeInserted(pos + 1,
                                                            comments.size() - 1);
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                }
            });
        }

        public void setContent(RedditMoreComments comment) {
            mComment = comment;

            mProgressBar.setVisibility(mComment.isLoading() ? VISIBLE : GONE);
            float dp = itemView.getResources().getDisplayMetrics().density;
            itemView.setPadding(Math.round(4 * dp * mComment.getLevel()), 0, 0, 0);
            if (mComment.getLevel() > 0) {
                mLevelIndicator.setVisibility(VISIBLE);
                switch (mComment.getLevel() % 4) {
                    case 1:
                        mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.green));
                        break;
                    case 2:
                        mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.cyan));
                        break;
                    case 3:
                        mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.blue));
                        break;
                    case 0:
                        mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.pink));
                        break;
                }
            } else {
                mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.primary_dark));
            }
        }
    }
}
