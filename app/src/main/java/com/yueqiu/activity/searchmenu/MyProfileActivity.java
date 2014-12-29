package com.yueqiu.activity.searchmenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.yueqiu.R;
import com.yueqiu.view.CornerListView;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_myprofile);


        findViewById(R.id.myprofile_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



}
