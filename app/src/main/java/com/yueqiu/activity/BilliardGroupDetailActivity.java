package com.yueqiu.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.GroupDetailContentAdapter;
import com.yueqiu.bean.GroupDetailCommentItem;
import com.yueqiu.bean.GroupDetailContentItem;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.IGroupDetailItem;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BilliardGroupDetailActivity extends Activity implements View.OnClickListener{

    private static final int PRAISE_SUCCESS = 11;
    public static final int REPLY_CODE = 1;

    private View mPraiseView,mReplyView;
    private TextView mTvYueqiu, mTvYueqiuCircle, mTvFriendCircle,
            mTvWeichat, mTvQQZone, mTvTencentWeibo, mTvSinaWeibo, mTvRenren;
    private CustomNetWorkImageView mOwnerImg;//mExtraImg;
    private TextView mOwnerTv,mOwnerSexTv,mReadCountTv,mCreateTimeTv,mPaiseCountTv,
            mReplyCountTv;//mTitleTv,mContentTv;
//    private LinearLayout mContentContainer;
    private Button mBtnCancel;
    private Dialog mShareDlg;
    private int mNoteId,mReplyCount;
    private ProgressBar mPreProgress;
    private TextView mPreTextView;
    private Drawable mProgressDrawable;
    private GroupNoteInfo mGroupInfo;
    private ImageLoader mImageLoader;
    private ListView mListView;
    private GroupDetailContentAdapter mContentAdapter;


    private List<IGroupDetailItem> mDetailItemList = new ArrayList<IGroupDetailItem>();
    private Map<String,Integer> mParamsMap = new HashMap<String,Integer>();
    private Map<String,String>  mUrlAndMethodMap = new HashMap<String, String>();
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiard_group_detail);
        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.billiard_group_detail));

        initView();
        mImageLoader = VolleySingleton.getInstance().getImgLoader();
        Bundle args = getIntent().getExtras();
        mNoteId = args.getInt(DatabaseConstant.GroupInfo.NOTE_ID);
//        mReplyCount = args.getInt(DatabaseConstant.GroupInfo.COMMENT_COUNT);

        if(Utils.networkAvaiable(this)){
            requestDetail();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }

    }



    private void initView(){
        mPraiseView = findViewById(R.id.billiard_group_praise_view);
        mReplyView = findViewById(R.id.billiard_group_reply_view);

        mOwnerImg = (CustomNetWorkImageView) findViewById(R.id.billiard_group_detail_img);
        mOwnerImg.setDefaultImageResId(R.drawable.default_head);
        mOwnerImg.setErrorImageResId(R.drawable.default_head);

//        mExtraImg = (NetworkImageView) findViewById(R.id.group_detail_extra_img);
        mOwnerTv = (TextView) findViewById(R.id.billiard_group_detail_owner);
        mOwnerSexTv = (TextView) findViewById(R.id.billiard_group_detail_ower_sex);
        mReadCountTv = (TextView) findViewById(R.id.billiard_group_detail_read_count_tv);
        mCreateTimeTv = (TextView) findViewById(R.id.billiard_group_detail_publish_time);
        mPaiseCountTv = (TextView) findViewById(R.id.billiard_group_detail_praise_count_tv);
        mReplyCountTv = (TextView) findViewById(R.id.billiard_group_detail_reply_count_tv);
//        mTitleTv = (TextView) findViewById(R.id.group_detail_title);
//        mContentTv = (TextView) findViewById(R.id.group_detail_content);
//        mContentContainer = (LinearLayout) findViewById(R.id.group_detail_content_container);
        mListView = (ListView) findViewById(R.id.group_detail_listview);

        mPreProgress = (ProgressBar) findViewById(R.id.pre_progress);
        mPreTextView = (TextView) findViewById(R.id.pre_text);

        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgress.getIndeterminateDrawable().getBounds();
        mPreProgress.setIndeterminateDrawable(mProgressDrawable);
        mPreProgress.getIndeterminateDrawable().setBounds(bounds);

        mPraiseView.setOnClickListener(this);
        mReplyView.setOnClickListener(this);
    }

    private void requestDetail(){

        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        //TODO:id值要根据intent传进来的id设置，这里先设为
        mParamsMap.put(HttpConstants.GroupDetail.ID,mNoteId);


        HttpUtil.requestHttp(HttpConstants.GroupDetail.URL,mParamsMap,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","group detail response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getString("result") != null) {
                                GroupNoteInfo info = setGroupInfo(response);
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS, info).sendToTarget();
                            }else{
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.REQUEST_ERROR){
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
            }
        });

