package com.yueqiu.activity.searchmenu.nearby;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.adapter.SearchDatingDetailedGridAdapter;
import com.yueqiu.adapter.SearchMateSubFragmentListAdapter;
import com.yueqiu.bean.SearchDatingDetailedAlreadyBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author scguo
 *
 * 用于实现约球详细的Activity,即点击约球Fragment(BilliardsSearchDatingFragment)当中的ListView当中的任何一个item的时候跳转到的
 * 一个页面，就是约球详情Activity
 *
 */
public class SearchBilliardsDatingActivity extends Activity
{
    private static final String TAG = "SearchBilliardsDatingActivity";
    private GridView mGridAlreadyFlow;
    private ImageView mUserPhoto;
    private TextView mUserName, mUserGender, mTvFollowNum, mTvTime1, mTvTime2;

    private Button mBtnFollow;
    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billiards_dating);


        mGridAlreadyFlow = (GridView) findViewById(R.id.gridview_search_dating_detailed_already_flow);
        mUserPhoto = (ImageView) findViewById(R.id.img_search_dating_detail_photo);
        mUserName = (TextView) findViewById(R.id.tv_search_dating_detail_nickname);
        mUserGender = (TextView) findViewById(R.id.tv_search_dating_detail_gender);
        mTvFollowNum = (TextView) findViewById(R.id.tv_search_dating_detail_follow_num);
        mTvTime1 = (TextView) findViewById(R.id.tv_search_dating_detailed_time);
        mTvTime2 = (TextView) findViewById(R.id.tv_search_dating_detailed_time_1);

        (mBtnFollow = (Button) findViewById(R.id.btn_search_dating_detailed_iwantin)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        // the parameter we need to set are all retrieved from the previous activity

        initGridList();
        mGridAlreadyFlow.setAdapter(new SearchDatingDetailedGridAdapter(this, (ArrayList<SearchDatingDetailedAlreadyBean>) mFollowList));



    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
        {
            mActionBar = getActionBar();
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // TODO: 获取约球的时间list，是以一个列表的形式返回的，具体可能包含不止两条时间
    private String retrieveDatingTimeList()
    {


        return "";
    }

    // TODO: 获取约球详情信息
    private String retrieveDatingDetailedInfo()
    {
        return "";
    }

    // TODO: 用于处理“我要参加”的处理事件
    // TODO: 是POST请求
    private void joinActivity()
    {
        boolean resultStatus = false;





        String resultStr = resultStatus ? getResources().getString(R.string.search_dating_detailed_btn_i_want_in_success)
                : getResources().getString(R.string.search_dating_detailed_btn_i_want_in_failed);

        Toast.makeText(this, resultStr, Toast.LENGTH_LONG).show();
    }

    // TODO: 用于完成台球厅分享的网络的请求处理过程
    private void shareBilliardsRoomRequest()
    {

    }



    private List<SearchDatingDetailedAlreadyBean> mFollowList = new ArrayList<SearchDatingDetailedAlreadyBean>();

    // TODO: the following are just the static test data, and we should
    // TODO: remove them to add the dynamic data
    private void initGridList()
    {
        int i;
        for (i = 0; i < 5; ++i)
        {
            mFollowList.add(new SearchDatingDetailedAlreadyBean("", "温柔的语"));
        }
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

        switch (id)
        {
            case R.id.search_room_action_collect:
                return true;
            case R.id.search_room_action_share:
                Log.d(TAG, "the popupWindow has been clicked on ");
                popupShareWindow();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private PopupWindow mPopupWindow;
    private TextView mTvYueqiu, mTvYueqiuFriend, mTvFriendCircle, mTvWeichat, mTvQQZone, mTvTencentWeibo, mTvSinaWeibo, mTvRenren;
    private Button mBtnCopyLink, mBtnCancel;
    // 弹出约球详情分享的popupWindow
    private void popupShareWindow()
    {
        View popupWindowView = getLayoutInflater().inflate(R.layout.search_dating_detail_popupwindow, null);
        mPopupWindow = new PopupWindow(popupWindowView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        // 我们是必须要为PopupWindow设置一个Background drawable才能使PopupWindow工作正常
        // 当时我们由于已经在layout当中设置了background，所以这里我们使用一个技巧就是设置background的Bitmap为null
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        mPopupWindow.getContentView().setFocusableInTouchMode(true);
        mPopupWindow.getContentView().setFocusable(true);

        mPopupWindow.setAnimationStyle(R.style.SearchDatingDetailedPopupWindowStyle);

        // TODO: 处理PopupWindow当中的TextView被点击之后的处理事件
        // TODO: 当前PopupWindow中的TextView和Button是可以响应点击事件的，例如点击之后可以弹出Toast，但是却不可以使用selector，这是一个很严重的问题
        (mTvYueqiu = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_yuqeiufirend)).setOnClickListener(new OnShareIconClickListener());
        (mTvYueqiuFriend = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_yueqiucircle)).setOnClickListener(new OnShareIconClickListener());
        (mTvFriendCircle = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_friendcircle)).setOnClickListener(new OnShareIconClickListener());
        (mTvWeichat = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_weichat)).setOnClickListener(new OnShareIconClickListener());

        (mTvQQZone = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_qqzone)).setOnClickListener(new OnShareIconClickListener());
        (mTvTencentWeibo = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_qqweibo)).setOnClickListener(new OnShareIconClickListener());
        (mTvSinaWeibo = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_sinaweibo)).setOnClickListener(new OnShareIconClickListener());
        (mTvRenren = (TextView) popupWindowView.findViewById(R.id.img_search_dating_detail_share_renren)).setOnClickListener(new OnShareIconClickListener());

        (mBtnCopyLink = (Button) popupWindowView.findViewById(R.id.btn_search_dating_detailed_copylink)).setOnClickListener(new OnShareIconClickListener());
        (mBtnCancel = (Button) popupWindowView.findViewById(R.id.btn_search_dating_detailed_cancel)).setOnClickListener(new OnShareIconClickListener());

        mPopupWindow.showAtLocation(findViewById(R.id.search_dating_detailed_activity_main), Gravity.BOTTOM, 0, 0);
    }

    private class OnShareIconClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.img_search_dating_detail_share_yuqeiufirend:
                    Toast.makeText(SearchBilliardsDatingActivity.this, "shareing", Toast.LENGTH_LONG).show();
                    break;
                case R.id.img_search_dating_detail_share_yueqiucircle:
                    break;
                case R.id.img_search_dating_detail_share_friendcircle:
                    break;
                case R.id.img_search_dating_detail_share_weichat:
                    break;
                case R.id.img_search_dating_detail_share_qqzone:
                    break;
                case R.id.img_search_dating_detail_share_qqweibo:
                    break;
                case R.id.img_search_dating_detail_share_sinaweibo:
                    break;
                case R.id.img_search_dating_detail_share_renren:
                    break;
                case R.id.btn_search_dating_detailed_copylink:
                    break;
                case R.id.btn_search_dating_detailed_cancel:
                    break;
            }
        }
    }


}
