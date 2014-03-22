package me.williamhester.reddit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.williamhester.areddit.Comment;

/**
 * Created by William on 1/4/14.
 */
public class CommentView extends View {

    private Comment mComment;
    private Context mContext;
    private LinearLayout mReplies;
    private LinearLayout mRootView;
    private TextView mUser;
    private TextView mScore;
    private TextView mTime;
    private TextView mCommentText;

    public CommentView(Context context, Comment comment) {
        super(context);
        mContext = context;
        mComment = comment;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.view_comment, mRootView);
        mRootView = (LinearLayout) v.findViewById(R.id.root);
        mUser = (TextView) v.findViewById(R.id.user);
        mScore = (TextView) v.findViewById(R.id.score);
        mTime = (TextView) v.findViewById(R.id.time);
        mCommentText = (TextView) v.findViewById(R.id.comment_text);
        mReplies = (LinearLayout) v.findViewById(R.id.replies);

        if (mComment == null)
            Log.e("BreaditDebug", "You're about to get an NPE");

        mUser.setText(mComment.getAuthor());
        mScore.setText(mComment.getScore() + "");
        mTime.setText(calculateTime(mComment.getCreated(), (double) System.currentTimeMillis() / 1000));
        mCommentText.setText(mComment.getBody());

        for (Comment reply : mComment.getReplies()) {
            CommentView cv = new CommentView(mContext, reply);
            mReplies.addView(cv);
        }
        Log.d("BreaditDebug", "finished inflation from constructor");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRootView = (LinearLayout) findViewById(R.id.root);
        mUser = (TextView) findViewById(R.id.user);
        mScore = (TextView) findViewById(R.id.score);
        mTime = (TextView) findViewById(R.id.time);
        mCommentText = (TextView) findViewById(R.id.comment_text);
        mReplies = (LinearLayout) findViewById(R.id.replies);

        if (mComment == null)
            Log.e("BreaditDebug", "You're about to get an NPE");

        mUser.setText(mComment.getAuthor());
        mScore.setText(mComment.getScore() + "");
        mTime.setText(calculateTime(mComment.getCreated(), (double) System.currentTimeMillis() / 1000));
        mCommentText.setText(mComment.getBody());

        for (Comment reply : mComment.getReplies()) {
            CommentView cv = new CommentView(mContext, reply);
            mReplies.addView(cv);
        }
    }

    private String calculateTime(double postTime, double currentTime) {
        double difference = currentTime - postTime;
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
