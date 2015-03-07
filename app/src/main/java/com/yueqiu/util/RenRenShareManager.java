package com.yueqiu.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.renn.sharecomponent.MessageTarget;
import com.renn.sharecomponent.RennShareComponent;
import com.renn.sharecomponent.ShareMessageError;
import com.renn.sharecomponent.message.RennImgTextMessage;
import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;


/**
 * Created by scguo on 15/3/2.
 */
public class RenRenShareManager
{
    private static final String TAG = "RenRenShareManager";

    private RennShareComponent mRennShareComponent;
    private MessageTarget mType;

    private Context mContext;
    private RenRenShareManager(Context context)
    {
        this.mContext = context;
        // 初始化人人SDK中关于分享的功能的类和方法
        mRennShareComponent = RennShareComponent.getInstance(context);
        mRennShareComponent.init(HttpConstants.RENREN_APP_ID,
                HttpConstants.RENREN_APP_KEY,
                HttpConstants.RENREN_SECRET_KEY);

        // 我们将这里的默认消息设置发送到人人的时间线上,即所有好友可见
        // 我们在进行分享时并没有提供只是分享到好友还是分享到好友状态当中，所以我们默认的分享到时间线上，即所有人可见
        mType = MessageTarget.TO_RENREN;
    }

    private static RenRenShareManager sInstance;

    public static RenRenShareManager getInstance(Context context)
    {
        return null == sInstance ? new RenRenShareManager(context) : sInstance;
    }

    private static final String RENREN_PKG_NAME = "com.renren.mobile.android";

    public boolean isRenrenClientInstalled()
    {
        PackageManager pm = mContext.getPackageManager();
        try
        {
            pm.getPackageInfo(RENREN_PKG_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 我们分享到人人的信息采用文字和图片都进行分享
     *
     * @param text 分享到人人的文字信息
     * @param bitmap 分享到人人的图片
     */
    public void shareToRenren(String text, Bitmap bitmap)
    {
        Log.d(TAG, " share to renren ");
        RennImgTextMessage msg = new RennImgTextMessage();
        msg.setThumbData(bitmap);
        msg.setTitle("约求");
        // 我们把这里的Url设置成约求项目的官方网站
        msg.setUrl("http://www.pinruiwenhua.com");
        msg.setDescription(text);
        mRennShareComponent.setSendMessageListener(new RennShareComponent.SendMessageListener()
        {
            @Override
            public void onSendMessageSuccess(String s, Bundle bundle)
            {
                Log.d(TAG, " share success here ");
                Toast.makeText(mContext, mContext.getString(R.string.renren_share_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageFailed(String s, ShareMessageError shareMessageError)
            {
                if (isRenrenClientInstalled())
                {
                    // 如果在人人安装的情况下仍然分享失败，我们就弹出分享失败的Toast，关于提醒用户先安装人人客户端的操作
                    // 我们在其他的Utils.showSheet()当中已经完成了
                    Toast.makeText(mContext, mContext.getString(R.string.renren_share_failed) + s, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSendMessageCanceled(String s)
            {
                Toast.makeText(mContext, mContext.getString(R.string.renren_share_cancelld), Toast.LENGTH_SHORT).show();
            }
        });

        mRennShareComponent.sendMessage(msg, mType);

        // 我们在这里进行我们创建的Bitmap回收

    }
}
