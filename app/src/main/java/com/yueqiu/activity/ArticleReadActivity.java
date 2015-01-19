package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.yueqiu.R;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ArticleReadActivity extends Activity {
    private WebView mWebView;
    private String mData;
    private String mUrl;
    private ProgressBar mPreProgress;
    private Drawable mProgressDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_read);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.usage_and_article));
        actionBar.setDisplayHomeAsUpEnabled(true);

        mWebView = (WebView) findViewById(R.id.article_webview);
        WebSettings wSet = mWebView.getSettings();
        wSet.setJavaScriptEnabled(true);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
        mPreProgress.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");

        new MyTask().execute();



    }

    private class MyTask extends AsyncTask<Void,Void,String>{


        @Override
        protected String doInBackground(Void... params) {
            mData = getFromAssets("policy.html");
            return mData;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mHandler.obtainMessage(0,result).sendToTarget();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPreProgress.setVisibility(View.VISIBLE);
        }
    }



    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mWebView.loadDataWithBaseURL(mUrl, (String)msg.obj, "text/html", "utf-8", null);
            mPreProgress.setVisibility(View.GONE);
        }
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(
                    getResources().getAssets().open(fileName));

            BufferedReader bufReader = new BufferedReader(inputReader);

            String line;
            String result = "";

            while ((line = bufReader.readLine()) != null)
                result += line;
            if (bufReader != null)
                bufReader.close();
            if (inputReader != null)
                inputReader.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


