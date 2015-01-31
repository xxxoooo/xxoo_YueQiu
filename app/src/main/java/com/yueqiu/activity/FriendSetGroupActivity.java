package com.yueqiu.activity;

import android.support.v4.app.Fragment;

import com.yueqiu.fragment.addfriend.FriendSetGroupFragment;

/**
 * Created by doushuqi on 15/1/8.
 */
public class FriendSetGroupActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new FriendSetGroupFragment();
    }
}
