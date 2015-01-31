package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.UserDao;
import com.yueqiu.fragment.profilesetup.MyProfileRadioSetupFragment;
import com.yueqiu.fragment.profilesetup.MyProfileSetupListener;
import com.yueqiu.fragment.profilesetup.MyProfileTextSetupFragment;
import com.yueqiu.fragment.profilesetup.PhotoSetupFragment;
import com.yueqiu.util.Utils;

/**
 * Created by doushuqi on 15/1/4.
 */
public class ProfileSetupActivity extends SingleFragmentActivity implements MyProfileSetupListener {

    private static final String TAG = "ProfileSetupActivity";
    public static final String KEY_ARGUMENT = "com.yueqiu.profilesetupactivity.key";
    private ActionBar mActionBar;
    private String mSetupContent;
    private UserInfo mUserInfo;
    private UserDao mUserDao = DaoFactory.getUser(this);

    @Override
    public Fragment createFragment() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        int id = getIntent().getIntExtra(MyProfileActivity.EXTRA_FRAGMENT_ID, -1);
        mUserInfo = mUserDao.getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        return getCreateFragment(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.setup_confirm:
                updateProfile();
                finish();
                break;
        }
        return true;
    }

    private void updateProfile() {
        //TODO:更新服务器端的资料和本地数据！！

        Intent intent = new Intent();
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment instanceof PhotoSetupFragment)
            return;//TODO:上传头像 need to develop
//        String str = ((EditText) fragment.getView()
//                .findViewById(R.id.my_profile_setup_text)).getText().toString();
        intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, mSetupContent);
        setResult(Activity.RESULT_OK, intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setFragmentActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.confirm, menu);
        return true;
    }

    private Fragment getCreateFragment(int id) {
        switch (id) {
            case 0:
                mActionBar.setTitle(R.string.picture);
                return new PhotoSetupFragment();
            case 1:
//                mActionBar.setTitle(R.string.account);
                return null;
            case 2:
//                mActionBar.setTitle(R.string.gender);
                return null;
            case 3:
                String nick = getString(R.string.nick_name);
                mActionBar.setTitle(nick);
                return new MyProfileTextSetupFragment(nick);
            case 4:
                String region = getString(R.string.region);
                mActionBar.setTitle(region);
                return new MyProfileTextSetupFragment(region);
            case 5:
                mActionBar.setTitle(R.string.level);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.level_base),
                        getString(R.string.level_middle),
                        getString(R.string.level_master)
                }, mUserInfo.getLevel());
            case 6:
                mActionBar.setTitle(R.string.ball_type);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.ball_type_1),
                        getString(R.string.ball_type_2),
                        getString(R.string.ball_type_3)
                }, mUserInfo.getBall_type());
            case 7:
                mActionBar.setTitle(R.string.billiards_cue);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.cue_1),
                        getString(R.string.cue_2)
                }, mUserInfo.getBallArm());
            case 8:
                mActionBar.setTitle(R.string.cue_habits);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.habit_1),
                        getString(R.string.habit_2),
                        getString(R.string.habit_3)
                }, mUserInfo.getUsedType());
            case 9:
                String playAge = getString(R.string.play_age);
                mActionBar.setTitle(playAge);
                return new MyProfileTextSetupFragment(playAge);
            case 10:
                String idol = getString(R.string.idol);
                mActionBar.setTitle(idol);
                return new MyProfileTextSetupFragment(idol);
            case 11:
                String sign = getString(R.string.sign);
                mActionBar.setTitle(sign);
                return new MyProfileTextSetupFragment(sign);
            case 12:
                return null;
            default:
                return null;
        }
    }

    @Override
    public void setOnSetupListener(String str) {
        mSetupContent = str;
    }
}
