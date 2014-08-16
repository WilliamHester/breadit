package me.williamhester.ui.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.williamhester.models.Account;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 8/1/14.
 */
public abstract class VotableViewHolder extends RecyclerView.ViewHolder {

    // The mBody TextView is protected so that it can be modified by its children.
    protected TextView mBody;
    private Account mAccount;

    private TextView mTime;
    protected TextView mMetadata;
    private View mBackgroundVoteView;
    private View mForegroundVoteView;
    protected SwipeView mSwipeView;
    private Votable mVotable;

    public VotableViewHolder(View itemView, Account account) {
        super(itemView);

        mBody = (TextView) itemView.findViewById(R.id.body);
        mTime = (TextView) itemView.findViewById(R.id.time);
        mBackgroundVoteView = itemView.findViewById(R.id.vote_background);
        mForegroundVoteView = itemView.findViewById(R.id.vote_foreground);
        mMetadata = (TextView) itemView.findViewById(R.id.metadata);
        mSwipeView = (SwipeView) itemView.findViewById(R.id.swipe_view);
        mAccount = account;
        mSwipeView.setUp(mBackgroundVoteView, mForegroundVoteView, mVoteListener);
        mSwipeView.setEnabled(mAccount != null);
    }

    public void setContent(Votable votable) {
        mVotable = votable;

        mSwipeView.recycle(mVotable);
        if (mTime != null) {
            mTime.setText(Utilities.calculateTimeShort(mVotable.getCreatedUtc()));
        }
        setVoteStatus();
    }

    private void setVoteStatus() {
        switch (mVotable.getVoteStatus()) {
            case Votable.DOWNVOTED:
                mForegroundVoteView.setScaleY(0f);
                mBackgroundVoteView.setVisibility(View.VISIBLE);
                mBackgroundVoteView.setBackgroundColor(
                        mBackgroundVoteView.getResources().getColor(R.color.periwinkle));
                break;
            case Votable.UPVOTED:
                mForegroundVoteView.setScaleY(0f);
                mBackgroundVoteView.setVisibility(View.VISIBLE);
                mBackgroundVoteView.setBackgroundColor(
                        mBackgroundVoteView.getResources().getColor(R.color.orangered));
                break;
            default:
                mBackgroundVoteView.setVisibility(View.GONE);
                mForegroundVoteView.setScaleY(0f);
                break;
        }
    }

    private SwipeView.SwipeListener mVoteListener = new SwipeView.SwipeListener() {
        @Override
        public void onRightToLeftSwipe() {
            mVotable.setVoteStatus(mVotable.getVoteStatus() == Votable.DOWNVOTED ? Votable.NEUTRAL : Votable.DOWNVOTED);
            RedditApi.vote(itemView.getContext(), mVotable, mAccount);
        }

        @Override
        public void onLeftToRightSwipe() {
            mVotable.setVoteStatus(mVotable.getVoteStatus() == Votable.UPVOTED ? Votable.NEUTRAL : Votable.UPVOTED);
            RedditApi.vote(itemView.getContext(), mVotable, mAccount);
        }
    };
}