//        mUrlAndMethodMap.put(PublicConstant.URL, HttpConstants.GroupDetail.URL);
//        mUrlAndMethodMap.put(PublicConstant.METHOD, HttpConstants.RequestMethod.GET);
//
//        new DetailRequestTask(mParamsMap).execute(mUrlAndMethodMap);
    }
    private GroupNoteInfo setGroupInfo(JSONObject object){
        GroupNoteInfo info = new GroupNoteInfo();
        try{
            JSONObject result = object.getJSONObject("result");
            info.setNoteId(Integer.parseInt(result.getString("id")));
            mNoteId = info.getNoteId();
            info.setUserName(result.getString("username"));
            info.setSex(Integer.valueOf(result.getString("sex")));
            info.setBrowseCount(result.getInt("look_number"));
            info.setIssueTime(result.getString("create_time"));
            info.setTitle(result.getString("title"));
            info.setContent(result.getString("content"));
            info.setLoveNums(result.getInt("number"));
            info.setCommentCount(result.getInt("commen_num"));
            mReplyCount = info.getCommentCount();
            Log.d("wy","group detail url ->" + result.getString("img_url"));
            info.setImg_url(result.getString("u_img_url"));
            //TODO:服务器那边还需要再加一个u_img_url
            info.setExtra_img_url(result.getString("img_url"));

            JSONArray comment_list = result.getJSONArray("comment_list");
            for(int i=0;i<comment_list.length();i++){
                UserInfo user = new UserInfo();
                user.setUsername(comment_list.getJSONObject(i).getString("username"));
                user.setImg_url(comment_list.getJSONObject(i).getString("u_img_url"));
                String sex = comment_list.getJSONObject(i).getString("sex");
                user.setSex(Integer.parseInt(sex.equals("null") ? "1" : sex));
                user.setContent(comment_list.getJSONObject(i).getString("content"));
                user.setComment_time(comment_list.getJSONObject(i).getString("create_time"));
                info.mCommentList.add(user);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return info;
    }

    private void updateUI(GroupNoteInfo info){
        //TODO:由于服务器那边目前没有传img_url，等传了，修正

        Log.d("wy","group detail img_url ->" + info.getImg_url());
        if (! TextUtils.isEmpty(info.getImg_url()))
        {
            mOwnerImg.setImageUrl("http://" + info.getImg_url(), mImageLoader);
        }
        mOwnerTv.setText(info.getUserName());
        mOwnerSexTv.setText(info.getSex() == 1 ? getString(R.string.man) : getString(R.string.woman));
        if(info.getSex() == 1){
            mOwnerSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.male, 0);
        }else{
            mOwnerSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.female, 0);
        }
        mReadCountTv.setText(String.valueOf(info.getBrowseCount()));
        mCreateTimeTv.setText(info.getIssueTime());
        mPaiseCountTv.setText(getString(R.string.praise,info.getLoveNums()));
        mReplyCountTv.setText(getString(R.string.reply,info.getCommentCount()));
//        mTitleTv.setText(info.getTitle());
//        mContentTv.setText(info.getContent());
        //TODO:在发布台球圈话题时，要上传图片，这里要根据返回的图片数，动态添加imageview，活动详情也一样是这个逻辑
//        mExtraImg.setImageUrl("http://" + info.getExtra_img_url(),mImageLoader);
        //TODO:更新中间的listview
        GroupDetailContentItem contentItem = new GroupDetailContentItem(info.getTitle(),info.getContent(),info.getExtra_img_url());
        mDetailItemList.add(contentItem);

        for(int i=0;i<info.mCommentList.size();i++){
            UserInfo user = info.mCommentList.get(i);
            GroupDetailCommentItem commentItem = new GroupDetailCommentItem(user.getUsername(),user.getImg_url(),user.getComment_time(),user.getContent());
            mDetailItemList.add(commentItem);
        }

        mContentAdapter = new GroupDetailContentAdapter(mDetailItemList,BilliardGroupDetailActivity.this);
        mListView.setAdapter(mContentAdapter);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mPreProgress.setVisibility(View.GONE);
            mPreTextView.setVisibility(View.GONE);
            switch(msg.what){
                case PublicConstant.GET_SUCCESS:
                    mGroupInfo = (GroupNoteInfo) msg.obj;
                    updateUI(mGroupInfo);
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(msg.obj == null){
                        Utils.showToast(BilliardGroupDetailActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(BilliardGroupDetailActivity.this, (String) msg.obj);
                    }
                    break;
                case PublicConstant.TIME_OUT:
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.http_request_time_out));
                    break;
                case PublicConstant.NO_NETWORK:
                    //TODO:如果用缓存的话，这里是有逻辑上的问题，但是目前不需要缓存，所以暂时没问题
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.network_not_available));
                    break;
                case PublicConstant.FAVOR_SUCCESS:
                    //TODO:如果有缓存功能的话，这里还得插入收藏的数据库
                    Intent shareIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                    sendBroadcast(shareIntent);
                    Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.store_success));
                    break;
                case PublicConstant.NO_RESULT:
                    //TODO:还有listview的emptyview
                    Utils.showToast(BilliardGroupDetailActivity.this,getString(R.string.no_group_detail_info));
                    break;
                case PRAISE_SUCCESS:
                    Integer count = (Integer) msg.obj;
                    mPaiseCountTv.setText(getString(R.string.praise,count));
                    break;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiard_group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }else if(id == R.id.billiard_detail_action_share)
        {
            YueQiuApp.sScreenBitmap = Utils.getCurrentScreenShot(mRootView);
            mShareDlg = Utils.showSheet(this, YueQiuApp.sScreenBitmap);
            mShareDlg.show();

        }else if(id == R.id.billiard_detail_action_collect){
            int user_id = YueQiuApp.sUserInfo.getUser_id();
            if(user_id < 1 ){
                Utils.showToast(this,getString(R.string.please_login_first));
            }else{
                if(Utils.networkAvaiable(this)){
                    store();
                }else{
                    mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int user_id = YueQiuApp.sUserInfo.getUser_id();
        switch(v.getId()){
            case R.id.billiard_group_praise_view:
                if(user_id < 1){
                    Utils.showToast(this, getString(R.string.please_login_first));
                }else {
                    if (Utils.networkAvaiable(BilliardGroupDetailActivity.this)) {
                        praise();
                    } else {
                        Utils.showToast(BilliardGroupDetailActivity.this, getString(R.string.network_not_available));
                    }
                }
                break;
            case R.id.billiard_group_reply_view:
                if(user_id < 1){
                    Utils.showToast(this, getString(R.string.please_login_first));
                }else {
                    Intent intent = new Intent(BilliardGroupDetailActivity.this, GroupDetailReplyActivity.class);
                    intent.putExtra("id", mNoteId);
                    startActivityForResult(intent, REPLY_CODE);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }
                break;
        }
    }


    private void praise(){
        Map<String,Integer> param = new HashMap<String, Integer>();
        param.put(HttpConstants.Praise.ID,mNoteId);
        HttpUtil.requestHttp(HttpConstants.Praise.URL,param,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","praise response->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.obtainMessage(PRAISE_SUCCESS,response.getJSONObject("result").getInt("count")).sendToTarget();
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

    private void store(){
        mPreProgress.setVisibility(View.VISIBLE);
        mPreTextView.setVisibility(View.VISIBLE);

        mParamsMap.put(HttpConstants.Play.TYPE,PublicConstant.GROUP);
        mParamsMap.put(HttpConstants.Play.ID,mNoteId);
        mParamsMap.put(HttpConstants.Play.USER_ID,YueQiuApp.sUserInfo.getUser_id());


        HttpUtil.requestHttp(HttpConstants.Favor.STORE_URL,mParamsMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.sendEmptyMessage(PublicConstant.FAVOR_SUCCESS);
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                }catch(JSONException e){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REPLY_CODE && resultCode == RESULT_OK){
            mReplyCount += 1;
            mReplyCountTv.setText(getString(R.string.reply,mReplyCount));

            String create_time = data.getStringExtra("create_time");
            String content = data.getStringExtra("content");

            GroupDetailCommentItem item = new GroupDetailCommentItem(YueQiuApp.sUserInfo.getUsername(),YueQiuApp.sUserInfo.getImg_url(),
                    create_time,content);

            mDetailItemList.add(1,item);
            mContentAdapter.notifyDataSetChanged();
        }
    }
}
