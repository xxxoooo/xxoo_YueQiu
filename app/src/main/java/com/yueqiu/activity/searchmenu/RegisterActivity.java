package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.app.ActionBar;

import com.yueqiu.R;

/**
 * Created by yinfeng on 14/12/18.
 */
public class RegisterActivity extends Activity  implements View.OnClickListener{
    private static final String TAG = "RegisterActivity";
    private EditText mEtUserName,mEtPwd,mEtNumber;
    private RadioGroup mRGroup;
    private Button mBtnRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initActionBar();
        initView();
    }

    private void Log(String msg)
    {
        Log.i(TAG,msg);
    }
    private void initActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.register));
    }
    private void initView()
    {
        mEtNumber = (EditText) findViewById(R.id.register_et_username);
        mEtPwd = (EditText) findViewById(R.id.register_et_pwd);
        mEtNumber = (EditText) findViewById(R.id.register_et_phonenumber);
        mRGroup = (RadioGroup) findViewById(R.id.register_rg_sex);
        mBtnRegister = (Button) findViewById(R.id.register_btn_register);
        mBtnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.register_btn_register:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
