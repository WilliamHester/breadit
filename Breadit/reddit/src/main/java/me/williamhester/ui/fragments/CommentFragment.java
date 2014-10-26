package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.SubmissionViewHolder;

public class CommentFragment extends AccountFragment {

    private static final String PERMALINK = "permalink";
    private static final int REPLY_REQUEST = 1;
    private static final int EDIT_REQUEST = 2;

    private final ArrayList<AbsComment> mCommentsList = new ArrayList<>();
    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;
    private ListView mCommentsListView;
    private String mPermalink;
    private Submission mSubmission;
    private View mHeaderView;
    private OnSubmissionLoaded mCallback;

    private String mSortType;

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
        } else if (args != null) {
            mSortType = RedditApi.COMMENT_SORT_TOP; // TODO: Change this to the settings singleton
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment, root, false);
        mCommentsListView = (ListView) v.findViewById(R.id.comments);
        if (mHeaderView == null && mSubmission != null) {
            createHeaderView(inflater);
        }
        if (mCommentsListView.getHeaderViewsCount() == 0 && mHeaderView != null) {
            mCommentsListView.addHeaderView(mHeaderView);
        }
        mCommentAdapter = new CommentArrayAdapter(mContext);
        mCommentsListView.setAdapter(mCommentAdapter);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.submission_fragment, menu);

        Url url = new Url(mSubmission.getUrl());

        if (mSubmission == null || mSubmission.isSelf()) {
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
        menu.removeItem(R.id.action_open_link_in_browser);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_comments: {
                PopupMenu popupMenu = new PopupMenu(mContext,
                        getActivity().findViewById(R.id.action_sort_comments));
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
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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
                mCommentAdapter.notifyDataSetChanged();
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
                mCommentsList.remove(index);
                mCommentsList.add(index, (Comment) newThing);
                if (mCommentAdapter != null) {
                    mCommentAdapter.notifyDataSetChanged();
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
    }

    public void setOnSubmissionLoadedListener(OnSubmissionLoaded listener) {
        mCallback = listener;
    }

    private void createHeaderView() {
        createHeaderView((LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    @SuppressLint("InflateParams")
    private void createHeaderView(LayoutInflater inflater) {
        mHeaderView = inflater.inflate(R.layout.header_comments, null);
        mHeaderView.findViewById(R.id.option_reply).setVisibility(View.VISIBLE);
        mHeaderView.setTag(new SubmissionViewHolder(mHeaderView, new SubmissionViewHolder.SubmissionCallbacks() {
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
            public void onCardClicked(Submission submission) { }

            @Override
            public void onCardLongPressed(SubmissionViewHolder holder) { }

            @Override
            public void onOptionsRowItemSelected(View view, Submission submission) {
                switch (view.getId()) {
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
                                    return;
                                }
//                                mSubmissionList.remove(submission);
//                                mSubmissionsAdapter.notifyDataSetChanged();
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
//                                            mSubmissionList.remove(submission);
//                                            mSubmissionsAdapter.notifyDataSetChanged();
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
//                                        mSubmissionsAdapter.notifyDataSetChanged();
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
        }));
        ((SubmissionViewHolder) mHeaderView.getTag()).setContent(mSubmission);
        ((SubmissionViewHolder) mHeaderView.getTag()).expandOptions();
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
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
            return true;
        }
    };

    private CommentViewHolder.CommentClickCallbacks mCommentCallbacks =  new CommentViewHolder.CommentClickCallbacks() {

        private CommentViewHolder mFocusedViewHolder;

        /*
         Bug notes - when a certain type of gif is opened, the comment adapter fails
         This causes the comment adapter to not be updated properly and means that the underlying
         vies do not get updated properly and can cause an ArrayIndexOutOfBoundsException if this is
         not fixed, although somewhat rare.
         */

        @Override
        public void onBodyClick(final Comment comment) {
            comment.setHidden(!comment.isHidden());
            int position = mCommentAdapter.getPosition(comment);
            if (comment.isHidden()) {
                hideComment(position);
            } else {
                showComment(position);
            }
            mCommentAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMoreClick(CommentViewHolder commentView, final MoreComments comment) {
            if (!comment.isLoading()) {
                comment.setIsLoading(true);
                RedditApi.getMoreChildren(mSubmission.getName(),
                        mSortType, comment.getChildren(), comment.getLevel(),
                        new FutureCallback<ArrayList<Thing>>() {
                            @Override
                            public void onCompleted(Exception e, ArrayList<Thing> comments) {
                                comment.setIsLoading(false);
                                if (e != null) {
                                    e.printStackTrace();
                                    return;
                                }
                                int insert = mCommentsList.indexOf(comment);
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
                                            mCommentAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        });
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
        public void onOptionsRowItemSelected(View view, AbsComment comment) {
            switch (view.getId()) {
                case R.id.option_reply:
                    Fragment reply = ReplyFragment.newInstance((Comment) comment);
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
                        parent = (Thing) mCommentsList.get(commentIndex);
                    }
                    ReplyFragment fragment = ReplyFragment.newInstance(parent, (Comment) comment);
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
                    String link = RedditApi.REDDIT_URL + mSubmission.getPermalink()
                            + ((Comment) comment).getName();
                    sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent,
                            getResources().getText(R.string.share_with)));
                    break;
            }
        }
    };

    private class CommentArrayAdapter extends ArrayAdapter<AbsComment> {

        public CommentArrayAdapter(Context context) {
            super(context, R.layout.list_item_comment, mCommentsList);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.list_item_comment, null);
                convertView.setTag(new CommentViewHolder(convertView, mCommentCallbacks, mSubmission.getAuthor()));
            }
            ((CommentViewHolder) convertView.getTag()).setContent(getItem(position));

            return convertView;
        }
    }

    private FutureCallback<Submission> mSubmissionCallback = new FutureCallback<Submission>() {
        @Override
        public void onCompleted(Exception e, Submission result) {
            if (e != null) {
                return;
            }
            Object imgurData = null;
            if (mSubmission != null)
                imgurData = mSubmission.getImgurData();
            mSubmission = result;
            mSubmission.setImgurData(imgurData);
            if (mCallback != null) {
                createHeaderView();
                mCommentsListView.addHeaderView(mHeaderView);
                mCallback.onSubmissionLoaded(mSubmission);
            } else { // need to replace the submission object inside the SwipeView header
                ((SubmissionViewHolder) mHeaderView.getTag()).setContent(mSubmission);
                ((SubmissionViewHolder) mHeaderView.getTag()).expandOptions();
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
            mCommentsListView.setAdapter(mCommentAdapter);
            mCommentAdapter.notifyDataSetChanged();
        }
    };

    private void hideComment(int position) {
        AbsComment comment = mCommentsList.get(position);
        int level = comment.getLevel();
        position++;
        ArrayList<AbsComment> children = new ArrayList<>();
        while (position < mCommentsList.size() && mCommentsList.get(position).getLevel() > level) {
            children.add(mCommentsList.remove(position));
        }
        ((Comment) comment).hide(children);
    }

    private void showComment(int position) {
        AbsComment comment = mCommentsList.get(position);
        ArrayList<AbsComment> children = ((Comment) comment).unhideComment();
        for (AbsComment c : children) {
            mCommentsList.add(++position, c);
        }
    }

    public interface OnSubmissionLoaded {
        public void onSubmissionLoaded(Submission submission);
    }
}
