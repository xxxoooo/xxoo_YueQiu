package com.yueqiu;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.yueqiu.activity.searchmenu.nearby.SearchResultActivity;
import com.yueqiu.fragment.chatbar.AddPersonFragment;
import com.yueqiu.fragment.chatbar.ContactFragment;
import com.yueqiu.fragment.chatbar.MessageFragment;

/**
 * Created by doushuqi on 14/12/17.
 * 聊吧Activity
 */
public class ChatBarActivity extends FragmentActivity {

    private ActionBar mActionBar;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private RadioGroup radioGroup;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chatbar_main);
        initView();
		fragmentManager = getSupportFragmentManager();
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup1);
        ((RadioButton)radioGroup.findViewById(R.id.radio0)).setChecked(true);
        
        transaction = fragmentManager.beginTransaction();
        Fragment fragment = new MessageFragment();
        transaction.replace(R.id.chatbar_fragment_container, fragment);
        transaction.commit();
       
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            	switch (checkedId) {
				case R.id.radio0:
					transaction = fragmentManager.beginTransaction();
	                Fragment messageFragment = new MessageFragment();
	                transaction.replace(R.id.chatbar_fragment_container, messageFragment);
	                transaction.commit();
                    mActionBar.setTitle(R.string.btn_liaoba_message);
					break;
				case R.id.radio1:
					transaction = fragmentManager.beginTransaction();
	                Fragment contactFragment = new ContactFragment();
	                transaction.replace(R.id.chatbar_fragment_container, contactFragment);
	                transaction.commit();
                    mActionBar.setTitle(R.string.btn_liaoba_contact);
					break;
				case R.id.radio2:
					transaction = fragmentManager.beginTransaction();
	                Fragment addPersonFragment = new AddPersonFragment();
	                transaction.replace(R.id.chatbar_fragment_container, addPersonFragment);
	                transaction.commit();
                    mActionBar.setTitle(R.string.btn_liaoba_add_friend);
					break;
            	}
            }
        });
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            mActionBar.setTitle(getString(R.string.btn_liaoba_message));
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
               return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }

//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.chatbar_fragment_container);
//        switch (item.getItemId()) {
//            case R.id.chatbar_menu_search:
//                if (fragment instanceof MessageFragment) {
//                    Intent intent = new Intent(this, ChatBarSearchActivity.class);
//                    startActivity(intent);
//                }
//                break;
//        }
//
//        return super.onMenuItemSelected(featureId, item);
//    }

    private void initView() {

    }

}
