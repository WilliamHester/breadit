package me.williamhester.ui.views;

import android.view.View;
import android.widget.TextView;

import me.williamhester.models.Comment;
import me.williamhester.reddit.R;

/**
 * Created by william on 12/30/14.
 */
public class SubmissionCommentViewHolder extends CommentViewHolder {

    private TextView mLinkTitle;
    private TextView mLinkMetaData;
    private TextView mSubreddit;

    public SubmissionCommentViewHolder(View itemView, final SubmissionCommentCallbacks callbacks) {
        super(itemView, callbacks);

        mLinkTitle = (TextView) itemView.findViewById(R.id.link_title);
        mLinkMetaData = (TextView) itemView.findViewById(R.id.link_metadata);
        mSubreddit = (TextView) itemView.findViewById(R.id.link_subreddit);

        itemView.findViewById(R.id.submission_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onLinkClicked(mComment);
            }
        });
    }

    @Override
    public void setContent(Object comment) {
        super.setContent(comment);

        mSubreddit.setText(mComment.getSubreddit());
        mLinkTitle.setText(mComment.getLinkTitle());
        mLinkMetaData.setText("by " + mComment.getLinkAuthor());
    }

    public interface SubmissionCommentCallbacks extends CommentCallbacks {
        public void onLinkClicked(Comment comment);
    }
}
