package me.williamhester.ui.views;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;

import java.text.DecimalFormat;

import me.williamhester.models.Comment;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * This class serves to allow the separation of the Comment from its adapters
 *
 * Created by William on 6/10/14.
 */
public class CommentView extends RelativeLayout {

    private Comment mComment;
    private View mVoteIndicator;
    private TextView mCommentMetadata;
    private TextView mBody;
    private LinearLayout mEditedText;
    private EditText mReplyBody;
    private Button mCancel;
    private Button mConfirm;
    private OnCommentMotionEventListener mEventListener;
    private GestureDetector mGestureDetector;

    private CommentView mThis = this;

    public CommentView(Context context, Comment comment) {
        super(context);
        mComment = comment;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_item_comment, this);
        setupViews();
        loadData();

        mGestureDetector = new GestureDetector(context, new SwipeDetector());

        setOnTouchListener(mTouchListener);
    }

    public void setComment(Comment comment) {
        mComment = comment;
        loadData();
    }

    private void setupViews() {
        mVoteIndicator = findViewById(R.id.vote_status);
        mCommentMetadata = (TextView) findViewById(R.id.comment_metadata);
        mBody = (TextView) findViewById(R.id.body);
        mEditedText = (LinearLayout) findViewById(R.id.edited_text);
        mReplyBody = (EditText) findViewById(R.id.reply_body);
        mCancel = (Button) findViewById(R.id.cancel_reply);
        mConfirm = (Button) findViewById(R.id.confirm_reply);
    }

    private void loadData() {
        float dp = getResources().getDisplayMetrics().density;

        setPadding(Math.round(6 * dp * mComment.getLevel()), 0, 0, 0);

        if (mComment.isBeingEdited()) {
            mConfirm.setEnabled(true);
            mCancel.setEnabled(true);
            mReplyBody.setEnabled(true);
            mBody.setVisibility(View.GONE);
            mEditedText.setVisibility(View.VISIBLE);
//            mConfirm.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (position == 1)
//                        new ReplyAsyncTask(replyBody.getText().toString(),
//                                mSubmission.getName()).execute();
//                    else if (position > 1)
//                        new ReplyAsyncTask(replyBody.getText().toString(),
//                                getItem(position - 1).getName()).execute();
//                    confirm.setEnabled(false);
//                    cancel.setEnabled(false);
//                    replyBody.setEnabled(false);
//                }
//            });
//            mCancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    mCommentsList.remove(position);
//                    mCommentAdapter.notifyDataSetChanged();
//                }
//            });
        } else {
            mEditedText.setVisibility(View.GONE);
            mBody.setVisibility(View.VISIBLE);
        }

        setMetadata();

        switch (mComment.getVoteStatus()) {
            case Votable.DOWNVOTED:
                mVoteIndicator.setVisibility(View.VISIBLE);
                mVoteIndicator.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                break;
            case Votable.UPVOTED:
                mVoteIndicator.setVisibility(View.VISIBLE);
                mVoteIndicator.setBackgroundColor(getResources().getColor(R.color.orangered));
                break;
            default:
                mVoteIndicator.setVisibility(View.GONE);
                break;
        }

        if (mComment.isHidden()) {
            mBody.setVisibility(View.GONE);
        } else {
            mBody.setVisibility(View.VISIBLE);
        }

        String bodyText = formatHtmlForMeta(mComment.getBodyHtml());
        SpannableStringBuilder text = HtmlParser.parseHtml(StringEscapeUtils.unescapeHtml4(bodyText));
        mBody.setText(text);
        mBody.setMovementMethod(new LinkMovementMethod());
    }

    private void setMetadata() {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString author = new SpannableString(mComment.getAuthor());
        if (getTag() instanceof Boolean && (Boolean) getTag()) {
            author.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.op)), 0,
                    author.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        ssb.append(author);
        ssb.append(" ");

        DecimalFormat format = new DecimalFormat("###,###,###");
        ssb.append(format.format(mComment.getScore()));

        if (mComment.getScore() != 1) {
            ssb.append(" points ");
        } else {
            ssb.append(" point ");
        }

        ssb.append(Utilities.calculateTimeShort(mComment.getCreatedUtc()));

        mCommentMetadata.setText(ssb.toString());
    }

    public void setOnCommentMotionEventListener(OnCommentMotionEventListener listener) {
        mEventListener = listener;
    }

    /**
     * A somewhat hackish way to stop the app from crashing and opening the link properly
     * @param html the string of html
     * @return a formatted string
     */
    private String formatHtmlForMeta(String html) {
        if (html.contains("href=\"")) {
            html = html.replace("&lt;a href=\"/r/", "&lt;a href=\"me.williamhester.breadit://subreddit/");
            html = html.replace("&lt;a href=\"/u/", "&lt;a href=\"me.williamhester.breadit://user/");
            html = html.replace("&lt;a href=\"http://www.reddit.com/u/", "&lt;a href=\"me.williamhester.breadit://user/");
            html = html.replace("&lt;a href=\"http://reddit.com/u/", "&lt;a href=\"me.williamhester.breadit://user/");
            if (html.contains("reddit.com/r/")) {
                return Utilities.formatHtml(html);
            }
            return html;
        }
        return html;
    }

    public void hide() {
        mComment.setHidden(true);
        mBody.setVisibility(GONE);
    }

    public void show() {
        mComment.setHidden(false);
        mBody.setVisibility(VISIBLE);
    }

    public boolean isHidden() {
        return mComment.isHidden();
    }

    public Comment getComment() {
        return mComment;
    }

    public void setVoteStatus(int status) {
        mComment.setVoteStatus(status);
        Log.d("CommentView", "Setting vote status " + status);
        switch (mComment.getVoteStatus()) {
            case Votable.UPVOTED:
                mVoteIndicator.setVisibility(View.VISIBLE);
                mVoteIndicator.setBackgroundColor(getResources().getColor(R.color.orangered));
                break;
            case Votable.DOWNVOTED:
                mVoteIndicator.setVisibility(View.VISIBLE);
                mVoteIndicator.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                break;
            default:
                mVoteIndicator.setVisibility(View.GONE);
                break;
        }
        setMetadata();
    }

    public interface OnCommentMotionEventListener {
        public void onSingleTap(CommentView commentView);
        public void onLongPress(CommentView commentView);
        public void onDoubleTap(CommentView commentView);
        public boolean onUpVote(CommentView commentView);
        public boolean onDownVote(CommentView commentView);
        public boolean onNeutralVote(CommentView commentView);
    }

    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private static class CommentLinkMovementMethod extends LinkMovementMethod {

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
//                if (super.onTouchEvent(widget, buffer, event)) {
//                    mLinkIsPressed = true; // Really horrible hack to prevent the onLongPress()
//                    //    method from being called
//                }
//                View v = mCommentsListView.getChildAt(mPosition
//                        - mCommentsListView.getFirstVisiblePosition());
//                if (v != null)
//                    event.setLocation(event.getX(), event.getY() + v.getY());
//                return mGestureDetector.onTouchEvent(event);
//            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                boolean ret = super.onTouchEvent(widget, buffer, event);
//                View v = mCommentsListView.getChildAt(mPosition
//                        - mCommentsListView.getFirstVisiblePosition());
//                if (v != null)
//                    event.setLocation(event.getX(), event.getY() + v.getY());
//                mGestureDetector.onTouchEvent(event);
//                return ret;
//            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                View v = mCommentsListView.getChildAt(mPosition
//                        - mCommentsListView.getFirstVisiblePosition());
//                if (v != null)
//                    event.setLocation(event.getX(), event.getY() + v.getY());
//                mGestureDetector.onTouchEvent(event);
            }
            return false;
        }
    }

    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 170;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev) {
            if (mEventListener != null) {
                mEventListener.onSingleTap(mThis);
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            if (mEventListener != null) {
                mEventListener.onLongPress(mThis);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent ev) {
            if (mEventListener != null) {
                mEventListener.onDoubleTap(mThis);
                return true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent ev1, MotionEvent ev2, float velocityX, float velocityY) {
            if (mEventListener != null
                    && Math.abs(ev1.getX() - ev2.getX()) > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
                    && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (ev1.getX() < ev2.getX()) {
                    Log.d("CommentView", "Right swipe");
                    if (mComment.getVoteStatus() == Votable.UPVOTED) {
                        if (mEventListener.onNeutralVote(mThis)) {
                            setVoteStatus(Votable.NEUTRAL);
                        }
                    } else {
                        if (mEventListener.onUpVote(mThis)) {
                            setVoteStatus(Votable.UPVOTED);
                        }
                    }
                } else {
                    Log.d("CommentView", "Left swipe");
                    if (mComment.getVoteStatus() == Votable.DOWNVOTED) {
                        if (mEventListener.onNeutralVote(mThis)) {
                            setVoteStatus(Votable.NEUTRAL);
                        }
                    } else {
                        if (mEventListener.onDownVote(mThis)) {
                            setVoteStatus(Votable.DOWNVOTED);
                        }
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }
    }
}
