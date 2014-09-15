package me.williamhester.ui.views;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import org.apache.commons.lang3.StringEscapeUtils;

import me.williamhester.models.Comment;
import me.williamhester.models.MoreComments;
import me.williamhester.models.Votable;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {
    private Comment mComment;
    private CommentClickCallbacks mCallback;
    private String mSubmissionAuthor;

    public CommentViewHolder(View itemView, CommentClickCallbacks callbacks, String submissionAuthor) {
        super(itemView);
        mCallback = callbacks;
        mBody.setMovementMethod(new LinkMovementMethod());
        mSubmissionAuthor = submissionAuthor;
    }

    @Override
    public void setContent(Votable votable) {
        super.setContent(votable);
        mComment = (Comment) votable;
        float dp = itemView.getResources().getDisplayMetrics().density;
        itemView.setPadding(Math.round(4 * dp * mComment.getLevel()), 0, 0, 0);
        if (votable instanceof MoreComments) {
            itemView.findViewById(R.id.comment_content).setOnClickListener(mMoreClickListener);
            mMetadata.setVisibility(View.GONE);
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("Load more comments...");
            sb.setSpan(new ForegroundColorSpan(itemView.getResources().getColor(R.color.auburn_blue)),
                    0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBody.setText(sb);
        } else {
            itemView.findViewById(R.id.comment_content).setOnClickListener(mHideCommentsClickListener);

            if (mComment.getSpannableBody() == null && mComment.getBodyHtml() != null) {
                HtmlParser parser = new HtmlParser(StringEscapeUtils.unescapeHtml4(mComment.getBodyHtml()));
                mComment.setSpannableBody(parser.getSpannableString());
            }
            mBody.setText(mComment.getSpannableBody());
            if (isHidden()) {
                mBody.setVisibility(View.GONE);
            } else {
                mBody.setVisibility(View.VISIBLE);
            }

            mMetadata.setVisibility(View.VISIBLE);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(mComment.getAuthor());
            if (mComment.getAuthor().equals(mSubmissionAuthor)) {
                ssb.setSpan(new ForegroundColorSpan(Color.WHITE),
                        0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new BackgroundColorSpan(itemView.getResources().getColor(R.color.op)),
                        0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.append(" ")
                    .append(String.valueOf(mComment.getScore()))
                    .append(" ")
                    .append(itemView.getResources().getQuantityString(R.plurals.points, mComment.getScore()));
            mMetadata.setText(ssb);
        }
    }

    public boolean isHidden() {
        return mComment.isHidden();
    }

    private View.OnClickListener mMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onMoreClick(CommentViewHolder.this, mComment);
        }
    };

    private View.OnClickListener mHideCommentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onHideClick(mComment);
            mBody.setVisibility(View.GONE);
        }
    };

    public interface CommentClickCallbacks {
        public void onMoreClick(CommentViewHolder viewHolder, Comment comment);
        public void onHideClick(Comment comment);
    }
}
