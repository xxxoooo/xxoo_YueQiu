package com.yueqiu.activity;

import android.app.ActionBar;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.JoinListAdapter;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.FavorDao;
import com.yueqiu.dao.PlayDao;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
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

/**
 * Created by yinfeng on 15/1/6.
 */
public class PlayDetailActivity extends Activity implements View.OnClickListener{



    private ActionBar mActionBar;
    private TextView mUserNameTv,mSexTv,mBrowseCountTv,mCreateTimeTv;
    private TextView mTitleTv,mTypeTv,mAddressTv,mBeginTimeTv,mEndTimeTv,
            mModuleTv,mContactTv,mPhoneTv,mContentTv,mPreText;
    private Button mJoin;
    private CustomNetWorkImageView mHeadImgIv;
    private PlayDao mPlayDao;
    private ProgressBar mPreProgressBar;
    private Drawable mProgressDrawable;
    private GridView mPartInGridView;
    private int mTableId,mInfoType;
    private String mCreateTime;
    private boolean mStroe = false;
    private ImageLoader mImgLoader;
    private JoinListAdapter mJoinAdapter;
    private NetworkImageView mExtraImage;

    private Map<String,Integer> mParamMap = new HashMap<String, Integer>();
    private Map<String,String> mUrlAndMethodMap = new HashMap<String, String>();
    private PlayInfo mCachePlayInfo,mPlayInfo;
    private FavorDao mFavorDao;
    private int mPlayType;

    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_detail);
        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);

        initActionBar();
        initView();
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        Bundle args = getIntent().getExtras();
        mPlayType = args.getInt(PublicConstant.PLAY_TYPE);
        mTableId = args.getInt(DatabaseConstant.PlayTable.TABLE_ID);
        mCreateTime = args.getString(DatabaseConstant.PlayTable.CREATE_TIME);
