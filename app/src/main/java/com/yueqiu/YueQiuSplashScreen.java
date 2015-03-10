package com.yueqiu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.yueqiu.adapter.SplashScreenPagerAdapter;
import com.yueqiu.adapter.SplashScreenViewPager;
import com.yueqiu.fragment.nearby.common.NearbyParamsPreference;


public class YueQiuSplashScreen extends FragmentActivity implements SplashScreenViewPager.OnSwipeOutLisener
{
    private static final String TAG = "YueQiuSplashScreen";

    public static final String KEY_CUREENT_SCREEN_BITMAP = "key_current_screen_bitmap";
    private SplashScreenViewPager mViewPager;
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

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        NearbyParamsPreference preference = NearbyParamsPreference.getInstance();

        // TODO: 目前不需要判断是否是第一次启动，也就是每次都显示那三个页面
        // TODO: 如果需要添加判断的话，我们直接将下面的可以正常工作代码采用就可以了
//        if (! preference.getFirstEnter(this))
//        {
//            // 我们之前已经看过这些简介界面了，这样我们就能直接进入到约球里面了
//            Intent mainIntent = new Intent(YueQiuSplashScreen.this, BilliardNearbyActivity.class);
//            YueQiuSplashScreen.this.startActivity(mainIntent);
////            overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
//            YueQiuSplashScreen.this.finish();
//        } else
//        {
//            // 这是我们首次进入约球，我们需要加载SplashScreen当中
//            setContentView(R.layout.activity_yue_qiu_splash_screen);
//            Log.d(TAG, " set the swipe listener ");
//            mViewPager = (SplashScreenViewPager) findViewById(R.id.splash_screen_view_pager);
//            // 设置回调
//            mViewPager.setOnSwipeOutLisener(YueQiuSplashScreen.this);
//
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            SplashScreenPagerAdapter fragmentPagerAdapter = new SplashScreenPagerAdapter(this, fragmentManager);
//            mViewPager.setAdapter(fragmentPagerAdapter);
//        }

        // 这是我们首次进入约球，我们需要加载SplashScreen当中
        setContentView(R.layout.activity_yue_qiu_splash_screen);
        Log.d(TAG, " set the swipe listener ");
        mViewPager = (SplashScreenViewPager) findViewById(R.id.splash_screen_view_pager);
        // 设置回调
        mViewPager.setOnSwipeOutLisener(YueQiuSplashScreen.this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        SplashScreenPagerAdapter fragmentPagerAdapter = new SplashScreenPagerAdapter(this, fragmentManager);
        mViewPager.setAdapter(fragmentPagerAdapter);

        // TODO: 我们可以在这里加入更多的初始化事件，比如加入数据库的初始化过程，
        // TODO: 并不是只是机械的等待1000ms，这样有点浪费
        // TODO: 或者我们也可以在这里加入网络的判断事件，判断用户是否已经登录，或者
        // TODO: 是否是第一次进入本程序
        // 以下这种方案不予采用了，改成了ViewPager的方式来实现
//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                Intent mainIntent = new Intent(YueQiuSplashScreen.this, BilliardNearbyActivity.class);
//                YueQiuSplashScreen.this.startActivity(mainIntent);
//                overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
//                YueQiuSplashScreen.this.finish();
//            }
//        }, 850);
    }

    /**
     * YueQiuSplashScreen需要实现这个方法用于回调
     */
    @Override
    public void onSwipeRightMost()
    {
        Log.d(TAG, " inside the splash screen, we have received the callback ");

        // 我们之前已经看过这些简介界面了，这样我们就能直接进入到约球里面了
        Intent mainIntent = new Intent(YueQiuSplashScreen.this, BilliardNearbyActivity.class);
//        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        YueQiuSplashScreen.this.startActivity(mainIntent);
        overridePendingTransition(R.anim.push_bottom_in, R.anim.push_bottom_out);
//        YueQiuSplashScreen.this.finish();

    }
}
