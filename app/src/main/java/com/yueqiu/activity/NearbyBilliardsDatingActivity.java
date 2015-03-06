package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.adapter.NearbyDatingDetailedGridAdapter;
import com.yueqiu.bean.NearbyDatingDetailedAlreadyBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.progress.FoldingCirclesDrawable;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils.parseGenderDrawable;

/**
 * @author scguo
 *
 * 用于实现约球详细的Activity,即点击约球Fragment(BilliardsNearbyDatingFragment)当中的ListView当中的任何一个item的时候跳转到的
 * 一个页面，就是约球详情Activity
 *
 */
public class NearbyBilliardsDatingActivity extends Activity
{
    private static final String TAG = "NearbyBilliardsDatingActivity";
    private GridView mGridAlreadyFlow;

    // 用于加载用户头像的ImageLoader
    private ImageLoader mImgLoader;
    // 以下的字段都是用于显示在约球详情Activity顶部的Column上面的内容
    private NetworkImageView mUserPhoto,mExtraImg;
    private TextView mUserName, mUserGender, mTvFollowNum, mTvTime1, mTvTime2,mPreText;

    private TextView mTvTitle, mTvActivityAddress, mTvStartTime, mTvEndTime, mTvModel, mTvContact, mTvPhoneNum, mTvActivityIntro;

    private Button mBtnFollow;
    private ActionBar mActionBar;

    private NearbyDatingDetailedGridAdapter mAlreadyInUserGridAdapter;

    private static int sNodeId;

