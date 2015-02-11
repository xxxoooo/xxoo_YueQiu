package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;

/**
 * Created by doushuqi on 15/1/8.
 */
public class UpgradeAssistantActivity extends Activity {

    private TextView mAccountTv,mSexTv,mNickNameTv,mDistrictTv,mLevelTv
            ,mBallType,mBallArm,mUsedTypeTv,mBallAge,mIdolTv,mSignTv,mCostTv
            ,mTypeTv,mExperienceTv;
    private NetworkImageView mPhotoView,mNewerPhotoView;
    private ImageLoader mImgLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_assistant);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.update_to_assistant));

        initView();
    }

    private void initView(){
        mAccountTv = (TextView) findViewById(R.id.upgrade_account_name);
        mSexTv = (TextView) findViewById(R.id.upgrade_sex);
        mNickNameTv = (TextView) findViewById(R.id.upgrade_nickname);
        mDistrictTv = (TextView) findViewById(R.id.upgrade_district);
        mLevelTv = (TextView) findViewById(R.id.upgrade_level);
        mBallType = (TextView) findViewById(R.id.upgrade_ball_type);
        mBallArm = (TextView) findViewById(R.id.upgrade_ball_arm);
        mUsedTypeTv = (TextView) findViewById(R.id.upgrade_use_type);
        mBallAge = (TextView) findViewById(R.id.upgrade_ball_age);
        mIdolTv = (TextView) findViewById(R.id.upgrade_idol);
        mSignTv = (TextView) findViewById(R.id.upgrade_sign);
        mCostTv = (TextView) findViewById(R.id.upgrade_cost);
        mTypeTv = (TextView) findViewById(R.id.upgrade_type);
        mExperienceTv = (TextView) findViewById(R.id.upgrade_experience);

        mPhotoView = (NetworkImageView) findViewById(R.id.upgrade_photo_view);
        mNewerPhotoView = (NetworkImageView) findViewById(R.id.upgrade_newer_photo);

        String unset = getString(R.string.unset);
        mPhotoView.setDefaultImageResId(R.drawable.default_head);
        mPhotoView.setImageUrl(YueQiuApp.sUserInfo.getImg_url(),mImgLoader);
        mAccountTv.setText(YueQiuApp.sUserInfo.getUsername());
        mSexTv.setText(YueQiuApp.sUserInfo.getSex() == 1
                ? getString(R.string.man) : getString(R.string.woman));
        mNickNameTv.setText("".equals(YueQiuApp.sUserInfo.getNick())
                ? unset : YueQiuApp.sUserInfo.getNick());
        mDistrictTv.setText("".equals(YueQiuApp.sUserInfo.getDistrict())
                ? unset : YueQiuApp.sUserInfo.getDistrict());
        mLevelTv.setText(1 == YueQiuApp.sUserInfo.getLevel()
                ? getString(R.string.level_base) : ((2 == YueQiuApp.sUserInfo.getLevel()) ?
                getString(R.string.level_middle) : getString(R.string.level_master)));
        mBallType.setText(1 == YueQiuApp.sUserInfo.getBall_type()
                ? getString(R.string.ball_type_1) : (2 == YueQiuApp.sUserInfo.getBall_type() ?
                getString(R.string.ball_type_2) : getString(R.string.ball_type_3)));
        mBallArm.setText(1 == YueQiuApp.sUserInfo.getBallArm()
                ? getString(R.string.cue_1) : getString(R.string.cue_2));
        mUsedTypeTv.setText(1 == YueQiuApp.sUserInfo.getUsedType()
                ? getString(R.string.habit_1) : (2 == YueQiuApp.sUserInfo.getUsedType() ?
                getString(R.string.habit_2) : getString(R.string.habit_3)));
        mBallAge.setText(YueQiuApp.sUserInfo.getBallAge());
        mIdolTv.setText("".equals(YueQiuApp.sUserInfo.getIdol())
                ? unset : YueQiuApp.sUserInfo.getIdol());
        //TODO:最新照片、费用、经历、类型没数据，类型是啥？定义不明确

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(this);
        getMenuInflater().inflate(R.menu.commit, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            case R.menu.commit:
                //提交升级助教的资料

                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
