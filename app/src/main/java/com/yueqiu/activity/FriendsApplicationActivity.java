package com.yueqiu.activity;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.yueqiu.R;
import com.yueqiu.fragment.addfriend.FriendManageFragment;
import com.yueqiu.fragment.addfriend.FriendsApplicationFragment;

/**
 * Created by doushuqi on 15/1/7.
 * 好友申请处理
 */
public class FriendsApplicationActivity extends SingleFragmentActivity {

    public final static Fragment sFriendsApplication = new FriendsApplicationFragment();
    public Fragment mCurrentFragment;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private ActionBar mActionBar;

    @Override
    public Fragment createFragment() {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mCurrentFragment = sFriendsApplication;
        return mCurrentFragment;
    }

    public void switchFragment(Fragment fragment) {
        if (mCurrentFragment == fragment)
            return;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (fragment.isAdded()){
            transaction.hide(mCurrentFragment).show(fragment).commit();
        }

        else{
            transaction.hide(mCurrentFragment).add(R.id.fragment_container, fragment).commit();
        }
        setActionBarTitle(fragment);
        mCurrentFragment = fragment;
    }

    private void setActionBarTitle(Fragment fragment) {
        if (fragment instanceof FriendsApplicationFragment)
            mActionBar.setTitle(R.string.qiuyou_application);
        else mActionBar.setTitle(R.string.qiuyou_manage);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                doBack();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void doBack(){
        Fragment currentFragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof FriendsApplicationFragment) {
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        } else if (currentFragment instanceof FriendManageFragment) {
            //fixme:fragment 之间的切换（需修改）
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }
    }
}
