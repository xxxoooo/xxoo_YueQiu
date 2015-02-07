package com.yueqiu.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.fragment.nearby.common.NearbyFragmentsCommonUtils;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;
import com.yueqiu.util.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.AlgorithmParameterGenerator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author scguo
 *         <p/>
 *         这是用于展示球厅的具体Activity
 *         当我们点击球厅子Fragment(BilliardsNearbyRoomFragment)当中的ListView的任何的一个item，就会
 *         跳转到当前的这个Fragment当中
 */
public class NearbyBilliardRoomActivity extends Activity
{
    private static final String TAG = "NearbyBilliardRoomActivity";

    private NetworkImageView mRoomPhoto;
    private TextView mRoomName;
    private TextView mRoomRatingNum;
    private RatingBar mRoomRatingBar;
    private TextView mRoomPrice, mRoomTag, mRoomAddress, mRoomPhone;
    private TextView mRoomDetailedInfo;

    private ActionBar mActionBar;

//    private FrameLayout mWindowRootElem;

    private ImageLoader mImgLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_billiard_room);

        mRoomPhoto = (NetworkImageView) findViewById(R.id.img_search_room_detailed_photo);
        mRoomName = (TextView) findViewById(R.id.tv_search_room_detailed_name);
        mRoomRatingBar = (RatingBar) findViewById(R.id.ratingbar_search_room_detailed_ratingbar);
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
            double price = receivedData.getDouble(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PRICE);
            float level = receivedData.getFloat(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_LEVEL);
            String tag = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_TAG);
            String info = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_DETAILED_INFO);
            String address = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_ADDRESS);
            String phone = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHONE);
            String photoUrl = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_PHOTO);
            String name = receivedData.getString(NearbyFragmentsCommonUtils.KEY_ROOM_FRAGMENT_NAME);


            // 把我们得到的数据全部渲染到Activity当中
            mRoomPrice.setText(String.valueOf(price));
            mRoomRatingBar.setRating(level);
            mRoomRatingNum.setText(String.valueOf(level));
            mRoomAddress.setText(address);
            mRoomTag.setText(tag);
            mRoomPrice.setText(String.valueOf(price));
            mRoomDetailedInfo.setText(info);
            mRoomPhone.setText(phone);
            mRoomName.setText(name);

            Log.d(TAG, " the room photo url we get are : " + photoUrl);
            mRoomPhoto.setDefaultImageResId(R.drawable.default_reommend_img);
            mRoomPhoto.setImageUrl(photoUrl, mImgLoader);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

    }

    // TODO: 用于得到球厅详情信息的网络请求处理过程
    // TODO: 这里暂时还不知道怎么处理(目前的处理是直接从前一个Fragment的List当中直接获取
    // TODO: 我们目前还不确定Server端的策略，如果他有提供这个interface，那么我们就直接在这里进行了，否则的话，就从新进行请求)
    private String getRoomDetailedInfo()
    {


        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_billiards_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.search_room_detail_action_collect:
                mUIEventsHandler.sendEmptyMessage(ADD_TO_FAVOR);
                break;
            case R.id.search_room_detail_action_share:
                Dialog dlg = Utils.showSheet(this);
                dlg.show();

                break;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: 以下用于展示球厅详情Activity当中的分享球厅的popupWindow,
    // TODO: 我们在这里并没有重新创建，而是直接将所有的代码从BilliardsDatingActivity当中复制过来的。在后期的代码优化过程当中，
    // TODO: 我们需要将展示球厅分享的popupWindow部分单独分拆出来形成一个独立的模块
    private PopupWindow mPopupWindow;
    private TextView mTvYueqiu, mTvYueqiuFriend, mTvFriendCircle, mTvWeichat, mTvQQZone, mTvTencentWeibo, mTvSinaWeibo, mTvRenren;
    private Button mBtnCancel;

    // 弹出约球详情分享的popupWindow
    private void popupShareWindow()
    {
        View popupWindowView = getLayoutInflater().inflate(R.layout.dialog_share, null);
        mPopupWindow = new PopupWindow(popupWindowView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        // 我们是必须要为PopupWindow设置一个Background drawable才能使PopupWindow工作正常
        // 当时我们由于已经在layout当中设置了background，所以这里我们使用一个技巧就是设置background的Bitmap为null
        // 为popupWindow设置背景是为了使popupWindow在点击popupWindow外部的时候可以自动dismiss掉
//        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mPopupWindow.getContentView().setFocusableInTouchMode(true);
        mPopupWindow.getContentView().setFocusable(true);

        mPopupWindow.setAnimationStyle(R.style.SearchDatingDetailedPopupWindowStyle);

        (mTvYueqiu = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_yuqeiufirend)).setOnClickListener(new OnShareIconClickListener());
        (mTvYueqiuFriend = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_yueqiucircle)).setOnClickListener(new OnShareIconClickListener());
        (mTvFriendCircle = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_friendcircle)).setOnClickListener(new OnShareIconClickListener());
        (mTvWeichat = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_weichat)).setOnClickListener(new OnShareIconClickListener());

        (mTvQQZone = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_qqzone)).setOnClickListener(new OnShareIconClickListener());
        (mTvTencentWeibo = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_qqweibo)).setOnClickListener(new OnShareIconClickListener());
        (mTvSinaWeibo = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_sinaweibo)).setOnClickListener(new OnShareIconClickListener());
        (mTvRenren = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_renren)).setOnClickListener(new OnShareIconClickListener());

        (mBtnCancel = (Button) popupWindowView.findViewById(R.id.btn_search_dating_detailed_cancel)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mPopupWindow.dismiss();
                // while the popupWindow is dismissed from the current activity, make the background back to the normal state
//                mWindowRootElem.getForeground().setAlpha(0);
//                mWindowRootElem.forceLayout();
            }
        });

        // TODO: 当我们在后期代码压缩时，如果需要将popupWindow抽离出来的时候，需要将以下用于设置PopupWindow的显示
        // TODO: 位置的时候，我们就需要将popupWindow当前所在的Activity的准确的layout文件的根元素的指定，否则就会发生异常
        mPopupWindow.showAtLocation(findViewById(R.id.search_room_detailed_whole_container), Gravity.BOTTOM, 0, 0);
