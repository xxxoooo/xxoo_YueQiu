package com.yueqiu.activity.searchmenu.nearby;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * @author scguo
 *
 * 用于实现约球详细的Activity,即点击约球Fragment当中的ListView当中的任何一个item的时候跳转到的
 * 一个页面，就是约球详情Activity
 *
 */
public class SearchBilliardsDatingActivity extends Activity
{
    private GridView mGridAlreadyFlow;
    private ImageView mUserPhoto;
    private TextView mUserName, mUserGender, mTvTime1, mTvTime2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiards_dating);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiards_dating, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}