    private ProgressBar mPreProgressBar;
    private Drawable mProgressDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_dating_detail);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();

        Intent datingIntent = getIntent();
        Bundle receivedData = datingIntent.getExtras();
        sNodeId = receivedData.getInt(NearbyFragmentsCommonUtils.KEY_DATING_TABLE_ID, -1);
        String userPhotoUrl = receivedData.getString(NearbyFragmentsCommonUtils.KEY_DATING_FRAGMENT_PHOTO, "");
        String userName = receivedData.getString(NearbyFragmentsCommonUtils.KEY_DATING_USER_NAME, "");
        Log.d(TAG, " the node id we get are : " + sNodeId + ", and the dating photo we get are : " + userPhotoUrl);

        mGridAlreadyFlow = (GridView) findViewById(R.id.gridview_search_dating_detailed_already_flow);
        mUserPhoto = (NetworkImageView) findViewById(R.id.img_search_dating_detail_photo);
        mUserPhoto.setDefaultImageResId(R.drawable.default_head);
        mUserPhoto.setErrorImageResId(R.drawable.default_head);

        mExtraImg = (NetworkImageView) findViewById(R.id.date_detail_extra_img);

        if (! TextUtils.isEmpty(userPhotoUrl))
        {
            mUserPhoto.setImageUrl(HttpConstants.IMG_BASE_URL + userPhotoUrl, mImgLoader);
        }

        mUserName = (TextView) findViewById(R.id.tv_search_dating_detail_nickname);
        mUserName.setText(userName);
        mUserGender = (TextView) findViewById(R.id.tv_search_dating_detail_gender);
        mTvFollowNum = (TextView) findViewById(R.id.tv_search_dating_detail_follow_num);

        mTvTime1 = (TextView) findViewById(R.id.tv_search_dating_detailed_time); // 活动的开始时间
        mTvTime2 = (TextView) findViewById(R.id.tv_search_dating_detailed_time_1); // 活动的结束时间

        // 以下是约球详细信息的列表TextView
        mTvTitle = (TextView) findViewById(R.id.tv_search_dating_info_title);
        mTvActivityAddress = (TextView) findViewById(R.id.tv_search_dating_detailed_address);
        mTvStartTime = (TextView) findViewById(R.id.tv_search_dating_detailed_starttime);
        mTvEndTime = (TextView) findViewById(R.id.tv_search_dating_detailed_endtime);
        mTvModel = (TextView) findViewById(R.id.tv_search_dating_detailed_model);
        mTvContact = (TextView) findViewById(R.id.tv_search_dating_detailed_contact);
        mTvPhoneNum = (TextView) findViewById(R.id.tv_search_dating_detailed_phonenum);
        mTvActivityIntro = (TextView) findViewById(R.id.tv_search_dating_detailed_activity_intro);

        mPreProgressBar = Utils.$(this,R.id.pre_progress);
        mPreText = Utils.$(this,R.id.pre_text);
        mProgressDrawable = new FoldingCirclesDrawable.Builder(this).build();
        Rect bounds = mPreProgressBar.getIndeterminateDrawable().getBounds();
        mPreProgressBar.setIndeterminateDrawable(mProgressDrawable);
        mPreProgressBar.getIndeterminateDrawable().setBounds(bounds);

        (mBtnFollow = (Button) findViewById(R.id.btn_search_dating_detailed_iwantin)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mHandler.sendEmptyMessage(JOIN_ACTIVITY);
            }
        });

        mHandler.sendEmptyMessage(RETRIEVE_DATING_DETAILED_INFO);
        mAlreadyInUserGridAdapter = new NearbyDatingDetailedGridAdapter(this, (ArrayList<NearbyDatingDetailedAlreadyBean>) mFollowList);
        mGridAlreadyFlow.setAdapter(mAlreadyInUserGridAdapter);
        mAlreadyInUserGridAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
        {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private static final int JOIN_ACTIVITY = 1 << 1;
    private static final int RETRIEVE_DATING_DETAILED_INFO = 1 << 2;
    private static final int SET_DATING_DETAILED_INFO = 1 << 3;
//    private static final int SHOW_TOAST = 1 << 4;
    private static final int JOIN_SUCCESS = 1 << 5;
    private static final int ADD_TO_FAVOR_SUCCESS = 1 << 6;
    private static final int ADD_TO_FAVOR_FAILURE = 1 << 8;
    private static final int ADD_TO_FAVOR = 1 << 7;
    private static final int USER_HAS_NOT_LOGIN = 1 << 9;
    private static final int RETRIEVE_FOLLOWER_LIST = 1 << 10;


    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            showPreProgress(false);
            switch (msg.what)
            {
                case JOIN_ACTIVITY:
                    Log.d(TAG, " start requesting to join the activity ");

                     if (sNodeId != -1)
                     {
                         if(Utils.networkAvaiable(NearbyBilliardsDatingActivity.this)) {
                             joinActivity(
                                     YueQiuApp.sUserInfo.getUser_id(), // userId
                                     PublicConstant.JOIN_TYPE_DATE, // current type
                                     sNodeId // pid we get from the previous dating item list
                             );
                         }else{
                             Utils.showToast(NearbyBilliardsDatingActivity.this,getString(R.string.network_not_available));
                         }
                     }
                    break;
                case ADD_TO_FAVOR:
                    Log.d(TAG, " the current dating id we get are : sNodeId --> " + sNodeId);
                    if (sNodeId != -1)
                    {
                        if(Utils.networkAvaiable(NearbyBilliardsDatingActivity.this)) {
                            addDatingToFavor(
                                    YueQiuApp.sUserInfo.getUser_id(), // 用户ID，从YueQiuApp当中获取
                                    1, // type Dating信息的type为1
                                    sNodeId // noteId 当前的Dating所在的具体的table
                            );
                        }else{
                            Utils.showToast(NearbyBilliardsDatingActivity.this,getString(R.string.network_not_available));
                        }
                     }
                    break;

                case ADD_TO_FAVOR_SUCCESS:
                    Utils.showToast(NearbyBilliardsDatingActivity.this, getString(R.string.store_success));
                    // 发送广播用于通知已经成功的加入收藏当中
                    Intent favorIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                    sendBroadcast(favorIntent);
                    break;
                case ADD_TO_FAVOR_FAILURE:
                    Utils.showToast(NearbyBilliardsDatingActivity.this, getString(R.string.store_failure));
                    break;
                case RETRIEVE_DATING_DETAILED_INFO:
                    Log.d(TAG, " start retrieving all the information of the dating detailed info activity ");
                    if(Utils.networkAvaiable(NearbyBilliardsDatingActivity.this)) {
                        retrieveDatingDetailedInfo(sNodeId);
                    }else{
                        Utils.showToast(NearbyBilliardsDatingActivity.this,getString(R.string.network_not_available));
                    }
                    break;

                case SET_DATING_DETAILED_INFO:
                    Log.d(TAG, " has get the data from the server, and then set it on the TextView");
                    Message detailedInfoMsg = msg;
                    Log.d(TAG, " the message we get for the TextView are : " + (detailedInfoMsg.what == SET_DATING_DETAILED_INFO));
                    Bundle infoBundle = detailedInfoMsg.getData();
                    String appointId = infoBundle.getString(KEY_APPOINT_ID);
                    String sex = infoBundle.getString(KEY_SEX);
//                    String lookNum = infoBundle.getString(KEY_LOOK_NUM);
                    String titleInfo = infoBundle.getString(KEY_TITLE_INFO);
                    String address = infoBundle.getString(KEY_ADDRESS);
                    String startTime = infoBundle.getString(KEY_START_TIME);
                    String endTime = infoBundle.getString(KEY_END_TIME);
                    String model = infoBundle.getString(KEY_MODEL);
                    String createTime = infoBundle.getString(KEY_CREATE_TIME); // 活动的发布日期
                    //TODO:等服务器加上
                    String img_url = infoBundle.getString(KEY_IMG_URL);

                    Log.d(TAG, " the data we get are : " + " titleInfo : " + titleInfo + " , address : " + address + " , startTime :" + startTime
                            + " , endTime : " + endTime + " , model " + model);

                    String realSexStr = NearbyFragmentsCommonUtils.parseGenderStr(NearbyBilliardsDatingActivity.this, sex);
                    mUserGender.setText(realSexStr);
                    mUserGender.setCompoundDrawablesWithIntrinsicBounds(0, 0, parseGenderDrawable(realSexStr), 0);
                    mUserGender.setCompoundDrawablePadding(6);
                    mTvTime1.setText(createTime);
//                    mTvFollowNum.setText(lookNum);
                    mTvTitle.setText(titleInfo);
                    mTvActivityAddress.setText(address);
                    mTvStartTime.setText(startTime);
                    mTvEndTime.setText(endTime);
                    mTvModel.setText(model);
                    mExtraImg.setImageUrl("http://" + img_url,mImgLoader);
                    break;
                case JOIN_SUCCESS:
                    Intent joinIntent = new Intent(PublicConstant.SLIDE_PART_IN_ACTION);
                    sendBroadcast(joinIntent);
                    showToast((String) msg.obj);
                    break;
                case USER_HAS_NOT_LOGIN:
                    Utils.showToast(NearbyBilliardsDatingActivity.this, getString(R.string.please_login_first));
                    break;

                case RETRIEVE_FOLLOWER_LIST:
                    ArrayList<NearbyDatingDetailedAlreadyBean> followerList = (ArrayList<NearbyDatingDetailedAlreadyBean>) msg.obj;
                    mFollowList.addAll(followerList);
                    mAlreadyInUserGridAdapter.notifyDataSetChanged();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    if(null == msg.obj){
                        Utils.showToast(NearbyBilliardsDatingActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(NearbyBilliardsDatingActivity.this, (String) msg.obj);
                    }
                    break;
            }
        }
    };


    // TODO: 获取约球的时间list，是以一个列表的形式返回的，具体可能包含不止两条时间
    private String retrieveDatingTimeList()
    {

        return "";
    }

    // TODO: 服务器端有很多的interface仍然没有实现，需要后期提醒.
    // TODO: 以下定义的字段都是我们确定至少需要的
    private static final String KEY_APPOINT_ID = "appointId"; // 活动的ID
    private static final String KEY_SEX = "sex";
