package me.williamhester.ui.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.AbsComment;
import me.williamhester.models.Comment;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.MoreComments;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.Url;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.SwipeView;

public class CommentFragment extends AccountFragment {

    private static final String PERMALINK = "permalink";

    private ArrayList<AbsComment> mCommentsList;
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
            mCommentsList = savedInstanceState.getParcelableArrayList("comments");
            mSubmission = savedInstanceState.getParcelable("submission");
            mPermalink = savedInstanceState.getString("permalink");
            mSortType = savedInstanceState.getString("sortType");
        } else if (args != null) {
            mSortType = RedditApi.COMMENT_SORT_TOP; // TODO: Change this to the settings singleton
            mSubmission = args.getParcelable("submission");
            if (mSubmission != null) {
                mPermalink = mSubmission.getPermalink();
                RedditApi.getSubmissionData(getActivity(), mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            } else {
                mPermalink = args.getString(PERMALINK);
                if (mPermalink.contains("reddit.com")) {
                    mPermalink = mPermalink.substring(mPermalink.indexOf("reddit.com") + 10);
                }
                RedditApi.getSubmissionData(mContext, mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
            mCommentsList = new ArrayList<>();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort_comments) {
            PopupMenu popupMenu = new PopupMenu(getActivity(),
                    getActivity().findViewById(R.id.action_sort_comments));
            popupMenu.setOnMenuItemClickListener(mSortClickListener);
            popupMenu.inflate(R.menu.comment_sorts);
            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void createHeaderView(LayoutInflater inflater) {
        mHeaderView = inflater.inflate(R.layout.header_comments, null);
        TextView domain = (TextView) mHeaderView.findViewById(R.id.domain);
        TextView commentData = (TextView) mHeaderView.findViewById(R.id.num_comments);
        TextView metadata = (TextView) mHeaderView.findViewById(R.id.metadata);
        TextView subreddit = (TextView) mHeaderView.findViewById(R.id.subreddit_title);
        TextView body = (TextView) mHeaderView.findViewById(R.id.body);
        TextView selfText = (TextView) mHeaderView.findViewById(R.id.self_text);
        SwipeView swipeView = (SwipeView) mHeaderView.findViewById(R.id.swipe_view);
        View foregroundVote = mHeaderView.findViewById(R.id.vote_foreground);
        View backgroundVote = mHeaderView.findViewById(R.id.vote_background);

        switch (mSubmission.getVoteStatus()) {
            case Votable.DOWNVOTED:
                foregroundVote.setScaleY(0f);
                backgroundVote.setVisibility(View.VISIBLE);
                backgroundVote.setBackgroundColor(
                        backgroundVote.getResources().getColor(R.color.periwinkle));
                break;
            case Votable.UPVOTED:
                foregroundVote.setScaleY(0f);
                backgroundVote.setVisibility(View.VISIBLE);
                backgroundVote.setBackgroundColor(
                        backgroundVote.getResources().getColor(R.color.orangered));
                break;
            default:
                backgroundVote.setVisibility(View.GONE);
                foregroundVote.setScaleY(0f);
                break;
        }

        swipeView.recycle(mSubmission);
        swipeView.setUp(mHeaderView.findViewById(R.id.vote_background),
                mHeaderView.findViewById(R.id.vote_foreground), new SwipeView.SwipeListener() {
                    @Override
                    public void onRightToLeftSwipe() {
                        mSubmission.setVoteStatus(mSubmission.getVoteStatus() == Votable.DOWNVOTED ? Votable.NEUTRAL : Votable.DOWNVOTED);
                        RedditApi.vote(getActivity(), mSubmission);
                        Intent result = new Intent();
                        Bundle extras = new Bundle();
                        extras.putString("name", mSubmission.getName());
                        extras.putInt("status", mSubmission.getVoteStatus());
                        result.putExtras(extras);
                        getActivity().setResult(SubredditFragment.VOTE_REQUEST_CODE, result);
                    }

                    @Override
                    public void onLeftToRightSwipe() {
                        mSubmission.setVoteStatus(mSubmission.getVoteStatus() == Votable.UPVOTED ? Votable.NEUTRAL : Votable.UPVOTED);
                        RedditApi.vote(getActivity(), mSubmission);
                        Intent result = new Intent();
                        Bundle extras = new Bundle();
                        extras.putString("name", mSubmission.getName());
                        extras.putInt("status", mSubmission.getVoteStatus());
                        result.putExtras(extras);
                        getActivity().setResult(SubredditFragment.VOTE_REQUEST_CODE, result);
                    }
                });
        swipeView.invalidate();
        View nsfwWarning = mHeaderView.findViewById(R.id.nsfw_warning);
        final ImageButton button = (ImageButton) mHeaderView.findViewById(R.id.preview_button);

        body.setText(Html.fromHtml(mSubmission.getTitle()).toString());
        domain.setText(mSubmission.getDomain());
        commentData.setText(mSubmission.getNumberOfComments() + " comments");
        subreddit.setText("/r/" + mSubmission.getSubredditName());
        metadata.setText(mSubmission.getAuthor() + " " + mSubmission.getScore() + " "
                + getResources().getQuantityString(R.plurals.points,
                mSubmission.getScore()));

        if (mSubmission.isNsfw()) {
            nsfwWarning.setVisibility(View.VISIBLE);
        } else {
            nsfwWarning.setVisibility(View.GONE);
        }

        View container = mHeaderView.findViewById(R.id.content_preview);
        ImageView imageView = (ImageView) mHeaderView.findViewById(R.id.image);

        if (mSubmission.isSelf()) {
            if (mSubmission.getBodyHtml() != null) {
                HtmlParser parser = new HtmlParser(Html.fromHtml(mSubmission.getBodyHtml()).toString());
                selfText.setText(parser.getSpannableString());
                selfText.setVisibility(View.VISIBLE);
                selfText.setMovementMethod(new LinkMovementMethod());
            }
        } else {
            final Url linkDetails = new Url(mSubmission.getUrl());
            if (linkDetails.getType() != Url.NOT_SPECIAL) {
                String id = linkDetails.getLinkId();
                if (linkDetails.getType() == Url.IMGUR_IMAGE) {
                    if (mSubmission.getImgurData() == null) {
                        imageView.setImageDrawable(null);
                        ImgurApi.getImageDetails(id, mHeaderView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview(mHeaderView);
                    }
                } else if (linkDetails.getType() == Url.IMGUR_ALBUM) {
                    if (mSubmission.getImgurData() == null) {
                        imageView.setImageDrawable(null);
                        ImgurApi.getAlbumDetails(id, mHeaderView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview(mHeaderView);
                    }
                } else if (linkDetails.getType() == Url.YOUTUBE) {
                    imageView.setVisibility(View.VISIBLE);
                    ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                    button.setImageResource(R.drawable.ic_youtube);
                    button.setVisibility(View.VISIBLE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getFragmentManager().beginTransaction()
                                    .add(R.id.container, YouTubeFragment.newInstance(linkDetails.getLinkId()), "youtube")
                                    .addToBackStack("youtube")
                                    .commit();
                        }
                    });
                } else if (linkDetails.getType() == Url.NORMAL_IMAGE) {
                    imageView.setVisibility(View.VISIBLE);
                    ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getFragmentManager().beginTransaction()
                                    .add(R.id.container, ImagePagerFragment.newInstance(linkDetails), "picture")
                                    .addToBackStack("picture")
                                    .commit();
                        }
                    });
                }
            } else {
                container.setVisibility(View.GONE);
            }
        }
    }

    private FutureCallback<Submission> mImgurCallback = new FutureCallback<Submission>() {
        @Override
        public void onCompleted(Exception e, Submission result) {
            if (e != null) {
                e.printStackTrace();
            } else if (mSubmission == result) {
                setImagePreview(mHeaderView);
            }
        }
    };

    /**
     * Attempts to set the image preview
     */
    private void setImagePreview(View v) {
        final ImgurImage image;
        if (mSubmission.getImgurData() instanceof ImgurAlbum) {
            image = ((ImgurAlbum) mSubmission.getImgurData()).getImages().get(0);
        } else if (mSubmission.getImgurData() instanceof ImgurImage) {
            image = (ImgurImage) mSubmission.getImgurData();
        } else {
            image = null;
        }
        ImageView imageView = (ImageView) v.findViewById(R.id.image);
        if (image != null && !image.isAnimated()) {
            imageView.setVisibility(View.VISIBLE);
            ImgurApi.loadImage(image.getUrl(), imageView, null);
            ImageButton button = (ImageButton) v.findViewById(R.id.preview_button);
            button.setVisibility(View.INVISIBLE);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment f = ImagePagerFragment.newInstance(mSubmission.getImgurData());
                    if (f != null) {
                        getFragmentManager().beginTransaction()
                                .add(R.id.container, f, "imgur")
                                .addToBackStack("imgur")
                                .commit();
                    }
                }
            });
        } else {
            v.findViewById(R.id.content_preview).setVisibility(View.GONE);
        }
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
                RedditApi.getSubmissionData(getActivity(), mPermalink, mSortType, mSubmissionCallback, mCommentCallback);
            }
            return true;
        }
    };

    private CommentViewHolder.CommentClickCallbacks mCommentCallbacks =  new CommentViewHolder.CommentClickCallbacks() {

        private CommentViewHolder mFocusedViewHolder;

        @Override
        public void onHideClick(Comment comment) {
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
                RedditApi.getMoreChildren(getActivity(), mSubmission.getName(),
                        "top", comment.getChildren(), comment.getLevel(),
                        new FutureCallback<ArrayList<AbsComment>>() {
                            @Override
                            public void onCompleted(Exception e, ArrayList<AbsComment> comments) {
                                comment.setIsLoading(false);
                                if (e != null) {
                                    e.printStackTrace();
                                }
                                int insert = mCommentsList.indexOf(comment);
                                mCommentsList.remove(insert);
                                mCommentsList.addAll(insert, comments);
                                mCommentAdapter.notifyDataSetChanged();
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
                SwipeView swipeView = (SwipeView) mHeaderView.findViewById(R.id.swipe_view);
                swipeView.recycle(mSubmission);
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
            mCommentAdapter = new CommentArrayAdapter(mContext);
            mCommentsListView.setAdapter(mCommentAdapter);
            mCommentAdapter.notifyDataSetChanged();
        }
    };

    private FutureCallback<Votable> mEditVotableCallback = new FutureCallback<Votable>() {
        @Override
        public void onCompleted(Exception e, Votable result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            if (result instanceof Submission) {
                // update Submission view
            } else if (result instanceof Comment) {
                mCommentAdapter.notifyDataSetChanged();
            }
        }
    };

    private class ReplyAsyncTask extends AsyncTask<Void, Void, Void> {

        private String mReplyText;
        private String mName;

        public ReplyAsyncTask(String replyText, String fullname){
            mReplyText = replyText;
            mName = fullname;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mAccount != null && mReplyText != null && mReplyText.length() != 0) {
                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("api-type", "json"));
                apiParams.add(new BasicNameValuePair("text", mReplyText));
                apiParams.add(new BasicNameValuePair("thing_id", mName));
                Utilities.post(apiParams, "http://www.reddit.com/api/comment", mAccount);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            // re-parse the comment tree and find the one that didn't exist before with the user's
            //     name as the author name. Scroll to that one...
            // That, or parse the new structure until the new comment is found and add that one in.
            // pass new data for the comment and then notifydatasetchanged
        }
    }

    private class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {

        private String mFullname;
        private int mPosition;

        public DeleteAsyncTask(String fullname, int position) {
            mFullname = fullname;
            mPosition = position;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("id", mFullname));
            Utilities.post(apiParams, "http://www.reddit.com/api/del", mAccount);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mPosition > 0) {
                mCommentAdapter.remove(mCommentAdapter.getItem(mPosition));
                mCommentAdapter.notifyDataSetChanged();
            } else {
                if (getActivity() != null)
                    getActivity().finish();
            }
        }
    }

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
