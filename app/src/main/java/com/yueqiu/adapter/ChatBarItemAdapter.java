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
        return 20;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Log.e(TAG, "getView " + position + " " + convertView);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_account, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
            viewHolder.mAccount = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
            viewHolder.mLastMessage = (TextView) convertView.findViewById(R.id.chatbar_item_lastmessage_tv);
            viewHolder.mSendTime = (TextView) convertView.findViewById(R.id.chatbar_item_sendtime_tv);
            Log.e(TAG, " button  " + viewHolder.mImageView + " title " + viewHolder.mAccount + " text " + viewHolder.mLastMessage);
            //绑定viewholder对象
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //TODO:设置布局中控件的内容,need data!
        Log.e(TAG, String.valueOf(convertView));
//        viewHolder.mAccount.setText();
//        viewHolder.mLastMessage.setText();

        return convertView;
    }

    final class ViewHolder {
        public ImageView mImageView;
        public TextView mAccount;
        public TextView mLastMessage;
        public TextView mSendTime;
    }
}


