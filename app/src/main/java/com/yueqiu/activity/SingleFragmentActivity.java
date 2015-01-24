package com.yueqiu.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.yueqiu.R;

/**
 * Created by doushuqi on 15/1/4.
 */
public abstract class SingleFragmentActivity extends FragmentActivity{

    public abstract Fragment createFragment();

    private int geLayoutResId() {
        return R.layout.activity_chatbar_fragment_container;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(geLayoutResId());
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }
}
