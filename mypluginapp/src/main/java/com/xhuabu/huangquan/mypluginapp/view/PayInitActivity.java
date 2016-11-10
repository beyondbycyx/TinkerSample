package com.xhuabu.huangquan.mypluginapp.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.heepay.plugin.activity.Constant;
import com.heepay.plugin.api.HeepayPlugin;
import com.xhuabu.huangquan.mypluginapp.R;
import com.xhuabu.huangquan.mypluginapp.domain.PaymentInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import android.content.DialogInterface.OnCancelListener;
/**
 * 购物车内商品详情展示
 * 
 * @author hy
 * 
 */
@SuppressWarnings("deprecation")
@SuppressLint("HandlerLeak")
public class PayInitActivity extends Activity {

	private static final String TAG = "PayInitActivity";
	private EditText amtEt;
	private Button btn_init, btn_query;
	private RadioGroup rg_pay_way;
	private RadioButton rb_wechat_pay, rb_alipay, rb_jcard;
	private String _payType;
	private String _agentBillNo;
	// private Dialog progressDialog;
	private LinearLayout payResultLL;
	private TextView billNoTv;
	private EditText payResultTv;
	private boolean isDebug = false;

	private PaymentInfo _paymentInfo;
	// 支付宝sdk支付状态
	private String resultStatus;
	private String result;
	private String memo;

