package com.yueqiu.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.Attr;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import android.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 14/12/19.
 * 我的资料主Activity
 */
public class MyProfileActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "MyProfileActivity";
    private Button mAssistant, mCoach;
    private RelativeLayout mPhoto, mAccount, mGender, mNickName, mRegion, mLevel, mBallType,
            mBilliardsCue, mCueHabits, mPlayAge, mIdol, mSign,mCost,mWorkLive,mMyType,mZizhi;//mTheNewestPost;
    private TextView mNickNameTextView, mRegionTextView, mLevelTextView, mBallTypeTextView,
            mBilliardsCueTextView, mCueHabitsTextView, mPlayAgeTextView, mIdolTextView,
            mSignTextView, mAccountTextView, mGenderTextView,mCostTextView,mMyTypeTextView,
            mWorkLiveTextView,mZizhiTextView;
//    private ImageView mTheNewestPostImageView;
    private CustomNetWorkImageView mPhotoImageView;
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mEditor;

    public static final String EXTRA_FRAGMENT_ID =
            "com.yueqiu.activity.searchmenu.myprofileactivity.fragment_id";
    public static final String EXTRA_USERINFO =
            "com.yueqiu.activity.searchmenu.myprofileactivity.userinfo";
    public static int EXTRA_REQUEST_ID = 0;
    public static String EXTRA_RESULT_ID = "com.yueqiu.activity.searchmenu.myprofileactivity.result_id";

    //data
    private int mUserId = YueQiuApp.sUserInfo.getUser_id();
    private UserInfo mUserInfo;
    private JSONArray mJSONArray;
    private static final int DATA_ERROR = 1;
    private static final int DATA_SUCCESS = 2;
    private Map<String, String> mMap = new HashMap<String, String>();
    private UserDao mUserDao;
    private ImageLoader mImgLoader;
    private LinearLayout mAfterUpgradeAssitantView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        mSharedPreference = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);
        mEditor = mSharedPreference.edit();
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mUserDao = DaoFactory.getUser(this);
        mMap.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(mUserId));
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_my_profile_str));
        initView();
        setClickListener();
