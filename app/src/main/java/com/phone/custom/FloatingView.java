package com.phone.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Phone on 2017/5/20.
 */

public class FloatingView extends View {

    private Rect mOutterMainRect;
    private Rect mOutterSubRect;
    private Rect mInnerMainRect;
    private Rect mInnerSubRect;

    private Paint mPaint;
    private int mOutterRectStartColor= Color.WHITE;
    private int mOutterRectEndColor=Color.GRAY;
    private int mInnerRectStartColor=Color.WHITE;
    private int mInnerRectEndColor=Color.GRAY;

    private float mRadiusRate=0.7f;

    private float mRateOfRadiusBySize=0.5f;
    private float mRateOfRadiusByRadius=0.6f;

    private Ball mOutterBall;
    private Ball mInnerBall;

    private Point center;

    private boolean isClick;
    private boolean isLongClick;

    public FloatingView(Context context) {
        this(context,null);
    }

    public FloatingView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOutterMainRect=new Rect();
        mOutterSubRect=new Rect();
        mInnerMainRect=new Rect();
        mInnerSubRect=new Rect();
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        center=new Point();
        center.x=(getMeasuredWidth()+getPaddingLeft()-getPaddingRight())/2;
        center.y=(getMeasuredHeight()+getPaddingTop()-getPaddingBottom())/2;
        mOutterBall=new Ball(center,100,0);
        mInnerBall=new Ball(center,50,0);
        invalidate();
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isClick=true;
            }
        });
        super.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isLongClick=true;
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mOutterBall!=null){
            mOutterBall.draw(canvas);
        }
        if(mInnerBall!=null){
            mInnerBall.draw(canvas);
        }

    }

//    private int mLastMotionX;
//    private int mLastMotionY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsume=super.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                mLastMotionX= (int) event.getRawX();
//                mLastMotionY= (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                if(isLongClick){
                    //执行长按动画，动画执行完成后，isLongClick置为false并响应长按操作
                }else {

                    double dx = event.getX() - center.x;
                    double dy = event.getY() - center.y;
                    if (dx == 0) {
                        return true;
                    }
                    float angle = (float) (Math.atan(dy / dx) * 180 / Math.PI);
                    if (dx < 0) {
                        angle += 180;
                    }
                    if (mOutterBall != null) {
                        mOutterBall.setAngle(angle);
                    }
                    if (mInnerBall != null) {
                        mInnerBall.setAngle(angle);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isClick){
                    //执行点击动画，动画执行完成后，isClick置为false并响应点击操作
                    startAnim();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return isConsume||event.getAction()==MotionEvent.ACTION_DOWN;
    }

    private void startAnim() {
        ValueAnimator clickAnimator=new ValueAnimator();
        clickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

            }
        });
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
//        super.setOnClickListener(l);
        Log.w("phoneTest","not allow to set OnClickListener");
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
//        super.setOnLongClickListener(l);
        Log.w("phoneTest","not allow to set OnLongClickListener");
    }
}
