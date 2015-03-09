package com.yueqiu.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yueqiu.R;
import com.yueqiu.constant.HttpConstants;

import java.io.ByteArrayOutputStream;

/**
 * Created by scguo on 15/2/28.
 *
 * 用于实现微信分享的具体实现管理类
 *
 */
public class WeChatShareManager implements IWXAPIEventHandler
{
    private static final String TAG = "WeChatShareManager";

    private static final int THUMB_SIZE = 150;

    // 定义分享的类型--文字
    public static final int WECHAT_SHARE_WAY_TEXT = 1;
    // 定义分享的类型--图片
    public static final int WECHAT_SHARE_WAY_PIC = 2;

    private static WeChatShareManager sInstance;
    private final IWXAPI mWXApi;
    private Context mContext;
    private static final String APP_ID = HttpConstants.WEIXIN_APP_ID;

    private Intent mIntent;
    private WeChatShareManager(Context context, Intent intent)
    {
        this.mContext = context;
        this.mIntent = intent;
        // 初始化分享的API
        mWXApi = WXAPIFactory.createWXAPI(context, APP_ID);
        mWXApi.registerApp(APP_ID);
        // 向分享API添加事件监听
        mWXApi.handleIntent(intent, this);
    }

    public static WeChatShareManager getInstance(Context context, Intent intent)
    {
        if (null == sInstance)
        {
            sInstance = new WeChatShareManager(context, intent);
        }
        return sInstance;
    }

    public void shareByWeChat(ShareContent shareContent, boolean shareToTimeLine)
    {
        if (! mWXApi.isWXAppInstalled())
        {
            Toast.makeText(mContext, mContext.getString(R.string.weixin_need_to_install_first), Toast.LENGTH_SHORT).show();
            return;
        } else
        {
            switch (shareContent.getShareWay())
            {
                case WECHAT_SHARE_WAY_PIC:
                    Log.d(TAG, " share picture to we chat ");
                    sharePic(shareContent, shareToTimeLine);
                    // 分享图片到微信
                    break;
                case WECHAT_SHARE_WAY_TEXT:
                    // 分享文字内容到微信
                    Log.d(TAG, " share text to we chat ");
                    shareText(shareContent, shareToTimeLine);
                    break;
            }
        }
    }

    // 微信发送请求到第三方应用时会调用这个方法
    @Override
    public void onReq(BaseReq baseReq)
    {
        Log.d(TAG, " the weChat sending message to the 3-rd party app ");
    }

    // 当我们向微信发送消息，需要监听的就是这个接口的返回值
    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    // 以下就是向微信发送数据时得到的所有的返回值
    // ERR_OK = 0;
    // ERR_COMM = -1;
    // ERR_USER_CANCEL = -2;
    // ERR_SENT_FAILED = -3;
    // ERR_AUTH_DENIED = -4;
    // ERR_UNSUPPORT = -5;
    @Override
    public void onResp(BaseResp baseResp)
    {
        Log.d(TAG, " we are sending message to the WeChat, and the error code we get are : " + baseResp.errCode
                    + " \n and the error reason are : " + baseResp.errStr);
        switch (baseResp.errCode)
        {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                // 验证失败

                break;
            case BaseResp.ErrCode.ERR_COMM:

                break;
            case BaseResp.ErrCode.ERR_OK:
                // 分享成功

                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:

                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                // 不支持分享操作，可能是用户当前的设备上面没有安装微信程序

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                break;

        }
    }

    private abstract class ShareContent
    {
        protected abstract int getShareWay();
        protected abstract String getContent();
        protected abstract String getTitle();
        protected abstract String getURL();
        protected abstract Bitmap getPicRes();
    }

    public class ShareTextContent extends ShareContent
    {
        private final String SHARE_CONTENT;
        public ShareTextContent(String shareContent)
        {
            this.SHARE_CONTENT = shareContent;
        }

        @Override
        protected int getShareWay()
        {
            return WECHAT_SHARE_WAY_TEXT;
        }

        @Override
        protected String getContent()
        {
            return SHARE_CONTENT;
        }