//        initData();

    }

    private void initView() {
        mAssistant = (Button) findViewById(R.id.update_assistant_btn);
        mCoach = (Button) findViewById(R.id.update_coach_btn);
        mPhoto = (RelativeLayout) findViewById(R.id.my_profile_photo);
        mAccount = (RelativeLayout) findViewById(R.id.my_profile_account);
        mGender = (RelativeLayout) findViewById(R.id.my_profile_gender);
        mNickName = (RelativeLayout) findViewById(R.id.my_profile_nick_name);
        mRegion = (RelativeLayout) findViewById(R.id.my_profile_region);
        mLevel = (RelativeLayout) findViewById(R.id.my_profile_level);
        mBallType = (RelativeLayout) findViewById(R.id.my_profile_ball_type);
        mBilliardsCue = (RelativeLayout) findViewById(R.id.my_profile_billiards_cue);
        mCueHabits = (RelativeLayout) findViewById(R.id.my_profile_cue_habits);
        mPlayAge = (RelativeLayout) findViewById(R.id.my_profile_play_age);
        mIdol = (RelativeLayout) findViewById(R.id.my_profile_idol);
        mSign = (RelativeLayout) findViewById(R.id.my_profile_sign);
        mAfterUpgradeAssitantView = (LinearLayout) findViewById(R.id.profile_after_upgrade_assistant);
        mCost = (RelativeLayout) findViewById(R.id.profile_cost_re);
        mWorkLive = (RelativeLayout) findViewById(R.id.profile_work_live_re);
        mMyType = (RelativeLayout) findViewById(R.id.profile_type_re);
        mZizhi = (RelativeLayout) findViewById(R.id.profile_zizhi_re);

        mCostTextView = (TextView) findViewById(R.id.profile_upgrade_cost);
        mMyTypeTextView = (TextView) findViewById(R.id.profile_upgrade_type);
        mWorkLiveTextView = (TextView) findViewById(R.id.profile_upgrade_experience);
        mZizhiTextView = (TextView) findViewById(R.id.profile_upload_zizhi);
//        mTheNewestPost = (RelativeLayout) findViewById(R.id.my_profile_the_new_post);

        mAccountTextView = (TextView) findViewById(R.id.my_profile_account_tv);
        mGenderTextView = (TextView) findViewById(R.id.my_profile_gender_tv);
        mNickNameTextView = (TextView) findViewById(R.id.my_profile_nick_name_tv);
        mRegionTextView = (TextView) findViewById(R.id.my_profile_region_tv);
        mLevelTextView = (TextView) findViewById(R.id.my_profile_level_tv);
        mBallTypeTextView = (TextView) findViewById(R.id.my_profile_ball_type_tv);
        mBilliardsCueTextView = (TextView) findViewById(R.id.my_profile_billiards_cue_tv);
        mCueHabitsTextView = (TextView) findViewById(R.id.my_profile_cue_habits_tv);
        mPlayAgeTextView = (TextView) findViewById(R.id.my_profile_play_age_tv);
        mIdolTextView = (TextView) findViewById(R.id.my_profile_idol_tv);
        mSignTextView = (TextView) findViewById(R.id.my_profile_sign_tv);

        mPhotoImageView = (CustomNetWorkImageView) findViewById(R.id.my_profile_photo_iv);
//        mTheNewestPostImageView = (ImageView) findViewById(R.id.my_profile_the_new_post_im);
        mPhotoImageView.setDefaultImageResId(R.drawable.default_head);
        mPhotoImageView.setErrorImageResId(R.drawable.default_head);




    }

    @Override
    protected void onResume() {
        super.onResume();

//        if(YueQiuApp.sUserInfo.getTitle().equals(getString(R.string.nearby_billiard_coauch_str))){
//            mAssistant.setVisibility(View.GONE);
//            mCoach.setVisibility(View.GONE);
//        }
        initData();
        if(YueQiuApp.sUserInfo.getTitle().equals(getString(R.string.nearby_billiard_mate_str))){
            mAfterUpgradeAssitantView.setVisibility(View.GONE);
        }

        if(YueQiuApp.sUserInfo.getTitle().equals(getString(R.string.nearby_billiard_assist_coauch_str))
                ){
            mAssistant.setVisibility(View.GONE);
            mCoach.setVisibility(View.GONE);
            mAfterUpgradeAssitantView.setVisibility(View.VISIBLE);
            mZizhi.setVisibility(View.GONE);
        }

        if(YueQiuApp.sUserInfo.getTitle().equals(getString(R.string.nearby_billiard_coauch_str))){
            mAssistant.setVisibility(View.GONE);
            mCoach.setVisibility(View.GONE);
            mAfterUpgradeAssitantView.setVisibility(View.VISIBLE);
        }
    }

    //初始化我的资料数据
    private void initData() {

        if (Utils.networkAvaiable(this)) {

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    map.put(HttpConstants.GetMyInfo.USER_ID, mUserId);

                    HttpUtil.requestHttp(HttpConstants.GetMyInfo.URL,map,HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Log.d("wy","myprofile response->" + response);
                            Message message = new Message();
                            try {
                                if (!response.isNull("code")) {
                                    if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                                        String sex = response.getJSONObject("result").getString(DatabaseConstant.UserTable.SEX);
                                        String img_url = response.getJSONObject("result").getString(DatabaseConstant.UserTable.IMG_URL);
                                        String username = response.getJSONObject("result").getString(DatabaseConstant.UserTable.USERNAME);
                                        String nick = response.getJSONObject("result").getString(DatabaseConstant.UserTable.NICK);
                                        String district = response.getJSONObject("result").getString(DatabaseConstant.UserTable.DISTRICT);
                                        String level = response.getJSONObject("result").getString(DatabaseConstant.UserTable.LEVEL);
                                        String ball_type = response.getJSONObject("result").getString(DatabaseConstant.UserTable.BALL_TYPE);
                                        String appoint_date = response.getJSONObject("result").getString(DatabaseConstant.UserTable.APPOINT_DATE);
                                        String ball_arm = response.getJSONObject("result").getString(DatabaseConstant.UserTable.BALLARM);
                                        String usedType = response.getJSONObject("result").getString(DatabaseConstant.UserTable.USERDTYPE);
                                        String ball_age = response.getJSONObject("result").getString(DatabaseConstant.UserTable.BALLAGE);
                                        String idol = response.getJSONObject("result").getString(DatabaseConstant.UserTable.IDOL);
                                        String idol_name = response.getJSONObject("result").getString(DatabaseConstant.UserTable.IDOL_NAME);
                                        String new_img = response.getJSONObject("result").getString(DatabaseConstant.UserTable.NEW_IMG);
                                        String cost = response.getJSONObject("result").getString(DatabaseConstant.UserTable.COST);
                                        String my_type = response.getJSONObject("result").getString(DatabaseConstant.UserTable.MY_TYPE);
                                        String work_live = response.getJSONObject("result").getString(DatabaseConstant.UserTable.WORK_LIVE);
                                        int zizhi = response.getJSONObject("result").getInt(DatabaseConstant.UserTable.ZIZHI);

                                        mMap.put(DatabaseConstant.UserTable.SEX, sex);
                                        mMap.put(DatabaseConstant.UserTable.IMG_URL, img_url);
                                        mMap.put(DatabaseConstant.UserTable.USERNAME, username);
                                        mMap.put(DatabaseConstant.UserTable.NICK, nick);
                                        mMap.put(DatabaseConstant.UserTable.DISTRICT, district);
                                        mMap.put(DatabaseConstant.UserTable.LEVEL, level);
                                        mMap.put(DatabaseConstant.UserTable.BALL_TYPE, ball_type);
                                        mMap.put(DatabaseConstant.UserTable.APPOINT_DATE, appoint_date);
                                        mMap.put(DatabaseConstant.UserTable.BALLARM, ball_arm);
                                        mMap.put(DatabaseConstant.UserTable.USERDTYPE, usedType);
                                        mMap.put(DatabaseConstant.UserTable.BALLAGE, ball_age);
                                        mMap.put(DatabaseConstant.UserTable.IDOL, idol);
                                        mMap.put(DatabaseConstant.UserTable.IDOL_NAME, idol_name);
                                        mMap.put(DatabaseConstant.UserTable.NEW_IMG, new_img);
                                        //TODO:等服务器那边加上了，取消注释
                                        mMap.put(DatabaseConstant.UserTable.COST,cost);
                                        mMap.put(DatabaseConstant.UserTable.MY_TYPE,my_type);
                                        mMap.put(DatabaseConstant.UserTable.WORK_LIVE,work_live);
                                        mMap.put(DatabaseConstant.UserTable.ZIZHI,String.valueOf(zizhi));

//                                        mUserInfo = Utils.mapingObject(UserInfo.class, response.getJSONObject("result"));
                                        mUserInfo = new UserInfo();
                                        mUserInfo.setSex(Integer.valueOf(sex));
                                        mUserInfo.setImg_url(img_url);
                                        mUserInfo.setUsername(username);
                                        mUserInfo.setNick(nick);
                                        mUserInfo.setDistrict(district);
                                        mUserInfo.setLevel(Integer.valueOf(level));
                                        mUserInfo.setBall_type(Integer.valueOf(ball_type));
                                        mUserInfo.setAppoint_date(appoint_date);
                                        mUserInfo.setBallArm(Integer.valueOf(ball_arm));
                                        mUserInfo.setUsedType(Integer.valueOf(usedType));
                                        mUserInfo.setBallAge(ball_age);
                                        mUserInfo.setIdol(idol);
                                        mUserInfo.setIdol_name(idol_name);
                                        mUserInfo.setCost(cost);
                                        mUserInfo.setMy_type(Integer.valueOf(my_type));
                                        mUserInfo.setWork_live(work_live);
                                        mUserInfo.setZizhi(zizhi);


                                        message.what = DATA_SUCCESS;
                                        message.obj = mUserInfo;

                                    } else if (response.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                        mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                                    } else {
                                        message.what = DATA_ERROR;
                                        message.obj = response.getString("msg");
                                    }
                                    mHandler.sendMessage(message);
                                } else {
                                    message.what = DATA_ERROR;
                                    mHandler.sendMessage(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                //加载本地数据
                                getMyProfileFromLocal();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                            mHandler.sendEmptyMessage(DATA_ERROR);
                        }
                    });

        } else {
            getMyProfileFromLocal();
            mHandler.obtainMessage(DATA_SUCCESS, mUserInfo).sendToTarget();
        }
    }

    private void getMyProfileFromLocal() {
        mUserInfo = mUserDao.getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_ERROR:
                    Log.i(TAG, "error to get profile from service");
                    if (null == msg.obj) {
                        Utils.showToast(MyProfileActivity.this, getString(R.string.http_request_error));
                    } else {
                        Utils.showToast(MyProfileActivity.this, (String) msg.obj);
                    }
                    getMyProfileFromLocal();
                    updateUI(mUserInfo);
                    updateGlobalUserId(mUserInfo);
                    break;
                case DATA_SUCCESS:
                    mUserDao.updateUserInfo(mMap);
                    updateUI((UserInfo) msg.obj);
                    updateGlobalUserId((UserInfo) msg.obj);
                    break;
                case PublicConstant.TIME_OUT:
                    Toast.makeText(MyProfileActivity.this, getString(R.string.http_request_time_out), Toast.LENGTH_SHORT).show();
                    getMyProfileFromLocal();
                    updateUI(mUserInfo);
                    updateGlobalUserId(mUserInfo);
                    break;
            }
        }
    };

    private void updateUI(UserInfo userInfo) {
        String unset = getString(R.string.unset);
        mPhotoImageView.setImageUrl("http://" + userInfo.getImg_url(),mImgLoader);
        mAccountTextView.setText(userInfo.getUsername());
        mGenderTextView.setText(userInfo.getSex() == 1
                ? getString(R.string.man) : getString(R.string.woman));
        mNickNameTextView.setText("".equals(userInfo.getNick())
                ? unset : userInfo.getNick());
        mRegionTextView.setText("".equals(userInfo.getDistrict())
                ? unset : userInfo.getDistrict());
        mLevelTextView.setText(1 == userInfo.getLevel()
                ? getString(R.string.level_base) : ((2 == userInfo.getLevel()) ?
                getString(R.string.level_middle) : getString(R.string.level_master)));
        mBallTypeTextView.setText(1 == userInfo.getBall_type()
                ? getString(R.string.ball_type_1) : (2 == userInfo.getBall_type() ?
                getString(R.string.ball_type_2) : getString(R.string.ball_type_3)));
        mBilliardsCueTextView.setText(1 == userInfo.getBallArm()
                ? getString(R.string.cue_1) : getString(R.string.cue_2));
        mCueHabitsTextView.setText(1 == userInfo.getUsedType()
                ? getString(R.string.habit_1) : (2 == userInfo.getUsedType() ?
                getString(R.string.habit_2) : getString(R.string.habit_3)));
        mPlayAgeTextView.setText("0".equals(userInfo.getIdol_name())
                ? unset : userInfo.getBallAge());
        mIdolTextView.setText("".equals(userInfo.getIdol())
                ? unset : userInfo.getIdol());
        mSignTextView.setText("".equals(userInfo.getIdol_name())|| "0".equals(userInfo.getIdol_name())
                ? unset : userInfo.getIdol_name());
        mCostTextView.setText(TextUtils.isEmpty(userInfo.getCost()) || "0".equals(userInfo.getIdol_name())
                ? unset : userInfo.getCost());
        mMyTypeTextView.setText(TextUtils.isEmpty(String.valueOf(userInfo.getMy_type()))?
                unset : getTypeStrByTypeId(userInfo.getMy_type()));
        mWorkLiveTextView.setText(TextUtils.isEmpty(userInfo.getWork_live()) ?
                unset : userInfo.getWork_live());
        mZizhiTextView.setText(TextUtils.isEmpty(String.valueOf(userInfo.getZizhi())) ?
                unset : getZizhiById(userInfo.getZizhi()));
//        mTheNewestPostImageView.setImageDrawable();
    }


    private void updateGlobalUserId(UserInfo user){
        YueQiuApp.sUserInfo.setUsername(user.getUsername());
        YueQiuApp.sUserInfo.setImg_url(user.getImg_url());
        YueQiuApp.sUserInfo.setSex(user.getSex());
        YueQiuApp.sUserInfo.setNick(user.getNick());
        YueQiuApp.sUserInfo.setDistrict(user.getDistrict());
        YueQiuApp.sUserInfo.setLevel(user.getLevel());
        YueQiuApp.sUserInfo.setBall_type(user.getBall_type());
        YueQiuApp.sUserInfo.setBallArm(user.getBallArm());
        YueQiuApp.sUserInfo.setUsedType(user.getUsedType());
        YueQiuApp.sUserInfo.setBallAge(user.getBallAge());
        YueQiuApp.sUserInfo.setIdol(user.getIdol());
        YueQiuApp.sUserInfo.setIdol_name(user.getIdol_name());
        //TODO:等服务器加上这几个字段取消注释
        YueQiuApp.sUserInfo.setCost(user.getCost());
        YueQiuApp.sUserInfo.setMy_type(user.getMy_type());
        YueQiuApp.sUserInfo.setWork_live(user.getWork_live());
        YueQiuApp.sUserInfo.setZizhi(user.getZizhi());

        mEditor.putString(DatabaseConstant.UserTable.USERNAME,user.getUsername());
        mEditor.putString(DatabaseConstant.UserTable.IMG_URL,user.getImg_url());
        mEditor.putInt(DatabaseConstant.UserTable.SEX,user.getSex());
        mEditor.putString(DatabaseConstant.UserTable.NICK,user.getNick());
        mEditor.putString(DatabaseConstant.UserTable.DISTRICT,user.getDistrict());
        mEditor.putInt(DatabaseConstant.UserTable.LEVEL,user.getLevel());
        mEditor.putInt(DatabaseConstant.UserTable.BALL_TYPE,user.getBall_type());
        mEditor.putInt(DatabaseConstant.UserTable.BALLARM,user.getBallArm());
        mEditor.putInt(DatabaseConstant.UserTable.USERDTYPE,user.getUsedType());
        mEditor.putString(DatabaseConstant.UserTable.BALLAGE, user.getBallAge());
        mEditor.putString(DatabaseConstant.UserTable.IDOL,user.getIdol());
        mEditor.putString(DatabaseConstant.UserTable.IDOL_NAME,user.getIdol_name());
        mEditor.putString(DatabaseConstant.UserTable.COST,user.getCost());
        mEditor.putString(DatabaseConstant.UserTable.MY_TYPE,String.valueOf(user.getMy_type()));
        mEditor.putString(DatabaseConstant.UserTable.WORK_LIVE,user.getWork_live());
        mEditor.putInt(DatabaseConstant.UserTable.ZIZHI,user.getZizhi());

        mEditor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //保存数据到本地
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                mUserDao.updateUserInfo(mMap);
                finish();
                overridePendingTransition(R.anim.top_in, R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setClickListener() {
        mAssistant.setOnClickListener(this);
        mCoach.setOnClickListener(this);
        mPhoto.setOnClickListener(this);
        mAccount.setOnClickListener(this);
        mGender.setOnClickListener(this);
        mNickName.setOnClickListener(this);
        mRegion.setOnClickListener(this);
        mLevel.setOnClickListener(this);
        mBallType.setOnClickListener(this);
        mBilliardsCue.setOnClickListener(this);
        mCueHabits.setOnClickListener(this);
        mPlayAge.setOnClickListener(this);
        mIdol.setOnClickListener(this);
        mSign.setOnClickListener(this);
        mCost.setOnClickListener(this);
        mMyType.setOnClickListener(this);
        mWorkLive.setOnClickListener(this);
//        mTheNewestPost.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                mUserDao.updateUserInfo(mMap);
                finish();
                overridePendingTransition(R.anim.top_in, R.anim.top_out);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.update_assistant_btn:
                //TODO:升级助教界面
                Intent assitant = new Intent(this, UpgradeAssistantActivity.class);
                assitant.putExtra(DatabaseConstant.UserTable.TITLE,getString(R.string.nearby_billiard_assist_coauch_str));
                startActivity(assitant);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.update_coach_btn:
                //TODO:升级教练界面
                Intent coach = new Intent(this, UpgradeAssistantActivity.class);
                coach.putExtra(DatabaseConstant.UserTable.TITLE,getString(R.string.nearby_billiard_coauch_str));
                startActivity(coach);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.my_profile_photo:
                startMyActivity(Attr.PHOTO);
                break;
            case R.id.my_profile_account:
//                startMyActivity(1);
                break;
            case R.id.my_profile_gender:
//                startMyActivity(2);
                break;
            case R.id.my_profile_nick_name:
                startMyActivity(Attr.NICKNAME);
                break;
            case R.id.my_profile_region:
                startMyActivity(Attr.DISTRICT);
                break;
            case R.id.my_profile_level:
                startMyActivity(Attr.LEVEL);
                break;
            case R.id.my_profile_ball_type:
                startMyActivity(Attr.BALL_CLASS);
                break;
            case R.id.my_profile_billiards_cue:
                startMyActivity(Attr.BALL_ARM);
                break;
            case R.id.my_profile_cue_habits:
                startMyActivity(Attr.USE_TYPE);
                break;
            case R.id.my_profile_play_age:
                startMyActivity(Attr.BALL_AGE);
                break;
            case R.id.my_profile_idol:
                startMyActivity(Attr.IDOL);
                break;
            case R.id.my_profile_sign:
                startMyActivity(Attr.IDOL_NAME);
                break;
            case R.id.profile_cost_re:
                startMyActivity(Attr.COST);
                break;
            case R.id.profile_type_re:
                startMyActivity(Attr.MY_TYPE);
                break;
            case R.id.profile_work_live_re:
                startMyActivity(Attr.WORK_LIVE);
                break;
//            case R.id.my_profile_the_new_post:
//                startMyActivity(12);
//                break;
        }
    }

    private void startMyActivity(Attr attr) {
        Log.d("wy","attr ->" + attr.name());
        Intent i = new Intent(this, ProfileSetupActivity.class);
        i.putExtra(EXTRA_FRAGMENT_ID, attr.ordinal());
        startActivityForResult(i, attr.ordinal());
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String str = data.getStringExtra(EXTRA_RESULT_ID);
        if ("".equals(str) || null == str)
            return;
        if (mUserInfo == null)
            mUserInfo = new UserInfo();

        Attr attr = getAttrById(requestCode);
        switch (attr) {
            case PHOTO:
                String img_url = "http://" + data.getStringExtra(PublicConstant.IMG_URL);
                mPhotoImageView.setImageUrl(img_url,mImgLoader);
            case NICKNAME:
                mNickNameTextView.setText(str);
                mUserInfo.setNick(str);
                mMap.put(DatabaseConstant.UserTable.NICK, str);
                break;
            case DISTRICT:
                mRegionTextView.setText(str);
                mUserInfo.setDistrict(str);
                mMap.put(DatabaseConstant.UserTable.DISTRICT, str);
                break;
            case LEVEL:
                mLevelTextView.setText("1".equals(str)
                        ? getString(R.string.level_base) : (("2".equals(str)) ?
                        getString(R.string.level_middle) : getString(R.string.level_master)));
                mUserInfo.setLevel(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.LEVEL, str);
                break;
            case BALL_CLASS:
                mBallTypeTextView.setText("1".equals(str)
                        ? getString(R.string.ball_type_1) : ("2".equals(str) ?
                        getString(R.string.ball_type_2) : getString(R.string.ball_type_3)));
                mUserInfo.setBall_type(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.BALL_TYPE, str);
                break;
            case BALL_ARM:
                mBilliardsCueTextView.setText("1".equals(str)
                        ? getString(R.string.cue_1) : getString(R.string.cue_2));
                mUserInfo.setUsedType(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.BALLARM, str);
                break;
            case USE_TYPE:
                mCueHabitsTextView.setText("1".equals(str)
                        ? getString(R.string.habit_1) : ("2".equals(str) ?
                        getString(R.string.habit_2) : getString(R.string.habit_3)));
                mUserInfo.setBallArm(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.USERDTYPE, str);
                break;
            case BALL_AGE:
                mPlayAgeTextView.setText(str);
                mUserInfo.setBallAge(str);
                mMap.put(DatabaseConstant.UserTable.BALLAGE, str);
                break;
            case IDOL:
                mIdolTextView.setText(str);
                mUserInfo.setIdol(str);
                mMap.put(DatabaseConstant.UserTable.IDOL, str);
                break;
            case IDOL_NAME:
                mSignTextView.setText(str);
                mUserInfo.setIdol_name(str);
                mMap.put(DatabaseConstant.UserTable.IDOL_NAME, str);
                break;
            case COST:
                mCostTextView.setText(str);
                mUserInfo.setCost(str);
                mMap.put(DatabaseConstant.UserTable.COST,str);
                break;
            case MY_TYPE:
                int typeId = Integer.valueOf(str);
                String myType = getTypeStrByTypeId(typeId);
                mMyTypeTextView.setText(myType);
                mUserInfo.setMy_type(typeId);
                mMap.put(DatabaseConstant.UserTable.MY_TYPE,String.valueOf(typeId));
                break;
            case WORK_LIVE:
                mWorkLiveTextView.setText(str);
                mUserInfo.setWork_live(str);
                mMap.put(DatabaseConstant.UserTable.WORK_LIVE,str);
                break;
            case ZIZHI:
                int id = Integer.valueOf(str);
                String zizhi = getZizhiById(id);
                mZizhiTextView.setText(zizhi);
                mUserInfo.setZizhi(id);
                mMap.put(DatabaseConstant.UserTable.ZIZHI,String.valueOf(id));
                break;

        }
        mUserDao.updateUserInfo(mMap);
    }

    private Attr getAttrById(int id){
        Attr attr = null;
        if(id == 0){
            attr = Attr.PHOTO;
        }else if(id == 1){
            attr = Attr. NICKNAME;
        }else if(id == 2) {
            attr = Attr.DISTRICT;
        }else if(id == 3){
            attr = Attr.LEVEL;
        }else if(id == 3){
            attr = Attr.BALL_CLASS;
        }else if(id == 4){
            attr = Attr.BALL_ARM;
        }else if(id == 5){
            attr = Attr.USE_TYPE;
        }else if(id == 6){
            attr = Attr.BALL_AGE;
        }else if(id == 7){
            attr = Attr.IDOL;
        }else if(id == 8){
            attr = Attr.IDOL_NAME;
        }else if(id == 9){
            attr = Attr.COST;
        }else if(id == 10){
            attr = Attr.COST;
        }else if(id == 11){
            attr = Attr.MY_TYPE;
        }else if(id == 12){
            attr = Attr.WORK_LIVE;
        }else if(id == 13){
            attr = Attr.ZIZHI;
        }
        return attr;
    }
    private String getTypeStrByTypeId(int type_id){
        String str = "";
        switch (type_id){
            case PublicConstant.LOVELY_TYPE:
                str = getString(R.string.lovely_type);
                break;
            case PublicConstant.MATURE_TYPE:
                str = getString(R.string.mature_type);
                break;
            case PublicConstant.GODNESS_TYPE:
                str = getString(R.string.godness_type);
                break;
            case PublicConstant.CHARMING_TYPE:
                str = getString(R.string.charming_type);
                break;
            case PublicConstant.STRENGTH_TYPE:
                str = getString(R.string.strength_type);
                break;
            case PublicConstant.HANDSOME_TYPE:
                str = getString(R.string.handsome_type);
                break;
        }
        return str;
    }

    private String getZizhiById(int id){
        String str = "";
        switch(id){
            case PublicConstant.ZIZHI_COUNTRY_MEMBER:
                str = getString(R.string.zizhi_country_team_member);
                break;
            case PublicConstant.ZIZHI_PROFESSION:
                str = getString(R.string.zizhi_profession_memeber);
                break;
            case PublicConstant.ZIZHI_COACH:
                str = getString(R.string.zizhi_coach);
                break;
            case PublicConstant.ZIZHI_OTHER:
                str = getString(R.string.billiard_other);
                break;
        }
        return str;
    }

}
