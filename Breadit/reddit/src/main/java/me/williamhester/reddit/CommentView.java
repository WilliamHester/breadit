package me.williamhester.reddit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;

public class CommentView extends LinearLayout {

    private boolean mHidden = false;

    private Comment mComment;
    private Context mContext;
    private LinearLayout mReplies;
    private List<CommentView> mChildren;
    private TextView mAuthor;
    private TextView mScore;
    private TextView mTime;
    private TextView mCommentText;

    public CommentView(Context context, Comment comment) {
        super(context);
        mChildren = new ArrayList<CommentView>();
        mContext = context;
        mComment = comment;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.view_comment, null);
        mAuthor = (TextView) v.findViewById(R.id.author);
        mScore = (TextView) v.findViewById(R.id.score);
        mTime = (TextView) v.findViewById(R.id.time);
        mCommentText = (TextView) v.findViewById(R.id.comment_text);
        mReplies = (LinearLayout) v.findViewById(R.id.replies);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        try {
            mAuthor.setText(removeEndQuotes(mComment.getAuthor()));
            mScore.setText(mComment.getScore() + "");
            mTime.setText(calculateTime(mComment.getCreated(), System.currentTimeMillis() / 1000));
            mCommentText.setText(removeEscapeSequences(removeEndQuotes(mComment.getBody())));

            if (mComment.getReplies() != null)
                for (Comment reply : mComment.getReplies()) {
                    CommentView cv = new CommentView(mContext, reply);
                    mReplies.addView(cv, params);
                    mChildren.add(cv);
                }
            else
                mReplies.addView(new View(context),
                        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                (int) getResources().getDisplayMetrics().density * 8));
        } catch (NullPointerException e) {
            Log.i("CommentView", "Caught a NullPointerException");
        }
        addView(v, params);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHidden) {
                    mCommentText.setVisibility(VISIBLE);
                    if (mComment.getReplies() != null) {
                        for (CommentView c : mChildren) {
                            c.setVisibility(VISIBLE);
                        }
                    }
                    mHidden = false;
                } else {
                    mCommentText.setVisibility(GONE);
                    if (mComment.getReplies() != null) {
                        for (CommentView c : mChildren) {
                            c.setVisibility(GONE);
                        }
                    }
                    mHidden = true;
                }
            }
        });
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

    private String removeEndQuotes(String s) {
        if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String removeEscapeSequences(String s) {
        return s.replace("\\", "");
    }

}
