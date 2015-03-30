package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yueqiu.R;
import com.yueqiu.activity.FriendNewPhotoActivity;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.view.CustomNetWorkImageView;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendProfileFragment extends Fragment implements View.OnClickListener{

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    private Button mButton;
    private CustomNetWorkImageView mImageView;
    private TextView mAccountTextView, mGenderTextView,
            mNickNameTextView, mDistrictTextView, mLevelTextView,
            mBallTypeTextView, mUsedTypeTextView, mBallArmTextView,mNewPhotoTv;
    private RelativeLayout mNewPhotoRe;
    private int mUserId;
    private static final int DATA_SUCCESS = 1;
    private UserInfo mFriendInfo;
    public static final String IMG_URL_REAL_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.img_url";
    public static final String ACCOUNT_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.account_key";
    public static final String GENDER_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.gender_key";
    public static final String DISTRICT_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.district_key";
    public static final String FRIEND_USER_ID = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.friend_user_id_key";
    public static final String USER_NAME_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.username_key";

    private String img_path, account, gender, nick_name, district, level, ball_type, ball_arm, used_type, user_id;
    private ImageLoader mImageLoader;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mUserId = getActivity().getIntent().getIntExtra(AddPersonFragment.FRIEND_INFO_USER_ID, 0);
        mImageLoader = VolleySingleton.getInstance().getImgLoader();


    }

    @Override
    public void onStart() {
        super.onStart();
        String username = getActivity().getIntent().getStringExtra(AddPersonFragment.FRIEND_INFO_USERNAME);
        mActionBar.setTitle(username);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);
        initView(view);
        initData();
        mButton = (Button) view.findViewById(R.id.add_to_friends);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString(IMG_URL_REAL_KEY, img_path);
                args.putString(ACCOUNT_KEY, account);
                args.putString(GENDER_KEY, gender);
                args.putString(DISTRICT_KEY, district);
                args.putString(FRIEND_USER_ID, user_id);
                Fragment fragment = new VerificationFragment();
                fragment.setArguments(args);
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.addToBackStack("com.yueqiu.activity.RequestAddFriendActivity");
                ft.replace(R.id.fragment_container, fragment).commit();
            }
        });
        return view;
    }

    private void initView(View view) {
        mImageView = (CustomNetWorkImageView) view.findViewById(R.id.friend_profile_photo);
        mAccountTextView = (TextView) view.findViewById(R.id.friend_profile_account);
        mGenderTextView = (TextView) view.findViewById(R.id.friend_profile_gender);
        mNickNameTextView = (TextView) view.findViewById(R.id.friend_profile_nick_name);
        mDistrictTextView = (TextView) view.findViewById(R.id.friend_profile_district);
        mLevelTextView = (TextView) view.findViewById(R.id.friend_profile_level);
        mBallTypeTextView = (TextView) view.findViewById(R.id.friend_profile_ball_type);
        mBallArmTextView = (TextView) view.findViewById(R.id.friend_profile_ball_arm);
        mUsedTypeTextView = (TextView) view.findViewById(R.id.friend_profile_used_type);
        mNewPhotoRe = (RelativeLayout) view.findViewById(R.id.friend_new_photo_re);
        mNewPhotoTv = (TextView) view.findViewById(R.id.friend_new_photo_tv);

        mNewPhotoRe.setOnClickListener(this);
    }

    private void initData() {
        if (mUserId == 0) {
            toast();
            return;
        }

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put(HttpConstants.GetMyInfo.USER_ID, mUserId);

        HttpUtil.requestHttp(HttpConstants.GetMyInfo.URL,map, HttpConstants.RequestMethod.GET,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("wy","friend profile response ->" + response);
                Message message = new Message();
                try {
                    if (response.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
//                        mFriendInfo = Utils.mapingObject(UserInfo.class, response.getJSONObject("result"));


                        String user_id = response.getJSONObject("result").getString(DatabaseConstant.UserTable.USER_ID);
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
                        int img_count = response.getJSONObject("result").getInt(DatabaseConstant.UserTable.IMG_COUNT);


                        mFriendInfo = new UserInfo();
                        mFriendInfo.setUser_id(Integer.valueOf(user_id));
                        mFriendInfo.setSex(Integer.valueOf(sex));
                        mFriendInfo.setImg_url(img_url);
                        mFriendInfo.setUsername(username);
                        mFriendInfo.setNick(nick);
                        mFriendInfo.setDistrict(district);
                        mFriendInfo.setLevel(Integer.valueOf(level));
                        mFriendInfo.setBall_type(Integer.valueOf(ball_type));
                        mFriendInfo.setAppoint_date(appoint_date);
                        mFriendInfo.setBallArm(Integer.valueOf(ball_arm));
                        mFriendInfo.setUsedType(Integer.valueOf(usedType));
                        mFriendInfo.setBallAge(ball_age);
                        mFriendInfo.setIdol(idol);
                        mFriendInfo.setIdol_name(idol_name);
                        mFriendInfo.setCost(cost);
                        mFriendInfo.setMy_type(Integer.valueOf(my_type));
                        mFriendInfo.setWork_live(work_live);
                        mFriendInfo.setZizhi(zizhi);
                        mFriendInfo.setImg_count(img_count);
                        message.what = DATA_SUCCESS;
                        message.obj = mFriendInfo;

                    }else{
                        message.what = PublicConstant.REQUEST_ERROR;
                        message.obj = response.getString("msg");
                    }
                    mHandler.sendMessage(message);
                    //TODO:如果出现错误？
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

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_SUCCESS:
                    updateUI();
                    break;
                case PublicConstant.REQUEST_ERROR:
                    mButton.setEnabled(false);
                    if(null == msg.obj){
                        Utils.showToast(getActivity(),getActivity().getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(getActivity(), (String) msg.obj);
                    }
                    break;
                default:
                    toast();
                    break;
            }
        }
    };

    private void updateUI() {
        user_id = String.valueOf(mFriendInfo.getUser_id());

        String img_url = mFriendInfo.getImg_url();
        img_path = mFriendInfo.getImg_url();//fake
        account = mFriendInfo.getUsername();
        gender = mFriendInfo.getSex() == 1
                ? getString(R.string.man) : getString(R.string.woman);
        nick_name = TextUtils.isEmpty(mFriendInfo.getNick()) ?
                getString(R.string.unset) : mFriendInfo.getNick();
        district = "".equals(mFriendInfo.getDistrict()) ?
                "未知" : mFriendInfo.getDistrict();
        level = 1 == mFriendInfo.getLevel()
                ? getString(R.string.level_base) : ((2 == mFriendInfo.getLevel()) ?
                getString(R.string.level_middle) : getString(R.string.level_master));
        ball_type = 1 == mFriendInfo.getBall_type()
                ? getString(R.string.ball_type_1) : (2 == mFriendInfo.getBall_type() ?
                getString(R.string.ball_type_2) : getString(R.string.ball_type_3));
        ball_arm = 1 == mFriendInfo.getBallArm()
                ? getString(R.string.cue_1) : getString(R.string.cue_2);
        used_type = 1 == mFriendInfo.getUsedType()
                ? getString(R.string.habit_1) : (2 == mFriendInfo.getUsedType() ?
                getString(R.string.habit_2) : getString(R.string.habit_3));

        mImageView.setDefaultImageResId(R.drawable.default_head);
        mImageView.setErrorImageResId(R.drawable.default_head);
        mImageView.setImageUrl(HttpConstants.IMG_BASE_URL + img_url, mImageLoader);
        mAccountTextView.setText(account);
        mGenderTextView.setText(gender);
        mNickNameTextView.setText(nick_name);
        mDistrictTextView.setText(district);
        mLevelTextView.setText(level);
        mBallTypeTextView.setText(ball_type);
        mBallArmTextView.setText(ball_arm);
        mUsedTypeTextView.setText(used_type);
        mNewPhotoTv.setText(mFriendInfo.getImg_count() + "");
    }

    private void toast() {
        Toast.makeText(getActivity(), "获取好友信息失败！！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onClick(View v) {
        if(v == mNewPhotoRe){
            Intent intent = new Intent(getActivity(), FriendNewPhotoActivity.class);
            intent.putExtra("user_id",mUserId);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }
}
