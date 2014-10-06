package me.williamhester.ui.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import me.williamhester.models.AccountManager;
import me.williamhester.models.Votable;
import me.williamhester.reddit.R;

/**
 * Created by william on 7/25/14.
 */
public class SwipeView extends LinearLayout {

    private final long ANIMATION_LENGTH = 300;

    private int mTouchSlop;
    private int mFlingVelocity;
    private int mBackgroundColor;
    private float mDownRawX;
    private float mDownRawY;
    private float mDownX;
    private float mDownY;
    private float mSwipeDistance;
    private float mMinFlingDistance;
    private boolean mSwiping;

    private SwipeListener mSwipeListener;
    private VelocityTracker mVelocityTracker;
    private View mBackgroundView;
    private View mForegroundView;
    private Votable mVotable;
    private final Handler mHandler = new Handler();

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
        super(context, attrs, defStyleAttr);
        mSwipeDistance = 100 * context.getResources().getDisplayMetrics().density;
        mMinFlingDistance = mSwipeDistance / 2;
        init();
    }

    private void init() {
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        mFlingVelocity = (vc.getScaledMaximumFlingVelocity() + vc.getScaledMinimumFlingVelocity()) / 2;
        setEnabled(AccountManager.isLoggedIn());
    }

    public void recycle(Votable votable) {
        mVotable = votable;
        setEnabled(true);
    }

    public void setUp(View backgroundView, View foregroundView,
                      SwipeListener listener) {
        mBackgroundView = backgroundView;
        mForegroundView = foregroundView;
        mSwipeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mSwiping = onStart(ev);
                if (!mSwiping) super.onTouchEvent(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                super.onInterceptTouchEvent(ev);
                stopMotionTracking();
                break;
        }
        return mSwiping;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if (!isEnabled()) {
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
        mDownY = ev.getY();
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
        if (Math.abs(deltaX) > Math.abs(deltaY) * 2 && Math.abs(deltaY) < mTouchSlop) {
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

            float swipeDistanceX = ev.getX() - mDownX;
            float swipeDistanceY = ev.getY() - mDownY;
            float percent = Math.abs(swipeDistanceX / mSwipeDistance);
            mForegroundView.setVisibility(VISIBLE);
            if (Math.abs(swipeDistanceY) > Math.abs(swipeDistanceX)) {
                onCancelSwipe();
                mSwiping = false;
                return;
            }
            if (swipeDistanceX > 0) {
                if (mVotable.getVoteStatus() == Votable.UPVOTED) {
                    if (mForegroundView.getVisibility() == VISIBLE) {
                        mForegroundView.setVisibility(GONE);
                        mForegroundView.invalidate();
                    }
                    mBackgroundView.setAlpha(1f - percent);
                } else {
                    if (mForegroundView.getVisibility() == GONE) {
                        mForegroundView.setVisibility(VISIBLE);
                        mForegroundView.invalidate();
                    }
                    mForegroundView.setBackgroundColor(getResources().getColor(R.color.orangered));
                    mForegroundView.setScaleY(percent);
                }
            } else {
                if (mVotable.getVoteStatus() == Votable.DOWNVOTED) {
                    if (mForegroundView.getVisibility() == VISIBLE) {
                        mForegroundView.setVisibility(GONE);
                        mForegroundView.invalidate();
                    }
                    mBackgroundView.setAlpha(1f - percent);
                } else {
                    if (mForegroundView.getVisibility() == GONE) {
                        mForegroundView.setVisibility(VISIBLE);
                        mForegroundView.invalidate();
                    }
                    mForegroundView.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    mForegroundView.setScaleY(percent);
                }
            }

        }
    }

    private void onFinish(MotionEvent ev) {
        if (mSwiping) {
            mVelocityTracker.addMovement(ev);
            mVelocityTracker.computeCurrentVelocity(1000);
            float swipeDistance = ev.getX() - mDownX;
            float flingVelocity = mVelocityTracker.getXVelocity();
            if (swipeDistance > mSwipeDistance
                    || flingVelocity > mFlingVelocity && swipeDistance > mMinFlingDistance) {
                onLeftToRightSwipe();
            } else if (swipeDistance < -mSwipeDistance
                    || flingVelocity < -mFlingVelocity && swipeDistance < -mMinFlingDistance) {
                onRightToLeftSwipe();
            } else {
                onCancelSwipe();
            }
        } else {
            super.onTouchEvent(ev);
        }
        stopMotionTracking();
    }

    private void stopMotionTracking() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }
        mDownX = 0;
        mDownY = 0;
        mDownRawY = 0;
        mDownRawX = 0;
        mSwiping = false;
    }

    private void onRightToLeftSwipe() {
        int oldVoteStatus = mVotable.getVoteStatus();
        if (mSwipeListener != null) {
            mSwipeListener.onRightToLeftSwipe();
        }
        mBackgroundColor = getResources().getColor(R.color.periwinkle);
        if (oldVoteStatus == Votable.UPVOTED || oldVoteStatus == Votable.NEUTRAL) {
            mHandler.postDelayed(mFinishVoteRunnable, ANIMATION_LENGTH);
        }
    }

    private void onLeftToRightSwipe() {
        int oldVoteStatus = mVotable.getVoteStatus();
        if (mSwipeListener != null) {
            mSwipeListener.onLeftToRightSwipe();
        }
        mBackgroundColor = getResources().getColor(R.color.orangered);
        if (oldVoteStatus == Votable.DOWNVOTED || oldVoteStatus == Votable.NEUTRAL) {
            mHandler.postDelayed(mFinishVoteRunnable, ANIMATION_LENGTH);
        }
    }

    private void onCancelSwipe() {
        mHandler.postDelayed(mCancelSwipeRunnable, ANIMATION_LENGTH);
    }

    private Runnable mFinishVoteRunnable = new Runnable() {
        @Override
        public void run() {
            float scaleY = mForegroundView.getScaleY();
            float centerHeight = mForegroundView.getHeight() / 2f;
            float centerWidth = mForegroundView.getWidth() / 2f;
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, scaleY, 1, centerWidth, centerHeight);
            scaleAnimation.setFillAfter(true);
            scaleAnimation.setDuration(Math.abs(Math.round((ANIMATION_LENGTH * (1f - scaleY)))));
            scaleAnimation.setAnimationListener(new AnimationFinishedListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    mBackgroundView.setBackgroundColor(mBackgroundColor);
                    mBackgroundView.setVisibility(VISIBLE);
                    mBackgroundView.setAlpha(1f);
                    mBackgroundView.setScaleY(1f);
                    mForegroundView.setScaleY(0f);
                    mForegroundView.clearAnimation();
                }
            });
            mForegroundView.startAnimation(scaleAnimation);
        }
    };

    private Runnable mCancelSwipeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mForegroundView.getVisibility() == VISIBLE) {
                float scaleY = mForegroundView.getScaleY();
                float centerHeight = mForegroundView.getHeight() / 2f;
                float centerWidth = mForegroundView.getWidth() / 2f;
                ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, scaleY, 0, centerWidth, centerHeight);
                scaleAnimation.setDuration(Math.abs(Math.round((ANIMATION_LENGTH * (scaleY)))));
                scaleAnimation.setAnimationListener(new AnimationFinishedListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mForegroundView.setScaleY(0f);
                    }
                });
                mForegroundView.startAnimation(scaleAnimation);
            } else {
                float alpha = mBackgroundView.getAlpha();
                AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, 1.0f);
                alphaAnimation.setDuration(Math.abs(Math.round((ANIMATION_LENGTH * (1f - alpha)))));
                alphaAnimation.setAnimationListener(new AnimationFinishedListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mBackgroundView.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mBackgroundView.setAlpha(1f);
                    }
                });
                mBackgroundView.startAnimation(alphaAnimation);
            }
        }
    };

    private abstract class AnimationFinishedListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) { }

        @Override
        public void onAnimationRepeat(Animation animation) { }
    }

    public interface SwipeListener {
        public void onRightToLeftSwipe();
        public void onLeftToRightSwipe();
    }

}
