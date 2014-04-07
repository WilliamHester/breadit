package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;
import me.williamhester.areddit.Submission;
import me.williamhester.areddit.User;
import me.williamhester.areddit.Votable;

public class CommentFragment extends Fragment {

    private final int HEADER_VIEW_COUNT = 1;

    private ArrayList<Comment> mCommentsList;
    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;
    private GestureDetector mGestureDetector;
    private ListView mCommentsListView;
    private SparseArray<HiddenComments> mHiddenComments;
    private String mUrl;
    private String mPermalink;
    private Submission mSubmission;
    private TextView mNumComments;
    private User mUser;
    private View mHeaderView;
    private View.OnTouchListener mGestureListener;

    private int mSortType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContext = getActivity();
        if (args != null) {
            mUser = args.getParcelable("user");
            mSubmission = args.getParcelable("submission");
            if (mSubmission != null) {
                mUrl = mSubmission.getUrl();
                mPermalink = mSubmission.getPermalink();
            }
        }
        mCommentsList = new ArrayList<Comment>();
        mHiddenComments = new SparseArray<HiddenComments>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment, null);
        mGestureDetector = new GestureDetector(mContext, new SwipeDetector());
        mGestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        };
        if (mHeaderView == null) {
            mHeaderView = createHeaderView(inflater);
        }
        mCommentsListView = (ListView) v.findViewById(R.id.comments);
        mCommentsListView.addHeaderView(mHeaderView);
        mCommentAdapter = new CommentArrayAdapter(mContext);
        mCommentsListView.setAdapter(mCommentAdapter);
        mCommentsListView.setOnTouchListener(mGestureListener);
        return v;
    }

    public View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.view_comments_header, null);

        TextView selfText = (TextView) v.findViewById(R.id.self_text);

        View subView = v.findViewById(R.id.submission);

        final View voteStatus = v.findViewById(R.id.vote_status);
        TextView nameAndTime
                = (TextView) subView.findViewById(R.id.subreddit_name_and_time);
        TextView author = (TextView) subView.findViewById(R.id.author);
        ImageView thumbnail = (ImageView) subView.findViewById(R.id.thumbnail);
        TextView title = (TextView) subView.findViewById(R.id.title);
        TextView domain = (TextView) subView.findViewById(R.id.domain);
        final TextView points = (TextView) subView.findViewById(R.id.points);
        View spacer = subView.findViewById(R.id.spacer);
        mNumComments = (TextView) v.findViewById(R.id.num_comments);
        Spinner sortBy = (Spinner) v.findViewById(R.id.sort_by);

        // if the submission is a self post, we need to hide the thumbnail
        if (mSubmission.isSelf()) {
            thumbnail.setVisibility(View.GONE);
            spacer.setVisibility(View.GONE);
        } else {
            thumbnail.setVisibility(View.VISIBLE);
            spacer.setVisibility(View.VISIBLE);
            UrlImageViewHelper.setUrlDrawable(thumbnail, mSubmission.getThumbnailUrl());
        }

        nameAndTime.setText(" in " + mSubmission.getSubredditName() + " "
                + calculateTimeShort(mSubmission.getCreatedUtc()));

        switch (mSubmission.getVoteStatus()) {
            case Votable.DOWNVOTED:
                voteStatus.setVisibility(View.VISIBLE);
                voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                break;
            case Votable.UPVOTED:
                voteStatus.setVisibility(View.VISIBLE);
                voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                break;
            default:
                voteStatus.setVisibility(View.GONE);
                break;
        }

        title.setText(StringEscapeUtils.unescapeHtml4(mSubmission.getTitle()));
        author.setText(mSubmission.getAuthor());
        domain.setText("(" + mSubmission.getDomain() + ")");
        points.setText(mSubmission.getScore() + " points by ");

        if (mSubmission.isSelf() && mSubmission.getSelfTextHtml() != null) {
            selfText.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(mSubmission.getSelfTextHtml())));
            selfText.setMovementMethod(new CommentLinkMovementMethod(0));
        } else {
            selfText.setVisibility(View.GONE);
        }

        final String[] sortTypes = getResources().getStringArray(R.array.sort_types);

        mNumComments.setText("Top " + mCommentsList.size() + " comments. Sorted by");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.spinner_item, R.id.orange_spinner_text, sortTypes);
