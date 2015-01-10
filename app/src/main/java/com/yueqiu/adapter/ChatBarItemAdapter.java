package com.yueqiu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/18.
 * 聊吧账户信息adapter
 */
public class ChatBarItemAdapter extends BaseAdapter {
    private static final String TAG = "ChatBarItemAdapter";
    private LayoutInflater mLayoutInflater;
    private Context context;
    public ChatBarItemAdapter(Context context) {
        this.context = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }
    int i = 0;

    @Override
    public int getCount() {
        //TODO:need database data,
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_chatbar_account, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
            viewHolder.mAccount = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
            viewHolder.mGender = (TextView) convertView.findViewById(R.id.chatbar_item_gender_tv);
            viewHolder.mSendTime = (TextView) convertView.findViewById(R.id.chatbar_item_sendtime_tv);
            viewHolder.mVerifyMessage = (TextView) convertView.findViewById(R.id.friends_application_verify_message);
            viewHolder.mDistrict = (TextView) convertView.findViewById(R.id.chatbar_item_district_tv);
            //绑定viewholder对象
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //TODO:设置布局中控件的内容,need data!
        Log.e(TAG, String.valueOf(convertView));
//        viewHolder.mAccount.setText();
//        viewHolder.mLastMessage.setText();
        if (position == 0) {
            viewHolder.mAccount.setText("验证消息");
            viewHolder.mVerifyMessage.setVisibility(View.VISIBLE);
            viewHolder.mVerifyMessage.setText("tangxin请求添加为好友");
            viewHolder.mImageView.setImageResource(R.drawable.message);
            viewHolder.mGender.setVisibility(View.GONE);
            viewHolder.mDistrict.setVisibility(View.GONE);
        }

        return convertView;
    }

    final class ViewHolder {
        public ImageView mImageView;
        public TextView mAccount;
        public TextView mGender;
        public TextView mSendTime;
        public TextView mVerifyMessage;
        public TextView mDistrict;
    }
}


