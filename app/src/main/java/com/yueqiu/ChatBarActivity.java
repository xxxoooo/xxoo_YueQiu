package com.yueqiu;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.chatbar.ContactFragment;
import com.yueqiu.fragment.chatbar.MessageFragment;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧Activity
 */
public class ChatBarActivity extends FragmentActivity {

    public final static int num = 3;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private RadioGroup radioGroup;
    private TextView mBack,mTitle;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        initView();
		fragmentManager = getSupportFragmentManager();
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup1);
        ((RadioButton)radioGroup.findViewById(R.id.radio0)).setChecked(true);
        
        transaction = fragmentManager.beginTransaction();
        Fragment fragment = new MessageFragment();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
       
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            	switch (checkedId) {
				case R.id.radio0:
					transaction = fragmentManager.beginTransaction();
	                Fragment messageFragment = new MessageFragment();
	                transaction.replace(R.id.content, messageFragment);
	                transaction.commit();
                    mTitle.setText(R.string.btn_liaoba_message);
					break;
				case R.id.radio1:
					transaction = fragmentManager.beginTransaction();
	                Fragment contactFragment = new ContactFragment();
	                transaction.replace(R.id.content, contactFragment);
	                transaction.commit();
                    mTitle.setText(R.string.btn_liaoba_contact);
					break;
				case R.id.radio2:
					transaction = fragmentManager.beginTransaction();
	                Fragment addPersonFragment = new AddPersonFragment();
	                transaction.replace(R.id.content, addPersonFragment);
	                transaction.commit();
                    mTitle.setText(R.string.btn_liaoba_add_friend);
					break;
            	}
                
            }
        });
	}

    private void initView() {
        mBack = (TextView) findViewById(R.id.btn_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回按钮

            }
        });
        mTitle = (TextView) findViewById(R.id.tv_title);
    }

}
