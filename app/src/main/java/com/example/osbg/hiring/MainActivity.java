package com.example.osbg.hiring;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.http.*;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public WebView mWebView, mWebViewPop;
    public RelativeLayout mContainer;
    private Context mContext;
    private String deviceLanguage = "";
    public ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isInternetAvailable(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_net);
            builder.setMessage("Please check your settings first and restart the app.")
                    .setCancelable(false)
                    .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        }
                    });

            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorWhite));
                }
            });
            alertDialog.show();
        } else {
            mWebView = (WebView) findViewById(R.id.webview);
            mContainer = (RelativeLayout) findViewById(R.id.webview_frame);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
            loadingBar.setVisibility(View.VISIBLE);

            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);

            WebSettings webSettings = mWebView.getSettings();

            mWebView.setScrollbarFadingEnabled(true);
            mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mWebView.setWebViewClient(new CustomWebViewClient());
            mWebView.setWebChromeClient(new CustomWebChromeClient());
            /*mWebView.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeRight() {
                    if(mWebView.canGoBack()) {
                        mWebView.goBack();
                    }
                }

                @Override
                public void onSwipeLeft() {
                    if(mWebView.canGoForward()) {
                        mWebView.goForward();
                    }
                }
            });*/
            deviceLanguage = Locale.getDefault().getISO3Language().substring(0, 2);
            Log.d("devlang", "https://hiring.bg/?lang=_" + deviceLanguage);
            webSettings.setJavaScriptEnabled(true);
            webSettings.setAppCacheEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webSettings.setDomStorageEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setSupportMultipleWindows(true);

            mWebView.setWebViewClient(new CustomWebViewClient());
            mWebView.setWebChromeClient(new CustomWebChromeClient());

            mContext = this.getApplicationContext();

            switch (deviceLanguage) {
                case "bu": mWebView.loadUrl("https://hiring.bg/?lang=_bg"); break;
                case "de": mWebView.loadUrl("https://hiring.bg/?lang=_de"); break;
                case "fr": mWebView.loadUrl("https://hiring.bg/?lang=_fr"); break;
                case "it": mWebView.loadUrl("https://hiring.bg/?lang=_it"); break;
                case "sp": mWebView.loadUrl("https://hiring.bg/?lang=_es"); break;
                case "nl": mWebView.loadUrl("https://hiring.bg/?lang=_nl"); break;
                case "ru": mWebView.loadUrl("https://hiring.bg/?lang=_ru"); break;
                default: mWebView.loadUrl("https://hiring.bg/?lang="); break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static boolean isInternetAvailable(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public class CustomWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loadingBar.setVisibility(View.GONE);
            if (url.startsWith("https://m.facebook.com/v2.8/dialog/oauth")) {
                if (mWebViewPop != null) {
                    mWebViewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebViewPop);
                    mWebViewPop = null;
                }
                view.loadUrl("https://www.hiring.bg");
                return;
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d("onReceivedSslError", error.toString());
            handler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!isInternetAvailable(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), R.string.no_net, Toast.LENGTH_SHORT).show();
            } else {
                String host = Uri.parse(url).getHost();

                if (host.equals("hiring.bg")) {
                    if (mWebViewPop != null) {
                        mWebViewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebViewPop);
                        mWebViewPop = null;
                    }
                    return false;
                }
                if (host.equals("m.facebook.com") || host.equals("www.facebook.com") || host.equals("facebook.com") || host.equals("mobile.facebook.com")) {
                    return false;
                }
            }
            // Open the browser to handle a link
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url ));
            startActivity(browserIntent);
            mWebViewPop.setVisibility(View.GONE);
            mContainer.removeView(mWebViewPop);
            mWebViewPop = null;
            return true;
        }
    }

    public class CustomWebChromeClient extends  WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            loadingBar.setProgress(newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            mWebViewPop = new WebView(mContext);
            mWebViewPop.setVerticalScrollBarEnabled(false);
            mWebViewPop.setHorizontalScrollBarEnabled(false);
            mWebViewPop.setWebViewClient(new CustomWebViewClient());
            mWebViewPop.setWebChromeClient(new CustomWebChromeClient());
            mWebViewPop.getSettings().setJavaScriptEnabled(true);
            mWebViewPop.getSettings().setSupportMultipleWindows(true);
            mWebViewPop.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebViewPop.getSettings().setLoadWithOverviewMode(true);
            mWebViewPop.getSettings().setUseWideViewPort(true);
            mWebViewPop.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebViewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebViewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "window closed!");
        }
    }
}

