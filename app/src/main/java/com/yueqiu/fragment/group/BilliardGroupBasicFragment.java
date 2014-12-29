package com.yueqiu.fragment.group;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.BilliardGroupActivity;
import com.yueqiu.R;

/**
 * Created by wangyun on 14/12/17.
 * 台球圈基础的Fragment
 */
public class BilliardGroupBasicFragment extends Fragment {
    private View mView;
    private RadioGroup mGroup;
    private Bundle mBundle;
    private String mValue;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_billard_group_basic,null);
        mBundle = getArguments();
        mGroup = (RadioGroup) mView.findViewById(R.id.billiard_radio_group);
        ((RadioButton)mGroup.findViewById(R.id.billiard_time_sort)).setChecked(true);
        mValue = mBundle.getString(BilliardGroupActivity.BILLIARD_TAB_NAME) + getString(R.string.billiard_time);
        ((TextView)mView.findViewById(R.id.biiliard_child_text)).setText(mValue);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.billiard_time_sort:
                        mValue = mBundle.getString(BilliardGroupActivity.BILLIARD_TAB_NAME);
                        mValue += getString(R.string.billiard_time);
                        break;
                    case R.id.billiard_popularity_sort:
                        mValue = mBundle.getString(BilliardGroupActivity.BILLIARD_TAB_NAME);
                        mValue +=  getString(R.string.billiard_poplarity);
                        break;
                }
                ((TextView)mView.findViewById(R.id.biiliard_child_text)).setText(mValue);
            }
        });

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
