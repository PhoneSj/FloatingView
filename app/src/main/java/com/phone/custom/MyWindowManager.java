package com.phone.custom;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.phone.custom.widget.WrapFloatView;

/**
 * Created by Phone on 2017/5/26.
 */

public class MyWindowManager {

	private static WrapFloatView wrapFloatView;

	private static LayoutParams layoutParams;

	private static WindowManager mWindowManager;

	public static void createSmallWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (wrapFloatView == null) {
			wrapFloatView = new WrapFloatView(context);
			if (layoutParams == null) {
				layoutParams = new LayoutParams();
				layoutParams.type = LayoutParams.TYPE_PHONE;
				layoutParams.format = PixelFormat.RGBA_8888;
				layoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
				layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
				layoutParams.width = 300;
				layoutParams.height = 300;
				layoutParams.x = screenWidth;
				layoutParams.y = screenHeight / 2;
			}
			wrapFloatView.setParams(layoutParams);
			windowManager.addView(wrapFloatView, layoutParams);
		}
	}

	public static void removeSmallWindow(Context context) {
		if (wrapFloatView != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(wrapFloatView);
			wrapFloatView = null;
		}
	}

	public static boolean isWindowShowing() {
		return wrapFloatView != null;
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

}
