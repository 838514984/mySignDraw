package com.example.administrator.mywebviewdrawsign;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    SysWebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (SysWebView) findViewById(R.id.webview);
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl("file:///android_asset/sign/draw.html");
        findViewById(R.id.btn_thim).setOnClickListener(this);
        findViewById(R.id.btn_normal).setOnClickListener(this);
        findViewById(R.id.btn_blod).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_thim:
                mWebView.loadUrl("javascript:changeBold(0.5,2.5)");
                break;
            case R.id.btn_normal:
                mWebView.loadUrl("javascript:changeBold(2,5)");
                break;
            case R.id.btn_blod:
                mWebView.loadUrl("javascript:changeBold(5,8)");
                break;
            case R.id.clear:
                mWebView.loadUrl("javascript:clear()");
                break;
        }
    }
}
