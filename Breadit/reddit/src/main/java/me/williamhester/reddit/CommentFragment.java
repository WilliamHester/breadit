package me.williamhester.reddit;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.williamhester.areddit.Comment;
import me.williamhester.areddit.Submission;
import me.williamhester.areddit.Account;
import me.williamhester.areddit.Votable;
import me.williamhester.areddit.utils.Utilities;

public class CommentFragment extends Fragment {

    private final int HEADER_VIEW_COUNT = 1;

    private ArrayList<Comment> mCommentsList;
    private CommentArrayAdapter mCommentAdapter;
    private Context mContext;
    private GestureDetector mGestureDetector;
    private ListView mCommentsListView;
    private HashMap<String, HiddenComments> mHiddenComments;
    private String mUrl;
    private String mPermalink;
    private Submission mSubmission;
    private TextView mNumComments;
    private Account mAccount;
    private View mHeaderView;
    private View.OnTouchListener mGestureListener;

    private int mSortType;
    private boolean mLinkIsPressed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContext = getActivity();
        if (args != null) {
            mAccount = args.getParcelable("account");
            mSubmission = args.getParcelable("submission");
            if (mSubmission != null) {
                mUrl = mSubmission.getUrl();
                mPermalink = "http://www.reddit.com" + mSubmission.getPermalink();
            }
        }
        mCommentsList = new ArrayList<Comment>();
        mHiddenComments = new HashMap<String, HiddenComments>();
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

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
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
            LinearLayout replyLayout = (LinearLayout) convertView.findViewById(R.id.edited_text);
            final EditText replyBody = (EditText) convertView.findViewById(R.id.reply_body);
            final Button confirm = (Button) convertView.findViewById(R.id.confirm_reply);
            final Button cancel = (Button) convertView.findViewById(R.id.cancel_reply);

            if (getItem(position).isBeingEdited()) {
                confirm.setEnabled(true);
                cancel.setEnabled(true);
                replyBody.setEnabled(true);
                body.setVisibility(View.GONE);
                replyLayout.setVisibility(View.VISIBLE);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (position == 1)
                            new ReplyAsyncTask(replyBody.getText().toString(),
                                    mSubmission.getName()).execute();
                        else if (position > 1)
                            new ReplyAsyncTask(replyBody.getText().toString(),
                                    getItem(position - 1).getName()).execute();
                        confirm.setEnabled(false);
                        cancel.setEnabled(false);
                        replyBody.setEnabled(false);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCommentsList.remove(position);
                        mCommentAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                replyLayout.setVisibility(View.GONE);
                body.setVisibility(View.VISIBLE);
            }

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

