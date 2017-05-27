package com.phone.custom;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Phone on 2017/5/26.
 */

public class FloatViewService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MyWindowManager.createFloatView(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		MyWindowManager.createFloatView(this);
		return super.onStartCommand(intent, flags, startId);
	}

}
