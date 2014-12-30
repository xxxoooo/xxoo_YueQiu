package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.yueqiu.R;
import com.yueqiu.view.CornerListView;
import android.app.ActionBar;
import android.widget.TextView;

/**
 * Created by doushuqi on 14/12/19.
 * 我的资料主Activity
 */
public class MyProfileActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.search_my_profile_str));

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
