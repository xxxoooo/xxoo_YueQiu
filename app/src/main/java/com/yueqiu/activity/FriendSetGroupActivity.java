package com.yueqiu.activity;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.yueqiu.R;
import com.yueqiu.fragment.addfriend.FriendSetGroupFragment;

/**
 * Created by doushuqi on 15/1/8.
 */
public class FriendSetGroupActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new FriendSetGroupFragment();
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
