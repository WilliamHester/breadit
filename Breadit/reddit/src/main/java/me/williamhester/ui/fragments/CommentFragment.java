package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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
import java.util.Map;

import me.williamhester.SettingsManager;
import me.williamhester.models.AbsComment;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.models.MoreComments;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.models.Thing;
import me.williamhester.models.Votable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.activities.MainActivity;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;

public class CommentFragment extends AccountFragment {

    private static final String PERMALINK = "permalink";
    private static final int REPLY_REQUEST = 1;
    private static final int EDIT_REQUEST = 2;

    private final ArrayList<AbsComment> mCommentsList = new ArrayList<>();
    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;
    private ProgressBar mProgressBar;
    private String mPermalink;
    private String mSortType;
    private SwipeRefreshLayout mRefreshLayout;
    private Submission mSubmission;
    private OnSubmissionLoaded mCallback;

    private boolean mLoading = true;
    private boolean mRefreshing = false;

    public static CommentFragment newInstance(String permalink) {
        Bundle args = new Bundle();
        args.putString(PERMALINK, permalink);
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommentFragment newInstance(Submission submission) {
        Bundle args = new Bundle();
        args.putParcelable("submission", submission);
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContext = getActivity();
        if (savedInstanceState != null) {
            ArrayList<AbsComment> comments = savedInstanceState.getParcelableArrayList("comments");
            mCommentsList.addAll(comments);
            mSubmission = savedInstanceState.getParcelable("submission");
            mPermalink = savedInstanceState.getString("permalink");
            mSortType = savedInstanceState.getString("sortType");
            mLoading = savedInstanceState.getBoolean("loading");
            mRefreshing = savedInstanceState.getBoolean("refreshing");
        } else if (args != null) {
            mSortType = SettingsManager.getDefaultCommentSort();
            mSubmission = args.getParcelable("submission");
            if (mSubmission != null) {
                mPermalink = mSubmission.getPermalink();
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            } else {
                mPermalink = args.getString(PERMALINK);
                if (mPermalink.contains("reddit.com")) {
                    mPermalink = mPermalink.substring(mPermalink.indexOf("reddit.com") + 10);
                }
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment, root, false);
        RecyclerView commentsView = (RecyclerView) v.findViewById(R.id.comments);
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mRefreshLayout.setColorSchemeResources(R.color.orangered);
        mRefreshLayout.setRefreshing(mRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshing = true;
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
        });
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mCommentAdapter = new CommentArrayAdapter();
        commentsView.setAdapter(mCommentAdapter);
        commentsView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.comments_divider)));
        commentsView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!mLoading) {
            mProgressBar.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submission_fragment, menu);

        if (mSubmission != null) {
            Url url = new Url(mSubmission.getUrl());
            if (mSubmission.isSelf()) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
                Fragment f = getFragmentManager().findFragmentByTag("content");
                if (f != null) {
                    getFragmentManager().popBackStack();
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.container, ((SubmissionActivity) getActivity()).getContentFragment(), "content")
                            .addToBackStack("content")
                            .commit();
                }
                break;
            case R.id.action_open_link_in_browser:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSubmission.getUrl()));
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
            Thing parent = args.getParcelable("parentThing");
            Comment newComment = args.getParcelable("newComment");
            int index = 0;
            if (parent instanceof Comment) {
                index = mCommentsList.indexOf(parent) + 1;
            }
            mCommentsList.add(index, newComment);
            if (mCommentAdapter != null) {
                mCommentAdapter.notifyItemInserted(index + CommentArrayAdapter.HEADER_VIEW_COUNT);
            }
            return;
        } else if (requestCode == EDIT_REQUEST) {
            Bundle args = data.getExtras();
            Thing parent = args.getParcelable("parentThing");
            Votable newThing = args.getParcelable("newComment");
            Votable oldThing = args.getParcelable("oldThing");
            if (parent == null) {
                mSubmissionCallback.onCompleted(null, (Submission) newThing);
            } else {
                int index = mCommentsList.indexOf(oldThing);
                mCommentsList.set(index, (Comment) newThing);
                if (mCommentAdapter != null) {
                    mCommentAdapter.notifyItemChanged(index
                            + CommentArrayAdapter.HEADER_VIEW_COUNT);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("comments", mCommentsList);
        outState.putParcelable("submission", mSubmission);
        outState.putString("permalink", mPermalink);
        outState.putString("sortType", mSortType);
        outState.putBoolean("loading", mLoading);
        outState.putBoolean("refreshing", mRefreshing);
    }

    /**
     * Sets a listener that calls back when the Submission is loaded.
     *
     * @param listener the listener to call back to
     */
    public void setOnSubmissionLoadedListener(OnSubmissionLoaded listener) {
        mCallback = listener;
    }

    /**
     * Hides the comment at the specified position.
     *
     * @param position the position of the clicked comment
     * @return the number of comments that were hidden
     */
    private int hideComment(int position) {
        AbsComment comment = mCommentsList.get(position);
        int level = comment.getLevel();
        position++;
        ArrayList<AbsComment> children = new ArrayList<>();
        while (position < mCommentsList.size() && mCommentsList.get(position).getLevel() > level) {
            children.add(mCommentsList.remove(position));
        }
        ((Comment) comment).hide(children);
        return children.size();
    }

    /**
     * Shows the comment at the specified position.
     *
     * @param position the position of the clicked comment
     * @return the number of comments that were shown
     */
    private int showComment(int position) {
        AbsComment comment = mCommentsList.get(position);
        ArrayList<AbsComment> children = ((Comment) comment).unhideComment();
        for (AbsComment c : children) {
            mCommentsList.add(++position, c);
        }
        return children.size();
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
                mProgressBar.setVisibility(View.VISIBLE);
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
            return true;
        }
    };

    private CommentViewHolder.CommentClickCallbacks mCommentCallbacks = new CommentViewHolder.CommentClickCallbacks() {

        private CommentViewHolder mFocusedViewHolder;

        @Override
        public void onBodyClick(CommentViewHolder viewHolder, Comment comment) {
            comment.setHidden(!comment.isHidden());
            final int position = mCommentsList.indexOf(comment);
            mCommentAdapter.notifyItemChanged(position + 1);
            if (comment.isHidden()) {
                mCommentAdapter.notifyItemRangeRemoved(position + 2,
                        hideComment(position));
            } else {
                mCommentAdapter.notifyItemRangeInserted(position + 2, showComment(position));
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
        public void onOptionsRowItemSelected(View view, Comment comment) {
            switch (view.getId()) {
                case R.id.option_view_user:
                    Bundle b = new Bundle();
                    b.putString("username", comment.getAuthor());
                    Intent i = new Intent(getActivity(), UserActivity.class);
                    i.putExtras(b);
                    getActivity().startActivity(i);
                    break;
                case R.id.option_reply:
                    Fragment reply = ReplyFragment.newInstance(comment);
                    reply.setTargetFragment(CommentFragment.this, REPLY_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, reply, "ReplyFragment")
                            .addToBackStack("ReplyFragment")
                            .commit();
                    break;
                case R.id.option_edit: {
                    Thing parent;
                    if (comment.getLevel() == 0) {
                        parent = mSubmission;
                    } else {
                        int commentIndex = mCommentsList.indexOf(comment) - 1;
                        while (mCommentsList.get(commentIndex).getLevel() >= comment.getLevel()) {
                            commentIndex--;
                        }
                        parent = mCommentsList.get(commentIndex);
                    }
                    ReplyFragment fragment = ReplyFragment.newInstance(parent, comment);
                    fragment.setTargetFragment(CommentFragment.this, EDIT_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment, "Edit")
                            .addToBackStack("Edit")
                            .commit();
                    break;
                }
                case R.id.option_share:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String link = RedditApi.PUBLIC_REDDIT_URL + mSubmission.getPermalink()
                            + comment.getName();
                    sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent,
                            getResources().getText(R.string.share_with)));
                    break;
            }
        }
    };

    private class CommentArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int SUBMISSION = 1;
        private static final int COMMENT = 2;
        private static final int MORE_COMMENTS = 3;

        private static final int HEADER_VIEW_COUNT = 1;

        @SuppressLint("InflateParams")
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case SUBMISSION:
                    return new SubmissionViewHolder(inflater.inflate(R.layout.header_comments,
                            parent, false), mSubmissionCallbacks);
                case COMMENT:
                    return new CommentViewHolder(inflater.inflate(R.layout.list_item_comment,
                            parent, false), mCommentCallbacks);
                case MORE_COMMENTS:
                    return new MoreCommentsViewHolder(
                            inflater.inflate(R.layout.list_item_more_comments, parent, false));
            }
            return null; // Should never hit this case
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
            switch (getItemViewType(position)) {
                case SUBMISSION:
                    ((SubmissionViewHolder) vh).setContent(mSubmission);
                    ((SubmissionViewHolder) vh).expandOptions();
                    ((SubmissionViewHolder) vh).disableClicks();
                    break;
                case COMMENT:
                    ((CommentViewHolder) vh).setContent(
                            mCommentsList.get(position - HEADER_VIEW_COUNT));
                    break;
                case MORE_COMMENTS:
                    ((MoreCommentsViewHolder) vh).setContent(
                            (MoreComments) mCommentsList.get(position - HEADER_VIEW_COUNT));
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return SUBMISSION;
            } else if (mCommentsList.get(position - HEADER_VIEW_COUNT) instanceof Comment) {
                return COMMENT;
            } else {
                return MORE_COMMENTS;
            }
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(mCommentsList.get(position - HEADER_VIEW_COUNT).getId(), 36);
        }

        @Override
        public int getItemCount() {
            return mCommentsList.size() + (mSubmission != null ? 1 : 0);
        }
    }

    private SubmissionViewHolder.SubmissionCallbacks mSubmissionCallbacks = new SubmissionViewHolder.SubmissionCallbacks() {
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
        public void onLinkClicked(Submission submission) {

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
            // Do nothing
        }

        @Override
        public void onCardLongPressed(SubmissionViewHolder holder) {
            // Do nothing
        }

        @Override
        public void onOptionsRowItemSelected(View view, Submission submission) {
            switch (view.getId()) {
                case R.id.option_go_to_subreddit: {
                    Intent i = new Intent(view.getContext(), MainActivity.class);
                    i.setAction(Intent.ACTION_VIEW);
                    Bundle args = new Bundle();
                    args.putString(MainActivity.SUBREDDIT, submission.getSubredditName());
                    i.putExtras(args);
                    startActivity(i);
                    break;
                }
                case R.id.option_reply: {
                    ReplyFragment fragment = ReplyFragment.newInstance(submission);
                    fragment.setTargetFragment(CommentFragment.this, REPLY_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment, "Reply")
                            .addToBackStack("Reply")
                            .commit();
                    break;
                }
                case R.id.option_edit: {
                    ReplyFragment fragment = ReplyFragment.newInstance(null, submission);
                    fragment.setTargetFragment(CommentFragment.this, EDIT_REQUEST);
                    getFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment, "Reply")
                            .addToBackStack("Reply")
                            .commit();
                    break;
                }
                case R.id.option_view_user:
                    Bundle b = new Bundle();
                    b.putString("username", submission.getAuthor());
                    Intent i = new Intent(getActivity(), UserActivity.class);
                    i.putExtras(b);
                    getActivity().startActivity(i);
                    break;
                case R.id.option_save:
                    // TODO: actually save it
                    break;
                case R.id.option_share:
                    PopupMenu menu = new PopupMenu(mContext, view);
                    menu.inflate(R.menu.share);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            if (item.getItemId() == R.id.share_link) {
                                sendIntent.putExtra(Intent.EXTRA_TEXT, mSubmission.getUrl());
                            } else {
                                String link = "http://www.reddit.com" + mSubmission.getPermalink();
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
                case R.id.option_overflow:
                    inflateOverflowPopupMenu(view, submission);
                    break;
            }
        }

        @Override
        public void onVoted(Submission submission) {
            Bundle data = new Bundle();
            Intent i = new Intent();
            data.putString("name", submission.getName());
            data.putInt("status", submission.getVoteStatus());
            i.putExtras(data);
            getActivity().setResult(Activity.RESULT_OK, i);
        }

        @Override
        public boolean isFrontPage() {
            return false;
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
                            }
                        }
                    };
                    switch (item.getItemId()) {
                        case R.id.overflow_hide: {
                            FutureCallback<String> callback = new FutureCallback<String>() {
                                @Override
                                public void onCompleted(Exception e, String result) {
                                    if (e != null) {
                                        e.printStackTrace();
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
                                    mCommentAdapter.notifyItemChanged(0);
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
    };

    private FutureCallback<Submission> mSubmissionCallback = new FutureCallback<Submission>() {
        @Override
        public void onCompleted(Exception e, Submission result) {
            if (e != null) {
                return;
            }
            if (mSubmission != null) {
                mSubmission.update(result);
            } else {
                mSubmission = result;
            }
            getActivity().invalidateOptionsMenu();
            mCommentAdapter.notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.onSubmissionLoaded(mSubmission);
            }
        }
    };

    private FutureCallback<List<AbsComment>> mCommentCallback = new FutureCallback<List<AbsComment>>() {
        @Override
        public void onCompleted(Exception e, List<AbsComment> result) {
            if (e != null) {
                return;
            }
            mCommentsList.clear();
            mCommentsList.addAll(result);
            mCommentAdapter.notifyItemRangeInserted(1, mCommentsList.size());
            mRefreshLayout.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
            mRefreshing = mLoading = false;
        }
    };

    private class MoreCommentsViewHolder extends RecyclerView.ViewHolder {

        private MoreComments mComment;
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
                        mProgressBar.setVisibility(View.VISIBLE);
                        mComment.setIsLoading(true);
                        RedditApi.getMoreChildren(mSubmission.getName(),
                                mSortType, mComment.getChildren(), mComment.getLevel(),
                                new FutureCallback<ArrayList<Thing>>() {
                                    @Override
                                    public void onCompleted(Exception e,
                                                            final ArrayList<Thing> comments) {
                                        mComment.setIsLoading(false);
                                        if (e != null) {
                                            e.printStackTrace();
                                            return;
                                        }
                                        int insert = mCommentsList.indexOf(mComment);
                                        final int pos = insert + 1;
                                        mCommentsList.remove(insert);
                                        for (Thing thing : comments) {
                                            if (thing instanceof AbsComment) {
                                                mCommentsList.add(insert++, (AbsComment) thing);
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

        public void setContent(MoreComments comment) {
            mComment = comment;

            mProgressBar.setVisibility(mComment.isLoading() ? View.VISIBLE : View.GONE);
            float dp = itemView.getResources().getDisplayMetrics().density;
            itemView.setPadding(Math.round(4 * dp * mComment.getLevel()), 0, 0, 0);
            if (mComment.getLevel() > 0) {
                mLevelIndicator.setVisibility(View.VISIBLE);
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
                mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.card_view_gray));
            }
        }
    }

    public interface OnSubmissionLoaded {
        public void onSubmissionLoaded(Submission submission);
    }
}