//        mInfoType = Integer.parseInt(args.getString(DatabaseConstant.PlayTable.TYPE));
        mFavorDao = DaoFactory.getFavor(this);
        mPlayDao = DaoFactory.getPlay(this);
        //////////////////////////////////////////////////////////////////////
        //TODO:先去掉缓存功能，后期再根据需求加回来，目前逻辑没问题
        //TODO:同样是可以考虑用更有效率的loader或其他异步方法，而不是
        //TODO:简单地用线程，线程的生命周期不可控
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mCachePlayInfo = mPlayDao.getPlayInfoById(mTableId,mInfoType);
//                if(!TextUtils.isEmpty(mCachePlayInfo.getUsername())){
//                    mHandler.obtainMessage(PublicConstant.USE_CACHE,mCachePlayInfo).sendToTarget();
//                }
//            }
//        }).start();
        //////////////////////////////////////////////////////////////////////

        if(Utils.networkAvaiable(this)){
            requestDetail();
        }else{
            mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
        }

    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.activities_detail));
    }

    private void initView(){

        mHeadImgIv = Utils.$(this,R.id.play_detail_img_iv);
        mHeadImgIv.setDefaultImageResId(R.drawable.default_head);
        mHeadImgIv.setErrorImageResId(R.drawable.default_head);
        mUserNameTv = Utils.$(this,R.id.play_detail_name_tv);
        mSexTv = Utils.$(this,R.id.play_detail_gender);
        mBrowseCountTv = Utils.$(this,R.id.play_look_num);
        mCreateTimeTv = Utils.$(this,R.id.play_create_time_tv);
        mTitleTv = Utils.$(this,R.id.play_detail_title_tv);
        mTypeTv = Utils.$(this,R.id.play_detail_type_info_tv);
        mAddressTv = Utils.$(this,R.id.play_detail_address_info_tv);
        mBeginTimeTv = Utils.$(this,R.id.play_detail_begin_info);
        mEndTimeTv = Utils.$(this,R.id.play_detail_end_info);
        mModuleTv = Utils.$(this,R.id.play_detail_model_info);
        mContactTv = Utils.$(this,R.id.play_detail_contact_info);
        mPhoneTv = Utils.$(this,R.id.play_detail_phone_info);
        mContentTv = Utils.$(this,R.id.play_detail_illustration_info);
        mPartInGridView = Utils.$(this,R.id.play_detail_gridview);
        mJoin = Utils.$(this,R.id.btn_search_dating_detailed_join);
        mExtraImage = Utils.$(this,R.id.play_detail_extra_img);

        mPreProgressBar = Utils.$(this,R.id.pre_progress);
        mPreText = Utils.$(this,R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(PlayDetailActivity.this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);

        if(mPlayType == PublicConstant.PLAY_BUSSINESS){
            mSexTv.setVisibility(View.VISIBLE);
        }

        mJoin.setOnClickListener(this);
    }

    private void requestDetail(){

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreText.setVisibility(View.VISIBLE);

        mParamMap.clear();
        mParamMap.put(HttpConstants.Play.ID, mTableId);

        String url;
        switch(mPlayType){
            case PublicConstant.PLAY_BUSSINESS:
                url = HttpConstants.Play.BUSINESS_DETAIL;
                break;
            default:
                url = HttpConstants.Play.GETDETAIL;
                break;
        }

        HttpUtil.requestHttp(url,mParamMap,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","play detail response -> " + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            if(response.getString("result") != null){
                                PlayInfo info = setDetailInfoByJSON(response);
                                mHandler.obtainMessage(PublicConstant.GET_SUCCESS,info).sendToTarget();
                            }else{
                                mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT){
                            mHandler.sendEmptyMessage(PublicConstant.TIME_OUT);
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            mHandler.sendEmptyMessage(PublicConstant.NO_RESULT);
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR).sendToTarget();
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

    private PlayInfo setDetailInfoByJSON(JSONObject object){
        mPlayInfo = new PlayInfo();
        try{
            JSONObject result = object.getJSONObject("result");
            mPlayInfo.setTable_id(result.getString("id"));
            //TODO:u_img_url是头像
            mPlayInfo.setImg_url(result.getString("u_img_url"));
            mPlayInfo.setTitle(result.getString("title"));
            mPlayInfo.setUsername(result.getString("username"));
            mPlayInfo.setCreate_time(mCreateTime);
            mPlayInfo.setType(result.getString("type"));
            mPlayInfo.setBegin_time(result.getString("begin_time"));
            mPlayInfo.setEnd_time(result.getString("end_time"));
            mPlayInfo.setModel(result.getString("model"));
            mPlayInfo.setContent(result.getString("content"));
            mPlayInfo.setAddress(result.getString("address"));
            mPlayInfo.setContact(result.getString("name"));
            mPlayInfo.setPhone(result.getString("phone"));
            //TODO:img_url是上传的图片
            mPlayInfo.setExtra_img(result.getString("img_url"));
            if(mPlayType != PublicConstant.PLAY_BUSSINESS) {
                mPlayInfo.setSex(result.getString("sex"));
                mPlayInfo.setLook_num(result.getInt("look_num"));
                JSONArray join_list = result.getJSONArray("join_list");
                for (int i = 0; i < join_list.length(); i++) {
                    UserInfo user = new UserInfo();
                    user.setUsername(join_list.getJSONObject(i).getString("username"));
                    user.setImg_url(join_list.getJSONObject(i).getString("img_url"));
                    user.setUser_id(Integer.parseInt(join_list.getJSONObject(i).getString("user_id")));
                    mPlayInfo.mJoinList.add(user);
                }
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return mPlayInfo;
    }




    private void updateUI(final PlayInfo info){
        if (! TextUtils.isEmpty(info.getImg_url()))
        {
            mHeadImgIv.setImageUrl("http://"+ info.getImg_url(), mImgLoader);
        }
        mUserNameTv.setText(info.getUsername());
        mCreateTimeTv.setText(mCreateTime);
        mTitleTv.setText(info.getTitle());
        mTypeTv.setText(getDetailTypeStr(info.getType()));
        mAddressTv.setText(info.getAddress());
        mBeginTimeTv.setText(info.getBegin_time());
        mEndTimeTv.setText(info.getEnd_time());
        mModuleTv.setText(getModeStr(Integer.parseInt(info.getModel())));
        mContentTv.setText(info.getContent());
        mContactTv.setText(info.getContact());
        mPhoneTv.setText(info.getPhone());
        mBrowseCountTv.setText(info.getLook_num() + "");
        mBrowseCountTv.setText(info.getLook_num() + "");

        if(mPlayType != PublicConstant.PLAY_BUSSINESS) {

            mSexTv.setText(info.getSex().equals("1") ? getString(R.string.man) : getString(R.string.woman));
            if (info.getSex().equals("1")) {
                mSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.male, 0);
            } else {
                mSexTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.female, 0);
            }

        }

        mJoinAdapter = new JoinListAdapter(this, info.mJoinList);
        mPartInGridView.setAdapter(mJoinAdapter);
        mPartInGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(YueQiuApp.sUserInfo.getUser_id() < 1) {
                    Toast.makeText(PlayDetailActivity.this, getString(R.string.please_login_first), Toast.LENGTH_SHORT).show();
                }else if(Integer.valueOf(info.mJoinList.get(position).getUser_id()) == YueQiuApp.sUserInfo.getUser_id()){
                    return;
                }
                else{
                    Intent intent = new Intent(PlayDetailActivity.this, RequestAddFriendActivity.class);
                    int friendUserId = Integer.valueOf(info.mJoinList.get(position).getUser_id());
                    String username = info.mJoinList.get(position).getUsername();
                    intent.putExtra(AddPersonFragment.FRIEND_INFO_USER_ID, friendUserId);
                    intent.putExtra(AddPersonFragment.FRIEND_INFO_USERNAME, username);
                    startActivity(intent);
                }
            }
        });

        if(! TextUtils.isEmpty(info.getExtra_img())){
            mExtraImage.setImageUrl("http://" + info.getExtra_img(),mImgLoader);
        }
    }
    private String getDetailTypeStr(String type){
        String typeStr;
        switch(Integer.parseInt(type)){
            case PublicConstant.PLAY_GROUP:
                typeStr = getString(R.string.group_activity);
                break;
            case PublicConstant.PLAY_MEET_STAR:
                typeStr = getString(R.string.star_meet);
                break;
            case PublicConstant.PLAY_BILLIARD_SHOW:
                typeStr = getString(R.string.billiard_show);
                break;
            case PublicConstant.PLAY_COMPETITION:
                typeStr = getString(R.string.complete);
                break;
            case PublicConstant.PLAY_OTHER_ACTIVITY:
                typeStr = getString(R.string.billiard_other);
                break;
            default:
                typeStr = getString(R.string.billiard_other);
                break;
        }
        return typeStr;
    }
    private int getModeStr(int mode){
        int resId = R.string.charge_module_free;
        switch (mode){
            case SelectChargeModuleActivity.MODULE_AA:
                resId = R.string.charge_module_aa;
                break;
            case SelectChargeModuleActivity.MODULE_FREE:
                resId = R.string.charge_module_free;
                break;
            case SelectChargeModuleActivity.MODULE_PAY:
                resId = R.string.charge_module_pay;
                break;
        }
        return resId;
    }
    private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mPreProgressBar.setVisibility(View.GONE);
                mPreText.setVisibility(View.GONE);
                switch (msg.what) {
                    case PublicConstant.USE_CACHE:
                        PlayInfo cacheInfo = (PlayInfo) msg.obj;
                        updateUI(cacheInfo);
                        break;
                    case PublicConstant.GET_SUCCESS:
                        mPlayInfo = (PlayInfo) msg.obj;
                        updateUI(mPlayInfo);
                        //TODO:更新数据库,由于目前不需要缓存的功能
                        //TODO:所以先注释掉，后期需要缓存的时候再加入
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updatePlayInfoDb(mPlayInfo);
//                            }
//                        }).start();
                        break;
                    case PublicConstant.TIME_OUT:
                        Utils.showToast(PlayDetailActivity.this, getString(R.string.http_request_time_out));
                        break;
                    case PublicConstant.REQUEST_ERROR:
                        if(null == msg.obj){
                            Utils.showToast(PlayDetailActivity.this,getString(R.string.http_request_error));
                        }else{
                            Utils.showToast(PlayDetailActivity.this, (String) msg.obj);
                        }
                        break;
                    case PublicConstant.NO_RESULT:
                        Utils.showToast(PlayDetailActivity.this, getString(R.string.no_detail_info));
                        break;
                    case PublicConstant.NO_NETWORK:
                        //TODO:如果用缓存的话，这里是有逻辑上的问题，但是目前不需要缓存，所以暂时没问题
//                        if(TextUtils.isEmpty(mCachePlayInfo.getUsername()))
                            Utils.showToast(PlayDetailActivity.this, getString(R.string.network_not_available));
                        break;
                    case PublicConstant.FAVOR_SUCCESS:
                        //TODO:如果有缓存功能的话，这里还得插入收藏的数据库
                        Intent shareIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                        sendBroadcast(shareIntent);
                        Utils.showToast(PlayDetailActivity.this, getString(R.string.store_success));
                        break;
                    case PublicConstant.JOIN_SUCCESS:
                        UserInfo user = new UserInfo();
                        user.setUsername(YueQiuApp.sUserInfo.getUsername());
                        user.setImg_url(YueQiuApp.sUserInfo.getImg_url());
                        user.setUser_id(YueQiuApp.sUserInfo.getUser_id());
                        mPlayInfo.mJoinList.add(user);
                        mJoinAdapter.notifyDataSetChanged();
                        Intent joinIntent = new Intent(PublicConstant.SLIDE_PART_IN_ACTION);
                        sendBroadcast(joinIntent);
                        Utils.showToast(PlayDetailActivity.this, getString(R.string.join_success));
                        break;
                }
            }
    };

    //TODO:目前不需要缓存，所以先不调用
    private void updatePlayInfoDb(PlayInfo info){
        List<PlayInfo> list = new ArrayList<PlayInfo>();
        list.add(info);
        mPlayDao.updatesDetailPlayInfo(list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(PlayDetailActivity.this);
        getMenuInflater().inflate(R.menu.menu_activities_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.menu_store_to_favor:
                //TODO:收藏完后要更新数据库
                int user_id = YueQiuApp.sUserInfo.getUser_id();
                if(user_id < 1) {
                    Utils.showToast(this,getString(R.string.please_login_first));
                }else{
                    if(Utils.networkAvaiable(this)){
                        store();
                        //TODO:插入到本地数据库后可以，但是接口那边现在还没更新
                        //TODO:目前不需要缓存，后期需要缓存的时候再加上
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                FavorInfo favor = setFavorInfo(mPlayInfo);
//                                insertFavorDB(favor);
//                            }
//                        }).start();

                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.NO_NETWORK);
                    }
                }
                break;
            case R.id.menu_activities_share:
                YueQiuApp.sScreenBitmap = Utils.getCurrentScreenShot(mRootView);
                Dialog dlg = Utils.showSheet(this, YueQiuApp.sScreenBitmap);
                dlg.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
//
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
    //TODO:由于目前不需要缓存，这个方法暂时不调用
    private FavorInfo setFavorInfo(PlayInfo info){
        FavorInfo favor = new FavorInfo();
        favor.setUser_id(YueQiuApp.sUserInfo.getUser_id());
        favor.setTable_id(info.getTable_id());
        favor.setType(PublicConstant.FAVOR_PLAY_TYPE);
        favor.setTitle(info.getTitle());
        favor.setContent(info.getContent());
        favor.setCreateTime(info.getCreate_time());
        favor.setUserName(info.getUsername());
        //TODO:加入缓存后这个字段肯定要有
//        favor.setSubType(Integer.parseInt(info.getType()));
        return favor;
    }
    //TODO:由于目前不需要缓存，这个方法暂时不调用
    private void insertFavorDB(FavorInfo info){
        List<FavorInfo> list = new ArrayList<FavorInfo>();
        list.add(info);
        mFavorDao.insertFavorInfo(list);
    }

    private void store(){
        mParamMap.clear();
        mParamMap.put(HttpConstants.Play.TYPE,PublicConstant.PLAY);
        mParamMap.put(HttpConstants.Play.ID,mTableId);
        mParamMap.put(HttpConstants.Play.USER_ID,YueQiuApp.sUserInfo.getUser_id());

        mStroe = true;
        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreText.setText(getString(R.string.storing));
        mPreText.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.Favor.STORE_URL,mParamMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
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
            }
        });

    }



    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(Utils.networkAvaiable(this)) {
            int userId = YueQiuApp.sUserInfo.getUser_id();
            if(userId < 1){
                Utils.showToast(this,getString(R.string.please_login_first));
            }else {
                join();
            }
        }else{
            Utils.showToast(this,getString(R.string.network_not_available));
        }
    }

    private void join(){
        mParamMap.clear();
        mParamMap.put(HttpConstants.NearbyDating.USER_ID, YueQiuApp.sUserInfo.getUser_id());
        mParamMap.put(HttpConstants.NearbyDating.TYPE_ID, PublicConstant.JOIN_TYPE_PLAY);
        mParamMap.put(HttpConstants.NearbyDating.P_ID, mTableId);

        mPreProgressBar.setVisibility(View.VISIBLE);
        mPreText.setText(getString(R.string.joining));
        mPreText.setVisibility(View.VISIBLE);

        HttpUtil.requestHttp(HttpConstants.NearbyDating.URL_JOIN_ACTIVITY,mParamMap,HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            mHandler.sendEmptyMessage(PublicConstant.JOIN_SUCCESS);
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





}
