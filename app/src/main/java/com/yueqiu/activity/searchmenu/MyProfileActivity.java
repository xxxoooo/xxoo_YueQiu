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

    private CornerListView mCornerListView, mCornerListView2;
    private static final String ITEM_CATEGORY1[] = {"头像：", "账户：", "性别："};
    private static final String ITEM_CATEGORY2[] = {"昵称：", "区域：", "水平：", "球种：", "玩法：", "消费方式：", "约球时间："};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.custom_actionbar_layout, null);
            View saveMenuItem = customActionBarView.findViewById(R.id.save_menu_item);
            saveMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyProfileActivity.this.finish();
                }
            });
            TextView title = (TextView) customActionBarView.findViewById(R.id.action_bar_title);
            title.setText(getString(R.string.search_my_profile_str));
            actionBar.setDisplayShowCustomEnabled(true);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(customActionBarView,params);
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
        return super.onOptionsItemSelected(item);
    }
}
