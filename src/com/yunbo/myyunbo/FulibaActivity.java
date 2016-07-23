package com.yunbo.myyunbo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.ab.activity.AbActivity;
import com.ab.task.AbTask;
import com.ab.task.AbTaskItem;
import com.ab.task.AbTaskObjectListener;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbToastUtil;
import com.yunbo.control.DyUtil;
import com.yunbo.control.FanHaoUtil;
import com.yunbo.control.HistoryUtil;
import com.yunbo.control.Yy97Util;
import com.yunbo.mode.PageContent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("JavascriptInterface")
public class FulibaActivity extends AbActivity {

	private WebView webview;
	private ProgressBar progressbar;

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAbContentView(R.layout.activity_web);
		((Button) findViewById(R.id.button1))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						addAClickListner();
					}
				});
		((Button) findViewById(R.id.button1)).setText("��ȡ���źʹ���");
		  ((Button) findViewById(R.id.button1)).setVisibility(View.GONE);
		webview = (WebView) findViewById(R.id.webview);
		progressbar = (ProgressBar) findViewById(R.id.progressBar1);

		GridView gridView = (GridView) findViewById(R.id.mGridView);
		gridView.setVisibility(View.GONE);
		WebSettings webSettings = webview.getSettings();
		// ����WebView���ԣ��ܹ�ִ��Javascript�ű�
		webSettings.setJavaScriptEnabled(true);
		// ���ÿ��Է����ļ�
		webSettings.setAllowFileAccess(true);
		// ����֧������
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setAppCacheEnabled(true);

		webview.addJavascriptInterface(new MyJavascriptInterface(), "alistner");
		// ����Web��ͼ
		webview.setWebViewClient(new MyWebViewClient());
		// ����setWebChromeClient����
		webview.setWebChromeClient(new MyWebViewChromeClient());
		// ������Ҫ��ʾ����ҳ
		webview.loadUrl(baseurl);
		// getYunflvurl();
	}

	private class MyWebViewChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (newProgress == 100) {
				progressbar.setVisibility(View.GONE);
			} else {
				if (progressbar.getVisibility() == View.GONE)
					progressbar.setVisibility(View.VISIBLE);
				progressbar.setProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}

	};

	@Override
	// ���û���
	// ����Activity���onKeyDown(int keyCoder,KeyEvent event)����
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack(); // goBack()��ʾ����WebView����һҳ��
			return true;
		}
		finish();// �����˳�����
		return false;
	}

	String baseurl = "http://m.fuliba.net/";
	boolean finish = false;

	// Web��ͼ
	private class MyWebViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
//			if (!finish)
//				addAClickListner();
//			finish = true;
			super.onPageFinished(view, url);
		}

		public boolean shouldOverrideUrlLoading(final WebView view, String url) {
			
			baseurl = url;
			finish = false;
			if (baseurl.startsWith("fh://")) {

				Bundle bundle = new Bundle();
				bundle.putString("key", baseurl.substring(5));
				Intent intent = new Intent();
				intent.setClass(FulibaActivity.this, Ed2kSSActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				return !finish;
			}
			if (baseurl.startsWith("magnet:?xt=urn:btih:")) {

				String urlx=DyUtil.changeED2K(baseurl); 
				if (TextUtils.isEmpty(urlx)) {
					return true;
				}
				DyUtil.toGetPlayUrl(urlx, FulibaActivity.this);
				return !finish;
			}
//			view.loadUrl(url);
			final Context packageContext=FulibaActivity.this;
			AbDialogUtil.showProgressDialog(packageContext,
					R.drawable.progress_circular, "���ڼ�������...");
			AbTask mAbTask = new AbTask();
			final AbTaskItem item = new AbTaskItem();
			item.setListener(new AbTaskObjectListener() {

				@Override
				public void update(Object obj) {
					// TODO Auto-generated method stub
					 AbDialogUtil.removeDialog(packageContext);

					 html = obj.toString();
//					View view1 = mInflater.inflate(R.layout.dialog_edit_button1, null); 
//					final EditText urlText = (EditText) view1.findViewById(R.id.editTextzb);
//					AbDialogUtil.showAlertDialog(view1);
//					urlText.setText(baseurl+"\r\n"+html);
					Matcher m = Pattern
							.compile("([A-Za-z]{2,5})-(\\d{2,5})").matcher(html);
					StringBuffer sb = new StringBuffer();
					int i = 0;
					// ʹ��find()�������ҵ�һ��ƥ��Ķ���
					boolean result = m.find();
					// ʹ��ѭ�������������е�kelvin�ҳ����滻�ٽ����ݼӵ�sb��
					while (result) {
						i++;
						m.appendReplacement(sb,
								String.format("<a href='fh://%s'>%s</a>", m.group(), m.group()));
						// ����������һ��ƥ�����
						result = m.find();
					}
					// ������appendTail()���������һ��ƥ����ʣ���ַ����ӵ�sb�
					m.appendTail(sb);
					m = Pattern
							.compile("[A-Za-z0-9]{40}").matcher(sb.toString());
					  sb = new StringBuffer();
					  i = 0;
					// ʹ��find()�������ҵ�һ��ƥ��Ķ���
					  result = m.find();
					// ʹ��ѭ�������������е�kelvin�ҳ����滻�ٽ����ݼӵ�sb��
					while (result) {
						i++;
						m.appendReplacement(sb,
								String.format("<a href='magnet:?xt=urn:btih:%s'>%s</a>", m.group(), m.group()));
						// ����������һ��ƥ�����
						result = m.find();
					}
					// ������appendTail()���������һ��ƥ����ʣ���ַ����ӵ�sb�
					m.appendTail(sb);
					//AbToastUtil.showToast(FulibaActivity.this, "" + i);
					//if (i > 0)
						webview.loadDataWithBaseURL(baseurl, sb.toString(),
								"text/html", "UTF-8", null);
					//else view.loadUrl(baseurl);
				}

				@Override
				public Object getObject() {
					// TODO Auto-generated method stub 
					try {
						Document doc=Jsoup.connect(baseurl).get();
						return doc.html();
					} catch (Exception e) {
						// TODO: handle exception
					}
					return "";
				}
			});

			mAbTask.execute(item);
			return true;
		}

	}

	String html = "";

	// jsͨ�Žӿ�
	final private class MyJavascriptInterface {

		@JavascriptInterface
		public void openLink(String str) {
			// Toast.makeText(WebActivity.this, str, Toast.LENGTH_LONG).show();
			
		}

	}

	// ע��js��������
	private void addAClickListner() {
		webview.loadUrl("javascript:window.alistner.openLink('<head>'+"
				+ "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
	}
}