//    private static final String KEY_LOOK_NUM = "lookNum";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_TIME_1 = "time1";
    private static final String KEY_TIME_2 = "time2";
    private static final String KEY_CREATE_TIME = "createTime";
    private static final String KEY_TITLE_INFO = "titleInfo";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_END_TIME = "endTime";
    private static final String KEY_MODEL = "model";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_CONTACT_PHONENUM = "phoneNum";
    private static final String KEY_ACTIVITY_INTRO = "activityIntro";
    private static final String KEY_IMG_URL = "img_url";

//    private static final String KEY_TOAST_MSG = "toastContent";

    // TODO: 这个列表用于保存已经参加当前的约球活动的用户的头像的列表(我们目前还是用静态数据进行初始化，等到Server端接口完成之后，
    // TODO: 我们需要采用动态数据)
    private List<NearbyDatingDetailedAlreadyBean> mFollowList = new ArrayList<NearbyDatingDetailedAlreadyBean>();

    // TODO: 获取约球详情信息
    /**
     * 这里需要传递的是约球Id，而不是用户Id
     * @param datingId 约球id
     * @return
     */
    private void retrieveDatingDetailedInfo(final int datingId)
    {
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("id", datingId + "");

        showPreProgress(true);

        HttpUtil.requestHttp(HttpConstants.NearbyDating.URL_DATING_DETAILE, requestParams, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","date detail response ->" + response);
                try{
                    if(!response.isNull("code")){
                        if(response.getInt("code") == HttpConstants.ResponseCode.NORMAL){
                            JSONObject wholeResult = response.getJSONObject("result"); // 得到的总的json object
                            String appointId = wholeResult.getString("appoint_id"); // 约球id
                            String sex = wholeResult.getString("sex"); // 用户性别
//                String lookNumber = wholeResult.getString("look_number"); // 浏览数目，即眼睛图标对应的那个数字
                            String createTime = wholeResult.getString("create_time"); // 活动发布的日期
                            String title = wholeResult.getString("title"); // 活动主题
                            String address = wholeResult.getString("address"); // 活动地点
                            String beginTime = wholeResult.getString("begin_time"); // 活动开始时间
                            String endTime = wholeResult.getString("end_time"); // 活动结束时间
                            String model = wholeResult.getString("model"); // 收费模式 1.免费;2.收费;3.AA制
                            //TODO:等服务器加上
                            String img_url = wholeResult.getString("img_url");


                            String modelStr = "";
                            if (model.equals("1") || TextUtils.isEmpty(model))
                            {
                                modelStr = getResources().getString(R.string.search_dating_detailed_model_1);
                            } else if (model.equals("2"))
                            {
                                modelStr = getResources().getString(R.string.search_dating_detailed_model_2);
                            } else if (model.equals("3"))
                            {
                                modelStr = getResources().getString(R.string.search_dating_detailed_model_3);
                            }

                            Message msg = mHandler.obtainMessage(SET_DATING_DETAILED_INFO);
                            Bundle detailedInfoBundle = new Bundle();
                            detailedInfoBundle.putString(KEY_APPOINT_ID, appointId); // 活动Id
                            detailedInfoBundle.putString(KEY_SEX, sex); // 发布活动的用户的性别
//                detailedInfoBundle.putString(KEY_LOOK_NUM, lookNumber); // 当期活动的关注数目
                            detailedInfoBundle.putString(KEY_CREATE_TIME, createTime); // 活动发布日期
                            detailedInfoBundle.putString(KEY_TITLE_INFO, title); // 活动主题
                            detailedInfoBundle.putString(KEY_ADDRESS, address); // 活动地址
                            detailedInfoBundle.putString(KEY_START_TIME, beginTime); // 开始时间
                            detailedInfoBundle.putString(KEY_END_TIME, endTime); // 结束时间
                            detailedInfoBundle.putString(KEY_MODEL, modelStr); // 收费模式
                            //TODO:等服务器加上
                            detailedInfoBundle.putString(KEY_IMG_URL,img_url);
                            msg.setData(detailedInfoBundle);
                            msg.what = SET_DATING_DETAILED_INFO;
                            mHandler.sendMessage(msg);

                            // 以下得到的是已经参加这次活动的人员的列表
                            List<NearbyDatingDetailedAlreadyBean> cachedFollowList = new ArrayList<NearbyDatingDetailedAlreadyBean>();
                            JSONArray followList = wholeResult.getJSONArray("join_list");
                            Log.d(TAG_1, " the join_list we get are : " + followList.toString());
                            final int followSize = followList.length();
                            int i;
                            for (i = 0; i < followSize; ++i)
                            {
                                JSONObject followerJson = (JSONObject) followList.get(i);
                                if (followerJson != null)
                                {
                                    String followerId = followerJson.getString("user_id"); // 已经参加本次活动的人员的id
                                    String followerAccount = followerJson.getString("account"); // 已经参加本次活动的人员的account, 这里也就是用户的名字
                                    String followerPhotoUrl = followerJson.getString("img_url"); // 已经参加本次活动的人员的photo的url
                                    if (! TextUtils.isEmpty(followerId) && !TextUtils.isEmpty(followerAccount) && !TextUtils.isEmpty(followerPhotoUrl))
                                    {
                                        NearbyDatingDetailedAlreadyBean follower = new NearbyDatingDetailedAlreadyBean(followerId, followerPhotoUrl, followerAccount);
                                        cachedFollowList.add(follower);
                                    }
                                }
                            }
                            Log.d(TAG, " the finally follower list we get are : " + cachedFollowList.size());
                            if (cachedFollowList.size() > 0)
                            {
                                // 然后我们把这个消息发送到mUIHandler当中
                                mHandler.obtainMessage(RETRIEVE_FOLLOWER_LIST, cachedFollowList).sendToTarget();
                            }
                        }else if(response.getInt("code") == HttpConstants.ResponseCode.NO_RESULT){
                            //啥也不做
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getJSONObject("msg")).sendToTarget();
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

    private static final String TAG_1 = "scguo_datingActivity";
    /**
     *
     * @param userId 用户id
     * @param type 类型, 1对应于约球;2对应于活动
     * @param pId 即约球Id
     */
    private void joinActivity(int userId, int type, int pId)
    {
        Log.d(TAG, " jointActiivty --> the current userId : " + userId + " , and the type are : " + type + " , and the pId are : " + pId);


        if (userId < 1)
        {
            mHandler.sendEmptyMessage(USER_HAS_NOT_LOGIN);
            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId + "");
        requestParams.put("type_id", type + "");
        requestParams.put("p_id", pId + "");

        mPreText.setText(getString(R.string.joining));
        showPreProgress(true);

        HttpUtil.requestHttp(HttpConstants.NearbyDating.URL_JOIN_ACTIVITY, requestParams, HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","date join response ->" + response);
                try {
                    if(!response.isNull("code")) {
                        final int code = response.getInt("code");
                        if(code == HttpConstants.ResponseCode.NORMAL){
                            mHandler.obtainMessage(JOIN_SUCCESS,getString(R.string.join_success)).sendToTarget();
                        }else{
                            mHandler.obtainMessage(PublicConstant.REQUEST_ERROR,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(PublicConstant.REQUEST_ERROR);
                    }
                } catch (JSONException e) {
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

    /**
     * 将当前的约球详情加入到"我的收藏"当中
     *
     * @param userId 当前的用户Id
     * @param type 指定收藏的类型,对于约球来说，type值为1
     * @param nodeId 这个值指的是我们之前在获取约球列表时所获得的表Id,我们通过前一个List的item当中值传递过来直接获取就可以了
     *
     */
    private void addDatingToFavor(int userId, int type, int nodeId)
    {
        Log.d(TAG, " the current user id are : " + userId + " , and the type are : " + type + " , and the table ID are : " + nodeId);
        if (userId < 1)
        {
            mHandler.sendEmptyMessage(USER_HAS_NOT_LOGIN);
            // 直接返回，不执行之后的操作，节省资源
            return;
        }

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId + "");
        requestParams.put("type", type + "");
        requestParams.put("id", nodeId + "");

        mPreText.setText(getString(R.string.storing));
        showPreProgress(true);

        HttpUtil.requestHttp(HttpConstants.Favor.STORE_URL, requestParams, HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","date favor response ->" + response);
                try {
                    if(!response.isNull("code")) {
                        int code = response.getInt("code");
                        if(code == HttpConstants.ResponseCode.NORMAL){
                            mHandler.sendEmptyMessage(ADD_TO_FAVOR_SUCCESS);
                        }else{
                            mHandler.obtainMessage(ADD_TO_FAVOR_FAILURE,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mHandler.sendEmptyMessage(ADD_TO_FAVOR_FAILURE);
                    }
                } catch (JSONException e)
                {
                    Log.d(TAG, " exception happened while we add the current dating info into the server collection, cause to : " + e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mHandler.sendEmptyMessage(ADD_TO_FAVOR_FAILURE);
            }
        });

    }

    // 因为Toast是涉及到UI的操作，所以必须在MainUIThread当中执行，我们不可知直接在一个第三方Thread当中
    // 调用Toast。所以我们单独封装到另一个方法当中
    private void showToast(final String content)
    {
        Toast.makeText(NearbyBilliardsDatingActivity.this, content, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiards_dating, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id)
        {
            case R.id.search_room_action_collect:
                mHandler.sendEmptyMessage(ADD_TO_FAVOR);
                return true;
            case R.id.search_room_action_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);;
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // TODO: 用于完成台球厅分享的网络的请求处理过程
    private void shareBilliardsRoomRequest(String shareTarget)
    {

    }

    /////////////////////////////////////////////////////////////////////////////////
    // TODO: 以下只是我们加载的临时测试数据，在正式测试通过之后就可以直接删除了
    private void initGridList()
    {
        int i;
        for (i = 0; i < 5; ++i)
        {
            mFollowList.add(new NearbyDatingDetailedAlreadyBean("", "", "温柔的语-11111"));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPreProgress(boolean show){
        if(show){
            mPreProgressBar.setVisibility(View.VISIBLE);
            mPreText.setVisibility(View.VISIBLE);
        }else{
            mPreProgressBar.setVisibility(View.GONE);
            mPreText.setVisibility(View.GONE);
        }
    }
}