                return Comment.getComments(mPermalink, mAccount, lastComment, mSortType);
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
                Log.i("SubmitDialogFragment", "Response = " + Utilities.post(apiParams,
                        "http://www.reddit.com/api/comment", mAccount.getCookie(),
                        mAccount.getModhash()));
                Log.i("SubmitDialogFragment", "name = " + mName);
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

    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            int position = mCommentsListView.pointToPosition((int) ev.getX(), (int) ev.getY());
            if (position > 0 && !mLinkIsPressed) {
                View v = mCommentsListView.getChildAt(position - mCommentsListView.getFirstVisiblePosition());
                TextView commentText = (TextView) v.findViewById(R.id.comment_text);
                if (commentText.getVisibility() == View.VISIBLE) {
                    commentText.setVisibility(View.GONE);
                    mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).setHidden(true);
                    mHiddenComments.put(mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).getName(),
                            new HiddenComments(position));
                } else {
                    commentText.setVisibility(View.VISIBLE);
                    mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).setHidden(false);
                    ArrayList<Comment> hc = mHiddenComments
                            .get(mCommentAdapter.getItem(position - HEADER_VIEW_COUNT).getName()).getHiddenComments();
                    mHiddenComments.remove(position);
                    for (Comment c : hc) {
                        mCommentsList.add(position++, c);
                    }
                }
                mCommentAdapter.notifyDataSetChanged();
            } else if (mLinkIsPressed) {
                mLinkIsPressed = false;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (mAccount != null) {
                int position = mCommentsListView.pointToPosition((int) event.getX(),
                        (int) event.getY());
                View childView = mCommentsListView.getChildAt(position
                        - mCommentsListView.getFirstVisiblePosition());
                final int y = childView == null ? 0 : (int) childView.getY();
                mCommentsListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCommentsListView.smoothScrollBy(y, 300);
                    }
                }, 320);
                Comment c = null;
                if (position == 0) {
                    c = new Comment(mAccount, 0);
                } else if (position > 0) {
                    c = new Comment(mAccount, mCommentsList.get(position - 1).getLevel() + 1);
                }
                mCommentsList.add(position, c);
                mCommentAdapter.notifyDataSetChanged();
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            int position = mCommentsListView.pointToPosition((int) event.getX(), (int) event.getY());
            final Votable v;
            if (position == 0) {
                v = mSubmission;
            } else if (position > 0) {
                v = mCommentsList.get(position - HEADER_VIEW_COUNT);
            } else {
                v = null;
            }
            final int offset;
            ArrayList<String> options = new ArrayList<String>();
            if (mAccount != null && mAccount.getUsername().equals(v.getAuthor())) {
                options.add(getResources().getString(R.string.edit));
                options.add(getResources().getString(R.string.delete));
                offset = 0;
            } else {
                offset = 2;
            }
            options.add(getResources().getString(R.string.reply));
            options.add(getResources().getString(R.string.view_profile));
            options.add(getResources().getString(R.string.message_user));
            options.add(getResources().getString(R.string.save));
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String[] array = new String[options.size()];
            options.toArray(array);
            builder.setItems(array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which + offset) {
                        case 0:
                            // Edit
                            break;
                        case 1:
                            // Delete
                            break;
                        case 2:
                            // Reply
                            break;
                        case 3:
                            Bundle b = new Bundle();
                            b.putParcelable("account", mAccount);
                            b.putString("username", v.getAuthor());
                            Log.i("CommentFragment", v.getAuthor());
                            Intent i = new Intent(mContext, UserActivity.class);
                            i.putExtras(b);
                            mContext.startActivity(i);
                            // View user's profile
                            break;
                        case 4:
                            // Message user
                            break;
                        case 5:
                            // Save
                            break;
                    }
                }
            });
            builder.create().show();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mAccount != null) {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                int position = mCommentsListView.pointToPosition((int) e1.getX(), (int) e1.getY());
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Votable v = null;
                    if (position == 0) {
                        v = mSubmission;
                    } else if (position > 0) {
                        v = mCommentAdapter.getItem(position - HEADER_VIEW_COUNT);
                        if (v.getVoteStatus() == Comment.DOWNVOTED) {
                            new VoteAsyncTask(v.getName(), mAccount, VoteAsyncTask.NEUTRAL).execute();
                            v.setVoteStatus(Votable.NEUTRAL);
                        } else {
                            new VoteAsyncTask(v.getName(), mAccount, VoteAsyncTask.DOWNVOTE).execute();
                            v.setVoteStatus(Votable.DOWNVOTED);
                        }
                    }
                    setVoteStatus(v, position);
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Votable v = null;
                    if (position == 0) {
                        v = mSubmission;
                    } else if (position > 0) {
                        v = mCommentAdapter.getItem(position - HEADER_VIEW_COUNT);
                        if (v.getVoteStatus() == Comment.UPVOTED) {
                            new VoteAsyncTask(v.getName(), mAccount, VoteAsyncTask.NEUTRAL).execute();
                            v.setVoteStatus(Votable.NEUTRAL);
                        } else {
                            new VoteAsyncTask(v.getName(), mAccount, VoteAsyncTask.UPVOTE).execute();
                            v.setVoteStatus(Votable.UPVOTED);
                        }
                    }
                    setVoteStatus(v, position);
                }
            }
            return false;
        }

        private void setVoteStatus(Votable votable, int position) {
            if (votable != null) {
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
    }

    private class CommentLinkMovementMethod extends LinkMovementMethod {

        private int mPosition;

        public CommentLinkMovementMethod(int position) {
            mPosition = position;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (super.onTouchEvent(widget, buffer, event)) {
                    mLinkIsPressed = true; // Really horrible hack to prevent the onLongPress()
                                           //    method from being called
                }
                super.onTouchEvent(widget, buffer, event);
                View v = mCommentsListView.getChildAt(mPosition
                        - mCommentsListView.getFirstVisiblePosition());
                event.setLocation(event.getX(), event.getY() + v.getY());
                return mGestureDetector.onTouchEvent(event);
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                boolean ret = super.onTouchEvent(widget, buffer, event);
                View v = mCommentsListView.getChildAt(mPosition
                        - mCommentsListView.getFirstVisiblePosition());
                event.setLocation(event.getX(), event.getY() + v.getY());
                mGestureDetector.onTouchEvent(event);
                return ret;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                View v = mCommentsListView.getChildAt(mPosition
                        - mCommentsListView.getFirstVisiblePosition());
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