	private static final int INIT_RESULT = 1001;
	private static final int QUERY_RESULT = 1002;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case INIT_RESULT:
				// progressDialog.dismiss();
				PaymentInfo info = (PaymentInfo) message.obj;
				if (info.hasError()) {
					Toast.makeText(PayInitActivity.this, info.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
				payResultTv.setText("TokenId : " + info.getTokenID());
				billNoTv.setVisibility(View.VISIBLE);
				billNoTv.setText("商户订单号：" + ((PaymentInfo) message.obj).getBillNo());
				payResultLL.setVisibility(View.VISIBLE);
				btn_query.setVisibility(View.VISIBLE);
				startHeepayServiceJar();
				break;
			case QUERY_RESULT:
				// progressDialog.dismiss();
				PaymentInfo rsInfo = (PaymentInfo) message.obj;
				if ("查询单据成功".equals(rsInfo.getMessage())) {
					new AlertDialog.Builder(PayInitActivity.this).setTitle("查询结果")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setMessage("支付结果：" + rsInfo.getPayResultName() + "\n" + "商户订单号：" + _agentBillNo + "\n"
									+ "汇付宝单号：" + rsInfo.getJunnetBillNo() + "\n" + "支付类型：" + rsInfo.getPayTypeName()
									+ "" + "\n" + "支付金额：" + rsInfo.getPayAmount() + "元")
							.setPositiveButton("确定", null).create().show();
				} else {
					new AlertDialog.Builder(PayInitActivity.this).setTitle("查询结果")
							.setIcon(android.R.drawable.ic_dialog_info).setMessage(rsInfo.getMessage())
							.setPositiveButton("确定", null).create().show();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay_init);
		initWidgets();

		btn_init.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				payResultLL.setVisibility(View.GONE);
				initPay();
			};
		});
		btn_query.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				queryPay();
			}
		});
	}

	/**
	 * 控件初始化
	 */
	public void initWidgets() {
		rg_pay_way = (RadioGroup) findViewById(R.id.rg_pay_way);
		rb_wechat_pay = (RadioButton) findViewById(R.id.rb_wechat_pay);
		rb_jcard = (RadioButton) findViewById(R.id.rb_jcard);
		rb_alipay = (RadioButton) findViewById(R.id.rb_alipay);
		btn_init = (Button) findViewById(R.id.btn_init);
		btn_query = (Button) findViewById(R.id.btn_query);
		payResultLL = (LinearLayout) findViewById(R.id.paysuccess_successLL);
		payResultTv = (EditText) findViewById(R.id.paysuccess_txt);
		billNoTv = (TextView) findViewById(R.id.paysuccess_billNoTv);
		amtEt = (EditText) findViewById(R.id.et_payAmt);
		initPayWayEvents();
		// 支付限额范围0.1-3000
		amtEt.setText("0.01");
		_payType = "30";
	}

	/**
	 * 支付类型初始化
	 */
	private void initPayWayEvents() {
		rg_pay_way.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == rb_wechat_pay.getId()) {
					_payType = "30";
				}
				if (checkedId == rb_alipay.getId()) {
					_payType = "22";
				}
				if (checkedId == rb_jcard.getId()) {
					_payType = "10";
				}
			}
		});
	}

	/**
	 * 启动汇付宝安全支付服务
	 */
	private void startHeepayServiceJar() {
		Log.i(TAG, _paymentInfo.getTokenID() + "," + _paymentInfo.getAgentId() + ","
				+ _paymentInfo.getBillNo() + "," + _payType);

		HeepayPlugin.pay(this, _paymentInfo.getTokenID() + "," + _paymentInfo.getAgentId() + ","
				+ _paymentInfo.getBillNo() + "," + _payType);
	}

	/**
	 * 接收支付通知结果
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constant.RESULTCODE) {
			String respCode = data.getExtras().getString("respCode");
			String respMessage = data.getExtras().getString("respMessage");
			if (!TextUtils.isEmpty(respCode)) {
				// 支付结果状态（01成功/00处理中/-1 失败）
				if ("01".equals(respCode)) {
					Toast.makeText(getApplicationContext(), "支付成功", Toast.LENGTH_SHORT).show();
				}
				if ("00".equals(respCode)) {
					Toast.makeText(getApplicationContext(), "处理中...", Toast.LENGTH_SHORT).show();
				}
				if ("-1".equals(respCode)) {
					Toast.makeText(getApplicationContext(), "支付失败", Toast.LENGTH_SHORT).show();
				}
			}
			// 除支付宝sdk支付respMessage均为null
			if (!TextUtils.isEmpty(respMessage)) {
				// 同步返回的结果必须放置到服务端进行验证, 建议商户依赖异步通知
				PayResult result = new PayResult(respMessage);
				// 同步返回需要验证的信息
				String resultInfo = result.getResult();
				String resultStatus = result.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						Toast.makeText(this, "支付结果确认中", Toast.LENGTH_SHORT).show();

					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						Toast.makeText(this, "支付失败", Toast.LENGTH_SHORT).show();
					}
				}

			}
		}
	}

	/**
	 * 支付初始化InitPay
	 * 
	 *  ayAmt
	 *            支付金额
	 *  goodsName
	 *            商品名称
	 *  goodsNote
	 *            商品说明
	 *  goodsNum
	 *            商品数量
	 *   remark
	 *            备注
	 *  userIP
	 *            用户IP
	 * @return 返回 {@link PaymentInfo}
	 */
	public void initPay() {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		String amt = amtEt.getText().toString().trim();
		if (TextUtils.isEmpty(amt)) {
			Toast.makeText(PayInitActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
			return;
		}
		float amtInt = Float.parseFloat(amt);
		// if (amtInt < 0.1 || amtInt > 3000) {
		// Toast.makeText(PayInitActivity.this, "金额限制为0.1-3000",
		// Toast.LENGTH_SHORT).show();
		// btn_query.setVisibility(View.GONE);
		// return;
		// }
		pairs.add(new BasicNameValuePair("pay_amt", amt));
		// Log.v(SHOW_TAG, "pay_amt=" + amtEt.getText().toString());
		// goods_name ,good_note,remark 要注意url转码
		try {
			pairs.add(new BasicNameValuePair("agent_id", URLEncoder.encode("1959138", "UTF-8")));// 1751412,1602809
			pairs.add(new BasicNameValuePair("goods_name", URLEncoder.encode("虚拟	测试产品", "UTF-8")));
			pairs.add(new BasicNameValuePair("goods_note", URLEncoder.encode("虚拟测试产品0.01元", "UTF-8")));
			pairs.add(new BasicNameValuePair("remark", URLEncoder.encode("无", "UTF-8")));
			pairs.add(new BasicNameValuePair("user_identity", generateUserIdentity()));
			pairs.add(new BasicNameValuePair("goods_num", "1"));
			pairs.add(new BasicNameValuePair("user_ip", "127.0.0.1"));
			// pay_type:支付类型
			pairs.add(new BasicNameValuePair("pay_type", _payType));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		//此地址为商户初始化地址，客户端不建议存储敏感信息（不安全），需要商户根据文档在服务器端自行实现，注：请勿直接使用此地址进行初始化
		String url;
		if (isDebug) {
			url = "http://192.168.2.95/DemoHeepay/SDK/SDKInit.aspx";
		} else {
			// url = "http://211.103.157.45/DemoHeepay/SDK/SDKInit.aspx";
			url = "http://211.103.157.45/DemoHeepayTest/SDK/SDKInit.aspx";
		}
		postInitData(url, pairs);
	}

	/**
	 * 查询订单
	 */
	public void queryPay() {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("agent_bill_id", _agentBillNo));

		//此地址为商户查询地址，需要商户根据文档自行实现，请勿直接使用此地址进行初始化
		String url;
		if (isDebug) {
			url = "http://192.168.2.95/DemoHeepay/SDK/SDKQuery.aspx";
		} else {
			url = "http://211.103.157.45/DemoHeepayTest/SDK/SDKQuery.aspx";
		}
		postQueryData(url, pairs);
	}

	/**
	 * 初始化接口调用
	 * 
	 * @param url
	 *            初始化url
	 * @param pairs
	 *            初始化参数
	 */
	private void postInitData(final String url, final List<NameValuePair> pairs) {
		// progressDialog = ProgressDialog.show(this, "", "初始化...", false, true,
		// cancelListener);
		new Thread() {

			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 60000);
					client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 60000);

					HttpPost mPost = new HttpPost(url);
					mPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
					HttpResponse response = client.execute(mPost);
					HttpEntity responseEntity = null;
					if (response.getStatusLine().getStatusCode() == 200) {
						responseEntity = response.getEntity();
						InputStream is = responseEntity.getContent();
						InputStreamReader br = new InputStreamReader(is);
						_paymentInfo = new PaymentInfo();
						_paymentInfo = ParseInitReturnData(br, _paymentInfo);
						Message msg = Message.obtain();
						msg.obj = _paymentInfo;
						msg.what = INIT_RESULT;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * 查询接口调用
	 * 
	 * @param url
	 *            查询url
	 * @param pairs
	 *            查询参数(只需要一个商户订单号)
	 */
	private void postQueryData(final String url, final List<NameValuePair> pairs) {
		// progressDialog = ProgressDialog.show(this, "", "正在查询...", false,
		// true, cancelListener);
		new Thread() {
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
					client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, 60000);
					// 连接超时60s
					client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 60000);
					HttpPost mPost = new HttpPost(url);
					mPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
					HttpResponse response = client.execute(mPost);
					HttpEntity responseEntity = null;
					if (response.getStatusLine().getStatusCode() == 200) {
						responseEntity = response.getEntity();
						InputStream is = responseEntity.getContent();
						InputStreamReader br = new InputStreamReader(is);
						PaymentInfo paymentInfo = new PaymentInfo();
						paymentInfo = ParseQueryReturnData(br, paymentInfo);
						Message msg = Message.obtain();
						msg.obj = paymentInfo;
						msg.what = QUERY_RESULT;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * 解析出初始化接口所需的参数
	 * 
	 * @param br
	 * @param info
	 * @return
	 * @throws Exception
	 */
	private PaymentInfo ParseInitReturnData(InputStreamReader br, PaymentInfo info) throws Exception {
		XmlPullParser xmlParser = Xml.newPullParser();
		xmlParser.setInput(br);
		// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
		int evtType = xmlParser.getEventType();
		StringBuilder sb = new StringBuilder();
		while (evtType != XmlPullParser.END_DOCUMENT) {
			if (evtType == XmlPullParser.START_TAG) {
				String tag = xmlParser.getName();
				sb.append("<" + tag + ">");
				String strTextValue = "";
				if (tag.equals("HasError")) {
					strTextValue = xmlParser.nextText();
					info.setHasError(!strTextValue.equalsIgnoreCase("false"));
				} else if (tag.equals("Message")) {
					strTextValue = xmlParser.nextText();
					// Log.v(SHOW_TAG, "Message:" + strTextValue);
					info.setMessage(strTextValue);
				} else if (tag.equals("TokenID")) {
					strTextValue = xmlParser.nextText();
					info.setTokenID(strTextValue);
					// Log.v(SHOW_TAG, "TokenID:" + info.getTokenID());
				} else if (tag.equals("AgentID")) {
					strTextValue = xmlParser.nextText();
					info.setAgentId(strTextValue);
					// Log.v(SHOW_TAG, "AgentId:" + info.getAgentId());
				} else if (tag.equals("AgentBillID")) {
					strTextValue = xmlParser.nextText();
					_agentBillNo = strTextValue;
					info.setBillNo(strTextValue);
					// Log.v(SHOW_TAG, "AgentBillID:" + info.getBillNo());
				}
				sb.append(strTextValue);

			} else if (evtType == XmlPullParser.END_TAG) {
				String tag = xmlParser.getName();
				sb.append("<" + tag + "/>");

			}
			// 如果xml没有结束，则导航到下一个river节点
			evtType = xmlParser.next();
		}
		System.out.println(sb.toString());
		return info;
	}

	/**
	 * 解析出查询接口所需的参数
	 * 
	 * @param br
	 * @param info
	 * @return
	 * @throws Exception
	 */
	private PaymentInfo ParseQueryReturnData(InputStreamReader br, PaymentInfo info) throws Exception {
		XmlPullParser xmlParser = Xml.newPullParser();
		xmlParser.setInput(br);
		// 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
		int evtType = xmlParser.getEventType();
		StringBuilder sb = new StringBuilder();

		while (evtType != XmlPullParser.END_DOCUMENT) {
			if (evtType == XmlPullParser.START_TAG) {
				String tag = xmlParser.getName();
				sb.append("<" + tag + ">");

				String strTextValue = "";
				if (tag.equals("BillInfo")) {
					strTextValue = xmlParser.nextText();
					info.setBillInfo(strTextValue);
					// Log.v(SHOW_TAG, "BillInfo:" + info.getBillInfo());
				} else if (tag.equals("Message")) {
					strTextValue = xmlParser.nextText();
					// Log.v(SHOW_TAG, "Message:" + strTextValue);
					info.setMessage(strTextValue);
				}
				sb.append(strTextValue);

			}
			// 如果xml没有结束，则导航到下一个river节点
			evtType = xmlParser.next();
		}
		System.out.println(sb.toString());

		return info;
	}

	/**
	 * Dialog监听器
	 */
	protected OnCancelListener cancelListener = new OnCancelListener() {
		public void onCancel(DialogInterface dlg) {
			dlg.dismiss();
		}
	};

	public static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create HEX String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String sTmp = Integer.toHexString(0xFF & messageDigest[i]);
				switch (sTmp.length()) {
				case 0:
					hexString.append("00");
					break;
				case 1:
					hexString.append("0");
					hexString.append(sTmp);
					break;
				default:
					hexString.append(sTmp);
					break;
				}
			}

			return hexString.toString().toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "";
	}

	private String generateUserIdentity() {
		TelephonyManager tm = (TelephonyManager) getSystemService(android.content.Context.TELEPHONY_SERVICE);
		String m_sPhoneID = tm.getDeviceId();
		if (!TextUtils.isEmpty(m_sPhoneID)) {
			return md5(m_sPhoneID);
		} else {
			return md5(UUID.randomUUID().toString());
		}
	}
}
