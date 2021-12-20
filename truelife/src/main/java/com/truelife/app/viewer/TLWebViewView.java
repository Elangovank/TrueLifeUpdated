package com.truelife.app.viewer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.truelife.R;
import com.truelife.base.BaseActivity;
import com.truelife.base.TLFragmentManager;
import com.truelife.util.AppDialogs;

import org.jetbrains.annotations.Nullable;


public class TLWebViewView extends BaseActivity {

    public static final String TAG = TLWebViewView.class.getSimpleName();
    WebView wv;
    private int ViewSize = 0;
    private String myPathStr;
    private TLFragmentManager myFragmentManager;
    private ImageView myBackIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_viewer);
        init();
    }

    public void home(String doc) {
        WebSettings webSetting = wv.getSettings();
        webSetting.setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl(doc);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void clickListener() {

    }

    @Override
    public void init() {
        try {
            myFragmentManager = new TLFragmentManager(myContext);

            if (getIntent().hasExtra("url"))
                myPathStr = getIntent().getStringExtra("url");

            wv = findViewById(R.id.webView);
            home(myPathStr);
            myBackIcon = findViewById(R.id.back_icon);
            myBackIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


            loadPdfFile();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadPdfFile() {


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (wv.canGoBack()) {
                        wv.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            AppDialogs.INSTANCE.showProgressDialog(myContext);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            AppDialogs.INSTANCE.hideProgressDialog();
        }

    }
}
