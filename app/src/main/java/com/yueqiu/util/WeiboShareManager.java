package com.yueqiu.util;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.share.BaseRequest;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;

/**
 * Created by scguo on 15/3/2.
 */
public class WeiboShareManager implements IWeiboHandler.Request
{
    private static final String TAG = "WeiboShareManager";
    public static final String KEY_SHARE_TYPE = "key_share_type";
    public static final int SHARE_CLIENT = 1;
    public static final int SHARE_ALL_IN_ONE = 2;

    private TextObject getTextObj()
    {
        TextObject textObject = new TextObject();
        textObject.text = getSharedText();
        return textObject;
    }

    private IWeiboShareAPI mWeiboShareApi = null;
    private BaseRequest mBaseRequest = null;
    private static WeiboShareManager sInstance;
    private final Context mContext;
    private WeiboShareManager(Context context, Intent intent)
    {
        this.mContext = context;
        mWeiboShareApi = WeiboShareSDK.createWeiboAPI(context, HttpConstants.WEIBO_APP_KEY);

        mWeiboShareApi.handleWeiboRequest(intent, this);
    }

    public static WeiboShareManager getInstance(Context context, Intent intent)
    {
        return sInstance == null ? new WeiboShareManager(context, intent) : sInstance;
    }

    private String getSharedText()
    {

        return "";
    }


    /**
     * 第三方应用响应微博客户端的请求，提供需要分享的数据
     * 新浪微博实现消息分享的原理就是本地应用首先向新浪微博发送请求，然后新浪微博接受到请求
     * 之后，进行验证，验证通过之后，然后我们就可以向新浪微博发送我们想要分享的数据了。
     * responseMessage()的作用就是在我们本地应用在通过了新浪微博的验证之后发送数据的具体
     * 过程了
     *
     */
    private void responseMessage()
    {
        if (mWeiboShareApi.isWeiboAppSupportAPI())
        {
            final int supportAPI = mWeiboShareApi.getWeiboAppSupportAPI();
            if (supportAPI >= 10351)
            {
                // 这样我们就可以分享多个Object了，即不止包含文字，还可以包含图片等
            }
        }

    }

    @Override
    public void onRequest(BaseRequest baseRequest)
    {
        mBaseRequest = baseRequest;

        final int resId = (null == mBaseRequest) ? R.string.weibo_share_fail : R.string.weibo_share_success;
        Toast.makeText(mContext, mContext.getResources().getString(resId), Toast.LENGTH_SHORT).show();
    }
}
























