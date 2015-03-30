package com.yueqiu.activity;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.NewPhotoAdapter;
import com.yueqiu.bean.INewPhotoItem;
import com.yueqiu.bean.NewPhotoAddImage;
import com.yueqiu.bean.NewPhotoShow;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ActionBar;

public class FriendNewPhotoActivity extends FragmentActivity {
    private GridView mGridView;
    private NewPhotoAdapter mAdapter;
    private ProgressBar mPreProgressBar;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;

    private List<INewPhotoItem> mList = new ArrayList<INewPhotoItem>();
    private List<String> mImgUrlList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_new_photo);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.the_new_post));

        int user_id = getIntent().getIntExtra("user_id",0);
        mGridView = (GridView) findViewById(R.id.friend_new_photo_grid);
        mPreProgressBar = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);
        mPreTextView.setText(getString(R.string.feed_backing));

        if(Utils.networkAvaiable(this)){
            getImgList(user_id);
        }else{
            Toast.makeText(this,getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onOptionsItemSelected(item);
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
    private void getImgList(int user_id){
        Map<String,Integer> param = new HashMap<String,Integer>();

        param.put(HttpConstants.GET_NEW_PHOTO.USER_ID, user_id);
        Log.d("wy", "new photo para is ->" + param);

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.GET_NEW_PHOTO.URL, param, HttpConstants.RequestMethod.GET, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy", "new photo response ->" + response);
                try {
                    if (!response.isNull("code")) {
                        if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                            if (!response.get("result").toString().equals("null")) {
                                JSONArray result = response.getJSONArray("result");
                                for (int i = 0; i < result.length(); i++) {
                                    String img_url = result.getJSONObject(i).getString("img_url");
                                    mImgUrlList.add(img_url);
                                }
                                mHandler.sendEmptyMessage(PublicConstant.GET_SUCCESS);
                            }
                        } else {
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR, response.getString("msg")).sendToTarget();
                        }
                    } else {
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
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
            mPreTextView.setVisibility(View.GONE);
            mPreProgressBar.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    for(int i=mImgUrlList.size() - 1 ; i >= 0 ; i--){
                        NewPhotoShow shotItem = new NewPhotoShow();
                        shotItem.setImgUrl(mImgUrlList.get(i));
                        mList.add(0,shotItem);
                    }
                    mAdapter = new NewPhotoAdapter(FriendNewPhotoActivity.this,mList,null);
                    mGridView.setAdapter(mAdapter);
//                    mAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj != null){
                        Utils.showToast(FriendNewPhotoActivity.this, (String) msg.obj);
                    }else {
                        Utils.showToast(FriendNewPhotoActivity.this, getString(R.string.http_request_error));
                    }
                    break;
                case PublicConstant.NO_NETWORK:
                    Toast.makeText(FriendNewPhotoActivity.this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
                    break;

            }


        }
    };

}
