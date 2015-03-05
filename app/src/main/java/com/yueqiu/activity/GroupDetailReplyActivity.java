package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GroupDetailReplyActivity extends Activity {

    private int mNoteId;
    private EditText mEditText;

    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_reply);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(getString(R.string.reply1));
        mNoteId = getIntent().getIntExtra("id",-1);
        mEditText = (EditText) findViewById(R.id.group_reply_edit);
        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);
        mPreTextView.setText(getString(R.string.feed_backing));

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_reply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.group_reply_item) {
            Utils.dismissInputMethod(this,mEditText);
            if(Utils.networkAvaiable(this)){
                reply();
            }else{
                Utils.showToast(this,getString(R.string.network_not_available));
            }

        }else if(id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
        }

        return true;
    }

    private void reply(){



        if(TextUtils.isEmpty(mEditText.getText().toString())){
            Utils.showToast(GroupDetailReplyActivity.this,getString(R.string.reply_content_can_not_empty));
            return;
        }

        Map<String,String> params = new HashMap<String, String>();
        params.put(HttpConstants.Reply.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        params.put(HttpConstants.Reply.TID,String.valueOf(mNoteId));
        params.put(HttpConstants.Reply.CONTENT,mEditText.getText().toString());

        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.Reply.URL,params,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","reply response is ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.obtainMessage(PublicConstant.GET_SUCCESS,response.getJSONObject("result").getString("create_time")).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
            }
        });

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch (msg.what){
                case PublicConstant.GET_SUCCESS:
                    Intent intent = new Intent();
                    intent.putExtra("create_time",(String)msg.obj);
                    intent.putExtra("content",mEditText.getText().toString());
                    setResult(RESULT_OK,intent);
                    finish();
                    overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(GroupDetailReplyActivity.this, getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(GroupDetailReplyActivity.this, (String) msg.obj);
                    }
                    break;
            }
        }
    };
}
