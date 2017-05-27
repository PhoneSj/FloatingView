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
import android.widget.Toast;

/**
 * Created by Phone on 2017/5/20.
 */

public class FloatView extends View {

	private static final int[] OUT_CIRCLE_COLOR_RANGE = { 0x00ffffff, 0x00ffffff, 0x1affffff, 0x62ffffff, 0x7effffff };
	private static final float[] OUT_CIRCLE_POSITION_RANGE = { 0.0f, 0.45f, 0.6f, 0.9f, 1.0f };
	private static final int[] INNER_CIRCLE_COLOR_RANGE = { 0xb2ffffff, 0xb2ffffff, 0xe5ffffff, 0xe5ffffff };
	private static final float[] INNER_CIRCLE_POSITION_RANGE = { 0.0f, 0.05f, 0.75f, 1.0f };

	/**
	 * 触发操作的上限阈值
	 */
	private float mUpThreshold = 0.99f;
	/**
	 * 触发操作的下限阈值
	 */
	private float mDownThreshold = 0.0f;
	/**
	 * 大圆半径与FloatView半径的比例值
	 */
	private float mInnerRadiusRate = 0.4f;
	/**
	 * 小圆半径与FloatView半径的比例值
	 */
	private float mOutterRadiusRate = 0.6f;
	/**
	 * 轻微滑动阈值
	 */
	private int mScaleTouchSlop;
	/**
	 * 是否正在拖拽
	 */
	private boolean isBeingDrag;
	/**
	 * 外圆
	 */
	private OutterBall mOutterBall;
	/**
	 * 内圆
	 */
	private InnerBall mInnerBall;
	/**
	 * 中心点
	 */
	private Point center = new Point();
	/**
	 * 拖拽动画
	 */
	private ValueAnimator mDragAnimator;
	/**
	 * 点击动画
	 */
	private ValueAnimator mClickAnimator;
	/**
	 * 当前拖拽的距离
	 */
	private int mDistance;
	/**
	 * down事件的横坐标
	 */
	private int mDownMotionX;
	/**
	 * down事件的纵坐标
	 */
	private int mDownMotionY;
	/**
	 * 当前手势
	 */
	private Mode mode = Mode.NONE;

	public enum Mode {
		CLICK, LEFT, UP, RIGHT, DOWN, NONE
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
		//在自定义处理事件前，先调用父类的处理方式
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
				float dx = event.getX() - center.x;
				float dy = event.getY() - center.y;
				startDragAnim(judgeWhichDirection(dx, dy));
				break;
			case MotionEvent.ACTION_CANCEL:
				isBeingDrag = false;
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
				temp = Mode.DOWN;
			} else {
				temp = Mode.UP;
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

	/**
	 * 开始拖拽动画
	 * 
	 * @param newMode
	 */
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

	/**
	 * 执行点击动画
	 */
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
				doFunction(Mode.CLICK);
			}
		});
		mClickAnimator.start();
	}

	/**
	 * 是否有动画正在执行
	 * 
	 * @return
	 */
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

	/**
	 * 响应操作
	 *
	 * @param newMode
	 */
	private void doFunction(Mode newMode) {
		Log.i("phoneTest", "doFunction..." + newMode);
		if (newMode == mode) {
			return;
		}
		switch (newMode) {
			case CLICK:
				Log.i("phoneTest", "响应CLICK操作");
				Toast.makeText(getContext(), "响应CLICK操作", Toast.LENGTH_SHORT).show();
				break;
			case LEFT:
				Log.i("phoneTest", "响应LEFT操作");
				Toast.makeText(getContext(), "响应LEFT操作", Toast.LENGTH_SHORT).show();
				break;
			case UP:
				Log.i("phoneTest", "响应TOP操作");
				Toast.makeText(getContext(), "响应TOP操作", Toast.LENGTH_SHORT).show();
				break;
			case RIGHT:
				Log.i("phoneTest", "响应RIGHT操作");
				Toast.makeText(getContext(), "响应RIGHT操作", Toast.LENGTH_SHORT).show();
				break;
			case DOWN:
				Log.i("phoneTest", "响应BOTTOM操作");
				Toast.makeText(getContext(), "响应BOTTOM操作", Toast.LENGTH_SHORT).show();
				break;
			case NONE:
				Log.i("phoneTest", "响应NONE操作");
				break;
		}
		mode = newMode;
	}

}
