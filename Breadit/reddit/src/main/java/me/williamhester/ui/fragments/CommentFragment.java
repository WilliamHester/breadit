package me.williamhester.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.williamhester.models.Comment;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.SwipeView;

public class CommentFragment extends AccountFragment {

    private static final String PERMALINK = "permalink";

    private ArrayList<Comment> mCommentsList;
    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;
    private ListView mCommentsListView;
    private HashMap<String, HiddenComments> mHiddenComments;
    private String mPermalink;
    private Submission mSubmission;
    private TextView mNumComments;
    private View mHeaderView;
    private OnSubmissionLoaded mCallback;

    private int mSortType;

    public static CommentFragment newInstance(String permalink) {
        Bundle args = new Bundle();
        args.putString(PERMALINK, permalink);
        CommentFragment fragment = new CommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommentFragment newInstance(Submission submission) {
        Bundle args = new Bundle();
        args.putSerializable("submission", submission);
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
            mSubmission = (Submission) savedInstanceState.getSerializable("submission");
            mPermalink = savedInstanceState.getString("permalink");
            mSortType = savedInstanceState.getInt("sortType");
        } else if (args != null) {
            mSubmission = (Submission) args.getSerializable("submission");
            if (mSubmission != null) {
                mPermalink = mSubmission.getPermalink();
                RedditApi.getSubmissionData(getActivity(), mPermalink, mSubmissionCallback, mCommentCallback);
            } else {
                mPermalink = args.getString(PERMALINK);
                if (mPermalink.contains("reddit.com")) {
                    mPermalink = mPermalink.substring(mPermalink.indexOf("reddit.com") + 10);
                }
                RedditApi.getSubmissionData(mContext, mPermalink, mSubmissionCallback, mCommentCallback);
            }
            mCommentsList = new ArrayList<>();
        }
        mHiddenComments = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment, null);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("comments", mCommentsList);
        outState.putSerializable("submission", mSubmission);
        outState.putString("permalink", mPermalink);
        outState.putInt("sortType", mSortType);
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
        swipeView.setUp(mHeaderView.findViewById(R.id.vote_background),
                mHeaderView.findViewById(R.id.vote_foreground), new SwipeView.SwipeListener() {
            @Override
            public void onRightToLeftSwipe() {
                mSubmission.setVoteStatus(mSubmission.getVoteStatus() == Votable.DOWNVOTED ? Votable.NEUTRAL : Votable.DOWNVOTED);
                RedditApi.vote(getActivity(), mSubmission);
            }

            @Override
            public void onLeftToRightSwipe() {
                mSubmission.setVoteStatus(mSubmission.getVoteStatus() == Votable.UPVOTED ? Votable.NEUTRAL : Votable.UPVOTED);
                RedditApi.vote(getActivity(), mSubmission);
            }
        });
        swipeView.recycle(mSubmission);
        View nsfwWarning = mHeaderView.findViewById(R.id.nsfw_warning);
        View submissionData = mHeaderView.findViewById(R.id.submission_data);
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
            }
        } else {
            UrlParser linkDetails = new UrlParser(mSubmission.getUrl());
            if (linkDetails.getType() != UrlParser.NOT_SPECIAL) {
                String id = linkDetails.getLinkId();
                if (linkDetails.getType() == UrlParser.IMGUR_IMAGE) {
                    if (mSubmission.getImgurData() == null) {
                        imageView.setImageDrawable(null);
                        ImgurApi.getImageDetails(id, mHeaderView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview(mHeaderView);
                    }
                } else if (linkDetails.getType() == UrlParser.IMGUR_ALBUM) {
                    if (mSubmission.getImgurData() == null) {
                        imageView.setImageDrawable(null);
                        ImgurApi.getAlbumDetails(id, mHeaderView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview(mHeaderView);
                    }
                } else if (linkDetails.getType() == UrlParser.YOUTUBE) {
                    imageView.setVisibility(View.VISIBLE);
                    ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                    button.setImageResource(R.drawable.ic_youtube);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                } else if (linkDetails.getType() == UrlParser.NORMAL_IMAGE) {
                    imageView.setVisibility(View.VISIBLE);
                    ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

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
     *
     * @return returns whether or not the data has been set.
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
                    if (mSubmission.getImgurData() instanceof ImgurImage) {
//                        mCallback.onImageViewClicked((ImgurImage) mSubmission.getImgurData());
                    } else if (mSubmission.getImgurData() instanceof ImgurAlbum) {
//                        mCallback.onImageViewClicked((ImgurAlbum) mSubmission.getImgurData());
                    }
                }
            });
        } else {
            v.findViewById(R.id.content_preview).setVisibility(View.GONE);
        }
    }

    private CommentViewHolder.CommentClickCallbacks mCommentCallbacks = new CommentViewHolder.CommentClickCallbacks() {

        @Override
        public void onHideClick(Comment comment) {
            comment.setHidden(!comment.isHidden());
            if (comment.isHidden()) {
                int position = mCommentAdapter.getPosition(comment);
                mHiddenComments.put(comment.getName(), new HiddenComments(position));
            } else {
                ArrayList<Comment> hc = mHiddenComments.get(comment.getName()).getHiddenComments();
                mHiddenComments.remove(comment.getName());
                int position = mCommentAdapter.getPosition(comment);
                for (Comment c : hc) {
                    mCommentsList.add(++position, c);
                }
            }
            mCommentAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMoreClick(CommentViewHolder commentView, Comment comment) {

        }

    };

    private class CommentArrayAdapter extends ArrayAdapter<Comment> {

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
            mSubmission = result;
            if (mCallback != null) {
                createHeaderView();
                mCommentsListView.addHeaderView(mHeaderView);
                mCallback.onSubmissionLoaded(mSubmission);
            }
        }
    };

    private FutureCallback<List<Comment>> mCommentCallback = new FutureCallback<List<Comment>>() {
        @Override
        public void onCompleted(Exception e, List<Comment> result) {
            if (e != null) {
                return;
            }
            mCommentsList.addAll(result);
            mCommentAdapter = new CommentArrayAdapter(mContext);
            mCommentsListView.setAdapter(mCommentAdapter);
            mCommentAdapter.notifyDataSetChanged();
            if (mNumComments != null) {
                mNumComments.setText("Top " + mCommentsList.size() + " comments. Sorted by");
            }
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

    private class HiddenComments {

        private ArrayList<Comment> mHiddenCommentsList = new ArrayList<>();

        public HiddenComments(int position) {
            int level = mCommentsList.get(position).getLevel();
            position++;
            while (position < mCommentsList.size() && mCommentsList.get(position).getLevel() > level) {
                mHiddenCommentsList.add(mCommentsList.remove(position));
            }
        }

        public ArrayList<Comment> getHiddenComments() {
            return mHiddenCommentsList;
        }

    }

    public interface OnSubmissionLoaded {
        public void onSubmissionLoaded(Submission submission);
    }
}