//        mWindowRootElem.getForeground().setAlpha(160);
    }


    private class OnShareIconClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.img_search_dating_detail_share_yuqeiufirend:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the yueqiu friends", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_yueqiucircle:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the yueqiu circle", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_friendcircle:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the friends circle", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_weichat:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the wechat", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_qqzone:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the qq zone", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_qqweibo:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the qq weibo", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_sinaweibo:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the qq sinaweibo", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_renren:
                    Toast.makeText(NearbyBilliardRoomActivity.this, "sharing to the qq renren", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    // 将当前的球厅加入我们收藏当中
    private void addRoomToFavor()
    {
        boolean resultStatus = false;
        ConcurrentHashMap<String, String> requestParams = new ConcurrentHashMap<String, String>();
        requestParams.put("user_id", YueQiuApp.sUserInfo.getUser_id() + "");
        requestParams.put("type", "2");
        requestParams.put("id", "");

        String rawResult = HttpUtil.urlClient(HttpConstants.Favor.STORE_URL, requestParams, HttpConstants.RequestMethod.POST);
        Log.d(TAG, " the raw result we get for the add the room to favor are : " + rawResult);
        try
        {
            JSONObject resultJson = new JSONObject(rawResult);
            final int code = resultJson.getInt("code");
            resultStatus = code == 1001 ? true : false;
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (resultStatus)
        {
            mUIEventsHandler.sendEmptyMessage(FAVOR_SUCCESS);
        } else
        {
            mUIEventsHandler.sendEmptyMessage(FAVOR_FAILURE);
        }
    }

    private static final int FAVOR_SUCCESS = 1 << 1;
    private static final int FAVOR_FAILURE = 1 << 2;
    private static final int ADD_TO_FAVOR = 1 << 3;

    private Handler mUIEventsHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case FAVOR_SUCCESS:
                    Intent favorSuccessIntent = new Intent(PublicConstant.SLIDE_FAVOR_ACTION);
                    NearbyBilliardRoomActivity.this.sendBroadcast(favorSuccessIntent);
                    Utils.showToast(NearbyBilliardRoomActivity.this, getString(R.string.store_success));
                    break;
                case FAVOR_FAILURE:
                    Utils.showToast(NearbyBilliardRoomActivity.this, getString(R.string.store_failure));
                    break;
                case ADD_TO_FAVOR:
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            addRoomToFavor();
                        }
                    }).start();
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.top_in,R.anim.top_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


}
