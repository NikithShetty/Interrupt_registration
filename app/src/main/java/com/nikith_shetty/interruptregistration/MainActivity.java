package com.nikith_shetty.interruptregistration;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private String protocol = "http://";
    private String webhost = "interrupt.tk/loginInfo.html";

    final static int MY_PERMISSIONS_REQUEST_INTERNET = 52141;
    boolean showOnlyRefresh = false;

    SwipeRefreshLayout refreshLayout;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        checkPermissions();

        refreshLayout.setRefreshing(true);
        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(protocol + webhost );
        webView.addJavascriptInterface(new WebViewInterface(this), "Android");
        webView.setWebViewClient(webViewClient);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!showOnlyRefresh)
                    webView.reload();
                else
                    showOnlyRefresh = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshLayout.setRefreshing(true);
                webView.reload();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private long lastPressedTime;
    private static final int PERIOD = 2000;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getDownTime() - lastPressedTime < PERIOD) {
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Press again to exit.",
                                Toast.LENGTH_SHORT).show();
                        lastPressedTime = event.getEventTime();
                    }
                    return true;
            }
        }
        return false;
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }
    }

    public class WebViewInterface{
        Context context;
        WebViewInterface(Context c){
            context = c;
        }

        @JavascriptInterface
        public void pageLoaded(){
            Log.e(TAG, "pageLoaded()");
            //Toast.makeText(MainActivity.this, "pageLoaded()", Toast.LENGTH_SHORT).show();
        }
    }

    WebViewClient webViewClient = new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            showOnlyRefresh = true;
            refreshLayout.setRefreshing(true);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            refreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],
                                           int[] grantResults){
        if(requestCode == MY_PERMISSIONS_REQUEST_INTERNET){
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission granted
                webView.loadUrl(protocol + webhost );
            } else {
                //Permission Denied
                finish();
            }
        }
    }
}
