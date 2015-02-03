package com.yueqiu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;


public class YueQiuSplashScreen extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yue_qiu_splash_screen);

        // TODO: 我们可以在这里加入更多的初始化事件，比如加入数据库的初始化过程，
        // TODO: 并不是只是机械的等待1000ms，这样有点浪费
        // TODO: 或者我们也可以在这里加入网络的判断事件，判断用户是否已经登录，或者
        // TODO: 是否是第一次进入本程序
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent mainIntent = new Intent(YueQiuSplashScreen.this, BilliardNearbyActivity.class);
                YueQiuSplashScreen.this.startActivity(mainIntent);
                overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
                YueQiuSplashScreen.this.finish();
            }
        }, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_yue_qiu_splash_screen, menu);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
