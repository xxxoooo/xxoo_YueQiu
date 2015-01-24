package com.yueqiu.activity;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.yueqiu.fragment.addfriend.FriendProfileFragment;

/**
 * Created by doushuqi on 15/1/8.
 */
public class RequestAddFriendActivity extends SingleFragmentActivity {

    public Fragment mCurrentFragment;
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private ActionBar mActionBar;
    @Override
    public Fragment createFragment() {
        mCurrentFragment = new FriendProfileFragment();
        return mCurrentFragment;
    }
}
