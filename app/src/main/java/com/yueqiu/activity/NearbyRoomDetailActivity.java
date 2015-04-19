package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
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
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.t.Weibo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.RenRenShareManager;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;
import com.yueqiu.util.WeChatShareManager;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class NearbyRoomDetailActivity extends Activity implements IWeiboHandler.Response, IUiListener{
    private static final String TAG = "NearbyBilliardRoomActivity";
    private static final String TAG_1 = "room_info_share_debug";

    private NetworkImageView mRoomPhoto;
    private TextView mRoomName;
    private TextView mRoomRatingNum;
    private RatingBar mRoomRatingBar;
    private TextView mRoomPrice, mRoomTag, mRoomAddress, mRoomPhone;
    private TextView mRoomDetailedInfo;

    private ActionBar mActionBar;

//    private FrameLayout mWindowRootElem;

    private ImageLoader mImgLoader;

    private IWeiboShareAPI mWeiboShareApi = null;

    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_billiard_room);

        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);

        mRoomPhoto = (NetworkImageView) findViewById(R.id.img_search_room_detailed_photo);
        mRoomName = (TextView) findViewById(R.id.tv_search_room_detailed_name);
        mRoomRatingBar = (RatingBar) findViewById(R.id.ratingbar_search_room_detailed_ratingbar);
        mRoomRatingBar.setStepSize(0.02f); // 我们接受到的rating的值的总数为100，但是我们只有5个星星，所以我们每次移动的步骤就是5/100=0.02
        mRoomRatingNum = (TextView) findViewById(R.id.tv_search_room_level_num);

        // the tag textView collection here
        mRoomPrice = (TextView) findViewById(R.id.tv_search_room_per_people_price);
        mRoomTag = (TextView) findViewById(R.id.tv_search_room_tag);
        mRoomAddress = (TextView) findViewById(R.id.tv_search_room_address);
        mRoomPhone = (TextView) findViewById(R.id.tv_search_room_phone);

        mRoomDetailedInfo = (TextView) findViewById(R.id.tv_search_room_detailed_info);

        // get the root element use to dimmer the activity background
