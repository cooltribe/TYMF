package com.searun.TYMF;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
//关于android2.3中javascript交互的问题
//http://code.google.com/p/android/issues/detail?id=12987
@SuppressLint("JavascriptInterface")
public class WebViewLocalActivity extends Activity {
	private WebView webView = null;
	private Handler handler = new Handler();
	private Button button = null;
	
	final class InJavaScript {
        public void runOnAndroidJavaScript(final String str) {
        	handler.post(new Runnable() {
                public void run() { 
                    TextView show = (TextView) findViewById(R.id.textview);
                    show.setText(str);
                }
            });
        }
    }
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	//调用javascript中的方法，传入string数据
//                webView.loadUrl("javascript:getFromAndroid('the data is from android!')");
                webView.loadUrl("javascript:getFromAndroid('" + "the data is from android!sssssssssss" +"')");
            }
        });
        
        
        webView = (WebView) findViewById(R.id.webview);
        
        //把本类的一个实例添加到js的全局对象window中，
        //这样就可以使用window.injs来调用它的方法
        webView.addJavascriptInterface(new InJavaScript(), "injs");
        
		//设置支持JavaScript脚本
		WebSettings webSettings = webView.getSettings();  
		webSettings.setJavaScriptEnabled(true);
		//设置可以访问文件
		webSettings.setAllowFileAccess(true);
		//设置支持缩放
		webSettings.setBuiltInZoomControls(true);
		
		webSettings.setDatabaseEnabled(true);  
		String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		webSettings.setDatabasePath(dir);
		
		//使用localStorage则必须打开
		webSettings.setDomStorageEnabled(true);
		
		webSettings.setGeolocationEnabled(true);
		//webSettings.setGeolocationDatabasePath(dir);
		
		
		//设置WebViewClient
		webView.setWebViewClient(new WebViewClient(){   
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {   
		        view.loadUrl(url);   
		        return true;   
		    }  
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
		});
		
		
		//设置WebChromeClient
		webView.setWebChromeClient(new WebChromeClient(){
			//处理javascript中的alert
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				//构建一个Builder来显示网页中的对话框
				Builder builder = new Builder(WebViewLocalActivity.this);
				builder.setTitle("Alert");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			//处理javascript中的confirm
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				Builder builder = new Builder(WebViewLocalActivity.this);
				builder.setTitle("confirm");
				builder.setMessage(message);
				builder.setPositiveButton(android.R.string.ok,
						new AlertDialog.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						});
				builder.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								result.cancel();
							}
						});
				builder.setCancelable(false);
				builder.create();
				builder.show();
				return true;
			};
			
			@Override
			//设置网页加载的进度条
			public void onProgressChanged(WebView view, int newProgress) {
				WebViewLocalActivity.this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
				super.onProgressChanged(view, newProgress);
			}

			//设置应用程序的标题title
			public void onReceivedTitle(WebView view, String title) {
				WebViewLocalActivity.this.setTitle(title);
				super.onReceivedTitle(view, title);
			}

			public void onExceededDatabaseQuota(String url,
					String databaseIdentifier, long currentQuota,
					long estimatedSize, long totalUsedQuota,
					WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(estimatedSize * 2);
			}
			
			public void onGeolocationPermissionsShowPrompt(final String origin,
					final GeolocationPermissions.Callback callback) {
				super.onGeolocationPermissionsShowPrompt(origin, callback);
				Log.i("获取GPS", "获取GPS获取GPS获取GPS获取GPS获取GPS获取GPS");
//				callback.invoke(origin, true, false);
				AlertDialog.Builder builder = new AlertDialog.Builder(WebViewLocalActivity.this);
				builder.setTitle("Locations");
				builder.setMessage(origin + " Would like to use your Current Location").setCancelable(true).setPositiveButton("Allow",
				                            new DialogInterface.OnClickListener() {
				                                @Override
				                                public void onClick(DialogInterface dialog,
				                                        int id) {
				                                    // origin, allow, remember
				                                    callback.invoke(origin, true, false);
				                                }
				                            })
				                    .setNegativeButton("Don't Allow",
				                            new DialogInterface.OnClickListener() {
				                                @Override
				                                public void onClick(DialogInterface dialog,
				                                        int id) {
				                                    // origin, allow, remember
				                                    callback.invoke(origin, false, true);
				                                }
				                            });
				AlertDialog alert = builder.create();
				alert.show();
			}
			
			public void onReachedMaxAppCacheSize(long spaceNeeded,
					long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(spaceNeeded * 2);
			}
		});
		// 覆盖默认后退按钮的作用，替换成WebView里的查看历史页面  
		webView.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if ((keyCode == KeyEvent.KEYCODE_BACK)
							&& webView.canGoBack()) {
						webView.goBack();
						return true;
					}
				}
				return false;
			}
		});
		
		webView.loadUrl("file:///android_asset/local.html");
    } 
}