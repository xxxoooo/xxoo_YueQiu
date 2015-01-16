package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.yueqiu.R;
import com.yueqiu.util.Utils;

/**
 * Created by yinfeng on 15/1/6.
 */
public class ActivitiesDetail extends Activity {
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_detail);
        initActionBar();
    }

    private void initActionBar()
    {
        mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle(getString(R.string.activities_detail));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.setActivityMenuColor(ActivitiesDetail.this);
        getMenuInflater().inflate(R.menu.menu_activities_detail,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
            case R.id.menu_activities_collect:
                break;
            case R.id.menu_activities_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
