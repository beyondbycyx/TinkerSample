package com.xhuabu.huangquan.mypluginapp.view;

import com.heepay.plugin.exception.CrashHandler;

import android.app.Application;

public class BaseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler.getInstance().init(this);
	}

}
