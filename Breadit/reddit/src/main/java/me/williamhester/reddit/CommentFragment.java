package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

            root.setPadding((int) (getResources().getDisplayMetrics().density
                    * 16 * getItem(position).getLevel()), 0, 0, 0);
            author.setText(removeEndQuotes(getItem(position).getAuthor()));
            score.setText(getItem(position).getScore() + "");
            time.setText(calculateTime(getItem(position).getCreatedUtc(), System.currentTimeMillis() / 1000));
            body.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(getItem(position).getBodyHtml())));

            return convertView;
        }

        private String removeEndQuotes(String s) {
            if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }

        private String removeEscapeSequences(String s) {
            return s.replace("\\n", "\n");
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

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        private String mName;

        public SwipeListener(String name) {
            mName = name;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getActivity(), "Right to Left swipe", Toast.LENGTH_SHORT).show();
                return false; // Right to left
            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Toast.makeText(getActivity(), "Left to right swipe", Toast.LENGTH_SHORT).show();
                return false; // Left to right
            }
            return false;
        }
    }

}
