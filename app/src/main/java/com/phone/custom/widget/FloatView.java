package com.phone.custom.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

public class FloatView extends View {

	private static final int[] OUT_CIRCLE_COLOR_RANGE = { 0x00ffffff, 0x00ffffff, 0x1affffff,
			0x62ffffff, 0x7effffff };
	private static final float[] OUT_CIRCLE_POSITION_RANGE = { 0.0f, 0.45f, 0.6f, 0.9f, 1.0f };
	private static final int[] INNER_CIRCLE_COLOR_RANGE = { 0xb2ffffff, 0xb2ffffff, 0xe5ffffff,
			0xe5ffffff };
	private static final float[] INNER_CIRCLE_POSITION_RANGE = { 0.0f, 0.05f, 0.75f, 1.0f };

	private float mUpThreshold = 0.99f;
	private float mDownThreshold = 0.0f;

	private float mInnerRadiusRate = 0.4f;
	private float mOutterRadiusRate = 0.55f;

	private int mScaleTouchSlop;
	private boolean isBeingDrag;

	private OutterBall mOutterBall;
	private InnerBall mInnerBall;

	private Point center = new Point();

	private ValueAnimator mDragAnimator;
	private ValueAnimator mClickAnimator;
	private int mDistance;

	private int mDownMotionX;
	private int mDownMotionY;

	private Mode mode = Mode.NONE;

	private enum Mode {
		LEFT, TOP, RIGHT, BOTTOM, NONE
	}

	public FloatView(Context context) {
		this(context, null);
	}

	public FloatView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		center.x = (getMeasuredWidth() + getPaddingLeft() - getPaddingRight()) / 2;
		center.y = (getMeasuredHeight() + getPaddingTop() - getPaddingBottom()) / 2;
		final int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
		mInnerBall = new InnerBall(center.x, center.y, radius, mInnerRadiusRate);
		mInnerBall.setColorsAndPosition(INNER_CIRCLE_COLOR_RANGE, INNER_CIRCLE_POSITION_RANGE);
		mOutterBall = new OutterBall(center.x, center.y, radius, mOutterRadiusRate);
		mOutterBall.setColorsAndPosition(OUT_CIRCLE_COLOR_RANGE, OUT_CIRCLE_POSITION_RANGE);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mInnerBall != null) {
			mInnerBall.draw(canvas);
		}
		if (mOutterBall != null) {
			mOutterBall.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

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
			case MotionEvent.ACTION_CANCEL:
				isBeingDrag = false;
				float dx = event.getX() - center.x;
				float dy = event.getY() - center.y;
				startDragAnim(judgeWhichDirection(dx, dy));
				break;
		}
		return isConsume || event.getAction() == MotionEvent.ACTION_DOWN;
	}

	/**
	 * 判定触发哪个方向的操作
	 */
	private Mode judgeWhichDirection(float dx, float dy) {
		Mode temp = Mode.NONE;
		double distance = Math.sqrt(dx * dx + dy * dy);
		int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
		if (distance < radius * mUpThreshold || distance > radius * mDownThreshold) {
			if (Math.abs(dx) > Math.abs(dy)) {
				if (dx > 0) {
					temp = Mode.RIGHT;
				} else {
					temp = Mode.LEFT;
				}
			} else if (dy > 0) {
				temp = Mode.BOTTOM;
			} else {
				temp = Mode.TOP;
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

	private void startDragAnim(final Mode newMode) {
		Log.i("phoneTest", "startDragAnim..." + newMode);
		//有动画正在执行，不响应
		if (isRunningOfAnimators()) {
			return;
		}
		mDragAnimator = ValueAnimator.ofInt(mDistance, 0);
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
		mDragAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				//TODO
				doFunction(newMode);
			}
		});
		mDragAnimator.start();
	}

	private void doFunction(Mode newMode) {
		Log.i("phoneTest", "doFunction..." + newMode);
		if (newMode == mode) {
			return;
		}
		switch (newMode) {
			case LEFT:
				Log.i("phoneTest", "响应LEFT操作");
				break;
			case TOP:
				Log.i("phoneTest", "响应TOP操作");
				break;
			case RIGHT:
				Log.i("phoneTest", "响应RIGHT操作");
				break;
			case BOTTOM:
				Log.i("phoneTest", "响应BOTTOM操作");
				break;
			case NONE:
				Log.i("phoneTest", "响应NONE操作");
				break;
		}
		mode = newMode;
	}

	public void startClickAnimator() {
		//有动画正在执行，不响应
		if (isRunningOfAnimators()) {
			return;
		}
		int radius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2;
		mClickAnimator = ValueAnimator.ofFloat(radius * mInnerRadiusRate, radius * mInnerRadiusRate * 1.2f,
				radius * mInnerRadiusRate);
		if (mClickAnimator.isRunning()) {
			return;
		}
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
		mClickAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				//TODO
			}
		});
		mClickAnimator.start();
	}

	private boolean isRunningOfAnimators() {
		if (mDragAnimator != null && mDragAnimator.isRunning()) {
			//			Log.i("phoneTest", "drag animator is running");
			return true;
		}
		if (mClickAnimator != null && mClickAnimator.isRunning()) {
			//			Log.i("phoneTest", "click animator is running");
			return true;
		}
		return false;
	}

}
