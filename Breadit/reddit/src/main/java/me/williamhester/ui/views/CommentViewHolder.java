package me.williamhester.ui.views;

import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import me.williamhester.models.AbsComment;
import me.williamhester.models.Comment;
import me.williamhester.models.MoreComments;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {
    private AbsComment mComment;
    private CommentClickCallbacks mCallback;
    private String mSubmissionAuthor;
    private View mContent;
    private TextView mAuthor;
    private View mLevelIndicator;
    private View mGoldIndicator;

    public CommentViewHolder(View itemView, CommentClickCallbacks callbacks, String submissionAuthor) {
        super(itemView);
        mCallback = callbacks;
        mBody.setMovementMethod(new CommentLinkMovementMethod());
        mSubmissionAuthor = submissionAuthor;
        mContent = itemView.findViewById(R.id.comment_content);
        mAuthor = (TextView) itemView.findViewById(R.id.author);
        mLevelIndicator = itemView.findViewById(R.id.level_indicator);
        mGoldIndicator = itemView.findViewById(R.id.gold_indicator);
    }

    @Override
    public void setContent(Object comment) {
        super.setContent(comment);
        mComment = (AbsComment) comment;
        float dp = itemView.getResources().getDisplayMetrics().density;
        itemView.setPadding(Math.round(4 * dp * mComment.getLevel()), 0, 0, 0);
        if (mComment.getLevel() > 0) {
            mLevelIndicator.setVisibility(View.VISIBLE);
            switch (mComment.getLevel() % 4) {
                case 1:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.green));
                    break;
                case 2:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.cyan));
                    break;
                case 3:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.blue));
                    break;
                case 0:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.pink));
                    break;
            }
        } else {
            mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.card_view_gray));
        }
        if (mComment instanceof MoreComments) {
            mContent.setOnClickListener(mMoreClickListener);
            mMetadata.setVisibility(View.GONE);
            mAuthor.setVisibility(View.GONE);
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("Load more comments...");
            sb.setSpan(new ForegroundColorSpan(itemView.getResources().getColor(R.color.light_blue)),
                    0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBody.setText(sb);
            mGoldIndicator.setVisibility(View.GONE);
        } else {
            Comment comment1 = (Comment) mComment;
            mMetadata.setVisibility(View.VISIBLE);
            mAuthor.setVisibility(View.VISIBLE);
            mContent.setOnClickListener(mHideCommentsClickListener);

            if (comment1.getSpannableBody() == null && comment1.getBodyHtml() != null) {
                HtmlParser parser = new HtmlParser(Html.fromHtml(comment1.getBodyHtml()).toString());
                comment1.setSpannableBody(parser.getSpannableString());
            }
            mBody.setText(comment1.getSpannableBody());
            if (isHidden()) {
                mBody.setVisibility(View.GONE);
            } else {
                mBody.setVisibility(View.VISIBLE);
            }

            mGoldIndicator.setVisibility(comment1.isGilded() ? View.VISIBLE : View.INVISIBLE);

            if (comment1.getAuthor().equals(mSubmissionAuthor)) {
                mAuthor.setBackgroundResource(R.drawable.author_background);
                mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
            } else {
                mAuthor.setBackground(null);
                mAuthor.setTextColor(itemView.getResources().getColor(R.color.comment_metadata_gray));
            }
            mAuthor.setText(comment1.getAuthor());
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(comment1.getScore()))
                    .append(" ")
                    .append(itemView.getResources().getQuantityString(R.plurals.points, comment1.getScore()));
            mMetadata.setText(sb);
        }
    }

    public boolean isHidden() {
        return mComment instanceof Comment && ((Comment) mComment).isHidden();
    }

    private View.OnClickListener mMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onMoreClick(CommentViewHolder.this, (MoreComments) mComment);
        }
    };

    private View.OnClickListener mHideCommentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onHideClick((Comment) mComment);
            mBody.setVisibility(View.GONE);
        }
    };

    private static class CommentLinkMovementMethod extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                } else {
                    widget.getParent().requestDisallowInterceptTouchEvent(false);
                    ((View) widget.getParent()).onTouchEvent(event);
                }
            } else {
                ((View) widget.getParent()).onTouchEvent(event);
                Selection.removeSelection(buffer);
            }
            return false;
        }
    }

    public interface CommentClickCallbacks {
        public void onMoreClick(CommentViewHolder viewHolder, MoreComments comment);
        public void onHideClick(Comment comment);
    }
}
