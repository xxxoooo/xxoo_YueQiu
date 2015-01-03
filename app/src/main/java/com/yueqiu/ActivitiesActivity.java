package com.yueqiu;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.yueqiu.activity.searchmenu.nearby.SearchResultActivity;
import com.yueqiu.adapter.ActivitiesListViewAdapter;
import com.yueqiu.bean.Activities;

import java.util.ArrayList;


/**
 * Created by yinfeng on 14/12/18.
 */
public class ActivitiesActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ActivitiesActivity";
    private ActionBar mActionBar;
    private ListView mListView;
    private ActivitiesListViewAdapter mAdapter;
    private ArrayList<Activities> mListData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activites);
        initActionBar();
        initView();


    }


    private void initView()
    {
        mListView = (ListView)findViewById(R.id.activity_activities_lv);
        initData();
        mAdapter = new ActivitiesListViewAdapter(mListData ,ActivitiesActivity.this);
        mListView.setAdapter(mAdapter);

    }

    private void initData()
    {
        mListData = new ArrayList<Activities>();
        for (int i = 0; i  < 20; i++)
        {
            mListData.add(new Activities());
        }
    }


    private void initActionBar(){
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.tab_title_activity));
    }

    @Override
    public void onClick(View view) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.billiard_search, menu);

        SearchManager searchManager =(SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =(SearchView) menu.findItem(R.id.near_nemu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        return true;
    }
}
