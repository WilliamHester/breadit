package me.williamhester.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.williamhester.models.Comment;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.CommentViewHolder;

public class CommentFragment extends AccountFragment {

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
    private boolean mLinkIsPressed = false;

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
            } else {
                mPermalink = args.getString("permalink");
                mPermalink = mPermalink.substring(mPermalink.indexOf("reddit.com") + 10);
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
            mHeaderView = createHeaderView(inflater);
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

    private View createHeaderView() {
        return createHeaderView((LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.view_comments_header, null);

        TextView selfText = (TextView) v.findViewById(R.id.body);

        View subView = v.findViewById(R.id.submission);

        final View voteStatus = v.findViewById(R.id.vote_status);
//        TextView nameAndTime
//                = (TextView) subView.findViewById(R.id.subreddit_name_and_time);
        TextView author = (TextView) subView.findViewById(R.id.author);
//        ImageView thumbnail = (ImageView) subView.findViewById(R.id.thumbnail);
        TextView title = (TextView) subView.findViewById(R.id.body);
        TextView domain = (TextView) subView.findViewById(R.id.domain);
        final TextView points = (TextView) subView.findViewById(R.id.metadata);
        mNumComments = (TextView) v.findViewById(R.id.num_comments);
        Spinner sortBy = (Spinner) v.findViewById(R.id.sort_by);
        LinearLayout edit = (LinearLayout) v.findViewById(R.id.edited_text);
        edit.setVisibility(View.GONE);

        title.setText(StringEscapeUtils.unescapeHtml4(mSubmission.getTitle()));
//        author.setText(mSubmission.getAuthor());
        domain.setText("(" + mSubmission.getDomain() + ")");
        points.setText(mSubmission.getScore() + " points by ");

        if (mSubmission.isSelf() && mSubmission.getBodyHtml() != null) {
            selfText.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(mSubmission.getBodyHtml())));
            selfText.setMovementMethod(new LinkMovementMethod());
        } else {
            selfText.setVisibility(View.GONE);
        }

        final String[] sortTypes = getResources().getStringArray(R.array.sort_types);

        mNumComments.setText("Top " + mCommentsList.size() + " comments. Sorted by");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.spinner_item, R.id.orange_spinner_text, sortTypes);
        sortBy.setAdapter(adapter);
        sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int old = mSortType;
                switch (i) {
                    case 0:
                        mSortType = Comment.TOP;
                        break;
                    case 1:
                        mSortType = Comment.BEST;
                        break;
                    case 2:
                        mSortType = Comment.NEW;
                        break;
                    case 3:
                        mSortType = Comment.HOT;
                        break;
                    case 4:
                        mSortType = Comment.CONTROVERSIAL;
                        break;
                    case 5:
                        mSortType = Comment.OLD;
                        break;
                }
                if (old != mSortType) {
                    RedditApi.getSubmissionData(mContext, mPermalink, mSubmissionCallback, mCommentCallback);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        return v;
    }

    public void scrollToTop() {
        mCommentsListView.smoothScrollToPositionFromTop(0, 0,
                mCommentsListView.getFirstVisiblePosition() * 5);
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
                convertView.setTag(new CommentViewHolder(convertView, mCommentCallbacks));
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
                mHeaderView = createHeaderView();
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
            mNumComments.setText("Top " + mCommentsList.size() + " comments. Sorted by");
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
