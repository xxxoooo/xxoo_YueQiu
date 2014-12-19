package com.yueqiu;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class HomeTabActivity extends TabActivity
{
    private static final String TAG = "HomeTabActivity";

    private TabHost mTabHost;
    private ArrayList<TabHost.TabSpec> mTabList = new ArrayList<TabHost.TabSpec>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_tab);

        initTabHost();
    }

    private static int[] sTabImgResIds = {R.drawable.tab_search, R.drawable.tab_chat_bar, R.drawable.tab_activity, R.drawable.tab_billiard_circle};
    private static int[] sTabNameResId = {R.string.tab_title_search, R.drawable.tab_chat_bar, R.drawable.tab_activity, R.drawable.tab_billiard_circle};

    private static final String TAB_TAG = "tab_tag";

    private void initTabHost()
    {
        mTabHost = getTabHost();
        int i;
        TabHost.TabSpec tabSpec;
        for (i = 0; i < 4; i++)
        {
            View tabLayout = getLayoutInflater().inflate(R.layout.tab_widget_layout, null);
            TextView tvName = (TextView) tabLayout.findViewById(R.id.tab_tv_item);
            tvName.setText(sTabNameResId[i]);
            ImageView img = (ImageView) tabLayout.findViewById(R.id.tab_img_item);
            img.setImageResource(sTabImgResIds[i]);
            tabSpec = mTabHost.newTabSpec(TAB_TAG + i);
            tabSpec.setIndicator(tabLayout);
            mTabList.add(tabSpec);
        }

        // add the tab in
        mTabHost.addTab(mTabList.get(0).setContent(new Intent(HomeTabActivity.this, BilliardSearchActivity.class)));
        mTabHost.addTab(mTabList.get(1).setContent(new Intent(HomeTabActivity.this, BilliardSearchActivity.class)));
        mTabHost.addTab(mTabList.get(2).setContent(new Intent(HomeTabActivity.this, BilliardSearchActivity.class)));
        mTabHost.addTab(mTabList.get(3).setContent(new Intent(HomeTabActivity.this, BilliardSearchActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}





























