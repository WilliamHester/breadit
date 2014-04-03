package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;
import me.williamhester.areddit.User;

/**
 * Created by William on 3/31/14.
 */
public class CommentFragment extends Fragment {

    private boolean mIsSelf;

    private Context mContext;
    private CommentArrayAdapter mCommentAdapter;
    private ListView mCommentsListView;
    private ArrayList<Comment> mCommentsList = new ArrayList<Comment>();
    private String mUrl;
    private String mPermalink;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mContext = getActivity();
        if (args != null) {
            mUrl = args.getString("url", null);
            mPermalink = args.getString("permalink", null);
            mIsSelf = args.getBoolean("isSelf", false);
            mUser = args.getParcelable("user");
        }
        mCommentsList = new ArrayList<Comment>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comment, null);
        mCommentsListView = (ListView) v.findViewById(R.id.comments);
        mCommentAdapter = new CommentArrayAdapter(mContext);
        mCommentsListView.setAdapter(mCommentAdapter);
//        final SwipeDetector swipeDetector = new SwipeDetector();
//        mCommentsListView.setOnTouchListener(swipeDetector);
        mCommentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.i("CommentFragment", "Entered onItemClick");
//                if (swipeDetector.swipeDetected()) {
//                    Log.i("CommentFragment", "Swipe Detected");
//                    Comment c = mCommentsList.get(position);
//                    View voteStatus = view.findViewById(R.id.vote_status);
//                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
//                        if (mCommentsList.get(position).getVoteStatus() == Comment.DOWNVOTED) {
//                            new VoteAsyncTask(c.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
//                            c.setVoteStatus(Comment.NEUTRAL);
//                        } else {
//                            new VoteAsyncTask(c.getName(), mUser, VoteAsyncTask.DOWNVOTE).execute();
//                            c.setVoteStatus(Comment.DOWNVOTED);
//                        }
//                    } else if (swipeDetector.getAction() == SwipeDetector.Action.LR) {
//                        if (mCommentsList.get(position).getVoteStatus() == Comment.UPVOTED) {
//                            new VoteAsyncTask(c.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
//                            c.setVoteStatus(Comment.NEUTRAL);
//                        } else {
//                            new VoteAsyncTask(c.getName(), mUser, VoteAsyncTask.UPVOTE).execute();
//                            c.setVoteStatus(Comment.UPVOTED);
//                        }
//                    }
//                    switch (c.getVoteStatus()) {
//                        case Comment.DOWNVOTED:
//                            voteStatus.setVisibility(View.VISIBLE);
//                            voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
//                            break;
//                        case Comment.UPVOTED:
//                            voteStatus.setVisibility(View.VISIBLE);
//                            voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
//                            break;
//                        default:
//                            voteStatus.setVisibility(View.GONE);
//                            break;
//                    }
//                }
            }
        });
        new CommentLoaderTask().execute();
        return v;
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
            TextView score = (TextView) convertView.findViewById(R.id.score);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView body = (TextView) convertView.findViewById(R.id.comment_text);
            View voteStatus = convertView.findViewById(R.id.vote_status);

            root.setPadding((int) (getResources().getDisplayMetrics().density
                    * 16 * getItem(position).getLevel()), 0, 0, 0);
            author.setText(removeEndQuotes(getItem(position).getAuthor()));
            score.setText(getItem(position).getScore() + " points by ");
            time.setText(" " + calculateTimeShort(getItem(position).getCreatedUtc()));
            body.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(getItem(position).getBodyHtml())));
            body.setMovementMethod(LinkMovementMethod.getInstance());

            switch (getItem(position).getVoteStatus()) {
                case Comment.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    break;
                case Comment.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    break;
            }


            return convertView;
        }

        private String removeEndQuotes(String s) {
            if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
                return s.substring(1, s.length() - 1);
            }
            return s;
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

        private String calculateTime(long postTime, long currentTime) {
            long difference = currentTime - postTime;
            String time;
            if (difference / 31536000 > 0) {
                if (difference / 3156000 == 1)
                    time = "1 Year Ago";
                else
                    time = difference / 3156000 + " Years Ago";
            } else if (difference / 2592000 > 0) {
                if (difference / 2592000 == 1)
                    time = "1 Month Ago";
                else
                    time = difference / 2592000 + " Months Ago";
            } else if (difference / 604800 > 0) {
                if (difference / 604800 == 1)
                    time = "1 Week Ago";
                else
                    time = difference / 604800 + " Weeks Ago";
            } else if (difference / 86400 > 0) {
                if (difference / 86400 == 1)
                    time = "1 Day Ago";
                else
                    time = difference / 86400 + " Day Ago";
            } else if (difference / 3600 > 0) {
                if (difference / 3600 == 1)
                    time = "1 Hour Ago";
                else
                    time = difference / 3600 + " Hours Ago";
            } else if (difference / 60 > 0) {
                if (difference / 60 == 1)
                    time = "1 Minute Ago";
                else
                    time = difference / 60 + " Minutes Ago";
            } else {
                if (difference == 1)
                    time = "1 Second Ago";
                else
                    time = difference + " Seconds Ago";
            }
            return time;
        }
    }

    private class CommentLoaderTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... params) {
            try {
                String lastComment = null;
                if (mCommentsList.size() > 0)
                    lastComment = mCommentsList.get(mCommentsList.size() - 1).getName();
                List<Comment> comments = Comment.getComments(mPermalink, mUser, lastComment);
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
            }
        }
    }

}
