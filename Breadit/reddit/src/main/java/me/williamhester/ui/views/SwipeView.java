package me.williamhester.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import me.williamhester.models.Account;
import me.williamhester.models.Votable;
import me.williamhester.reddit.R;

/**
 * Created by william on 7/25/14.
 */
public class SwipeView extends LinearLayout {

    private int mTouchSlop;
    private int mMinFlingVelocity;
    private long mAnimTime;
    private float mDownRawX;
    private float mDownRawY;
    private float mDownX;
    private boolean mSwiping;
    private boolean mEnabled;

    private Account mAccount;
    private SwipeListener mSwipeListener;
    private VelocityTracker mVelocityTracker;
    private View mBackgroundView;
    private View mForegroundView;
    private Votable mVotable;


    public SwipeView(Context context) {
        this(context, null, 0, 0);
    }

    public SwipeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwipeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }

    public void setVoteViews(View backgroundView, View foregroundView) {
        mBackgroundView = backgroundView;
        mForegroundView = foregroundView;
    }

    public void setVotable(Votable votable) {
        mVotable = votable;
    }

    public void setAccount(Account account) {
        mAccount = account;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mAccount == null) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mSwiping = onStart(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stopMotionTracking();
                break;
        }
        return mSwiping;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (mAccount == null) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                return onMove(ev);
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onFinish(ev);
                return false;
            default:
                return false;
        }
    }

    /**
     * Start tracking the motion event.
     *
     * @param ev the motion event where the ACTION_DOWN happened
     */
    private void onDown(MotionEvent ev) {
        mDownX = ev.getX();
        mDownRawX = ev.getRawX();
        mDownRawY = ev.getRawY();
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
    }

    /**
     * Track the motion.
     *
     * @param ev the motion event where the ACTION_DOWN happened.
     * @return whether or not the view is being swiped horizontally.
     */
    private boolean onMove(MotionEvent ev) {
        if (!mSwiping) {
            mSwiping = onStart(ev);
            return mSwiping;
        }
        updateSwipeProgress(ev);
        return mSwiping;
    }

    /**
     * Attempts to start the swipe.
     *
     * @param ev the ACTION_MOVE motion event in question
     * @return whether or not the swipe started
     */
    private boolean onStart(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);

        float deltaX = ev.getRawX() - mDownRawX;
        float deltaY = ev.getRawY() - mDownRawY;
        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaY) < mTouchSlop) {
            getParent().requestDisallowInterceptTouchEvent(true);
            if (Math.abs(deltaX) > mTouchSlop) {
                return true;
            }
        }
        return false;
    }

    private void updateSwipeProgress(MotionEvent ev) {
        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);

            float swipeDistance = ev.getX() - mDownX;
            float percent = Math.abs(swipeDistance / (getWidth() / 3f));
            if (mVotable.getVoteStatus() == Votable.NEUTRAL) {
                mForegroundView.setVisibility(GONE);
                mBackgroundView.setVisibility(VISIBLE);
                mBackgroundView.setScaleY(percent);
                if (swipeDistance > 0) {
                    mBackgroundView.setBackgroundColor(getResources().getColor(R.color.orangered));
                } else {
                    mBackgroundView.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                }
            } else {
                if (swipeDistance > 0) {
                    mForegroundView.setVisibility(GONE);
                    mBackgroundView.setAlpha(1f - percent);
                } else {
                    mForegroundView.setVisibility(VISIBLE);
                    mForegroundView.setScaleY(percent);
                }
                if (mVotable.getVoteStatus() == Votable.DOWNVOTED) {
                    mForegroundView.setBackgroundColor(getResources().getColor(R.color.orangered));
                } else {
                    mForegroundView.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                }
            }

        }
    }

    private void onFinish(MotionEvent ev) {
        if (mSwiping) {
            mVelocityTracker.addMovement(ev);
            mVelocityTracker.computeCurrentVelocity(1000);
            float swipeDistance = ev.getX() - mDownX;
            if (swipeDistance > getWidth() / 2.0f
                    || mVelocityTracker.getXVelocity() > mMinFlingVelocity) {
                onLeftToRightSwipe();
            } else {
                onRightToLeftSwipe();
            }
        }
        stopMotionTracking();
    }

    private void stopMotionTracking() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mDownX = 0;
        mDownRawY = 0;
        mDownRawX = 0;
        mSwiping = false;
    }

    private void onRightToLeftSwipe() {
        if (mSwipeListener != null) {
            mSwipeListener.onRightToLeftSwipe();
        }
        if (mVotable.getVoteStatus() == Votable.UPVOTED) {
            mBackgroundView.setBackgroundColor(getResources().getColor(R.color.periwinkle));
        } else {
            mBackgroundView.setVisibility(GONE);
        }
    }

    private void onLeftToRightSwipe() {
        if (mSwipeListener != null) {
            mSwipeListener.onLeftToRightSwipe();
        }
        if (mVotable.getVoteStatus() == Votable.DOWNVOTED) {
            mBackgroundView.setBackgroundColor(getResources().getColor(R.color.orangered));
        } else {
            mBackgroundView.setVisibility(GONE);
        }
    }

    public interface SwipeListener {
        public void onRightToLeftSwipe();
        public void onLeftToRightSwipe();
    }

}
