package com.yueqiu.fragment.participation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yueqiu.R;

/**
 * Created by doushuqi on 14/12/20.
 * 提到我中回复标签页
 */
public class BilliardDatingFragment extends Fragment {
    private static final String TAG = "ReplyMentionMeFragment";
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reply_mentioned_me, container, false);
        mListView = (ListView) view.findViewById(R.id.mention_me_reply_lv);
        mListView.setAdapter(new MyAdapter());
        return view;
    }

    class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return 10;
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
//                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_reply, null);
//                viewHolder = new ViewHolder();
//                viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.mention_me_account_iv);
//                viewHolder.mNickName = (TextView) convertView.findViewById(R.id.mention_me_nickname_tv);
//                viewHolder.mInfo = (TextView) convertView.findViewById(R.id.mention_meinfo_tv);
//                viewHolder.mTime = (TextView) convertView.findViewById(R.id.mention_me_time_tv);
//                viewHolder.mEditText = (EditText) convertView.findViewById(R.id.mention_me_reply_et);
//
//                viewHolder.mAdd = (Button) convertView.findViewById(R.id.mention_me_add_btn);
//                viewHolder.mReply = (Button) convertView.findViewById(R.id.mention_me_reply_btn);
                //绑定viewholder对象
//                convertView.setTag(viewHolder);
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
            public TextView mNickName;
            public TextView mInfo, mTime;
            public Button mAdd, mReply;
            public EditText mEditText;
        }
    }
}
