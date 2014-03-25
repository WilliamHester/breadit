package me.williamhester.reddit;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.williamhester.areddit.Comment;

/**
 * Created by William on 1/4/14.
 */
public class CommentView extends LinearLayout {

    private Comment mComment;
    private Context mContext;
    private LinearLayout mInfo;
    private LinearLayout mReplies;
    private TextView mAuthor;
    private TextView mScore;
    private TextView mTime;
    private TextView mCommentText;

    public CommentView(Context context, Comment comment) {
        super(context);
        mContext = context;
        mComment = comment;

        setOrientation(VERTICAL);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        mInfo = new LinearLayout(mContext);
        addView(mInfo, params);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1);
        mAuthor = new TextView(mContext);
        mInfo.addView(mAuthor, infoParams);
        mScore = new TextView(mContext);
        mInfo.addView(mScore, infoParams);
        mTime = new TextView(mContext);
        mInfo.addView(mTime, infoParams);

        mCommentText = new TextView(mContext);
        addView(mCommentText, params);
        mReplies = new LinearLayout(mContext);
        addView(mReplies, params);

        try {
            mAuthor.setText(mComment.getAuthor());
            mScore.setText(mComment.getScore() + "");
            mTime.setText(calculateTime(mComment.getCreated(), (double) System.currentTimeMillis() / 1000));
            mCommentText.setText(mComment.getBody());

            if (mComment.getReplies() != null)
                for (Comment reply : mComment.getReplies()) {
                    CommentView cv = new CommentView(mContext, reply);
                    mReplies.addView(cv, params);
                }
        } catch (NullPointerException e) {
            Log.i("CommentView", "Caught a NullPointerException");
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
