package com.yueqiu;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.fragment.activities.ActivitiesFragment;


/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = "ActivitiesActivity";

    private TextView mTvBack;
    private EditText mEtSearch;
    private Button mBtnSearch;
    private ViewPager mViewPage;
    private ActivitiesFragment mFragment;
    private FragmentManager mManager;
    private FragmentTransaction mTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_activites);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.activities));
    }

    private void initView()
    {
        mTvBack = (TextView)findViewById(R.id.activities_tv_back);
        mEtSearch = (EditText)findViewById(R.id.activities_et_search);
        mBtnSearch = (Button)findViewById(R.id.activities_btn_search);
        mFragment = new ActivitiesFragment();
        mManager = getSupportFragmentManager();
        mTransaction = mManager.beginTransaction();
        mTransaction.replace(R.id.activities_fl,mFragment);
        mTransaction.commit();
    }

    @Override
    public void onClick(View view) {

    }
}
