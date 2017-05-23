package com.phone.custom;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by Phone on 2017/5/20.
 */

public class FloatingView extends View {

    private static final int[] OUT_CIRCLE_COLOR_RANGE = {0x00ffffff, 0x00ffffff, 0x1affffff,
            0x62ffffff, 0x7effffff};
    private static final float[] OUT_CIRCLE_POSITION_RANGE = {0.0f, 0.45f, 0.6f, 0.9f, 1.0f};
    private static final int[] INNER_CIRCLE_COLOR_RANGE = {0xb2ffffff, 0xb2ffffff, 0xe5ffffff,
            0xe5ffffff};
    private static final float[] INNER_CIRCLE_POSITION_RANGE = {0.0f, 0.05f, 0.75f, 1.0f};
    private static final int[] INNER_CIRCLE_STROKE_COLOR_RANGE = {0x00ffffff, 0x00ffffff,
            0xffffffff, 0xffffffff};
    private static final float[] INNER_CIRCLE_STROKE_POSITION_RANGE = {0.0f, 0.94f, 0.98f, 1.0f};


    private float mUpThreshold = 0.99f;
    private float mDownThreshold = 0.0f;

    private float mInnerRadiusRate = 0.4f;
    private float mOutterRadiusRate = 0.5f;

    private int mScaleTouchSlop;
    private boolean isBeingDrag;

    private OutterBall mOutterBall;
    private InnerBall mInnerBall;

    private Point center;

    private ValueAnimator mDragAnimator;
    private ValueAnimator mClickAnimator;
    private int mDistance;

    private boolean isClick;

    private TriggerDirection mDirection;

    private enum TriggerDirection {
        LEFT, TOP, RIGHT, BOTTOM, NONE
    }

    public FloatingView(Context context) {
        this(context, null);
    }

