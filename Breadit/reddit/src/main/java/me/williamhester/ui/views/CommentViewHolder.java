package me.williamhester.ui.views;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import org.apache.commons.lang3.StringEscapeUtils;

import me.williamhester.models.Account;
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

    public CommentViewHolder(View itemView, Account account, CommentClickCallbacks callbacks) {
        super(itemView, account);
        mCallback = callbacks;
        mBody.setMovementMethod(new LinkMovementMethod());
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
                mComment.setBodyHtml(null); // Get rid of the old html, as it's useless now.
                mComment.setBody(null);
            }
            mBody.setText(mComment.getSpannableBody());
            if (isHidden()) {
                mBody.setVisibility(View.GONE);
            } else {
                mBody.setVisibility(View.VISIBLE);
            }

            mMetadata.setVisibility(View.VISIBLE);
            mMetadata.setText(mComment.getAuthor() + " " + mComment.getScore() + " "
                    + itemView.getResources().getQuantityString(R.plurals.points, mComment.getScore()));
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
