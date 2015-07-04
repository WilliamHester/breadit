package me.williamhester.ui.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.williamhester.models.Votable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * This is an abstraction of the ViewHolder that is needed for both the Comment and Submission
 * objects. It sets up the SwipeView and prepares some of the other Views so that others can
 * use them.
 *
 * Created by william on 8/1/14.
 */
public abstract class VotableViewHolder extends RecyclerView.ViewHolder {

    // The mBody TextView is protected so that it can be modified by its children.
    protected TextView mBody;

    private TextView mTime;
    protected TextView mMetadata;
    private View mBackgroundVoteView;
    private View mForegroundVoteView;
    protected SwipeView mSwipeView;
    private Votable mVotable;

    public VotableViewHolder(final View itemView) {
        super(itemView);

        mBody = (TextView) itemView.findViewById(R.id.markdown_body);
        mTime = (TextView) itemView.findViewById(R.id.time);
        mBackgroundVoteView = itemView.findViewById(R.id.vote_background);
        mForegroundVoteView = itemView.findViewById(R.id.vote_foreground);
        mMetadata = (TextView) itemView.findViewById(R.id.metadata);
        mSwipeView = (SwipeView) itemView.findViewById(R.id.swipe_view);
        mSwipeView.setUp(mBackgroundVoteView, mForegroundVoteView, new SwipeView.SwipeListener() {
            @Override
            public void onRightToLeftSwipe() {
                mVotable.setVoteStatus(mVotable.getVoteStatus() == Votable.DOWNVOTED ? Votable.NEUTRAL : Votable.DOWNVOTED);
                RedditApi.vote(itemView.getContext(), mVotable);
                onVoted();
            }

            @Override
            public void onLeftToRightSwipe() {
                mVotable.setVoteStatus(mVotable.getVoteStatus() == Votable.UPVOTED ? Votable.NEUTRAL : Votable.UPVOTED);
                RedditApi.vote(itemView.getContext(), mVotable);
                onVoted();
            }
        });
    }

    /**
     * Called by the SwipeListener to communicate to the subclass that the votable was just voted on
     */
    protected abstract void onVoted();

    public abstract void expandOptions();

    public abstract void collapseOptions();

    public void setContent(Object object) {
        if (object instanceof Votable) {
            mVotable = (Votable) object;
            mSwipeView.recycle(mVotable);
            if (mTime != null) {
                mTime.setText(calculateTimeShort(mVotable.getCreatedUtc()));
            }
            setVoteStatus();
        } else {
            mSwipeView.setEnabled(false);
        }
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

    public static void expand(final View v) {
        v.measure(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? FrameLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) Math.min(targetHeight
                / v.getContext().getResources().getDisplayMetrics().density, 300));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) Math.min(initialHeight
                / v.getContext().getResources().getDisplayMetrics().density, 300));
        v.startAnimation(a);
    }

    protected static String calculateTimeShort(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = Math.max(currentTime - postTime, 0);
        String time;
        if (difference / 31536000 > 0) {
            time = difference / 31536000 + "y";
        } else if (difference / 2592000 > 0) {
            time = difference / 2592000 + "mo";
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

}
