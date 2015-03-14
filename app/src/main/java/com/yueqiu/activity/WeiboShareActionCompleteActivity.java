package com.yueqiu.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;

public class WeiboShareActionCompleteActivity extends Activity implements IWeiboHandler.Response
{
    private static final String TAG = "WeiboShareActionCompleteActivity";

    private IWeiboShareAPI mWeiboShareApi;
    private Bitmap mSharedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("wy","aaa");
//        mSharedBitmap = getIntent().getParcelableExtra(PublicConstant.SHARE_TO_SINA_BITMAP);
        mSharedBitmap = YueQiuApp.sScreenBitmap;
        // 初始化WeiboShareSDK
        this.mWeiboShareApi = WeiboShareSDK.createWeiboAPI(this, HttpConstants.WEIBO_APP_KEY);
        // 将当前的应用注册到微博当中
        this.mWeiboShareApi.registerApp();

        if (savedInstanceState != null)
        {
            mWeiboShareApi.handleWeiboResponse(getIntent(), this);
        }
    }

    private static final int SHARE_CLIENT = 1;
    // 我们目前先将我们的shareType写死，之后根据需求再进行改变
    private static final int WEIBO_SHARE_TYPE = SHARE_CLIENT;

    /**
     * 第三方应用响应微博客户端的请求，提供需要分享的数据
     * 新浪微博实现消息分享的原理就是本地应用首先向新浪微博发送请求，然后新浪微博接受到请求
     * 之后，进行验证，验证通过之后，然后我们就可以向新浪微博发送我们想要分享的数据了。
     * responseMessage()的作用就是在我们本地应用在通过了新浪微博的验证之后发送数据的具体
     * 过程了
     *
     */
    private void responseMessage(String sharedText, Bitmap sharedBitmap)
    {
        if (mWeiboShareApi.isWeiboAppSupportAPI())
        {
            final int supportAPI = mWeiboShareApi.getWeiboAppSupportAPI();
            if (supportAPI >= 10351)
            {
                ImageObject imgObj = new ImageObject();
                if (null != sharedBitmap)
                {
                    imgObj.setImageObject(sharedBitmap);
                }

                TextObject textObj = new TextObject();
                textObj.text = sharedText;

                WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();

                weiboMultiMessage.imageObject = imgObj;
                weiboMultiMessage.textObject = textObj;

                // 这样我们就可以分享多个Object了，即不止包含文字，还可以包含图片等
                // 初始化到第三方的微博的消息的请求
                SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
                // 用transaction唯一标示请求(同微信请求处理过程一样，我们也是通过当前的时间字符串作为唯一的标示)
                request.transaction = String.valueOf(System.currentTimeMillis());
                request.multiMessage = weiboMultiMessage;
                // 发送信息到微博,这个过程理论上应该能唤起微博分享界面，但是如果验证失败了，也就是卡在这一步了
                Log.d(TAG, " we are going to invoke the WeiboShare Page activity");
                if (WEIBO_SHARE_TYPE == SHARE_CLIENT)
                {
                    // 从当前的Activity实例跳转到我们新浪微博指定的分享信息Activity，在这里我们
                    // 用于创建分享实例的context就是BilliardsRoomActiviity实例，所以我们可以把她强制转换成Activity实例,来
                    // 完成跳转
                    Log.d(TAG, " sending the request to share message to sina weibo ");
                    mWeiboShareApi.sendRequest(this, request);
                }
            } else
            {
                // 这样我们就只能发送一种类型的对象了，我们这里采用只发送文字
                TextObject textObject = new TextObject();
                textObject.text = sharedText;
                WeiboMessage weiboMessage = new WeiboMessage();

                weiboMessage.mediaObject = textObject;
                SendMessageToWeiboRequest singleRequest = new SendMessageToWeiboRequest();
                singleRequest.transaction = String.valueOf(System.currentTimeMillis());
                singleRequest.message = weiboMessage;
                // 发送这个单条消息到微博客户端的指定页面activity
                mWeiboShareApi.sendRequest(this, singleRequest);
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mWeiboShareApi.isWeiboAppInstalled())
        {
            // 2. 以下是添加了分享结果的监听回调方法，我们可以监听分享的结果，但是无法正确的获取微博的验证
            if (null != mWeiboShareApi)
            {
                if (mWeiboShareApi.isWeiboAppInstalled())
                {
                    shareWeiboMsg(getString(R.string.renren_share_content), mSharedBitmap);
                } else
                {
                    // 我们需要先提醒用户首先将微博的客户端安装上才可以进行以后的分享操作
                    Toast.makeText(WeiboShareActionCompleteActivity.this, getString(R.string.weibo_need_to_install_first), Toast.LENGTH_SHORT).show();
                }
            }
        } else
        {
            Toast.makeText(this, getString(R.string.weibo_need_to_install_first), Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    public void shareWeiboMsg(String text, Bitmap bitmap)
    {
        // 然后发送我们封装好的WeiboObject
        responseMessage(text, bitmap);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        mWeiboShareApi.handleWeiboResponse(intent, this);
    }

    private static final int STATUS_OK = 0;
    private static final int STATUS_CANCEL = 1;
    private static final int STATUS_FAIL = 2;

    @Override
    public void onResponse(BaseResponse baseResponse)
    {
        switch (baseResponse.errCode)
        {
            case STATUS_OK:
                Toast.makeText(this, getString(R.string.weibo_share_success), Toast.LENGTH_SHORT).show();
                this.finish();
                break;
            case STATUS_CANCEL:
                Toast.makeText(this, getString(R.string.weibo_share_cancelled), Toast.LENGTH_SHORT).show();
                this.finish();
                break;
            case STATUS_FAIL:
                Toast.makeText(this, getString(R.string.weibo_share_fail), Toast.LENGTH_SHORT).show();
                this.finish();
                break;
        }
    }
}
