package com.truelife.app.viewer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.truelife.R;
import com.truelife.base.BaseFragment;
import com.truelife.base.TLFragmentManager;
import com.truelife.util.AppDialogs;

import org.jetbrains.annotations.NotNull;


public class TLDocFileViewer extends BaseFragment {

    public static final String TAG = TLDocFileViewer.class.getSimpleName();

    private int ViewSize = 0;
    private String myPathStr;

    private TLFragmentManager myFragmentManager;
    private ImageView myBackIcon;
    WebView wv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.docu_viewer, container,
                false);
        myPathStr = getArguments().getString("FILE_PATH");
        classAndWidgetInitialize(rootView);
        return rootView;
    }


    private void classAndWidgetInitialize(View aView) {

        try {
            myFragmentManager = new TLFragmentManager(getActivity());

            String doc = "http://docs.google.com/gview?embedded=true&url=" + myPathStr;

            Log.e("Doc", doc);
            wv = aView.findViewById(R.id.webView);
            home(doc);
            myBackIcon = aView.findViewById(R.id.back_icon);
            myBackIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    myFragmentManager.onBackPress();
                }
            });


            loadPdfFile();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void home(String doc) {
        WebSettings webSetting = wv.getSettings();

        webSetting.setBuiltInZoomControls(true);
        webSetting.setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.loadUrl(doc);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onResumeFragment() {

    }

    @Override
    public void init(@NotNull View view) {

    }

    @Override
    public void initBundle() {

    }

    @Override
    public void clickListener() {

    }


    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            AppDialogs.INSTANCE.showProgressDialog(getActivity());
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


    private void loadPdfFile() {


    }

}
