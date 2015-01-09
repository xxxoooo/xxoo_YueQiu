package com.yueqiu.activity;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.yueqiu.R;
import com.yueqiu.fragment.requestaddfriend.FriendsApplicationFragment;
import com.yueqiu.util.SingleFragmentActivity;

/**
 * Created by doushuqi on 15/1/7.
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
}
