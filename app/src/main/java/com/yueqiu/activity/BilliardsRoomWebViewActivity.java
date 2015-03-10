package com.yueqiu.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

/**
 * 球厅详情Activity，现在应要求需要改成WebView的实现形式
 *
 */
public class BilliardsRoomWebViewActivity extends Activity
{
    private static final String TAG = "BilliardsRoomWebViewActivity";
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiards_room_web_view);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        // TODO: 我们最好在这里提供一个默认的Url，但是这个Url的内容还需要制定需求的SX来提供
        String pageUrl = getIntent().getStringExtra(NearbyFragmentsCommonUtils.KEY_ROOM_WEBVIEW_PAGE_URL);

        WebView webView = (WebView) findViewById(R.id.room_activity_webview);
        WebSettings webSettings = webView.getSettings();
        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);

        webSettings.setJavaScriptEnabled(true);


        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);
        webView.loadUrl(pageUrl);
    }
    private ActionBar mActionBar;

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private  class WebViewClientImpl extends WebViewClient
    {
        private Activity mActivity;

        public WebViewClientImpl(Activity activity)
        {
            this.mActivity = activity;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiards_room_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in, R.anim.top_out);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
