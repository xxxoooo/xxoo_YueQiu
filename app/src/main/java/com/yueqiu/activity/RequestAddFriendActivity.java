package com.yueqiu.activity;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.yueqiu.R;
import com.yueqiu.fragment.addfriend.FriendManageFragment;
import com.yueqiu.fragment.addfriend.FriendProfileFragment;
import com.yueqiu.fragment.addfriend.VerificationFragment;

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);
                if (fragment instanceof FriendManageFragment) {
                    mFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                            .remove(fragment).commit();
                    mFragmentManager.popBackStack();
                } else if (fragment instanceof VerificationFragment) {
                    mFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                            .remove(fragment).commit();
                    mFragmentManager.popBackStack();
                } else if (fragment instanceof FriendProfileFragment) {
                    this.finish();
                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }
}
