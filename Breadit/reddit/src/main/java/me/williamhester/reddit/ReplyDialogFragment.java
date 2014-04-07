package me.williamhester.reddit;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;
import me.williamhester.areddit.User;
import me.williamhester.areddit.Votable;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/6/14.
 */
public class ReplyDialogFragment extends DialogFragment {

    private Button mConfirm;
    private Button mCancel;
    private Comment mComment;
    private EditText mReplyText;
    private User mUser;
    private String mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable("user");
            mName = getArguments().getString("name");
            mComment = getArguments().getParcelable("comment");
        }
        setStyle(STYLE_NO_TITLE, getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_reply, null);
        if (mComment != null) {
            final View commentView = v.findViewById(R.id.root_comment);
            TextView author = (TextView) commentView.findViewById(R.id.author);
            TextView score = (TextView) commentView.findViewById(R.id.points);
            TextView time = (TextView) commentView.findViewById(R.id.time);
            final TextView body = (TextView) commentView.findViewById(R.id.comment_text);
            View voteStatus = commentView.findViewById(R.id.vote_status);

            author.setText(removeEndQuotes(mComment.getAuthor()));
            score.setText(mComment.getScore() + " points by ");
            time.setText(" " + calculateTimeShort(mComment.getCreatedUtc()));
            body.setText(Html.fromHtml(StringEscapeUtils.unescapeHtml4(mComment.getBodyHtml())));

            switch (mComment.getVoteStatus()) {
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
            commentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (body.getVisibility() == View.GONE) {
                        body.setVisibility(View.VISIBLE);
                    } else if (body.getVisibility() == View.VISIBLE) {
                        body.setVisibility(View.GONE);
                    }
                }
            });
        }
        
        mReplyText = (EditText) v.findViewById(R.id.reply_body);
        mConfirm = (Button) v.findViewById(R.id.confirm_reply);
        mCancel = (Button) v.findViewById(R.id.cancel_reply);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ReplyAsyncTask().execute();
            }
        });
        return v;
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

    public static ReplyDialogFragment newInstance(User user, String fullName, Comment root) {
        Bundle b = new Bundle();
        b.putParcelable("user", user);
        b.putString("name", fullName);
        b.putParcelable("comment", root);
        ReplyDialogFragment rf = new ReplyDialogFragment();
        rf.setArguments(b);
        return rf;
    }

    private class ReplyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (mUser != null && mReplyText != null && mReplyText.getText() != null
                    && mReplyText.getText().toString().length() != 0) {
                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("api-type", "json"));
                apiParams.add(new BasicNameValuePair("text", mReplyText.getText().toString()));
                apiParams.add(new BasicNameValuePair("thing_id", mName));
                Utilities.post(apiParams, "http://www.reddit.com/api/comment", mUser.getCookie(),
                        mUser.getModhash());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            dismiss();
        }
    }

}
