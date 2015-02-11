package com.yueqiu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;


public class YueQiuSplashScreen extends Activity
{

    // TODO: 当我们首次安装约球程序时，或者当前系统资源比较紧张时，约球的首页也就是SplashScreen的加载
    // TODO: 变慢时，就会首先出现一个带有actionBar的Activity出现，然后出现的才是SplashScreen，这种
    // TODO: 现象很快，基本上几百毫秒，对于内存大的手机，基本上就见不到这种效果了，仅仅是在首次安装时
    // TODO: 出现这种现象，第二次启动就没有了。这种现象的原因就是我们的AppTheme设置当中就有了ActionBar
    // TODO: 的相关设置，我们可以参考之前做的一些Demo(SafeSDKDemo)，将SplashScreen的Theme当中的背景元素直接
    // TODO: 设置成<android:WindowBackground>@drawable/splach_screen_bg</>，然后再加一个NoTitle
    // TODO: 的配置，基本上就可以解决问题了。但是却会带出另一个问题，就是首页的activity屏幕上移的问题。
    // TODO: 当这种现象明显时，我们需要研究一下.上面所有的扯淡，只是为了以后的改进提供一个解决的注意点。
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        }, 850);
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