//        adapter.setDropDownViewResource(android.R.layout.simple_list_item_2);
        sortBy.setAdapter(adapter);
        sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
                mCommentsList = new ArrayList<Comment>();
                mCommentAdapter = new CommentArrayAdapter(mContext);
                mCommentsListView.setAdapter(mCommentAdapter);
                mCommentsListView.setOnTouchListener(mGestureListener);
                new CommentLoaderTask().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        return v;
    }

    private String calculateTimeShort(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            time = difference / 3156000 + "y";
        } else if (difference / 2592000 > 0) {
            time = difference / 2592000 + "m";
        } else if (difference / 604800 > 0) {
            time = difference / 604800 + "w";
        } else if (difference / 86400 > 0) {
            time = difference / 86400 + "d";
        } else if (difference / 3600 > 0) {
            time = difference / 3600 + "h";
        } else if (difference / 60 > 0) {
            time = difference / 60 + "m";
        } else {
            time = difference + "s";
        }
        return time;
    }

    private class CommentArrayAdapter extends ArrayAdapter<Comment> {

        public CommentArrayAdapter(Context context) {
            super(context, R.layout.list_item_comment, mCommentsList);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_item_comment, parent, false);

            LinearLayout root = (LinearLayout) convertView.findViewById(R.id.root);
            TextView author = (TextView) convertView.findViewById(R.id.author);
            TextView score = (TextView) convertView.findViewById(R.id.points);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView body = (TextView) convertView.findViewById(R.id.comment_text);
            View voteStatus = convertView.findViewById(R.id.vote_status);

            root.setPadding((int) (getResources().getDisplayMetrics().density
                    * 12 * getItem(position).getLevel()), 0, 0, 0);
            author.setText(removeEndQuotes(getItem(position).getAuthor()));
            score.setText(getItem(position).getScore() + " points by ");
            time.setText(" " + calculateTimeShort(getItem(position).getCreatedUtc()));
            body.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(getItem(position).getBodyHtml())));
            body.setMovementMethod(new CommentLinkMovementMethod(position + HEADER_VIEW_COUNT));

            switch (getItem(position).getVoteStatus()) {
                case Votable.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    break;
                case Votable.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    break;
            }
            if (getItem(position).isHidden())
                body.setVisibility(View.GONE);
            else
                body.setVisibility(View.VISIBLE);

            return convertView;
        }

        private String removeEndQuotes(String s) {
            if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }
    }

    private class CommentLoaderTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... params) {
            try {
                String lastComment = null;
                if (mCommentsList.size() > 0)
                    lastComment = mCommentsList.get(mCommentsList.size() - 1).getName();
                List<Comment> comments = Comment.getComments(mPermalink, mUser, lastComment,
                        mSortType);
                return comments;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {
            if (result != null) {
                for (Comment comment : result) {
                    if (comment != null) {
                        Comment.CommentIterator iterator = comment.getCommentIterator();
                        while (iterator.hasNext()) {
                            mCommentsList.add(iterator.next());
                        }
                    }
                }
                mCommentAdapter.notifyDataSetChanged();
                mNumComments.setText("Top " + mCommentsList.size() + " comments. Sorted by");
            }
        }
    }

    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            int position = mCommentsListView.pointToPosition((int) ev.getX(), (int) ev.getY());
            if (position > 0) {
                View v = mCommentsListView.getChildAt(position - mCommentsListView.getFirstVisiblePosition());
                TextView commentText = (TextView) v.findViewById(R.id.comment_text);
                if (commentText.getVisibility() == View.VISIBLE) {
                    commentText.setVisibility(View.GONE);
                    mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).setHidden(true);
                    mHiddenComments.put(position, new HiddenComments(position));
                } else {
                    commentText.setVisibility(View.VISIBLE);
                    mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).setHidden(false);
                    ArrayList<Comment> hc = mHiddenComments.get(position).getHiddenComments();
                    mHiddenComments.remove(position);
                    for (Comment c : hc) {
                        mCommentsList.add(position++, c);
                    }
                }
                mCommentAdapter.notifyDataSetChanged();
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            int position = mCommentsListView.pointToPosition((int) event.getX(), (int) event.getY());
            View childView = mCommentsListView.getChildAt(position - mCommentsListView.getFirstVisiblePosition());
            final int y = childView == null ? 0 : (int) childView.getY();
            mCommentsListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCommentsListView.smoothScrollBy(y, 300);
                }
            }, 320);
            if (position > 0) {
                ReplyDialogFragment rf = ReplyDialogFragment.newInstance(mUser,
                        mCommentsList.get(position - HEADER_VIEW_COUNT).getName(),
                        mCommentsList.get(position - HEADER_VIEW_COUNT));
                rf.show(getFragmentManager(), "reply_fragment");
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mUser != null) {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                int position = mCommentsListView.pointToPosition((int) e1.getX(), (int) e1.getY());
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Votable v;
                    if (position == 0) {
                        v = mSubmission;
                    } else {
                        v = mCommentAdapter.getItem(position - HEADER_VIEW_COUNT);
                        if (v.getVoteStatus() == Comment.DOWNVOTED) {
                            new VoteAsyncTask(v.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
                            v.setVoteStatus(Votable.NEUTRAL);
                        } else {
                            new VoteAsyncTask(v.getName(), mUser, VoteAsyncTask.DOWNVOTE).execute();
                            v.setVoteStatus(Votable.DOWNVOTED);
                        }
                    }
                    setVoteStatus(v, position);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Votable v;
                    if (position == 0) {
                        v = mSubmission;
                    } else {
                        v = mCommentAdapter.getItem(position - HEADER_VIEW_COUNT);
                        if (v.getVoteStatus() == Comment.UPVOTED) {
                            new VoteAsyncTask(v.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
                            v.setVoteStatus(Votable.NEUTRAL);
                        } else {
                            new VoteAsyncTask(v.getName(), mUser, VoteAsyncTask.UPVOTE).execute();
                            v.setVoteStatus(Votable.UPVOTED);
                        }
                    }
                    setVoteStatus(v, position);
                }
            }
            return false;
        }

        private void setVoteStatus(Votable votable, int position) {
            View v = mCommentsListView.getChildAt(position - mCommentsListView.getFirstVisiblePosition());
            View voteStatus = v.findViewById(R.id.vote_status);
            TextView points = (TextView) v.findViewById(R.id.points);
            switch (votable.getVoteStatus()) {
                case Votable.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                    points.setText(votable.getScore() + " points by ");
                    break;
                case Votable.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    points.setText(votable.getScore() + " points by ");
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    points.setText(votable.getScore() + " points by ");
                    break;
            }
        }
    }

    private class CommentLinkMovementMethod extends LinkMovementMethod {

        private int mPosition;

        public CommentLinkMovementMethod(int position) {
            mPosition = position;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (super.onTouchEvent(widget, buffer, event))
                    return true;
                View v = mCommentsListView.getChildAt(mPosition - mCommentsListView.getFirstVisiblePosition());
                event.setLocation(event.getX(), event.getY() + v.getY());
                return mGestureDetector.onTouchEvent(event);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                boolean ret = super.onTouchEvent(widget, buffer, event);
                View v = mCommentsListView.getChildAt(mPosition - mCommentsListView.getFirstVisiblePosition());
                event.setLocation(event.getX(), event.getY() + v.getY());
                mGestureDetector.onTouchEvent(event);
                return ret;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                View v = mCommentsListView.getChildAt(mPosition - mCommentsListView.getFirstVisiblePosition());
                event.setLocation(event.getX(), event.getY() + v.getY());
                mGestureDetector.onTouchEvent(event);
            }
            return false;
        }
    }

    private class HiddenComments {

        private int mBelowPosition; // The position of the comment that is being collapsed
        private ArrayList<Comment> mHiddenCommentsList = new ArrayList<Comment>();

        public HiddenComments(int position) {
            mBelowPosition = position;
            int level = mCommentsList.get(position - HEADER_VIEW_COUNT).getLevel();
            position++;
            while (position < mCommentsList.size() && mCommentsList.get(position - HEADER_VIEW_COUNT).getLevel() > level) {
                mHiddenCommentsList.add(mCommentsList.remove(position - HEADER_VIEW_COUNT));
            }
        }

        public ArrayList<Comment> getHiddenComments() {
            return mHiddenCommentsList;
        }

        public int getBelowPosition() {
            return mBelowPosition;
        }

        @Override
        public boolean equals(Object o) {
            return ((HiddenComments)o).getBelowPosition() == mBelowPosition;
        }

    }
}