//        mWindowRootElem = (FrameLayout) findViewById(R.id.window_root_elem);
//        mWindowRootElem.getForeground().setAlpha(0);

        mImgLoader = VolleySingleton.getInstance().getImgLoader();

        // then, we need the data that transferred from the previous listView item to inflate
        // the detailed content of these TextView and ImageViews
        Intent receivedIntent = getIntent();
        Bundle receivedData = receivedIntent.getBundleExtra(NearbyFragmentsCommonUtils.KEY_BUNDLE_SEARCH_ROOM_FRAGMENT);
        if (null != receivedData) {
            double price = receivedData.getDouble(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE, 0.0);
            String level = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL, "1.0");
            String tag = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_TAG, "");
            String info = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_DETAILED_INFO, "");
            String address = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS, "");
            String phone = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHONE, "");
            String photoUrl = "http://" + receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO, "");
            String name = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME, "");
            String shopHours = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_SHOP_HOURS, "");

            // 把我们得到的数据全部渲染到Activity当中
            mRoomPrice.setText(String.valueOf(price));
            mRoomRatingBar.setRating(Float.parseFloat(level));
            mRoomRatingNum.setText(String.valueOf(level));
            mRoomAddress.setText(address);
            mRoomTag.setText(shopHours);
            mRoomPrice.setText(String.valueOf(price));
            mRoomDetailedInfo.setText(info);
            mRoomPhone.setText(phone);
            mRoomName.setText(name);

            Log.d(TAG, " the room photo url we get are : " + photoUrl);
            mRoomPhoto.setDefaultImageResId(R.drawable.default_reommend_img);
            mRoomPhoto.setErrorImageResId(R.drawable.default_reommend_img);
            mRoomPhoto.setImageUrl(photoUrl, mImgLoader);
        }

        // 初始化WeiboShareSDK
        this.mWeiboShareApi = WeiboShareSDK.createWeiboAPI(this, HttpConstants.WEIBO_APP_KEY);
        // 将当前的应用注册到微博当中
        this.mWeiboShareApi.registerApp();

        if (savedInstanceState != null) {
            mWeiboShareApi.handleWeiboResponse(getIntent(), this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        mWeiboShareApi.handleWeiboResponse(intent, this);
    }

    private static final int STATUS_OK = 0;
    private static final int STATUS_CANCEL = 1;
    private static final int STATUS_FAIL = 2;

    /**
     * 用于处理新浪微博分享的回调请求处理过程
     *
     * @param baseResponse
     */
    @Override
    public void onResponse(BaseResponse baseResponse){
        Log.d(TAG_1, " on share response, error code : " + baseResponse.errCode + ", error reason : " + baseResponse.errMsg);
        switch (baseResponse.errCode){
            case STATUS_OK:
                Toast.makeText(this, getString(R.string.weibo_share_success), Toast.LENGTH_SHORT).show();
                break;
            case STATUS_CANCEL:
                Toast.makeText(this, getString(R.string.weibo_share_cancelled), Toast.LENGTH_SHORT).show();
                break;
            case STATUS_FAIL:
                Toast.makeText(this, getString(R.string.weibo_share_fail), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void shareWeiboMsg(String text, Bitmap bitmap){
        // 然后发送我们封装好的WeiboObject
        responseMessage(text, bitmap);
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
    private void responseMessage(String sharedText, Bitmap sharedBitmap){
        if (mWeiboShareApi.isWeiboAppSupportAPI()){
            final int supportAPI = mWeiboShareApi.getWeiboAppSupportAPI();
            if (supportAPI >= 10351){
                ImageObject imgObj = new ImageObject();
                if (null != sharedBitmap){
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
                if (WEIBO_SHARE_TYPE == SHARE_CLIENT){
                    // 从当前的Activity实例跳转到我们新浪微博指定的分享信息Activity，在这里我们
                    // 用于创建分享实例的context就是BilliardsRoomActiviity实例，所以我们可以把她强制转换成Activity实例,来
                    // 完成跳转
                    Log.d(TAG, " sending the request to share message to sina weibo ");
                    mWeiboShareApi.sendRequest(this, request);
                }
            } else{
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
    protected void onResume(){
        super.onResume();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiards_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        switch (id) {
//            case R.id.search_room_detail_action_collect:
//                mUIEventsHandler.sendEmptyMessage(ADD_TO_FAVOR);
//                break;
            case R.id.search_room_detail_action_share:
                // 我们需要传入Intent来进行微信分享请求的结果监听
                // 我们可以在这个Intent当中传递我们需要的分享的具体数据
                // 我们目前需要分享包括基本的球厅的信息，球厅图片(Bitmap),球厅活动说明
                YueQiuApp.sScreenBitmap = Utils.getCurrentScreenShot(mRootView);
                Dialog dlg = Utils.showSheet(this, YueQiuApp.sScreenBitmap);
                dlg.show();
                // TODO: 以下是测试代码，我们暂时将所有的涉及到分享的代码移动到当前的Activity当中，因为关于分享的部分会涉及到一些关于
                // TODO: Activity生命周期的控制方法
//                Dialog dlg = showSheet(this, getIntent());
//                dlg.show();
                break;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in, R.anim.top_out);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: 腾讯微博分享需要安装QQ客户端
    // TODO: 但是QQ客户端如果安装上会导致分享到QQ空间无法实现(目前没有报出认出任何有关于这两者之间的冲突)
    public Dialog showSheet(final Context context, Intent intent){
        // 创建用于实现微信分享的实例
        final WeChatShareManager weChatShareManager = WeChatShareManager.getInstance(context, intent);
        final RenRenShareManager renRenShareManager = RenRenShareManager.getInstance(context);
        final Tencent tencent = Tencent.createInstance(HttpConstants.QQ_ZONE_APP_KEY, this);

        // 用于实现腾讯微博分享的回调
        final IUiListener tencentWeiboListener = new IUiListener(){
            @Override
            public void onComplete(Object response){
                try{
                    JSONObject result = (JSONObject) response;
                    int ret = result.getInt("ret");
                    if (result.has("data")){
                        JSONObject data = result.getJSONObject("data");
                        if (data.has("id")){
                            String lastAddedTweetId = data.getString("id");
                            Log.e(TAG_1, " ret : " + ret + " data = " + data + " time = " + lastAddedTweetId);
                        }
                    }
                    if (ret == 0){
                        Log.e(TAG_1, " successful by sending one tencent weibo to tencent weibo ");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onError(UiError uiError){
                Log.e(TAG_1, " error by sharing message to tencent weibo ");
            }

            @Override
            public void onCancel(){
                Log.e(TAG_1, " sharing to tencent weibo has been cancelled ");
            }
        };


        final Dialog dlg = new Dialog(context, R.style.ActionSheet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.dialog_share, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);

        Window window = dlg.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        final int cMakeBottom = -1000;
        lp.y = cMakeBottom;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        //dlg.setCanceledOnTouchOutside(false);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;
        dlg.setContentView(layout);
        dlg.getWindow().setLayout(width, height / 2);

//        TextView tvYueqiu = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yuqeiufirend);
//        TextView tvYueqiuCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yueqiucircle);
        TextView tvFriendCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_friendcircle);
        TextView tvWeichat = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_weichat);
        TextView tvQQZone = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqzone);
        TextView tvTencentWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqweibo);
        TextView tvSinaWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_sinaweibo);
        TextView tvRenren = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_renren);
        TextView btnCancel = (Button) dlg.findViewById(R.id.btn_search_dating_detailed_cancel);

        View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.btn_search_dating_detailed_cancel:
                        dlg.dismiss();
                        break;
                    // 现在我们不实现关于约球朋友和约球朋友圈的分享实现
//                    case R.id.img_search_dating_detail_share_yuqeiufirend:
//                        break;
//                    case R.id.img_search_dating_detail_share_yueqiucircle:
//                        break;

                    case R.id.img_search_dating_detail_share_friendcircle:
                        // 这是分享到朋友圈的实现(分享到朋友圈即直接分享到微信的timeLine上面)
                        // 我们需要分享的内容包括球厅的图片，球厅的价格以及球厅的活动信息说明
                        if (null != weChatShareManager) {
                            Log.d("wechat_share", " share to we chat ");
//                            weChatShareManager.shareByWeChat(weChatShareManager.new SharePicContent(R.drawable.ic_launcher),
//                                    WeChatShareManager.WECHAT_SHARE_WAY_PIC);

                            weChatShareManager.shareByWeChat(
                                    weChatShareManager.new ShareTextContent("以下内容来自微信SDK测试，与本人立场有关，十分TMD有关"),
                                    true);
                        }
                        break;
                    case R.id.img_search_dating_detail_share_weichat:
                        // 分享到微信的指定好友那里
                        if (null != weChatShareManager){
                            weChatShareManager.shareByWeChat(
                                    weChatShareManager.new ShareTextContent("下面是我发送的测试内容"),
                                    false);
                        }

                        break;
                    case R.id.img_search_dating_detail_share_qqzone:
                        final Bundle shareParmas = new Bundle();
                        final int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
                        shareParmas.putString(QzoneShare.SHARE_TO_QQ_TITLE, " this is a message from yueqiu ");
                        shareParmas.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, " yueqiu is a good app - from KingKimRi");
                        if (shareType != QzoneShare.SHARE_TO_QZONE_TYPE_APP){
                            shareParmas.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, HttpConstants.DEFAULT_DIRECT_URL);
                        }
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                Log.d(TAG_1, " share to qqzone ");
                                tencent.shareToQzone(NearbyRoomDetailActivity.this, shareParmas, NearbyRoomDetailActivity.this);
                            }
                        }).start();
                        break;
                    case R.id.img_search_dating_detail_share_qqweibo:
                        if (! tencent.isSessionValid()){
                            tencent.login(NearbyRoomDetailActivity.this, "all", new BaseUiListener()
                            {
                                @Override
                                protected void doComplete(JSONObject response)
                                {
                                    try{
                                        String token = response.getString(Constants.PARAM_ACCESS_TOKEN);
                                        String expires = response.getString(Constants.PARAM_EXPIRES_IN);
                                        String openId = response.getString(Constants.PARAM_OPEN_ID);
                                        if (! TextUtils.isEmpty(token) && ! TextUtils.isEmpty(expires) && ! TextUtils.isEmpty(openId)){
                                            tencent.setAccessToken(token, expires);
                                            tencent.setOpenId(openId);
                                        }
                                        final Weibo weibo = new Weibo(NearbyRoomDetailActivity.this, tencent.getQQToken());
                                        final String sharedContent = "";
                                        weibo.sendText(sharedContent, tencentWeiboListener);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        // 当前的Session是合法的
                        final boolean ready = tencent.isSessionValid() && tencent.getQQToken().getOpenId() != null;
                        if (!ready){
                            Toast.makeText(NearbyRoomDetailActivity.this, "Login first", Toast.LENGTH_SHORT).show();
                        } else{
                            final Weibo weibo = new Weibo(NearbyRoomDetailActivity.this, tencent.getQQToken());
                            String content = " this is a message send to tencent weibo " + System.currentTimeMillis();
                            weibo.sendText(content, tencentWeiboListener);
                            Toast.makeText(NearbyRoomDetailActivity.this, "successful share to tencent weibo ", Toast.LENGTH_SHORT).show();
                        }


                        break;
                    case R.id.img_search_dating_detail_share_sinaweibo:
                        // 1. 这是我们一开始使用的方法,但是无法检测到分享之后的返回事件，所以无法获取当前是否已经分享成功
//                        if (null != weiboShareManager) {
//                            Log.d("weibo_share", "Share to Sina Weibo");
//                            Bitmap sharedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//                            weiboShareManager.shareWeiboMsg(" share to wei bo ", sharedBitmap);
//                        }
                        // 2. 以下是添加了分享结果的监听回调方法，我们可以监听分享的结果，但是无法正确的获取微博的验证
//                        if (null != mWeiboShareApi)
//                        {
//                            if (mWeiboShareApi.isWeiboAppInstalled())
//                            {
//                                Bitmap sharedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//                                shareWeiboMsg("share to weibo", sharedBitmap);
//                            } else
//                            {
//                                // 我们需要先提醒用户首先将微博的客户端安装上才可以进行以后的分享操作
//                                Toast.makeText(NearbyBilliardRoomActivity.this, getString(R.string.weibo_need_to_install_first), Toast.LENGTH_SHORT).show();
//                            }
//                        }
                        startActivity(new Intent(NearbyRoomDetailActivity.this, WeiboShareActionCompleteActivity.class));

                        break;
                    case R.id.img_search_dating_detail_share_renren:
                        if (null != renRenShareManager){
                            // 以下我们采用分享的是测试图片
                            Bitmap sharedRennBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
                            Log.d("renren_share", "Share to renren site ");
                            renRenShareManager.shareToRenren("分享到人人的测试信息", sharedRennBitmap);
                        }
                        break;
                }
            }
        };
//        tvYueqiu.setOnClickListener(listener);
//        tvYueqiuCircle.setOnClickListener(listener);

        tvFriendCircle.setOnClickListener(listener);
        tvFriendCircle.setOnClickListener(listener);
        tvWeichat.setOnClickListener(listener);
        tvQQZone.setOnClickListener(listener);
        tvRenren.setOnClickListener(listener);
        tvSinaWeibo.setOnClickListener(listener);
        tvTencentWeibo.setOnClickListener(listener);
        btnCancel.setOnClickListener(listener);
        return dlg;
    }

    // 将当前的球厅加入我们收藏当中
    private void addRoomToFavor(){
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", YueQiuApp.sUserInfo.getUser_id() + "");
        requestParams.put("type", "2");
        requestParams.put("id", "");

        HttpUtil.requestHttp(HttpConstants.Favor.STORE_URL, requestParams, HttpConstants.RequestMethod.POST,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try{
                    if(response.isNull("code")) {
                        final int code = response.getInt("code");
                        if (code == HttpConstants.ResponseCode.NORMAL) {
                            mUIEventsHandler.sendEmptyMessage(FAVOR_SUCCESS);
                        } else {
                            mUIEventsHandler.obtainMessage(FAVOR_FAILURE,response.getString("msg")).sendToTarget();
                        }
                    }else{
                        mUIEventsHandler.sendEmptyMessage(FAVOR_FAILURE);
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mUIEventsHandler.sendEmptyMessage(FAVOR_FAILURE);
            }
        });

    }

    private static final int FAVOR_SUCCESS = 1 << 1;
    private static final int FAVOR_FAILURE = 1 << 2;

    private static final int ADD_TO_FAVOR = 1 << 3;

    private Handler mUIEventsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case FAVOR_SUCCESS:
                    Intent favorSuccessIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                    NearbyRoomDetailActivity.this.sendBroadcast(favorSuccessIntent);
                    Utils.showToast(NearbyRoomDetailActivity.this, getString(R.string.store_success));
                    break;
                case FAVOR_FAILURE:
                    if(null == msg.obj){
                        Utils.showToast(NearbyRoomDetailActivity.this,getString(R.string.http_request_error));
                    }else{
                        Utils.showToast(NearbyRoomDetailActivity.this, (String) msg.obj);
                    }
                    break;
                case ADD_TO_FAVOR:
                    if(Utils.networkAvaiable(NearbyRoomDetailActivity.this)) {
                        addRoomToFavor();
                    }else{
                        Utils.showToast(NearbyRoomDetailActivity.this,getString(R.string.network_not_available));
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in, R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response){
            if (null == response){
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0){
                return;
            }
            doComplete((JSONObject) response);
        }

        protected void doComplete(JSONObject values){

        }

        @Override
        public void onError(UiError uiError){

        }

        @Override
        public void onCancel(){

        }
    }

    /**
     * 以下的三个方法是用于监听腾讯的分享的回调监听方法
     */
    @Override
    public void onComplete(Object o){
        Log.d(TAG_1, " share complete : " + o.toString());
    }

    @Override
    public void onError(UiError uiError){
        Log.d(TAG_1, " share error : " + uiError.errorMessage + " error code : " + uiError.errorCode);

    }

    @Override
    public void onCancel(){
        Log.d(TAG_1, " share cancelled");

    }



}
