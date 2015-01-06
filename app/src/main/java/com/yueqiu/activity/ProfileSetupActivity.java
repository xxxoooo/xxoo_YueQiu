package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.activity.MyProfileActivity;
import com.yueqiu.fragment.myprofilesetup.BallTypeSetupFragment;
import com.yueqiu.fragment.myprofilesetup.BilliardsCueSetupFragment;
import com.yueqiu.fragment.myprofilesetup.CuehabitsSetupFragment;
import com.yueqiu.fragment.myprofilesetup.IdolSetupFragment;
import com.yueqiu.fragment.myprofilesetup.LevelSetupFragment;
import com.yueqiu.fragment.myprofilesetup.NickNameSetupFragment;
import com.yueqiu.fragment.myprofilesetup.PhotoSetupFragment;
import com.yueqiu.fragment.myprofilesetup.PlayAgeSetupFragment;
import com.yueqiu.fragment.myprofilesetup.RegionSetupFragment;
import com.yueqiu.fragment.myprofilesetup.SignSetupFragment;
import com.yueqiu.fragment.myprofilesetup.TheNewestPostSetupFragment;
import com.yueqiu.util.SingleFragmentActivity;
import com.yueqiu.util.Utils;

/**
 * Created by doushuqi on 15/1/4.
 */
public class ProfileSetupActivity extends SingleFragmentActivity {

    private static final String TAG = "ProfileSetupActivity";
    public static final String KEY_ARGUMENT = "com.yueqiu.profilesetupactivity.key";
    private ActionBar mActionBar;

    @Override
    public Fragment createFragment() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        int id = getIntent().getIntExtra(MyProfileActivity.EXTRA_FRAGMENT_ID, -1);
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
                .findFragmentById(R.id.my_profile_setup_fragment_container);
        if (fragment instanceof PhotoSetupFragment)
            return;//TODO:上传头像 need to develop
        String str = ((EditText) fragment.getView()
                .findViewById(R.id.my_profile_setup_text)).getText().toString();
        intent.putExtra(MyProfileActivity.EXTRA_RESULT_ID, str);
        setResult(Activity.RESULT_OK, intent);
        Toast.makeText(this, "更新资料！" + str, Toast.LENGTH_SHORT).show();
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
                mActionBar.setTitle(R.string.nick_name);
                return new NickNameSetupFragment();
            case 4:
                mActionBar.setTitle(R.string.region);
                return new RegionSetupFragment();
            case 5:
                mActionBar.setTitle(R.string.level);
                return new LevelSetupFragment();
            case 6:
                mActionBar.setTitle(R.string.ball_type);
                return new BallTypeSetupFragment();
            case 7:
                mActionBar.setTitle(R.string.billiards_cue);
                return new BilliardsCueSetupFragment();
            case 8:
                mActionBar.setTitle(R.string.cue_habits);
                return new CuehabitsSetupFragment();
            case 9:
                mActionBar.setTitle(R.string.play_age);
                return new PlayAgeSetupFragment();
            case 10:
                mActionBar.setTitle(R.string.idol);
                return new IdolSetupFragment();
            case 11:
                mActionBar.setTitle(R.string.sign);
                return new SignSetupFragment();
            case 12:
                mActionBar.setTitle(R.string.the_new_post);
                return new TheNewestPostSetupFragment();
            default:
                return null;
        }
    }
}
