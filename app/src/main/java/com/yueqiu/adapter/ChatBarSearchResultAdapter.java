package com.yueqiu.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/18.
 * 聊吧账户信息adapter
 */
public class ChatBarSearchResultAdapter extends BaseAdapter {
    private static final String TAG = "ChatBarSearchResultAdapter";
    private LayoutInflater mLayoutInflater;
    private Context context;
    public ChatBarSearchResultAdapter(Context context) {
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
            convertView = mLayoutInflater.inflate(R.layout.item_chatbar_account, null);
            viewHolder = new ViewHolder();
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.chatbar_item_account_iv);
            viewHolder.mAccount = (TextView) convertView.findViewById(R.id.chatbar_item_account_tv);
//            viewHolder.mBaseInfo = (TextView) convertView.findViewById(R.id.chatbar_item_base_info_tv);
//            viewHolder.mButton = (Button) convertView.findViewById(R.id.chatbar_item_add_friend_btn);
            //绑定viewholder对象
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //TODO:设置布局中控件的内容,need data!
        Log.e(TAG, String.valueOf(convertView));
//        viewHolder.mAccount.setText();
//        viewHolder.mLastMessage.setText();
        viewHolder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog dialog = new MyDialog(context);
                dialog.setTitle("消息验证");
                dialog.show();
            }
        });

        return convertView;
    }

    final class ViewHolder {
        public ImageView mImageView;
        public TextView mAccount;
        public TextView mBaseInfo;
        public Button mButton;
    }

    class MyDialog extends Dialog {
        public MyDialog(Context context) {
            super(context);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_chatbar_add_friends);
            TextView textView = (TextView)findViewById(R.id.back);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }
}


