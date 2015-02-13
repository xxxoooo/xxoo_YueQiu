package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;

import android.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
            mBilliardsCue, mCueHabits, mPlayAge, mIdol, mSign, mTheNewestPost;
    private TextView mNickNameTextView, mRegionTextView, mLevelTextView, mBallTypeTextView,
            mBilliardsCueTextView, mCueHabitsTextView, mPlayAgeTextView, mIdolTextView,
            mSignTextView, mAccountTextView, mGenderTextView;
    private ImageView mTheNewestPostImageView;
    private NetworkImageView mPhotoImageView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        mUserDao = DaoFactory.getUser(this);
        mMap.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(mUserId));
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_my_profile_str));
        mImgLoader = VolleySingleton.getInstance().getImgLoader();
        initView();
        setClickListener();
        initData();

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
        mTheNewestPost = (RelativeLayout) findViewById(R.id.my_profile_the_new_post);

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

        mPhotoImageView = (NetworkImageView) findViewById(R.id.my_profile_photo_iv);
        mTheNewestPostImageView = (ImageView) findViewById(R.id.my_profile_the_new_post_im);
        mPhotoImageView.setDefaultImageResId(R.drawable.default_head);
    }

    //初始化我的资料数据
    private void initData() {

        if (Utils.networkAvaiable(this)) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Map<String, Integer> map = new HashMap<String, Integer>();
                    map.put(HttpConstants.GetMyInfo.USER_ID, mUserId);

                    String result = HttpUtil.urlClient(HttpConstants.GetMyInfo.URL,
                            map, HttpConstants.RequestMethod.GET);
                    JSONObject object = Utils.parseJson(result);
                    Message message = new Message();
                    try {
                        if (!object.isNull("code")) {
                            if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {

                                mMap.put(DatabaseConstant.UserTable.SEX, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.SEX));
                                mMap.put(DatabaseConstant.UserTable.IMG_URL, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.IMG_URL));
                                mMap.put(DatabaseConstant.UserTable.USERNAME, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.USERNAME));
                                mMap.put(DatabaseConstant.UserTable.NICK, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.NICK));
                                mMap.put(DatabaseConstant.UserTable.DISTRICT, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.DISTRICT));
                                mMap.put(DatabaseConstant.UserTable.LEVEL, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.LEVEL));
                                mMap.put(DatabaseConstant.UserTable.BALL_TYPE, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.BALL_TYPE));
                                mMap.put(DatabaseConstant.UserTable.APPOINT_DATE, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.APPOINT_DATE));
                                mMap.put(DatabaseConstant.UserTable.BALLARM, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.BALLARM));
                                mMap.put(DatabaseConstant.UserTable.USERDTYPE, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.USERDTYPE));
                                mMap.put(DatabaseConstant.UserTable.BALLAGE, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.BALLAGE));
                                mMap.put(DatabaseConstant.UserTable.IDOL, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.IDOL));
                                mMap.put(DatabaseConstant.UserTable.IDOL_NAME, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.IDOL_NAME));
                                mMap.put(DatabaseConstant.UserTable.NEW_IMG, object.getJSONObject("result").
                                        getString(DatabaseConstant.UserTable.NEW_IMG));

                                mUserInfo = Utils.mapingObject(UserInfo.class, object.getJSONObject("result"));
                                message.what = DATA_SUCCESS;
                                message.obj = mUserInfo;


                            } else if (object.getInt("code") == HttpConstants.ResponseCode.TIME_OUT) {
                                mHandler.obtainMessage(PublicConstant.TIME_OUT).sendToTarget();
                            } else {
                                message.what = DATA_ERROR;
                                message.obj = object.getString("msg");
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
            }).start();
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
                    break;
            }
        }
    };

    private void updateUI(UserInfo userInfo) {
        String unset = getString(R.string.unset);
        mPhotoImageView.setImageUrl(userInfo.getImg_url(),mImgLoader);
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
        mPlayAgeTextView.setText(userInfo.getBallAge());
        mIdolTextView.setText("".equals(userInfo.getIdol())
                ? unset : userInfo.getIdol());
        mSignTextView.setText("".equals(userInfo.getIdol_name())
                ? unset : userInfo.getIdol_name());
//        mTheNewestPostImageView.setImageDrawable();
    }

    private void updateGlobalUserId(UserInfo user){
        YueQiuApp.sUserInfo.setUsername(user.getUsername());
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
        mTheNewestPost.setOnClickListener(this);
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
                startActivity(new Intent(this, UpgradeAssistantActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.update_coach_btn:
                //TODO:升级教练界面
                startActivity(new Intent(this, UpgradeAssistantActivity.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.my_profile_photo:
                startMyActivity(0);
                break;
            case R.id.my_profile_account:
//                startMyActivity(1);
                break;
            case R.id.my_profile_gender:
//                startMyActivity(2);
                break;
            case R.id.my_profile_nick_name:
                startMyActivity(3);
                break;
            case R.id.my_profile_region:
                startMyActivity(4);
                break;
            case R.id.my_profile_level:
                startMyActivity(5);
                break;
            case R.id.my_profile_ball_type:
                startMyActivity(6);
                break;
            case R.id.my_profile_billiards_cue:
                startMyActivity(7);
                break;
            case R.id.my_profile_cue_habits:
                startMyActivity(8);
                break;
            case R.id.my_profile_play_age:
                startMyActivity(9);
                break;
            case R.id.my_profile_idol:
                startMyActivity(10);
                break;
            case R.id.my_profile_sign:
                startMyActivity(11);
                break;
            case R.id.my_profile_the_new_post:
//                startMyActivity(12);
                break;
        }
    }

    private void startMyActivity(int id) {
        Intent i = new Intent(this, ProfileSetupActivity.class);
        i.putExtra(EXTRA_FRAGMENT_ID, id);
        startActivityForResult(i, id);
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
        switch (requestCode) {
            case 0:

                break;
            case 1:

                break;
            case 2:

                break;
            case 3:
                mNickNameTextView.setText(str);
                mUserInfo.setNick(str);
                mMap.put(DatabaseConstant.UserTable.NICK, str);
                break;
            case 4:
                mRegionTextView.setText(str);
                mUserInfo.setDistrict(str);
                mMap.put(DatabaseConstant.UserTable.DISTRICT, str);
                break;
            case 5:
                mLevelTextView.setText("1".equals(str)
                        ? getString(R.string.level_base) : (("2".equals(str)) ?
                        getString(R.string.level_middle) : getString(R.string.level_master)));
                mUserInfo.setLevel(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.LEVEL, str);
                break;
            case 6:
                mBallTypeTextView.setText("1".equals(str)
                        ? getString(R.string.ball_type_1) : ("2".equals(str) ?
                        getString(R.string.ball_type_2) : getString(R.string.ball_type_3)));
                mUserInfo.setBall_type(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.BALL_TYPE, str);
                break;
            case 7:
                mBilliardsCueTextView.setText("1".equals(str)
                        ? getString(R.string.cue_1) : getString(R.string.cue_2));
                mUserInfo.setUsedType(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.BALLARM, str);
                break;
            case 8:
                mCueHabitsTextView.setText("1".equals(str)
                        ? getString(R.string.habit_1) : ("2".equals(str) ?
                        getString(R.string.habit_2) : getString(R.string.habit_3)));
                mUserInfo.setBallArm(Integer.parseInt(str));
                mMap.put(DatabaseConstant.UserTable.USERDTYPE, str);
                break;
            case 9:
                mPlayAgeTextView.setText(str);
                mUserInfo.setBallAge(str);
                mMap.put(DatabaseConstant.UserTable.BALLAGE, str);
                break;
            case 10:
                mIdolTextView.setText(str);
                mUserInfo.setIdol(str);
                mMap.put(DatabaseConstant.UserTable.IDOL, str);
                break;
            case 11:
                mSignTextView.setText(str);
                mUserInfo.setIdol_name(str);
                mMap.put(DatabaseConstant.UserTable.IDOL_NAME, str);
                break;
            case 12:
                break;
        }
        mUserDao.updateUserInfo(mMap);
    }


}
