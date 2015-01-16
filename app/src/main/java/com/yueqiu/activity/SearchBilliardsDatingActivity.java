package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchDatingDetailedGridAdapter;
import com.yueqiu.bean.SearchDatingDetailedAlreadyBean;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author scguo
 *
 * 用于实现约球详细的Activity,即点击约球Fragment(BilliardsSearchDatingFragment)当中的ListView当中的任何一个item的时候跳转到的
 * 一个页面，就是约球详情Activity
 *
 */
public class SearchBilliardsDatingActivity extends Activity
{
    private static final String TAG = "SearchBilliardsDatingActivity";
    private GridView mGridAlreadyFlow;
    private ImageView mUserPhoto;
    private TextView mUserName, mUserGender, mTvFollowNum, mTvTime1, mTvTime2;

    private TextView mTvTitle, mTvActivityAddress, mTvStartTime, mTvEndTime, mTvModel, mTvContact, mTvPhoneNum, mTvActivityIntro;

    private Button mBtnFollow;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiards_dating);

        mGridAlreadyFlow = (GridView) findViewById(R.id.gridview_search_dating_detailed_already_flow);
        mUserPhoto = (ImageView) findViewById(R.id.img_search_dating_detail_photo);
        mUserName = (TextView) findViewById(R.id.tv_search_dating_detail_nickname);
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

        (mBtnFollow = (Button) findViewById(R.id.btn_search_dating_detailed_iwantin)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mHandler.sendEmptyMessage(JOIN_ACTIVITY);
            }
        });

        mHandler.sendEmptyMessage(RETRIEVE_DATING_DETAILED_INFO);

        // the parameter we need to set are all retrieved from the previous activity

        // TODO: 以下加载的是测试数据
        initGridList();
        mGridAlreadyFlow.setAdapter(new SearchDatingDetailedGridAdapter(this, (ArrayList<SearchDatingDetailedAlreadyBean>) mFollowList));
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
    private static final int SHOW_TOAST = 1 << 4;
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case JOIN_ACTIVITY:
                    Log.d(TAG, " start requesting to join the activity ");
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            joinActivity(1, 1, 2);
                        }
                    }).start();

                    break;
                case RETRIEVE_DATING_DETAILED_INFO:
                    Log.d(TAG, " start retrieving all the information of the dating detailed info activity ");
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            retrieveDatingDetailedInfo(1);
                        }
                    }).start();
                    break;

                case SET_DATING_DETAILED_INFO:
                    Log.d(TAG, " has get the data from the server, and then set it on the TextView");
                    Message detailedInfoMsg = msg;
                    Log.d(TAG, " the message we get for the TextView are : " + (detailedInfoMsg.what == SET_DATING_DETAILED_INFO));
                    Bundle infoBundle = detailedInfoMsg.getData();
                    String titleInfo = infoBundle.getString(KEY_TITLE_INFO);
                    String address = infoBundle.getString(KEY_ADDRESS);
                    String startTime = infoBundle.getString(KEY_START_TIME);
                    String endTime = infoBundle.getString(KEY_END_TIME);
                    String model = infoBundle.getString(KEY_MODEL);

                    Log.d(TAG, " the data we get are : " + titleInfo + " , address : " + address + " , startTime :" + startTime
                            + " , endTime : " + endTime + " , model " + model);
                    mTvTitle.setText(titleInfo);
                    mTvActivityAddress.setText(address);
                    mTvStartTime.setText(startTime);
                    mTvEndTime.setText(endTime);
                    mTvModel.setText(model);
                    break;
                case SHOW_TOAST:
                    Message toastMsg = msg;
                    Log.d(TAG, " the message are : " + (toastMsg.what == SHOW_TOAST) + " , and the message object are : " + toastMsg);
                    Bundle toastInfo = toastMsg.getData();
                    String toastStr = toastInfo.getString(KEY_TOAST_MSG);
                    Log.d(TAG, " the toast string are : " + toastStr);
                    showToast(toastStr);
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
    private static final String KEY_SEX = "sex";
    private static final String KEY_LOOK_NUM = "lookNum";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_TIME_1 = "time1";
    private static final String KEY_TIME_2 = "time2";
    private static final String KEY_TITLE_INFO = "titleInfo";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_END_TIME = "endTime";
    private static final String KEY_MODEL = "model";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_CONTACT_PHONENUM = "phoneNum";
    private static final String KEY_ACTIVITY_INTRO = "activityIntro";

    private static final String KEY_TOAST_MSG = "toastContent";

    // TODO: 这个列表用于保存已经参加当前的约球活动的用户的头像的列表(我们目前还是用静态数据进行初始化，等到Server端接口完成之后，
    // TODO: 我们需要采用动态数据)
    private List<SearchDatingDetailedAlreadyBean> mFollowList = new ArrayList<SearchDatingDetailedAlreadyBean>();

    // TODO: 获取约球详情信息
    /**
     *
     * @param datingId 约球id
     * @return
     */
    private void retrieveDatingDetailedInfo(final int datingId)
    {
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("id", datingId + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchDating.URL_DATING_DETAILE, requestParams, HttpConstants.RequestMethod.GET);
        Log.d(TAG, " the raw request we get for the dating detailed activity are : " + rawResult);
        try {
            // TODO: 以下有一些字段暂时服务器端还没有完全定义好，并返回，所有效果有一点差
            Log.d(TAG, " start parsing the json we get ");
            JSONObject resultJson = new JSONObject(rawResult);
            JSONObject wholeResult = resultJson.getJSONObject("result"); // 得到的总的json object
            String appointId = wholeResult.getString("appoint_id"); // 约球id
            String userId = wholeResult.getString("user_id"); // 用户id
            String account = wholeResult.getString("account"); // 用户昵称
            // TODO: 以下的两个字段服务器端还没有提供，我们需要联系服务器端的人员提供
//            String sex = wholeResult.getString("sex"); // 用户性别
//            String lookNumber = wholeResult.getString("look_number"); // 浏览数目，即眼睛图标对应的那个数字
            String createTime = wholeResult.getString("create_time"); // 活动发布的日期
            String title = wholeResult.getString("title"); // 活动主题
            String address = wholeResult.getString("address"); // 活动地点
            String beginTime = wholeResult.getString("begin_time"); // 活动开始时间
            String endTime = wholeResult.getString("end_time"); // 活动结束时间
            String model = wholeResult.getString("model"); // 收费模式 1.免费;2.收费;3.AA制

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
            detailedInfoBundle.putString(KEY_TITLE_INFO, title);
            detailedInfoBundle.putString(KEY_ADDRESS, address);
            detailedInfoBundle.putString(KEY_START_TIME, beginTime);
            detailedInfoBundle.putString(KEY_END_TIME, endTime);
            detailedInfoBundle.putString(KEY_MODEL, modelStr);
            msg.setData(detailedInfoBundle);
            msg.what = SET_DATING_DETAILED_INFO;
            mHandler.sendMessage(msg);

            Log.d(TAG, " the parsed result we get are : " + appointId + " ; " +
                        userId + " ; " + account + " ; " + "sex" + " ; " + "lookNumber" + " ; " + createTime + " ; " +
                        title + " ; " + address + " ; " + beginTime + " ; " + endTime + " ; " + model);
            // 以下得到的是已经参加这次活动的人员的列表
            JSONArray followList = wholeResult.getJSONArray("join_list");
            final int followSize = followList.length();
            int i;
            for (i = 0; i < followSize; ++i)
            {
                JSONObject follower = (JSONObject) followList.get(i);
                String followerId = follower.getString(""); // 已经参加本次活动的人员的id
                String followerAccount = follower.getString(""); // 已经参加本次活动的人员的account
                String followerPhotoUrl = follower.getString(""); // 已经参加本次活动的人员的photo的url
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, " exception happened in parsing the initial json data, and the causing are : " + e.toString());
        }
    }


    // TODO: 用于处理“我要参加”的处理事件
    // TODO: 是POST请求
    /**
     *
     * @param userId 用户id
     * @param type 类型, 1对应于活动;2对应于约球
     * @param pId 分为两种类型 活动/约球
     */
    private void joinActivity(int userId, int type, int pId)
    {
        boolean resultStatus = false;

        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", userId + "");
        requestParams.put("type_id", type + "");
        requestParams.put("p_id", pId + "");

        String rawResult = HttpUtil.urlClient(HttpConstants.SearchDating.URL_JOIN_ACTIVITY, requestParams, HttpConstants.RequestMethod.POST);

        Log.d(TAG, " the raw result we get that after posting the join activity request are : " + rawResult);
        try {
            JSONObject resultJson = new JSONObject(rawResult);
            final int code = resultJson.getInt("code");
            resultStatus = code == 1001 ? true : false;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String resultStr = resultStatus ? getResources().getString(R.string.search_dating_detailed_btn_i_want_in_success)
                : getResources().getString(R.string.search_dating_detailed_btn_i_want_in_failed);

        Message toastMeg = mHandler.obtainMessage(SHOW_TOAST);

        Bundle data = new Bundle();
        Log.d(TAG, " the toast result string are : " + resultStr);
        data.putString(KEY_TOAST_MSG, resultStr);
        toastMeg.setData(data);
        toastMeg.what = SHOW_TOAST;
        Log.d(TAG, " the Toast message object we received are : : " + toastMeg.getData());
        mHandler.sendMessage(toastMeg);
    }

    // 因为Toast是涉及到UI的操作，所以必须在MainUIThread当中执行，我们不可知直接在一个第三方Thread当中
    // 调用Toast。所以我们单独封装到另一个方法当中
    private void showToast(final String content)
    {
        Toast.makeText(SearchBilliardsDatingActivity.this, content, Toast.LENGTH_LONG).show();
    }

    // TODO: the following are just the static test data, and we should
    // TODO: remove them to add the dynamic data
    private void initGridList()
    {
        int i;
        for (i = 0; i < 5; ++i)
        {
            mFollowList.add(new SearchDatingDetailedAlreadyBean("", "温柔的语"));
        }
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
                return true;
            case R.id.search_room_action_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }




    // TODO: 用于完成台球厅分享的网络的请求处理过程
    private void shareBilliardsRoomRequest(String shareTarget)
    {

    }



}
