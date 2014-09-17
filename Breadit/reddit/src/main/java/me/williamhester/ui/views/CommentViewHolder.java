package me.williamhester.ui.views;

import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

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
    private View mContent;
    private TextView mAuthor;
    private View mLevelIndicator;

    public CommentViewHolder(View itemView, CommentClickCallbacks callbacks, String submissionAuthor) {
        super(itemView);
        mCallback = callbacks;
        mBody.setMovementMethod(new LinkMovementMethod());
        mSubmissionAuthor = submissionAuthor;
        mContent = itemView.findViewById(R.id.comment_content);
        mAuthor = (TextView) itemView.findViewById(R.id.author);
        mLevelIndicator = itemView.findViewById(R.id.level_indicator);
    }

    @Override
    public void setContent(Votable votable) {
        super.setContent(votable);
        mComment = (Comment) votable;
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
            mLevelIndicator.setVisibility(View.GONE);
        }
        if (votable instanceof MoreComments) {
            mContent.setOnClickListener(mMoreClickListener);
            mMetadata.setVisibility(View.GONE);
            mAuthor.setVisibility(View.GONE);
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("Load more comments...");
            sb.setSpan(new ForegroundColorSpan(itemView.getResources().getColor(R.color.auburn_blue)),
                    0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBody.setText(sb);
        } else {
            mMetadata.setVisibility(View.VISIBLE);
            mAuthor.setVisibility(View.VISIBLE);
            mContent.setOnClickListener(mHideCommentsClickListener);

            if (mComment.getSpannableBody() == null && mComment.getBodyHtml() != null) {
                HtmlParser parser = new HtmlParser(Html.fromHtml(mComment.getBodyHtml()).toString());
                mComment.setSpannableBody(parser.getSpannableString());
            }
            mBody.setText(mComment.getSpannableBody());
            if (isHidden()) {
                mBody.setVisibility(View.GONE);
            } else {
                mBody.setVisibility(View.VISIBLE);
            }

            if (mComment.getAuthor().equals(mSubmissionAuthor)) {
                mAuthor.setBackgroundResource(R.drawable.author_background);
                mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
            } else {
                mAuthor.setBackground(null);
                mAuthor.setTextColor(itemView.getResources().getColor(R.color.comment_metadata_gray));
            }
            mAuthor.setText(mComment.getAuthor());
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(mComment.getScore()))
                    .append(" ")
                    .append(itemView.getResources().getQuantityString(R.plurals.points, mComment.getScore()));
            mMetadata.setText(sb);
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

    private static class CommentLinkMovementMethod extends LinkMovementMethod {

    }

    public interface CommentClickCallbacks {
        public void onMoreClick(CommentViewHolder viewHolder, Comment comment);
        public void onHideClick(Comment comment);
    }
}
