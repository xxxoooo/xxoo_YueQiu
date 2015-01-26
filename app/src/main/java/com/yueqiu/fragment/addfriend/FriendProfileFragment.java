package com.yueqiu.fragment.addfriend;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by doushuqi on 15/1/6.
 */
public class FriendProfileFragment extends Fragment {

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    private Button mButton;
    private ImageView mImageView;
    private TextView mAccountTextView, mGenderTextView,
            mNickNameTextView, mDistrictTextView, mLevelTextView,
            mBallTypeTextView, mUsedTypeTextView, mBallArmTextView;
    private int mUserId;
    private static final int DATA_SUCCESS = 1;
    private UserInfo mFriendInfo;
    public static final String IMG_URL_REAL_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.img_url";
    public static final String ACCOUNT_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.account_key";
    public static final String GENDER_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.gender_key";
    public static final String DISTRICT_KEY = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.district_key";
    public static final String FRIEND_USER_ID = "com.yueqiu.fragment.requestaddfriend.FriendProfileFragment.friend_user_id_key";

    private String img_path, account, gender, nick_name, district, level, ball_type, ball_arm, used_type, user_id;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActivity().getActionBar();
            String username = getActivity().getIntent().getStringExtra(AddPersonFragment.FRIEND_INFO_USERNAME);
            mActionBar.setTitle(username);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setHasOptionsMenu(true);
        mFragmentManager = getActivity().getSupportFragmentManager();
        mUserId = getActivity().getIntent().getIntExtra(AddPersonFragment.FRIEND_INFO_USER_ID, 0);
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
        mImageView = (ImageView) view.findViewById(R.id.friend_profile_photo);
        mAccountTextView = (TextView) view.findViewById(R.id.friend_profile_account);
        mGenderTextView = (TextView) view.findViewById(R.id.friend_profile_gender);
        mNickNameTextView = (TextView) view.findViewById(R.id.friend_profile_nick_name);
        mDistrictTextView = (TextView) view.findViewById(R.id.friend_profile_district);
        mLevelTextView = (TextView) view.findViewById(R.id.friend_profile_level);
        mBallTypeTextView = (TextView) view.findViewById(R.id.friend_profile_ball_type);
        mBallArmTextView = (TextView) view.findViewById(R.id.friend_profile_ball_arm);
        mUsedTypeTextView = (TextView) view.findViewById(R.id.friend_profile_used_type);
    }

    private void initData() {
        if (mUserId == 0) {
            toast();
            return;
        }

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
                    if (object.getInt("code") == HttpConstants.ResponseCode.NORMAL) {
                        mFriendInfo = Utils.mapingObject(UserInfo.class, object.getJSONObject("result"));
                        message.what = DATA_SUCCESS;
                        message.obj = mFriendInfo;
                    }
                    mHandler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_SUCCESS:
                    updateUI();
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
        nick_name = mFriendInfo.getUsername();
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

//        mImageView.setImageDrawable();
        mAccountTextView.setText(account);
        mGenderTextView.setText(gender);
        mNickNameTextView.setText(nick_name);
        mDistrictTextView.setText(district);
        mLevelTextView.setText(level);
        mBallTypeTextView.setText(ball_type);
        mBallArmTextView.setText(ball_arm);
        mUsedTypeTextView.setText(used_type);
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


}