    public FloatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        center = new Point();
        center.x = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
        center.y = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
        final int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        mOutterBall = new OutterBall(center.x, center.y, radius, mOutterRadiusRate);
        mOutterBall.setColorsAndPosition(OUT_CIRCLE_COLOR_RANGE, OUT_CIRCLE_POSITION_RANGE);
        mInnerBall = new InnerBall(center.x, center.y, radius, mInnerRadiusRate);
        mInnerBall.setColorsAndPosition(INNER_CIRCLE_COLOR_RANGE, INNER_CIRCLE_POSITION_RANGE);
        invalidate();
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("phoneTest", "clickAnimater start");
                isClick = true;
                startClickAnimator();
            }
        });
        super.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //                isLongClick = true;
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mOutterBall != null) {
            mOutterBall.draw(canvas);
        }
        if (mInnerBall != null) {
            mInnerBall.draw(canvas);
        }

        //        Bitmap tempBitmap=Bitmap.createBitmap(300,300, Bitmap.Config.ARGB_8888);
        //        Canvas tempCanvas=new Canvas(tempBitmap);
        //        Rect src=new Rect(0,0,150,150);
        //        Rect des=new Rect(150,0,300,300);
        //        Paint tempPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        //        tempPaint.setStyle(Paint.Style.FILL);
        //        tempPaint.setColor(Color.BLUE);
        //        tempCanvas.drawCircle(tempBitmap.getWidth()/2,tempBitmap.getHeight()/2,150,tempPaint);
        //        canvas.drawBitmap(tempBitmap,src,des,null);
    }

    private int mDownMotionX;
    private int mDownMotionY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //        boolean flag = gestureDetector.onTouchEvent(event);
        //        return flag;
        if (isRunningOfAnimators()) {
            return false;
        }
        boolean isConsume = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownMotionX = (int) event.getRawX();
                mDownMotionY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int distanceX = (int) (event.getRawX() - mDownMotionX);
                int distanceY = (int) (event.getRawY() - mDownMotionY);

                if (isBeingDrag || Math.abs(distanceX) > mScaleTouchSlop || Math.abs(distanceY) > mScaleTouchSlop) {
                    isBeingDrag = true;

                    mDistance = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                    float dx = event.getX() - center.x;
                    float dy = event.getY() - center.y;

                    float angle = calculateAngle(dx, dy);

                    if (mOutterBall != null) {
                        mOutterBall.setAngle(angle);
                        mOutterBall.setOffset(mDistance);
                    }
                    if (mInnerBall != null) {
                        mInnerBall.setAngle(angle);
                        mInnerBall.setOffset(mDistance);
                    }
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                isBeingDrag = false;
                if (!isClick) {
                    startDragAnim();
                    float dx = event.getX() - center.x;
                    float dy = event.getY() - center.y;
                    mDirection = judgeWhichDirection(dx, dy);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return isConsume || event.getAction() == MotionEvent.ACTION_DOWN;
    }


    /**
     * 判定触发哪个方向的操作
     *
     * @param dx
     * @param dy
     * @return
     */
    private TriggerDirection judgeWhichDirection(float dx, float dy) {
        TriggerDirection temp = TriggerDirection.NONE;
        double distance = Math.sqrt(dx * dx + dy * dy);
        int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        if (distance < radius * mUpThreshold || distance > radius * mDownThreshold) {
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0) {
                    temp = TriggerDirection.RIGHT;
                } else {
                    temp = TriggerDirection.LEFT;
                }
            } else if (dy > 0) {
                temp = TriggerDirection.BOTTOM;
            } else {
                temp = TriggerDirection.TOP;
            }
        }
        return temp;
    }

    private float calculateAngle(float dx, float dy) {
        if (dx == 0) {
            return 0;
        }
        float angle = (float) (Math.atan(dy / dx) * 180 / Math.PI);
        if (dx < 0) {
            angle += 180;
        }
        return angle;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        //        super.setOnClickListener(l);
        Log.w("phoneTest", "not allow to set OnClickListener");
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        //        super.setOnLongClickListener(l);
        Log.w("phoneTest", "not allow to set OnLongClickListener");
    }

    private void startDragAnim() {
        //有动画正在执行，不响应
        if (isRunningOfAnimators()) {
            return;
        }
        if (mDragAnimator == null) {
            mDragAnimator = ValueAnimator.ofInt(mDistance, 0);
        }
        if (mDragAnimator.isRunning()) {
            return;
        }
        mDragAnimator.setIntValues(mDistance, 0);
        mDragAnimator.setDuration(400);
        mDragAnimator.setInterpolator(new OvershootInterpolator());
        mDragAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mDistance = (int) valueAnimator.getAnimatedValue();
                if (mOutterBall != null) {
                    mOutterBall.setOffset(mDistance);
                }
                if (mInnerBall != null) {
                    mInnerBall.setOffset(mDistance);
                }
                invalidate();
            }
        });
        mDragAnimator.start();
    }

    private void startClickAnimator() {
        //有动画正在执行，不响应
        if (isRunningOfAnimators()) {
            Log.i("phoneTest", "==============0===============");
            return;
        }
        int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
        if (mClickAnimator == null) {
            Log.i("phoneTest", "==============1===============");
            mClickAnimator = ValueAnimator.ofFloat(radius * mInnerRadiusRate,
                    radius * mInnerRadiusRate * 1.2f, radius * mInnerRadiusRate);
        }
        if (mClickAnimator.isRunning()) {
            Log.i("phoneTest", "==============2===============");
            return;
        }
        Log.i("phoneTest", "==============3===============");
        mClickAnimator.setDuration(500);
        mClickAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mClickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                if (mInnerBall != null) {
                    mInnerBall.setAllRadius((int) value);
                }
                invalidate();
            }
        });
        mClickAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isClick = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isClick = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mClickAnimator.start();
        startClickAnimator();
    }

    private boolean isRunningOfAnimators() {
        if (mDragAnimator != null && mDragAnimator.isRunning()) {
            Log.i("phoneTest", "drag animator is running");
            return true;
        }
        if (mClickAnimator != null && mClickAnimator.isRunning()) {
            Log.i("phoneTest", "click animator is running");
            return true;
        }
        return false;
    }

    //    private GestureDetector gestureDetector = new GestureDetector(new GestureDetector
    //            .SimpleOnGestureListener() {
    //        @Override
    //        public boolean onDown(MotionEvent event) {
    //            mDownMotionX = (int) event.getRawX();
    //            mDownMotionY = (int) event.getRawY();
    //            return true;
    //        }
    //
    //        @Override
    //        public boolean onSingleTapUp(MotionEvent event) {
    //            Log.i("phoenTest", "onSingleTapUp");
    //
    //            return super.onSingleTapUp(event);
    //        }
    //
    //        @Override
    //        public void onLongPress(MotionEvent event) {
    //            super.onLongPress(event);
    //            Log.i("phoenTest", "onSingleTapConfirmed");
    //        }
    //
    //        @Override
    //        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    //            Log.i("phoenTest", "onScroll");
    //            mDistanceX += distanceX;
    //            mDistanceY += distanceY;
    //            mDirection = judgeWhichDirection(mDistanceX, mDistanceY);
    //            return super.onScroll(e1, e2, distanceX, distanceY);
    //        }
    //
    //        @Override
    //        public boolean onSingleTapConfirmed(MotionEvent event) {
    //            Log.i("phoenTest", "onSingleTapConfirmed");
    //            return super.onSingleTapConfirmed(event);
    //        }
    //    });
    //
    //    private float mDistanceX;
    //    private float mDistanceY;
}
