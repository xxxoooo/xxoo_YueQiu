package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.Attr;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;
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

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private Attr mAttr;
    @Override
    public Fragment createFragment() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        int id = getIntent().getIntExtra(MyProfileActivity.EXTRA_FRAGMENT_ID, -1);
        mAttr = getAttrById(id);
        Log.d("wy","setup attr is ->" + mAttr.name());
        mUserInfo = mUserDao.getUserByUserId(String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        return getCreateFragment(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
//            case R.id.setup_confirm:
//                updateProfile();
////                finish();
//                break;
        }
        return super.onOptionsItemSelected(item);
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
        Bundle args = new Bundle();
        switch (mAttr) {
            case PHOTO:
                mActionBar.setTitle(R.string.picture);
                PhotoSetupFragment photoFragment = new PhotoSetupFragment();
                args.putString(PublicConstant.IMG_URL,YueQiuApp.sUserInfo.getImg_url());
                photoFragment.setArguments(args);
                return photoFragment;
            case NICKNAME:
                String nick = getString(R.string.nick_name);
                mActionBar.setTitle(nick);
                args.putString(DatabaseConstant.UserTable.NICK,YueQiuApp.sUserInfo.getNick());
                MyProfileTextSetupFragment nickFragment = new MyProfileTextSetupFragment(nick);
                nickFragment.setArguments(args);
                return nickFragment;
            case DISTRICT:
                String region = getString(R.string.region);
                mActionBar.setTitle(region);
                args.putString(DatabaseConstant.UserTable.DISTRICT,YueQiuApp.sUserInfo.getDistrict());
                MyProfileTextSetupFragment regionFragment = new MyProfileTextSetupFragment(region);
                regionFragment.setArguments(args);
                return regionFragment;
            case LEVEL:
                mActionBar.setTitle(getString(R.string.level));
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.level_base),
                        getString(R.string.level_middle),
                        getString(R.string.level_master)
                }, mUserInfo.getLevel(),getString(R.string.level));
            case BALL_CLASS:
                mActionBar.setTitle(getString(R.string.ball_type));
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.ball_type_1),
                        getString(R.string.ball_type_2),
                        getString(R.string.ball_type_3)
                }, mUserInfo.getBall_type(),getString(R.string.ball_type));
            case USE_TYPE:
                mActionBar.setTitle(getString(R.string.billiards_cue));
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.cue_1),
                        getString(R.string.cue_2)
                }, mUserInfo.getBallArm(),getString(R.string.billiards_cue));
            case BALL_ARM:
                mActionBar.setTitle(R.string.cue_habits);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.habit_1),
                        getString(R.string.habit_2),
                        getString(R.string.habit_3)
                }, mUserInfo.getUsedType(),getString(R.string.cue_habits));
            case BALL_AGE:
                String playAge = getString(R.string.play_age);
                mActionBar.setTitle(playAge);
                args.putString(DatabaseConstant.UserTable.BALLAGE,YueQiuApp.sUserInfo.getBallAge());
                MyProfileTextSetupFragment playAgeFragment = new MyProfileTextSetupFragment(playAge);
                playAgeFragment.setArguments(args);
                return playAgeFragment;
            case IDOL:
                String idol = getString(R.string.idol);
                mActionBar.setTitle(idol);
                args.putString(DatabaseConstant.UserTable.IDOL,YueQiuApp.sUserInfo.getIdol());
                MyProfileTextSetupFragment idolFragment = new MyProfileTextSetupFragment(idol);
                idolFragment.setArguments(args);
                return idolFragment;
            case IDOL_NAME:
                String sign = getString(R.string.sign);
                mActionBar.setTitle(sign);
                args.putString(DatabaseConstant.UserTable.IDOL_NAME,YueQiuApp.sUserInfo.getIdol_name());
                MyProfileTextSetupFragment signFragment = new MyProfileTextSetupFragment(sign);
                signFragment.setArguments(args);
                return signFragment;
            case COST:
                String cost = getString(R.string.cost);
                mActionBar.setTitle(cost);
                args.putString(DatabaseConstant.UserTable.COST,YueQiuApp.sUserInfo.getCost());
                MyProfileTextSetupFragment costFragment = new MyProfileTextSetupFragment(cost);
                costFragment.setArguments(args);
                return costFragment;
            case MY_TYPE:
                mActionBar.setTitle(R.string.type);
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.lovely_type),
                        getString(R.string.mature_type),
                        getString(R.string.godness_type),
                        getString(R.string.charming_type),
                        getString(R.string.strength_type),
                        getString(R.string.handsome_type)
                }, mUserInfo.getMy_type(),getString(R.string.type));
            case WORK_LIVE:
                String work_live = getString(R.string.profession_experiences);
                mActionBar.setTitle(work_live);
                args.putString(DatabaseConstant.UserTable.WORK_LIVE,YueQiuApp.sUserInfo.getWork_live());
                MyProfileTextSetupFragment workFragment = new MyProfileTextSetupFragment(work_live);
                workFragment.setArguments(args);
                return workFragment;

            case ZIZHI:
                mActionBar.setTitle(getString(R.string.zizhi));
                return new MyProfileRadioSetupFragment(new String[]{
                        getString(R.string.zizhi_profession_memeber),
                        getString(R.string.zizhi_profession_memeber),
                        getString(R.string.zizhi_coach),
                        getString(R.string.billiard_other)
                },mUserInfo.getZizhi(),getString(R.string.zizhi));
            default:
                return null;
        }
    }

    @Override
    public void setOnSetupListener(String str) {
        mSetupContent = str;
    }

    private Attr getAttrById(int id){
        Attr attr = null;
        if(id == 0){
            attr = Attr.PHOTO;
        }else if(id == 1){
            attr = Attr. NICKNAME;
        }else if(id == 2){
            attr = Attr.DISTRICT;
        }else if(id == 3){
            attr = Attr.LEVEL;
        }else if(id == 4){
            attr = Attr.BALL_CLASS;
        }else if(id == 5){
            attr = Attr.BALL_ARM;
        }else if(id == 6){
            attr = Attr.USE_TYPE;
        }else if(id == 7){
            attr = Attr.BALL_AGE;
        }else if(id == 8){
            attr = Attr.IDOL;
        }else if(id == 9){
            attr = Attr.IDOL_NAME;
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
}