        @Override
        protected String getTitle()
        {
            return null;
        }

        @Override
        protected String getURL()
        {
            return null;
        }

        @Override
        protected Bitmap getPicRes()
        {
            return null;
        }
    }

    public class SharePicContent extends ShareContent
    {
        private Bitmap mSharedBitmap;
        public SharePicContent(Bitmap bitmap)
        {
            this.mSharedBitmap = bitmap;
        }

        @Override
        protected int getShareWay()
        {
            return WECHAT_SHARE_WAY_PIC;
        }

        @Override
        protected String getContent()
        {
            return null;
        }

        @Override
        protected String getTitle()
        {
            return null;
        }

        @Override
        protected String getURL()
        {
            return null;
        }

        @Override
        protected Bitmap getPicRes()
        {
            return this.mSharedBitmap;
        }
    }

    /**
     *
     * @param shareContent
     * @param visibleToTimeLine 当这个值为true时，我们直接将当前的内容分享到微信朋友圈的timeLine,即朋友圈
     *
     */
    private void shareText(ShareContent shareContent, boolean visibleToTimeLine)
    {
        final String text = shareContent.getContent();
        if (! TextUtils.isEmpty(text))
        {
            Log.d(TAG, " sending text message to we chat, and the content we need to share are : " + text);
            // 初始化一个WXTextObject对象
            WXTextObject textObject = new WXTextObject();
            textObject.text = text;

            // 用WXTextObject对象初始化一个WXMediaMessage对象
            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = textObject;
            // 发送文本类型的消息时，title字段不起作用
            // msg.title = "Will be ignored";
            msg.description = text;

            // 构造一个完整的request
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            Log.d(TAG, " the transaction information are : " + req.transaction);
            req.transaction = buildTransaction("textshare");
            req.message = msg;
            // TODO: 现在在测试阶段，我们默认是将球厅的信息分享到Timeline上，也就是所有的好友都是可见的
            req.scene = visibleToTimeLine ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
            Log.d(TAG, " sending message here ... ");
            mWXApi.sendReq(req);
        }
        else
        {
            Log.d(TAG, " the content you share must not be null ");
        }
    }

    /**
     *
     * @param shareContent
     * @param visibleToTimeLine 当这个值为true时，我们直接将当前的内容分享到微信朋友圈的timeLine,即朋友圈
     */
    private void sharePic(ShareContent shareContent, boolean visibleToTimeLine)
    {
        final Bitmap sharedBitmap = shareContent.getPicRes();

        if (sharedBitmap != null)
        {
            Log.d(TAG, " decoding the bitmap that needs to share to wechat ");

            WXImageObject imgObj = new WXImageObject(sharedBitmap);

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = imgObj;

            Bitmap thumbBitmap = Bitmap.createScaledBitmap(sharedBitmap,
                    THUMB_SIZE,
                    THUMB_SIZE,
                    true);
            // TODO: 这里有一个bug，就是我们的Bitmap应该是被回收掉的，但是如果采用sharedBitmap.recycle()
            // TODO: 的话，就会抛出异常：trying to use a recycled bitmap android.graphics.Bitmap
            // TODO: 我们在制作用户头像时也遇到这个问题，但是如果不回收的话，时间长了就会造成内容拥挤，
            // TODO: 这个问题其实挺严重的，我们应该及早着手处理！！！
//            sharedBitmap.recycle();
            // 设置用于分享的内容的缩略图
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            thumbBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] shareByteArray = stream.toByteArray();
            if (shareByteArray != null)
            {
                // 我们要分享到微信的byteArray的大小要控制在32KB以内
                msg.thumbData = shareByteArray;
            }

            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = buildTransaction("imagesharedata");
            req.message = msg;
            req.scene = visibleToTimeLine ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
            mWXApi.sendReq(req);
        } else
        {
            Log.d(TAG, " Failed --> the image you share must have a valid resource id ");
        }
    }

    // 用于获取一个我们所分享的信息的唯一的标示符，所以结合当前的时间值来保证唯一性
    private String buildTransaction(final String type)
    {
        return TextUtils.isEmpty(type) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}






































































