package example.test.phong.testswipeback;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import static androidx.customview.widget.ViewDragHelper.EDGE_BOTTOM;
import static androidx.customview.widget.ViewDragHelper.EDGE_TOP;

/**
 * Created by Phong Huynh on 9/7/2018.
 */
public class SwipeLayout extends ViewGroup {
    private final OnSwipeBackListener mSwipeBackListener;
    private float mSwipeFactor = 0.5f;
    private float mMaskAlpha = 125;
    private int mDirectionMode = EDGE_TOP | EDGE_BOTTOM;
    private final ViewDragHelper mDragHelper;
    private int mTouchSlop;
    private boolean isSwipeFromEdge = true;
    private float downX, downY, swipeBackFraction, swipeBackFactor = 0.5f;
    private View mDragContentView;
    private int leftOffset = 0, topOffset = 0;
    private int width, height;
    private int touchedEdge = ViewDragHelper.INVALID_POINTER;
    private float autoFinishedVelocityLimit = 2000f;

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(mDirectionMode);
        mTouchSlop = mDragHelper.getTouchSlop();
        mSwipeBackListener = new OnSwipeBackListener() {
            @Override
            public void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor) {
                invalidate();
            }

            @Override
            public void onViewSwipeFinished(View mView, boolean isEnd) {
                if (isEnd) {
                    finish();
                }
            }
        };

    }

    private void finish() {
        ((Activity) getContext()).finish();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (childCount > 1) {
            throw new IllegalStateException("SwipeLayout must contain only one direct child");
        }
        int defaultMeasuredWidth = 0;
        int defaultMeasuredHeight = 0;
        int measuredWidth;
        int measuredHeight;
        if (childCount > 0) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            mDragContentView = getChildAt(0);
            defaultMeasuredWidth = mDragContentView.getMeasuredWidth();
            defaultMeasuredHeight = mDragContentView.getMeasuredHeight();
        }
        measuredWidth = View.resolveSize(defaultMeasuredWidth, widthMeasureSpec) + getPaddingLeft() + getPaddingRight();
        measuredHeight = View.resolveSize(defaultMeasuredHeight,
                                          heightMeasureSpec) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) return;

        int left = getPaddingLeft() + leftOffset;
        int top = getPaddingTop() + topOffset;
        int right = left + mDragContentView.getMeasuredWidth();
        int bottom = top + mDragContentView.getMeasuredHeight();

        mDragContentView.layout(left, top, right, bottom);

        if (changed) {
            width = getWidth();
            height = getHeight();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        if (mDragHelper.shouldInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void smoothScrollToY(int finalTop) {
        if (mDragHelper.settleCapturedViewAt(getPaddingLeft(), finalTop)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private boolean backJudgeBySpeed(float xvel, float yvel) {
        switch (mDirectionMode) {
            case EDGE_TOP:
                return yvel > autoFinishedVelocityLimit;
            case EDGE_BOTTOM:
                return yvel < -autoFinishedVelocityLimit;
        }
        return false;
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(@NonNull View view, int i) {
            return view == mDragContentView;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            topOffset = getPaddingTop();
            if (mDirectionMode == EDGE_TOP) {
                topOffset = Math.min(Math.max(top, getPaddingTop()), height);
            } else if (mDirectionMode == EDGE_BOTTOM) {
                topOffset = Math.min(Math.max(top, -height), getPaddingBottom());
            }
            return topOffset;
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            left = Math.abs(left);
            top = Math.abs(top);
            switch (mDirectionMode) {
                case EDGE_TOP:
                case EDGE_BOTTOM:
                    swipeBackFraction = 1.0f * top / height;
                    break;
            }
            if (mSwipeBackListener != null) {
                mSwipeBackListener.onViewPositionChanged(mDragContentView, swipeBackFraction, swipeBackFactor);
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            leftOffset = topOffset = 0;
            touchedEdge = ViewDragHelper.INVALID_POINTER;

            boolean isBackToEnd = backJudgeBySpeed(xvel, yvel) || swipeBackFraction >= swipeBackFactor;
            if (isBackToEnd) {
                switch (mDirectionMode) {
                    case EDGE_TOP:
                        smoothScrollToY(height);
                        break;
                    case EDGE_BOTTOM:
                        smoothScrollToY(-height);
                        break;
                }
            } else {
                switch (mDirectionMode) {
                    case EDGE_BOTTOM:
                    case EDGE_TOP:
                        smoothScrollToY(getPaddingTop());
                        break;
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (mSwipeBackListener != null) {
                    if (swipeBackFraction == 0) {
                        mSwipeBackListener.onViewSwipeFinished(mDragContentView, false);
                    } else if (swipeBackFraction == 1) {
                        mSwipeBackListener.onViewSwipeFinished(mDragContentView, true);
                    }
                }
            }
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return height;
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
            touchedEdge = edgeFlags;
        }
    }

    public interface OnSwipeBackListener {

        void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor);

        void onViewSwipeFinished(View mView, boolean isEnd);
    }
}