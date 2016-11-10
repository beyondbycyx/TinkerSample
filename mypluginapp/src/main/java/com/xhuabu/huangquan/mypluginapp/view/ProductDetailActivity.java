package com.xhuabu.huangquan.mypluginapp.view;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.xhuabu.huangquan.mypluginapp.R;

/**
 * 商品详情页面
 * 
 * @author hy
 * 
 */
public class ProductDetailActivity extends Activity {

	private Button btn_buy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay_info);
		btn_buy = (Button) this.findViewById(R.id.bt_buy);
		btn_buy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ProductDetailActivity.this,
						PayInitActivity.class);
				startActivity(intent);
			}
		});
	}
}
