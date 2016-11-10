package com.xhuabu.huangquan.mypluginapp.view;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.xhuabu.huangquan.mypluginapp.R;

/**
 * 欢迎界面
 * 
 * @author hy
 * 
 */
public class WelcomeActivity extends Activity {

	private Bundle bundle;
	public static int num = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_business_welcome);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(WelcomeActivity.this,
						ProductDetailActivity.class);
				if (bundle != null) {
					intent.putExtras(bundle);
				}
				startActivity(intent);
				finish();
			}
		}, 1500);
	}

}
